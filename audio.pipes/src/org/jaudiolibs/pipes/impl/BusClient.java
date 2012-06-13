/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
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
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 *
 * Linking this work statically or dynamically with other modules is making a
 * combined work based on this work. Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this work give you permission
 * to link this work with independent modules to produce an executable,
 * regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that
 * you also meet, for each linked independent module, the terms and conditions of
 * the license of that module. An independent module is a module which is not
 * derived from or based on this work. If you modify this work, you may extend
 * this exception to your version of the work, but you are not obligated to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package org.jaudiolibs.pipes.impl;

import java.nio.FloatBuffer;
import org.jaudiolibs.audioservers.AudioConfiguration;
import org.jaudiolibs.pipes.BufferRateListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.jaudiolibs.audioservers.AudioClient;
import org.jaudiolibs.pipes.*;

/**
 *
 * @author Neil C Smith
 */
public class BusClient implements Bus, AudioClient {
    
    private final static Logger LOG = Logger.getLogger(BusClient.class.getName());

    private float sampleRate;
    private int bufferSize;
    private int intBufferSize;
    private long time;
    private long bufferTime;
    private long avgBufferTime;
    private long startTime;
    private long bufferCount;
    private InputSource[] sources;
    private OutputSink[] sinks;
//    private List<BufferRateListener> listeners = new ArrayList<BufferRateListener>();
    private BufferRateListener[] listeners;
    private ConfigurationListener[] confListeners;

    /**
     *
     * @param inputs
     * @param outputs
     */
    public BusClient(int inputs, int outputs) {
        this(0, inputs, outputs);
    }
    
    public BusClient(int intBufferSize, int inputs, int outputs) {
        if (intBufferSize < 0 || inputs < 0 || outputs < 1) {
            throw new IllegalArgumentException();
        }
        this.intBufferSize = intBufferSize;
        sinks = new OutputSink[outputs];
        for (int i = 0; i < outputs; i++) {
            sinks[i] = new OutputSink();
        }
        sources = new InputSource[inputs];
        for (int i = 0; i < inputs; i++) {
            sources[i] = new InputSource();
        }
        listeners = new BufferRateListener[0];
        confListeners = new ConfigurationListener[0];
    }

    @Override
    public void addBufferRateListener(BufferRateListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
//        if (listeners.contains(listener)) {
//            return;
//        }
//        listeners.add(listener);
        listeners = Utils.arrayAdd(listeners, listener);
    }

    @Override
    public void removeBufferRateListener(BufferRateListener listener) {
//        listeners.remove(listener);
        listeners = Utils.arrayRemove(listeners, listener);
    }
    
    public void addConfigurationListener(ConfigurationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        confListeners = Utils.arrayAdd(confListeners, listener);
    }
    
    public void removeConfigurationListener(ConfigurationListener listener) {
        confListeners = Utils.arrayRemove(confListeners, listener);
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public Pipe getSink(int index) {
        return sinks[index];
    }

    @Override
    public int getSinkCount() {
        return sinks.length;
    }

    @Override
    public Pipe getSource(int index) {
        return sources[index];
    }

    @Override
    public int getSourceCount() {
        return sources.length;
    }

    public void disconnectAll() {
        for (InputSource source : sources) {
            if (source.getSinkCount() == 1) {
                source.getSink(0).removeSource(source);
            }
        }
        for (OutputSink sink : sinks) {
            if (sink.getSourceCount() == 1) {
                sink.removeSource(sink.getSource(0));
            }
        }
    }

    @Override
    public void configure(AudioConfiguration context) throws Exception {
        if (!context.isFixedBufferSize()) {
            throw new IllegalArgumentException("BusClient can currently only work with fixed buffer sizes.");
        }
        this.sampleRate = context.getSampleRate();
        this.bufferSize = context.getMaxBufferSize();

        // check internal buffer size
        if (intBufferSize != 0) {
            if (bufferSize % intBufferSize != 0) {
                throw new IllegalArgumentException("External buffersize is not a multiple of internal buffersize.");
            }
        } else {
            intBufferSize = bufferSize;
        }
        bufferTime = (long) ((intBufferSize / context.getSampleRate()) * 1000000000);
        avgBufferTime = bufferTime;
        bufferCount = 0;
        LOG.fine("Buffer time is " + bufferTime);
        // call conf listeners here - after our validation
        for (ConfigurationListener listener : confListeners) {
            listener.configure(context);
        }
        
        for (InputSource source : sources) {
            source.data = new float[intBufferSize];
        }
        for (OutputSink sink : sinks) {
            sink.buffer = new DefaultBuffer(sampleRate, intBufferSize);
        }
        int activeCount = Math.min(context.getOutputChannelCount(), sinks.length);
        for (int i = 0; i < activeCount; i++) {
            sinks[i].active = true;
        }
    }

    @Override
    public boolean process(long time, List<FloatBuffer> inputs, List<FloatBuffer> outputs, int nframes) {
        if (nframes != bufferSize) {
            // @TODO allow variable buffer sizes
            return false;
        }
        if ((time - this.time) < 0) {
            LOG.warning("Passed in time less than last time\nPassed in : " + time + "\nLast time : " + this.time);
        }
            
        int count = nframes / intBufferSize;
//        if (bufferCount <= 0) {
//            startTime = time;    
//            bufferCount = 0;
//        } else {
//            avgBufferTime = (time - startTime) / bufferCount;
//        }     
//        bufferCount += count;
        
        time -= (count - 1) * avgBufferTime;
        for (int i = 0; i < count; i++) {
            setTime(time);
            readInput(inputs);
            fireListeners();
            processSinks();
            writeOutput(outputs);
            time += avgBufferTime;
        }
        return true;
    }

    private void setTime(long time) {
        this.time = time;
    }

    private void readInput(List<FloatBuffer> inputs) {
        int count = Math.min(inputs.size(), sources.length);
        for (int i = 0; i < count; i++) {
            FloatBuffer data = inputs.get(i);
            data.get(sources[i].data);
        }
    }

    private void fireListeners() {
//        int count = listeners.size();
//        for (int i = 0; i < count; i++) {
//            listeners.get(i).nextBuffer(this);
//        }
        for (BufferRateListener listener : listeners) {
            listener.nextBuffer(this);
        }
    }

    private void processSinks() {
        for (OutputSink sink : sinks) {
            sink.process();
        }
    }

    private void writeOutput(List<FloatBuffer> outputs) {
        int count = Math.min(sinks.length, outputs.size());
        for (int i = 0; i < count; i++) {
            float[] data = sinks[i].buffer.getData();
            FloatBuffer out = outputs.get(i);
            out.put(data, 0, intBufferSize);
        }
    }

    @Override
    public void shutdown() {
        for (OutputSink sink : sinks) {
            sink.active = false;
        }
        for (ConfigurationListener listener : confListeners) {
            listener.shutdown();
        }
    }
    
    public static interface ConfigurationListener {
        
        public void configure(AudioConfiguration context) throws Exception;
        
        public void shutdown();
        
    }

    private class OutputSink extends Pipe {

        private Pipe source; // only allow one connection
        private boolean active = false;
        private DefaultBuffer buffer;

        @Override
        public void registerSource(Pipe source) {
            if (source == null) {
                throw new NullPointerException();
            }
            if (this.source != null) {
                throw new SinkIsFullException();
            }
            this.source = source;
        }

        @Override
        public void unregisterSource(Pipe source) {
            if (this.source == source) {
                this.source = null;
            }
        }

        @Override
        protected boolean isRenderRequired(Pipe source, long time) {
            return active; //(source == this.source && time == this.time);
        }

        private void process() {
            if (source != null) {
//                source.process(buffer, this, time);
                callSource(source, buffer, time);
            } else if (active) {
                buffer.clear();
            }
        }

        @Override
        public int getSourceCount() {
            return source == null ? 0 : 1;
        }

        @Override
        public int getSourceCapacity() {
            return 1;
        }

        @Override
        public Pipe getSource(int idx) {
            if (idx == 0 && source != null) {
                return source;
            }
            throw new IndexOutOfBoundsException();
        }

        @Override
        public int getSinkCount() {
            return 0;
        }

        @Override
        public int getSinkCapacity() {
            return 0;
        }

        @Override
        public Pipe getSink(int idx) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        protected void process(Pipe sink, Buffer buffer, long time) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void registerSink(Pipe sink) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void unregisterSink(Pipe sink) {
            throw new UnsupportedOperationException();
        }
    }

    private class InputSource extends Pipe {

        private Pipe sink;
        private float[] data;

        private InputSource() {
            data = null;
        }

        @Override
        public void process(Pipe sink, Buffer buffer, long time) {
            if (sink == this.sink && sinkRequiresRender(sink, time)) {
//                FloatBuffer in = data;
//                if (in == null) {
//                    buffer.clear();
//                } else {
//                    int len = Math.min(in.capacity(), buffer.getSize());
//                    float[] out = buffer.getData();
//                    in.get(out, 0, len);
//                }
                if (data == null) {
                    buffer.clear();
                } else {
                    float[] out = buffer.getData();
                    int len = buffer.getSize();
                    System.arraycopy(data, 0, out, 0, len);
                }
            }
        }

        @Override
        public void registerSink(Pipe sink) throws SourceIsFullException {
            if (sink == null) {
                throw new NullPointerException();
            }
            if (this.sink != null) {
                throw new SourceIsFullException();
            }
            this.sink = sink;
        }

        @Override
        public void unregisterSink(Pipe sink) {
            if (this.sink == sink) {
                this.sink = null;
            }
        }

        @Override
        public int getSourceCount() {
            return 0;
        }

        @Override
        public int getSourceCapacity() {
            return 0;
        }

        @Override
        public Pipe getSource(int idx) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        public int getSinkCount() {
            return sink == null ? 0 : 1;
        }

        @Override
        public int getSinkCapacity() {
            return 1;
        }

        @Override
        public Pipe getSink(int idx) {
            if (idx == 0 && sink != null) {
                return sink;
            }
            throw new IndexOutOfBoundsException();
        }

        @Override
        protected boolean isRenderRequired(Pipe source, long time) {
            return false;
        }

        @Override
        protected void registerSource(Pipe source) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void unregisterSource(Pipe source) {
            throw new UnsupportedOperationException();
        }
    }
}
