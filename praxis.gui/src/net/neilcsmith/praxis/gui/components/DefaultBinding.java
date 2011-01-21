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
package net.neilcsmith.praxis.gui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.info.ComponentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.interfaces.ComponentInterface;
import net.neilcsmith.praxis.gui.ControlBinding;
import net.neilcsmith.praxis.impl.BasicControl;

/**
 * 
 * @author Neil C Smith
 */
// @TODO sync on error?
// @TODO take router rather than host in constructor
class DefaultBinding extends BasicControl {

    private final static int LOW_SYNC_DELAY = 1000;
    private final static int MED_SYNC_DELAY = 200;
    private final static int HIGH_SYNC_DELAY = 50;
    private ControlAddress boundAddress;
    private DefaultGuiRoot host;
    private Binding binding;

    public DefaultBinding(DefaultGuiRoot host, ControlAddress boundAddress) {
        super(host);
        this.host = host;
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

    @Override
    protected void processReturn(Call call) throws Exception {
        if (boundAddress.equals(call.getFromAddress())) {
            binding.processSyncResponse(call);
        } else {
            binding.processInfoResponse(call);
        }
    }

    private ControlAddress getReturnAddress() {
        return getAddress();
    }

    private class Binding extends ControlBinding implements ActionListener {

        private List<ControlBinding.Adaptor> adaptors;
        private ControlInfo bindingInfo;
        private Timer syncTimer;
        private int lastCallID;
        private int infoMatchID;
        private boolean isProperty;
        private CallArguments arguments;

        private Binding() {
            adaptors = new ArrayList<ControlBinding.Adaptor>();
            syncTimer = new Timer(LOW_SYNC_DELAY, this);
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
        }

        @Override
        protected void send(Adaptor adaptor, CallArguments args) {
            PacketRouter router = host.getPacketRouter();
            ControlAddress returnAddress = getReturnAddress();
            Call call = Call.createQuietCall(boundAddress, returnAddress,
                    System.nanoTime(), args);
            router.route(call);
            lastCallID = call.getMatchID();
            this.arguments = args;
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
                        syncTimer.stop();
                    }
                } else {
                    syncTimer.setDelay(delayForRate(highRate));
                    if (!syncTimer.isRunning()) {
                        syncTimer.start();
                    }
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

                PacketRouter router = host.getPacketRouter();
                ControlAddress returnAddress = getReturnAddress();
//                ComponentAddress compAd = boundAddress.getComponentAddress();
//                String rootID = compAd.getRootID();
//                ControlAddress toAddress = ControlAddress.create("/" + rootID + ".info");
//                Call call = Call.createCall(toAddress, returnAddress,
//                        System.nanoTime(), compAd);
                ControlAddress toAddress = ControlAddress.create(
                        boundAddress.getComponentAddress(), ComponentInterface.INFO);
                Call call = Call.createCall(toAddress, returnAddress,
                        System.nanoTime(), CallArguments.EMPTY);
                        
                infoMatchID = call.getMatchID();
                router.route(call);

        }

        private void processInfoResponse(Call call) {
            if (call.getMatchID() == infoMatchID) {
                CallArguments args = call.getArgs();
                if (args.getSize() > 0) {
                    ComponentInfo compInfo = null;
                    try {
                        compInfo = ComponentInfo.coerce(args.get(0));
//                        bindingInfo = ControlInfo.coerce(compInfo.getControlsInfo().get(
//                                PString.valueOf(boundAddress.getID())));
                        // @TODO on null?
                        bindingInfo = compInfo.getControlInfo(boundAddress.getID());
                        isProperty = bindingInfo.isProperty();
                        for (Adaptor a : adaptors) {
                            a.updateBindingConfiguration();
                        }
                        updateSyncConfiguration();
                    } catch (ArgumentFormatException ex) {
                        Logger.getLogger(DefaultBinding.class.getName()).log(Level.SEVERE,
                                "" + call + "\n" + compInfo, ex);
                    }
                }
            }
        }

        private void processSyncResponse(Call call) {
            if (isProperty && call.getMatchID() == lastCallID) {
                arguments = call.getArgs();
                for (Adaptor a : adaptors) {
                    a.update();
                }
            }
        }

        private void sendSyncRequest() {
            PacketRouter router = host.getPacketRouter();
            Call call = Call.createCall(boundAddress, getReturnAddress(),
                    System.nanoTime(), CallArguments.EMPTY);
            router.route(call);
            lastCallID = call.getMatchID();
        }

        @Override
        public ControlInfo getBindingInfo() {
            return bindingInfo;
        }

        public void actionPerformed(ActionEvent e) {
            if (bindingInfo != null && bindingInfo.isProperty()) {
                sendSyncRequest();
            }
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
