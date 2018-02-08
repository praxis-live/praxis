/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Neil C Smith.
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
 *
 */
package org.praxislive.midi.components;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import org.praxislive.core.Argument;
import org.praxislive.core.IllegalRootStateException;
import org.praxislive.core.Lookup;
import org.praxislive.core.Packet;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.types.PMap;
import org.praxislive.impl.AbstractRoot;
import org.praxislive.impl.ArgumentProperty;
import org.praxislive.impl.InstanceLookup;
import org.praxislive.impl.StringProperty;
import org.praxislive.midi.MidiInputContext;

/**
 *
 * @author Neil C Smith
 */
public class MidiRoot extends AbstractRoot {

    private final static Logger LOG = Logger.getLogger(MidiRoot.class.getName());
    private ArgumentProperty device;
    private MidiThreadRouter router;
    private MidiDevice midiDevice;
    private Transmitter transmitter;
    private Lookup lookup;
    private MidiContextReceiver receiver;
    private Lock midiLock;
    private MidiMessage lastMessage;

    public MidiRoot() {
        buildControls();
        midiLock = new ReentrantLock();
    }

    private void buildControls() {
        ArgumentInfo info = ArgumentInfo.create(
                Argument.class, PMap.create(ArgumentInfo.KEY_EMPTY_IS_DEFAULT, true));
        device = ArgumentProperty.create(info);
        registerControl("device", device);
        registerControl("last-message", StringProperty.builder()
                .binding(new StringProperty.ReadBinding() {
                    @Override
                    public String getBoundValue() {
                        if (lastMessage != null) {
                            if (lastMessage instanceof ShortMessage) {
                                ShortMessage sm = (ShortMessage) lastMessage;
                                StringBuilder sb = new StringBuilder("Ch:")
                                        .append(sm.getChannel() + 1)
                                        .append(" Cmd:")
                                        .append(sm.getCommand())
                                        .append(" Data1:")
                                        .append(sm.getData1())
                                        .append(" Data2:")
                                        .append(sm.getData2());
                                return sb.toString();
                            } else {
                                return lastMessage.toString();
                            }
                        } else {
                            return "";
                        }
                    }
                }).build());
    }

    @Override
    protected void activating() {
        super.activating();
        router = new MidiThreadRouter(getPacketRouter());
        receiver = new MidiContextReceiver();
    }

    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = InstanceLookup.create(super.getLookup(), router, receiver);
        }
        return lookup;
    }

    @Override
    protected void starting() {
        super.starting();
        lastMessage = null;
        try {
            midiDevice = getDevice(device.getValue().toString());
            transmitter = midiDevice.getTransmitter();
            transmitter.setReceiver(receiver);
            midiDevice.open();
        } catch (MidiUnavailableException ex) {
            try {
                setIdle();
            } catch (IllegalRootStateException ex1) {
                // ignore
            }
        }
    }

    @Override
    protected void stopping() {
        super.stopping();
        closeDevice();

    }

    @Override
    protected void terminating() {
        super.terminating();
        closeDevice();

    }

    private void closeDevice() {
        if (transmitter != null) {
            transmitter.setReceiver(null);
            transmitter.close();
            transmitter = null;
        }
        if (midiDevice != null) {
            midiDevice.close();
            midiDevice = null;
        }
    }

    private MidiDevice getDevice(String device) throws MidiUnavailableException {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        if (infos.length == 0) {
            throw new MidiUnavailableException();
        }
        if (device == null || device.isEmpty()) {
            for (MidiDevice.Info info : infos) {
                MidiDevice dev = MidiSystem.getMidiDevice(info);
                if (dev.getMaxTransmitters() != 0) {
                    return dev;
                }
            }
            throw new MidiUnavailableException();
        } else {
            Pattern pattern = Pattern.compile(Pattern.quote(device), Pattern.CASE_INSENSITIVE);
            for (MidiDevice.Info info : infos) {
                if (pattern.matcher(info.getName()).matches()) {
                    MidiDevice dev = MidiSystem.getMidiDevice(info);
                    if (dev.getMaxTransmitters() != 0) {
                        return dev;
                    }
                }
            }
            for (MidiDevice.Info info : infos) {
                if (pattern.matcher(info.getName()).find()) {
                    MidiDevice dev = MidiSystem.getMidiDevice(info);
                    if (dev.getMaxTransmitters() != 0) {
                        return dev;
                    }
                }
            }
        }
        throw new MidiUnavailableException();

    }

    private class MidiContextReceiver
            extends MidiInputContext implements Receiver {

        public void send(final MidiMessage message, long timeStamp) {
            final long time = System.nanoTime(); //@TODO use timestamp
            invokeLater(new Runnable() {
                public void run() {
//                    if (getState() == RootState.ACTIVE_RUNNING) {
                    if (lastMessage instanceof ShortMessage) {
                        if ( ((ShortMessage) message).getCommand() < 0xF0) {
                            lastMessage = message;
                        }
                    } else {
                        lastMessage = message;
                    }
                    dispatch(message, time);
//                    } 
                }
            });
        }

        public void close() {
            // no op
        }
    }

    private class MidiThreadRouter implements PacketRouter {

        private PacketRouter delegate;

        private MidiThreadRouter(PacketRouter delegate) {
            this.delegate = delegate;
        }

        public void route(Packet packet) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "Sending call\n" + packet);
            }
            delegate.route(packet);

        }
    }
}
