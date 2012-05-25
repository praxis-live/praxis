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
import org.jaudiolibs.audioservers.AudioClient;
import org.jaudiolibs.pipes.Buffer;
import org.jaudiolibs.pipes.Bus;
import org.jaudiolibs.pipes.Sink;
import org.jaudiolibs.pipes.SinkIsFullException;
import org.jaudiolibs.pipes.Source;
import org.jaudiolibs.pipes.SourceIsFullException;

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

    @Override
    public void addBufferRateListener(BufferRateListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
    }

    @Override
    public void removeBufferRateListener(BufferRateListener listener) {
        listeners.remove(listener);
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public Sink getSink(int index) {
        return sinks[index];
    }

    @Override
    public int getSinkCount() {
        return sinks.length;
    }

    @Override
    public Source getSource(int index) {
        return sources[index];
    }

    @Override
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

    @Override
    public void configure(AudioConfiguration context) throws Exception {
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

    @Override
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

    @Override
    public void shutdown() {
        for (OutputSink sink : sinks) {
            sink.active = false;
        }
    }

    private class OutputSink implements Sink {

        private Source source; // only allow one connection
        private boolean active = false;
        private DefaultBuffer buffer;

        @Override
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

        @Override
        public void removeSource(Source source) {
            if (this.source == source) {
                source.unregisterSink(this);
                this.source = null;
            }
        }

        @Override
        public boolean isRenderRequired(Source source, long time) {
            return active; //(source == this.source && time == this.time);
        }

        @Override
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

        @Override
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

        @Override
        public void registerSink(Sink sink) throws SourceIsFullException {
            if (sink == null) {
                throw new NullPointerException();
            }
            if (this.sink != null) {
                throw new SourceIsFullException();
            }
            this.sink = sink;
        }

        @Override
        public void unregisterSink(Sink sink) {
            if (this.sink == sink) {
                this.sink = null;
            }
        }

        @Override
        public Sink[] getSinks() {
            if (sink == null) {
                return new Sink[0];
            } else {
                return new Sink[]{sink};
            }
        }

    }
}
