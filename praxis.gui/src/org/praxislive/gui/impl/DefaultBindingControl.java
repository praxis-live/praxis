/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package org.praxislive.gui.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.ArgumentFormatException;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.interfaces.ComponentInterface;
import org.praxislive.gui.ControlBinding;
import org.praxislive.impl.AbstractControl;

/**
 * 
 * @author Neil C Smith
 */
// @TODO sync on error?
// @TODO take router rather than host in constructor
public class DefaultBindingControl extends AbstractControl {

    private final static Logger LOG =
            Logger.getLogger(DefaultBindingControl.class.getName());
    private final static int LOW_SYNC_DELAY = 1000;
    private final static int MED_SYNC_DELAY = 200;
    private final static int HIGH_SYNC_DELAY = 50;
    private ControlAddress boundAddress;
    private Binding binding;
    private PacketRouter router;

    public DefaultBindingControl(ControlAddress boundAddress) {
        if (boundAddress == null) {
            throw new NullPointerException();
        }
        this.boundAddress = boundAddress;
        binding = new Binding();
    }

    public ControlInfo getInfo() {
        return null;
    }

    public void bind(ControlBinding.Adaptor adaptor) {
        binding.addAdaptor(adaptor);
    }

    public void unbind(ControlBinding.Adaptor adaptor) {
        binding.removeAdaptor(adaptor);
    }

    public void unbindAll() {
        binding.removeAll();
    }

    public void call(Call call, PacketRouter router) throws Exception {
        switch (call.getType()) {
            case RETURN:
//                processReturn(call);
                binding.processResponse(call);
                break;
            case ERROR:
//                processError(call);
                binding.processError(call);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

//    private void processReturn(Call call) {
////        if (boundAddress.equals(call.getFromAddress())) {
////            binding.processResponse(call);
////        } else {
////            binding.processInfo(call);
////        }
//    }
//
//    private void processError(Call call) {
////        if (boundAddress.equals(call.getFromAddress())) {
////            binding.processError(call);
////        } else {
////            binding.processInfoError(call);
////        }
//    }

    private ControlAddress getReturnAddress() {
        return getAddress();
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        router = null;
    }

    private PacketRouter getRouter() {
        if (router == null) {
            router = getLookup().get(PacketRouter.class);
        }
        return router;
    }

    private class Binding extends ControlBinding {

        private long invokeTimeOut = TimeUnit.MILLISECONDS.toNanos(5000);
        private long quietTimeOut = TimeUnit.MILLISECONDS.toNanos(200);
        private List<ControlBinding.Adaptor> adaptors;
        private ControlInfo bindingInfo;
        private Timer syncTimer;
//        private int lastCallID;
        private int infoMatchID;
        private boolean isProperty;
        private Call activeCall;
        private Adaptor activeAdaptor;
        private CallArguments arguments;

        private Binding() {
            adaptors = new ArrayList<ControlBinding.Adaptor>();
            syncTimer = new Timer(LOW_SYNC_DELAY, new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    processSync();
                }
            });
            arguments = CallArguments.EMPTY;
        }

        private void addAdaptor(Adaptor adaptor) {
            if (adaptor == null) {
                throw new NullPointerException();
            }
            if (adaptors.contains(adaptor)) {
                return;
            }
            adaptors.add(adaptor);
            bind(adaptor);
            updateAdaptorConfiguration(adaptor); // duplicate functionality
            if (bindingInfo == null) {
                sendInfoRequest();
            }
        }

        private void removeAdaptor(Adaptor adaptor) {
            if (adaptors.remove(adaptor)) {
                unbind(adaptor);
            }
            updateSyncConfiguration();
        }

        private void removeAll() {
            Iterator<Adaptor> itr = adaptors.iterator();
            while (itr.hasNext()) {
                Adaptor adaptor = itr.next();
                unbind(adaptor);
                itr.remove();
            }
            updateSyncConfiguration();
        }

        @Override
        protected void send(Adaptor adaptor, CallArguments args) {
            PacketRouter router = getRouter();
            ControlAddress returnAddress = getReturnAddress();
            Call call;
            if (adaptor.getValueIsAdjusting()) {
                call = Call.createQuietCall(boundAddress, returnAddress,
                        System.nanoTime(), args);
            } else {
                call = Call.createCall(boundAddress, returnAddress,
                        System.nanoTime(), args);
            }
            router.route(call);
            activeCall = call;
            activeAdaptor = adaptor;
            arguments = args;
            for (Adaptor ad : adaptors) {
                if (ad != adaptor) {
                    ad.update();
                }
            }
        }

        @Override
        protected void updateAdaptorConfiguration(Adaptor adaptor) {
            updateSyncConfiguration();
        }

        private void updateSyncConfiguration() {
            if (isProperty) {
                LOG.log(Level.FINE, "Updating sync configuration on {0}", boundAddress);
                boolean active = false;
                SyncRate highRate = SyncRate.None;
                for (Adaptor a : adaptors) {
                    if (a.isActive()) {
                        active = true;
                        SyncRate aRate = a.getSyncRate();
                        if (aRate.compareTo(highRate) > 0) {
                            highRate = aRate;
                        }
                    }
                }
                if (!active || highRate == SyncRate.None) {
                    if (syncTimer.isRunning()) {
                        LOG.log(Level.FINE, "Stopping sync timer on {0}", boundAddress);
                        syncTimer.stop();
                    }
                } else {
                    syncTimer.setDelay(delayForRate(highRate));
                    if (!syncTimer.isRunning()) {
                        LOG.log(Level.FINE, "Starting sync timer on {0}", boundAddress);
                        syncTimer.start();
                    }
                    processSync();
                }
            } else {
                if (syncTimer.isRunning()) {
                    syncTimer.stop();
                }
            }

        }

        private int delayForRate(SyncRate rate) {
            switch (rate) {
                case Low:
                    return LOW_SYNC_DELAY;
                case Medium:
                    return MED_SYNC_DELAY;
                case High:
                    return HIGH_SYNC_DELAY;
            }
            throw new IllegalArgumentException();
        }

        private void sendInfoRequest() {
            PacketRouter router = getRouter();
            ControlAddress returnAddress = getReturnAddress();
            ControlAddress toAddress = ControlAddress.create(
                    boundAddress.getComponentAddress(), ComponentInterface.INFO);
            Call call = Call.createCall(toAddress, returnAddress,
                    System.nanoTime(), CallArguments.EMPTY);

            infoMatchID = call.getMatchID();
            router.route(call);
        }

        private void processInfo(Call call) {
            if (call.getMatchID() == infoMatchID) {
                CallArguments args = call.getArgs();
                if (args.getSize() > 0) {
                    ComponentInfo compInfo = null;
                    try {
                        compInfo = ComponentInfo.coerce(args.get(0));
                        // @TODO on null?
                        bindingInfo = compInfo.getControlInfo(boundAddress.getID());
                        ControlInfo.Type type = bindingInfo.getType();
                        isProperty = (type == ControlInfo.Type.Property)
                                || (type == ControlInfo.Type.ReadOnlyProperty);

                    } catch (ArgumentFormatException ex) {
                        isProperty = false;
                        bindingInfo = null;
                        LOG.log(Level.WARNING, "" + call + "\n" + compInfo, ex);
                    }
                    for (Adaptor a : adaptors) {
                        a.updateBindingConfiguration();
                    }
                    updateSyncConfiguration();
                }
            }
        }

        private void processInfoError(Call call) {
            isProperty = false;
            bindingInfo = null;
            LOG.log(Level.WARNING, "Couldn't get info for {0}", boundAddress);
            for (Adaptor a : adaptors) {
                a.updateBindingConfiguration();
            }
            updateSyncConfiguration();
        }

        private void processResponse(Call call) {
            if (activeCall != null && call.getMatchID() == activeCall.getMatchID()) {
                if (activeAdaptor != null) {
                    activeAdaptor.onResponse(call.getArgs());
                    activeAdaptor = null;
                }
                if (isProperty) {
                    arguments = call.getArgs();
                    for (Adaptor a : adaptors) {
                        a.update();
                    }
                }
                activeCall = null;
            } else if (call.getMatchID() == infoMatchID) {
                processInfo(call);
            }
        }

        private void processError(Call call) {
            if (activeCall != null && call.getMatchID() == activeCall.getMatchID()) {
                if (activeAdaptor != null) {
                    activeAdaptor.onError(call.getArgs());
                    activeAdaptor = null;
                } else {
                    LOG.log(Level.WARNING, "Error on sync call - {0} - DEACTIVATING", call.getFromAddress());
                    syncTimer.stop();
                }
                activeCall = null;
            } else if (call.getMatchID() == infoMatchID) {
                processInfoError(call);
            }
        }

        private void processSync() {
            long now = System.nanoTime();
            if (activeCall != null) {
                if (activeCall.getType() == Call.Type.INVOKE) {
                    if ((now - activeCall.getTimecode()) < invokeTimeOut) {
                        return;
                    }
                } else {
                    if ((now - activeCall.getTimecode()) < quietTimeOut) {
                        return;
                    }
                }
            }
            if (isProperty) {
                PacketRouter router = getRouter();
                Call call = Call.createCall(boundAddress, getReturnAddress(),
                        now, CallArguments.EMPTY);
                router.route(call);
                activeCall = call;
                activeAdaptor = null;
            }

//            PacketRouter router = getRouter();
//            Call call = Call.createCall(boundAddress, getReturnAddress(),
//                    System.nanoTime(), CallArguments.EMPTY);
//            router.route(call);
//            lastCallID = call.getMatchID();
        }

        @Override
        public ControlInfo getBindingInfo() {
            return bindingInfo;
        }

        @Override
        public CallArguments getArguments() {
            return arguments;
        }

        @Override
        public ControlAddress getAddress() {
            return boundAddress;
        }
    }
}
