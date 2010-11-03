/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Neil C Smith. All rights reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 *
 */
package net.neilcsmith.praxis.midi;

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
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.core.InvalidAddressException;
import net.neilcsmith.praxis.core.Packet;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.RootHub;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.BasicControl;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 *
 * @author Neil C Smith
 */
public class DefaultMidiRoot extends AbstractRoot {

    private static Logger logger = Logger.getLogger(DefaultMidiRoot.class.getName());

    private StringProperty device;
    private StringProperty mapping;
    private volatile ControlMapping controlMap;
    private volatile MidiThreadRouter router;
    private volatile ControlAddress returnAddress;
    private MidiDevice midiDevice;
    private ControlReceiver receiver;
    private Transmitter transmitter;

    public DefaultMidiRoot() {
        buildControls();
    }

    private void buildControls() {
        device = StringProperty.create( "");
        registerControl("device", device);
        mapping = StringProperty.create( new MappingBinding(), "");
        registerControl("mapping", mapping);
        registerControl("_log", new LogControl());
    }

    @Override
    protected void initializing() {
        super.initializing();
        router = new MidiThreadRouter(getRootHub());
        returnAddress = ControlAddress.create(getAddress(), "_log");
    }

    @Override
    protected void starting() {
        super.starting();
        try {
            midiDevice = getDevice(device.getValue());
            receiver = new ControlReceiver();
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
        if (receiver != null) {
            receiver.close();
            receiver = null;
        }
        if (transmitter != null) {
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

    private class MappingBinding implements StringProperty.Binding {

        private String mappings;

        public void setBoundValue(long time, String value) {
            ControlMapping map = null;
            try {
                map = ControlMapping.create(value, returnAddress);
            } catch (ArgumentFormatException ex) {
                throw new IllegalArgumentException(ex);
            }
            controlMap = map;
            mappings = value;
        }

        public String getBoundValue() {
            return mappings;
        }
    }

    private class ControlReceiver implements Receiver {

        public void send(MidiMessage message, long timeStamp) {
            if (message instanceof ShortMessage) {
                ShortMessage msg = (ShortMessage) message;
                if (msg.getCommand() == ShortMessage.CONTROL_CHANGE) {
                    ControlMapping map = controlMap; // cache for thread safety
                    PacketRouter rtr = router;
                    if (map == null || rtr == null) {
                        return;
                    } else {
                        map.sendCalls(rtr, msg, System.nanoTime());
                    }
                }
            }
        }

        public void close() {
        }
    }

    private class MidiThreadRouter implements PacketRouter {

        private RootHub hub;

        private MidiThreadRouter(RootHub hub) {
            this.hub = hub;
        }

        public void route(Packet packet) {
            try {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Sending call\n" + packet);
                }
                hub.dispatch(packet);
            } catch (InvalidAddressException ex) {
                logger.log(Level.WARNING, "Invalid Root Address - " + packet.getRootID());
            }
        }

    }

    private class LogControl extends BasicControl {

        private LogControl() {
            super(DefaultMidiRoot.this);
        }

        public ControlInfo getInfo() {
            return null;
        }
    }
}
