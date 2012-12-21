/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 Neil C Smith.
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
package net.neilcsmith.praxis.audio.components;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.audio.AudioContext;
import net.neilcsmith.praxis.audio.AudioSettings;
import net.neilcsmith.praxis.audio.ClientRegistrationException;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.InstanceLookup;
import org.jaudiolibs.audioservers.AudioConfiguration;
import org.jaudiolibs.audioservers.AudioServer;
import org.jaudiolibs.pipes.BufferRateListener;
import org.jaudiolibs.pipes.BufferRateSource;
import org.jaudiolibs.pipes.impl.BusClient;

/**
 *
 * @author Neil C Smith
 */
public class DefaultAudioRoot extends AbstractRoot {
    
    private final static Logger LOG = Logger.getLogger(DefaultAudioRoot.class.getName());
    private final static int MAX_CHANNELS = 16;
    private final static int DEFAULT_INTERNAL_BUFFERSIZE = 64;
    private AudioContext.InputClient inputClient;
    private AudioContext.OutputClient outputClient;
    private BusClient bus;
    private BusListener busListener;
    private AudioServer server;
    private ArgumentProperty sampleRate;
    private ArgumentProperty bufferSize;
    private ArgumentProperty audioLib;
    private ArgumentProperty device;
    private AudioContext hub;
    private Lookup lookup;
//    private Placeholder[] inputs;
//    private Placeholder[] outputs;
    private long period = -1;

    public DefaultAudioRoot() {
        buildControls();
//        inputs = new Placeholder[2];
//        inputs[0] = new Placeholder();
//        inputs[1] = new Placeholder();
//        outputs = new Placeholder[2];
//        outputs[0] = new Placeholder();
//        outputs[1] = new Placeholder();
    }

    private void buildControls() {
        ArgumentInfo sharedInfo = ArgumentInfo.create(
                Argument.class, PMap.create(ArgumentInfo.KEY_EMPTY_IS_DEFAULT, true));
        sampleRate = ArgumentProperty.create(sharedInfo);
        bufferSize = ArgumentProperty.create(sharedInfo);
        audioLib = ArgumentProperty.create(sharedInfo);
        device = ArgumentProperty.create(sharedInfo);
        registerControl("samplerate", sampleRate);
        registerControl("buffersize", bufferSize);
        registerControl("library", audioLib);
        registerControl("device", device);
        hub = new Hub();
    }

    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            initLookup();
        }
        return lookup;
    }

    private void initLookup() {
        ExecutionContext exctxt = super.getLookup().get(ExecutionContext.class);
        if (exctxt == null) {
            LOG.warning("No ExecutionContext found for DefaultAudioRoot");
            lookup = InstanceLookup.create(super.getLookup(), hub);
        } else {
            lookup = InstanceLookup.create(super.getLookup(), hub, new AudioExecutionContext(exctxt));
        }
    }

    @Override
    protected void starting() {
        if (outputClient == null) {
            try {
                setIdle();
            } catch (IllegalRootStateException ex) {
            }
            return;
        }
        int bsize = DEFAULT_INTERNAL_BUFFERSIZE;
        if (!bufferSize.getValue().isEmpty()) {
            try {
                bsize = PNumber.coerce(bufferSize.getValue()).toIntValue();
            } catch (Exception ex) {
                //fall through
            }
        }
        bus = new BusClient(bsize,
                inputClient == null ? 0 : inputClient.getInputCount(),
                outputClient.getOutputCount());
        busListener = new BusListener();
        bus.addBufferRateListener(busListener);
        bus.addConfigurationListener(busListener);
        if (inputClient != null) {
            makeInputConnections();
        }
        makeOutputConnections();
        try {
            server = createServer(bus);
        } catch (Exception ex) {
            Logger.getLogger(DefaultAudioRoot.class.getName()).log(Level.SEVERE, null, ex);
            try {
                setIdle();
            } catch (IllegalRootStateException ex1) {
            }
            return;
        }
        setInterrupt(new Runnable() {
            public void run() {
                try {
                    server.run();
                } catch (Exception ex) {
                    Logger.getLogger(DefaultAudioRoot.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    setIdle();
                } catch (IllegalRootStateException ex) {
                    // ignore - state already changed
                }
            }
        });
    }

    private AudioServer createServer(BusClient bus) throws Exception {
        float srate = getSamplerate();
        int bsize = getBuffersize();
        String lib = getLibrary();

        String dev = "";
        Argument arg = device.getValue();
        if (!arg.isEmpty()) {
            dev = arg.toString();
        }

        AudioConfiguration ctxt = new AudioConfiguration(srate,
                bus.getSourceCount(),
                bus.getSinkCount(), 
                bsize, true);
        return AudioServerLoader.getInstance().load(getLookup(), lib, dev,
                "praxis-" + getAddress().getRootID(), ctxt, bus, null);
    }

    private int getSamplerate() {
        Argument arg = sampleRate.getValue();
        if (!arg.isEmpty()) {
            try {
                return PNumber.coerce(arg).toIntValue();
            } catch (Exception ex) {
            }
        }
        return AudioSettings.getSamplerate();
    }

    private int getBuffersize() {
        Argument arg = bufferSize.getValue();
        if (!arg.isEmpty()) {
            try {
                return PNumber.coerce(arg).toIntValue();
            } catch (Exception ex) {
            }
        }
        return AudioSettings.getBuffersize();
    }

    private String getLibrary() {
        Argument arg = audioLib.getValue();
        if (!arg.isEmpty()) {
            return arg.toString();
        }
        return AudioSettings.getLibrary();
    }

    // @TODO fix this!
//    private void makeConnections(Bus bus) {
//        bus.getSink(0).addSource(outputs[0]);
//        bus.getSink(1).addSource(outputs[1]);
//        inputs[0].addSource(bus.getSource(0));
//        inputs[1].addSource(bus.getSource(1));
//    }
    
    private void makeInputConnections() {
        int count = Math.min(inputClient.getInputCount(), bus.getSourceCount());
        for (int i=0; i<count; i++) {
            inputClient.getInputSink(i).addSource(bus.getSource(i));
        }
    }
    
    private void makeOutputConnections() {
        int count = Math.min(outputClient.getOutputCount(), bus.getSinkCount());
        for (int i=0; i<count; i++) {
            bus.getSink(i).addSource(outputClient.getOutputSource(i));
        }
    }

    @Override
    protected void stopping() {
        if (bus == null) {
            return;
        }
        setInterrupt(new Runnable() {
            public void run() {
                server.shutdown();
                bus.disconnectAll();
                server = null;
                bus = null;
                busListener = null;
            }
        });
    }

    @Override
    protected void terminating() {
        super.terminating();
        AudioServer s = server;
        server = null;
        if (s != null) {
            s.shutdown();
        }
        BusClient b = bus;
        bus = null;
        if (b != null) {
            b.disconnectAll();
        }
    }

    private class BusListener implements BufferRateListener, BusClient.ConfigurationListener {

        public void nextBuffer(BufferRateSource source) {
            try {
                nextControlFrame(source.getTime());
            } catch (IllegalRootStateException ex) {
                server.shutdown();
            }
        }

        public void configure(AudioConfiguration context) throws Exception {
            period = (long) ((context.getMaxBufferSize()
                    / context.getSampleRate()) * 1000000000);
        }

        public void shutdown() {
            period = -1;
        }
    }

    private class Hub extends AudioContext {

        public int registerAudioInputClient(AudioContext.InputClient client) throws ClientRegistrationException {
            if (inputClient == null) {
                inputClient = client;
            } else {
                throw new ClientRegistrationException();
            }
            return MAX_CHANNELS;
        }

        public void unregisterAudioInputClient(AudioContext.InputClient client) {
            if (inputClient == client) {
                inputClient = null;
                if (bus != null) {
                    bus.disconnectAll();
                    makeOutputConnections();
                }
            }
        }

        public int registerAudioOutputClient(AudioContext.OutputClient client) throws ClientRegistrationException {
            if (outputClient == null) {
                    outputClient = client;                 
            } else {
                throw new ClientRegistrationException();
            }
            return MAX_CHANNELS;
        }

        public void unregisterAudioOutputClient(AudioContext.OutputClient client) {
            if (outputClient == client) {
                outputClient = null;
                if (bus != null) {
                    bus.disconnectAll();
                    try {
                        setIdle();
                    } catch (IllegalRootStateException ex) {
                        // ignore, already stopping?
                    }
                }
            }
        }
    }

    private class AudioExecutionContext extends ExecutionContext {

        private ExecutionContext delegate;

        private AudioExecutionContext(ExecutionContext delegate) {
            this.delegate = delegate;
        }

        @Override
        public long getPeriod() {
            return period;
        }

        public void removeStateListener(StateListener listener) {
            delegate.removeStateListener(listener);
        }

        public void removeClockListener(ClockListener listener) {
            delegate.removeClockListener(listener);
        }

        public long getTime() {
            return delegate.getTime();
        }

        public State getState() {
            return delegate.getState();
        }

        public void addStateListener(StateListener listener) {
            delegate.addStateListener(listener);
        }

        public void addClockListener(ClockListener listener) {
            delegate.addClockListener(listener);
        }
    }
}
