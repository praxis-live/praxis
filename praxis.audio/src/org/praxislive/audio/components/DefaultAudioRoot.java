/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2014 Neil C Smith.
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
package org.praxislive.audio.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.audio.AudioContext;
import org.praxislive.audio.AudioSettings;
import org.praxislive.audio.ClientRegistrationException;
import org.praxislive.core.IllegalRootStateException;
import org.praxislive.core.Lookup;
import org.praxislive.impl.AbstractRoot;
import org.praxislive.impl.InstanceLookup;
import org.praxislive.impl.IntProperty;
import org.praxislive.impl.RootState;
import org.praxislive.impl.StringProperty;
import org.jaudiolibs.audioservers.AudioConfiguration;
import org.jaudiolibs.audioservers.AudioServer;
import org.jaudiolibs.audioservers.AudioServerProvider;
import org.jaudiolibs.audioservers.ext.ClientID;
import org.jaudiolibs.audioservers.ext.Device;
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
    private final static int DEFAULT_SAMPLERATE = 48000;
    private final static int DEFAULT_BLOCKSIZE = 64;

    private Map<String, LibraryInfo> libraries;
    private AudioContext.InputClient inputClient;
    private AudioContext.OutputClient outputClient;
    private BusClient bus;
    private BusListener busListener;
    private AudioServer server;

    // Permanent controls 
    private CheckedIntBinding sampleRate;
    private CheckedIntBinding blockSize;
    private LibraryBinding audioLib;

    // Dynamic controls
    private CheckedIntBinding extBufferSize;
    private DeviceBinding deviceName;
    private DeviceBinding inputDeviceName;

    private AudioContext audioCtxt;
    private Lookup lookup;
    private long period = -1;

    public DefaultAudioRoot() {
        extractLibraryInfo();
        buildDefaultControls();
        markDynamic();
    }

    private void extractLibraryInfo() {
        libraries = new LinkedHashMap<String, LibraryInfo>();
        List<Device> devices = new ArrayList<Device>();
        List<Device> inputDevices = new ArrayList<Device>();
        for (AudioServerProvider lib
                : Lookup.SYSTEM.getAll(AudioServerProvider.class)) {
            LOG.log(Level.FINE, "Audio Library : {0}", lib.getLibraryName());
            devices.clear();
            inputDevices.clear();
            for (Device device : lib.findAll(Device.class)) {
                if (device.getMaxOutputChannels() > 0) {
                    LOG.log(Level.FINE, "-- Found device : {0}", device.getName());
                    devices.add(device);
                } else if (device.getMaxInputChannels() > 0) {
                    LOG.log(Level.FINE, "-- Found input device : {0}", device.getName());
                    inputDevices.add(device);
                }
            }
            libraries.put(lib.getLibraryName(),
                    new LibraryInfo(lib,
                            devices.toArray(new Device[devices.size()]),
                            inputDevices.toArray(new Device[inputDevices.size()])));
        }
    }

    private void buildDefaultControls() {
        sampleRate = new CheckedIntBinding(DEFAULT_SAMPLERATE);
        IntProperty srCtl = IntProperty.builder()
                .binding(sampleRate)
                .minimum(2000)
                .maximum(192000)
                .defaultValue(DEFAULT_SAMPLERATE)  
                .suggestedValues(22050, 32000, 44100, 48000, 88200, 96000, 192000)
                .build();
        registerControl("sample-rate", srCtl);

        blockSize = new CheckedIntBinding(DEFAULT_BLOCKSIZE);
        IntProperty brCtl = IntProperty.builder()
                .binding(blockSize)
                .minimum(1)
                .maximum(512)
                .defaultValue(DEFAULT_BLOCKSIZE)
                .build();
        registerControl("block-size", brCtl);

        List<String> libs = new ArrayList<String>(libraries.keySet());
        Collections.sort(libs);
        libs.add(0, "");

        audioLib = new LibraryBinding();
        StringProperty libCtl = StringProperty.builder()
                .binding(audioLib)
                .defaultValue("")
                .emptyIsDefault()
                .allowedValues(libs.toArray(new String[libs.size()]))
                .build();
        registerControl("library", libCtl);

        audioCtxt = new AudioCtxt();
    }

    private void updateLibrary(String lib) {
        unregisterControl("device");
        deviceName = null;
        unregisterControl("input-device");
        inputDeviceName = null;
        unregisterControl("ext-buffer-size");
        extBufferSize = null;

        if (lib.isEmpty()) {
            return;
        }

        LibraryInfo info = libraries.get(lib);
        if (info == null) {
            return;
        }

        if (!"JACK".equals(lib)) {
            deviceName = new DeviceBinding();
            StringProperty devCtl = StringProperty.builder()
                    .binding(deviceName)
                    .defaultValue("")
                    .emptyIsDefault()
                    .suggestedValues(deviceNames(info.devices))
                    .build();
            registerControl("device", devCtl);
            inputDeviceName = new DeviceBinding();
            StringProperty inCtl = StringProperty.builder()
                    .binding(inputDeviceName)
                    .defaultValue("")
                    .emptyIsDefault()
                    .suggestedValues(deviceNames(info.inputDevices))
                    .build();
            registerControl("input-device", inCtl);
            extBufferSize = new CheckedIntBinding(AudioSettings.getBuffersize());
            IntProperty bsCtl = IntProperty.builder()
                    .binding(extBufferSize)
                    .suggestedValues(64, 128, 256, 512, 1024, 2048, 4096)
                    .build();
            registerControl("ext-buffer-size", bsCtl);
        }

    }
    
    

    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = InstanceLookup.create(super.getLookup(), audioCtxt);
        }
        return lookup;
    }

    @Override
    protected AbstractRoot.Context createContext() {
        return new Context();
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
        bus = new BusClient(blockSize.value,
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
        setDelegate(new Runnable() {
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
        interrupt();
    }

    private AudioServer createServer(BusClient bus) throws Exception {
        float srate = sampleRate.value;
        int buffersize = getBuffersize();

        boolean usingDefault = false;
        LibraryInfo info = libraries.get(audioLib.value);
        if (info == null) {
            info = libraries.get(AudioSettings.getLibrary());
            if (info == null) {
                throw new IllegalStateException("Audio library not found");
            }
            usingDefault = true;
        }
        LOG.log(Level.FINE, "Found audio library {0}\n{1}", new Object[]{
            info.provider.getLibraryName(), info.provider.getLibraryDescription()
        });

        Device device = findDevice(info, usingDefault, false);
        if (device != null) {
            LOG.log(Level.FINE, "Found device : {0}", device.getName());
        }
        Device inputDevice = null;
        if (device != null && device.getMaxInputChannels() == 0 && bus.getSourceCount() > 0) {
            inputDevice = findDevice(info, usingDefault, true);
            if (inputDevice != null) {
                LOG.log(Level.FINE, "Found input device : {0}", inputDevice.getName());
            }
        }

        ClientID clientID = new ClientID("PraxisLIVE-" + getAddress().getRootID());

        AudioConfiguration ctxt = new AudioConfiguration(srate,
                bus.getSourceCount(),
                bus.getSinkCount(),
                buffersize,
                createCheckedExts(device, inputDevice, clientID)
        );
        return info.provider.createServer(ctxt, bus);
    }

    private int getBuffersize() {
        int req = extBufferSize == null
                ? AudioSettings.getBuffersize()
                : extBufferSize.value;
        int block = blockSize.value;
        if (req < 1 || block < 1) {
            throw new IllegalArgumentException("Buffer / block values out of range");
        }
        if (block > req) {
            return block;
        }
        int bsize = block;
        while (bsize < req) {
            bsize += block;
        }
        LOG.log(Level.FINE, "Requesting buffersize of : {0}", bsize);
        return bsize;
    }

    private Device findDevice(LibraryInfo info, boolean usingDefault, boolean input) {
        String name = null;
        
        if (usingDefault) {
            name = input ? AudioSettings.getInputDeviceName() : AudioSettings.getDeviceName();
        } else {
            if (input) {
                name = inputDeviceName == null ? null : inputDeviceName.value;
            } else {
                name = deviceName == null ? null : deviceName.value;
            }
        }
        
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        Device[] devices = input ? info.inputDevices : info.devices;
        for (Device device : devices) {
            if (device.getName().equals(name)) {
                return device;
            }
        }
        for (Device device : devices) {
            if (device.getName().contains(name)) {
                return device;
            }
        }
        return null;
    }
    
    private void validateDevices() {
        if (deviceName == null || inputDeviceName == null) {
            return;
        }
        LibraryInfo info = libraries.get(audioLib.value);
        if (info == null) {
            return;
        }
        Device primary = findDevice(info, false, false);
        if (primary != null && primary.getMaxInputChannels() > 0) {
            inputDeviceName.value = "";
        }
    }

    private String[] deviceNames(Device[] devices) {
        String[] names = new String[devices.length + 1];
        names[0] = "";
        for (int i = 0; i < devices.length; i++) {
            names[i+1] = devices[i].getName();
        }
        return names;
    }

    private Object[] createCheckedExts(Object... exts) {
        List<Object> lst = new ArrayList<Object>(exts.length);
        for (Object o : exts) {
            if (o != null) {
                lst.add(o);
            }
        }
        return lst.toArray();
    }

    private void makeInputConnections() {
        int count = Math.min(inputClient.getInputCount(), bus.getSourceCount());
        for (int i = 0; i < count; i++) {
            inputClient.getInputSink(i).addSource(bus.getSource(i));
        }
    }

    private void makeOutputConnections() {
        int count = Math.min(outputClient.getOutputCount(), bus.getSinkCount());
        for (int i = 0; i < count; i++) {
            bus.getSink(i).addSource(outputClient.getOutputSource(i));
        }
    }

    @Override
    protected void stopping() {
        if (bus == null) {
            return;
        }
//        setInterrupt(new Runnable() {
//            public void run() {
        server.shutdown();
        bus.disconnectAll();
        server = null;
        bus = null;
        busListener = null;
//            }
//        });
        interrupt();
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
                update(source.getTime(), true);
            } catch (IllegalRootStateException ex) {
                server.shutdown();
            }
        }

        public void configure(AudioConfiguration context) throws Exception {
            float srate = context.getSampleRate();
            if (Math.round(srate) != sampleRate.value) {
                sampleRate.value = Math.round(srate);
            }
            period = (long) ((blockSize.value
                    / srate) * 1000000000);
        }

        public void shutdown() {
            period = -1;
        }
    }

    private class AudioCtxt extends AudioContext {

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

        @Override
        public double getSampleRate() {
            return sampleRate.value;
        }

        @Override
        public int getBlockSize() {
            return blockSize.value;
        }
    }

    private class Context extends AbstractRoot.Context {

        @Override
        public long getPeriod() {
            return period;
        }

        @Override
        public boolean supportsPeriod() {
            return true;
        }

    }

    private class CheckedIntBinding implements IntProperty.Binding {

        private int value;

        private CheckedIntBinding(int defaultValue) {
            this.value = defaultValue;
        }

        public void setBoundValue(long time, int value) {
            if (getState() == RootState.ACTIVE_RUNNING) {
                throw new IllegalStateException();
            }
            this.value = value;
        }

        public int getBoundValue() {
            return value;
        }

    }

    private class CheckedStringBinding implements StringProperty.Binding {

        private String value;

        private CheckedStringBinding(String defaultValue) {
            this.value = defaultValue;
        }

        public void setBoundValue(long time, String value) {
            if (getState() == RootState.ACTIVE_RUNNING) {
                throw new IllegalStateException();
            }
            this.value = value;
        }

        public String getBoundValue() {
            return value;
        }

    }

    private class LibraryBinding implements StringProperty.Binding {

        private String value = "";

        public void setBoundValue(long time, String value) {
            if (getState() == RootState.ACTIVE_RUNNING) {
                throw new IllegalStateException();
            }
            if (this.value.equals(value)) {
                return;
            }
            if (value.isEmpty() || libraries.containsKey(value)) {
                this.value = value;
                updateLibrary(value);
            }
        }

        public String getBoundValue() {
            return value;
        }

    }
    
    private class DeviceBinding implements StringProperty.Binding {

        private String value = "";
        
        public void setBoundValue(long time, String value) {
            if (getState() == RootState.ACTIVE_RUNNING) {
                throw new IllegalStateException();
            }
            if (this.value.equals(value)) {
                return;
            }
            this.value = value;
            validateDevices();
        }

        public String getBoundValue() {
            return value;
        }

    }

    private static class LibraryInfo {

        private final AudioServerProvider provider;
        private final Device[] devices;
        private final Device[] inputDevices;

        private LibraryInfo(AudioServerProvider provider,
                Device[] devices, Device[] inputDevices) {
            this.provider = provider;
            this.devices = devices;
            this.inputDevices = inputDevices;
        }

    }
}
