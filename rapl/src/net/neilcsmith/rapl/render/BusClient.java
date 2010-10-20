/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
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
 */
package net.neilcsmith.rapl.render;

import java.nio.FloatBuffer;
import net.neilcsmith.audioservers.AudioContext;
import net.neilcsmith.rapl.core.BufferRateListener;
import java.util.ArrayList;
import java.util.List;
import net.neilcsmith.audioservers.AudioClient;
import net.neilcsmith.rapl.core.Buffer;
import net.neilcsmith.rapl.core.Bus;
import net.neilcsmith.rapl.core.Sink;
import net.neilcsmith.rapl.core.SinkIsFullException;
import net.neilcsmith.rapl.core.Source;
import net.neilcsmith.rapl.core.SourceIsFullException;

/**
 *
 * @author Neil C Smith
 */
//@TODO Optimise reading and writing from buffers now we've moved to float[]
public class BusClient implements Bus, AudioClient {

    private float sampleRate;
    private int bufferSize;
    private long time;
    private InputSource[] sources;
    private OutputSink[] sinks;
    private List<BufferRateListener> listeners = new ArrayList<BufferRateListener>();

    /**
     * 
     * @param inputs
     * @param outputs
     */
    public BusClient(int inputs, int outputs) {
        if (inputs < 0 || outputs < 1) {
            throw new IllegalArgumentException();
        }
        sinks = new OutputSink[outputs];
        for (int i = 0; i < outputs; i++) {
            sinks[i] = new OutputSink();
        }
        sources = new InputSource[inputs];
        for (int i = 0; i < inputs; i++) {
            sources[i] = new InputSource();
        }


    }

    public void addBufferRateListener(BufferRateListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
    }

    public void removeBufferRateListener(BufferRateListener listener) {
        listeners.remove(listener);
    }

    public long getTime() {
        return time;
    }

    public Sink getSink(int index) {
        return sinks[index];
    }

    public int getSinkCount() {
        return sinks.length;
    }

    public Source getSource(int index) {
        return sources[index];
    }

    public int getSourceCount() {
        return sources.length;
    }

    public void disconnectAll() {
        for (Source source : sources) {
            for (Sink sink : source.getSinks()) {
                sink.removeSource(source);
            }
        }
        for (Sink sink : sinks) {
            for (Source source : sink.getSources()) {
                sink.removeSource(source);
            }
        }
    }

    public void configure(AudioContext context) throws Exception {
        this.sampleRate = context.getSampleRate();
        this.bufferSize = context.getMaxBufferSize();
        for (OutputSink sink : sinks) {
            sink.buffer = new DefaultBuffer(sampleRate, bufferSize);
        }
        int activeCount = Math.min(context.getOutputChannelCount(), sinks.length);
        for (int i = 0; i < activeCount; i++) {
            sinks[i].active = true;
        }
    }

    public boolean process(long time, List<FloatBuffer> inputs, List<FloatBuffer> outputs, int nframes) {
        if (nframes != bufferSize) {
            // @TODO allow variable buffer sizes
            return false;
        }
        setTime(time);
        setInputData(inputs);
        fireListeners();
        processSinks();
        writeOutput(outputs, nframes);
        return true;
    }

    private void setTime(long time) {
        this.time = time;
    }

    private void setInputData(List<FloatBuffer> inputs) {
        int count = Math.min(inputs.size(), sources.length);
        for (int i = 0; i < count; i++) {
            sources[i].data = inputs.get(i);
        }
    }

    private void fireListeners() {
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
            listeners.get(i).nextBuffer(this);
        }
    }

    private void processSinks() {
        for (OutputSink sink : sinks) {
            sink.process();
        }
    }

    private void writeOutput(List<FloatBuffer> outputs, int nframes) {
        int count = Math.min(sinks.length, outputs.size());
        for (int i=0; i<count; i++) {
            float[] data = sinks[i].buffer.getData();
            FloatBuffer out = outputs.get(i);
            for (int f=0; f<nframes; f++) {
                out.put(f, data[f]);
            }
        }
    }

    public void shutdown() {
        for (OutputSink sink : sinks) {
            sink.active = false;
        }
    }

    private class OutputSink implements Sink {

        private Source source; // only allow one connection
        private boolean active = false;
        private DefaultBuffer buffer;

        public void addSource(Source source) throws SinkIsFullException, SourceIsFullException {
            if (source == null) {
                throw new NullPointerException();
            }
            if (this.source != null) {
                throw new SinkIsFullException();
            }
            source.registerSink(this);
            this.source = source;
        }

        public void removeSource(Source source) {
            if (this.source == source) {
                source.unregisterSink(this);
                this.source = null;
            }
        }

        public boolean isRenderRequired(Source source, long time) {
            return active; //(source == this.source && time == this.time);
        }

        public Source[] getSources() {
            if (source == null) {
                return new Source[0];
            } else {
                return new Source[]{source};
            }
        }

        private void process() {
            if (source != null) {
                source.process(buffer, this, time);
            } else if (active) {
                buffer.clear();
            }
        }
    }

    private class InputSource implements Source {

        private Sink sink;
        private FloatBuffer data;

        private InputSource() {
            data = null;
        }

        public void process(Buffer buffer, Sink sink, long time) {
            if (sink == this.sink && sink.isRenderRequired(this, time)) {
                FloatBuffer in = data;
                if (in == null) {
                    buffer.clear();
                } else {
                    int len = Math.min(in.capacity(), buffer.getSize());
                    float[] out = buffer.getData();
                    for (int i = 0; i < len; i++) {
                        out[i] = in.get(i);
                    }
                }
            }
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time, boolean recurse) {
            // ignore ??
        }

        public void registerSink(Sink sink) throws SourceIsFullException {
            if (sink == null) {
                throw new NullPointerException();
            }
            if (this.sink != null) {
                throw new SourceIsFullException();
            }
            this.sink = sink;
        }

        public void unregisterSink(Sink sink) {
            if (this.sink == sink) {
                this.sink = null;
            }
        }

        public Sink[] getSinks() {
            if (sink == null) {
                return new Sink[0];
            } else {
                return new Sink[]{sink};
            }
        }
    }
}
