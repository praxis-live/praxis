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
 *
 */
package net.neilcsmith.praxis.midi.components;

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
import javax.sound.midi.Transmitter;
import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.core.InvalidAddressException;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Packet;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.RootHub;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.InstanceLookup;
import net.neilcsmith.praxis.impl.RootState;
import net.neilcsmith.praxis.impl.StringProperty;
import net.neilcsmith.praxis.midi.MidiInputContext;

/**
 *
 * @author Neil C Smith
 */
public class MidiRoot extends AbstractRoot {

    private static Logger logger = Logger.getLogger(MidiRoot.class.getName());
    private StringProperty device;
    private MidiThreadRouter router;
    private MidiDevice midiDevice;
    private Transmitter transmitter;
    private Lookup lookup;
    private MidiContextReceiver receiver;
    private Lock midiLock;

    public MidiRoot() {
        buildControls();
        midiLock = new ReentrantLock();
    }

    private void buildControls() {
        device = StringProperty.create("");
        registerControl("device", device);
    }

    @Override
    protected void initializing() {
        super.initializing();
        router = new MidiThreadRouter(getRootHub());
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
    protected void processControlFrame(Packet packet, RootState currentState) {
        try {
            midiLock.lock();
            super.processControlFrame(packet, currentState);
        } finally {
            midiLock.unlock();
        }
        
    }
    
    

    @Override
    protected void starting() {
        super.starting();
        try {
            midiDevice = getDevice(device.getValue());
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

        public void send(MidiMessage message, long timeStamp) {
            try {
                midiLock.lock();
                dispatch(message, System.nanoTime());
            } finally {
                midiLock.unlock();
            }
            
        }

        public void close() {
            // no op
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
}
