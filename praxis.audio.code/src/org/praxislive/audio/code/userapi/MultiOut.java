/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 *
 */
package org.praxislive.audio.code.userapi;

import java.util.ArrayList;
import java.util.List;
import org.jaudiolibs.pipes.Buffer;
import org.jaudiolibs.pipes.Pipe;
import org.jaudiolibs.pipes.SourceIsFullException;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
abstract class MultiOut extends Pipe {

    private int maxSinks;
    private List<Pipe> sinks;
    private Buffer[] buffers;
    private long time;
    private long renderReqTime;
    private boolean renderReqCache;
    private int renderIdx = 0;

    protected MultiOut(int maxSinks) {
        if (maxSinks < 1) {
            throw new IllegalArgumentException();
        }
        this.maxSinks = maxSinks;
        this.sinks = new ArrayList<Pipe>();
        buffers = new Buffer[0];
    }

    @Override
    protected void process(Pipe sink, Buffer buffer, long time) {
        int sinkIndex = sinks.indexOf(sink);
        if (sinkIndex < 0) {
            return;
        }

        if (this.time != time) {
            boolean rendering = isRendering(time);
            this.time = time;
            checkBuffers(buffer);
            process(buffers, rendering);
        }
        if (sinkRequiresRender(sink, time)) {
            writeOutput(buffers, buffer, sinkIndex);
        }
    }

    private void checkBuffers(Buffer out) {
        // check size
        if (buffers.length != sinks.size()) {
            Buffer[] newBufs = new Buffer[sinks.size()];
            System.arraycopy(buffers, 0, newBufs, 0,
                    buffers.length < newBufs.length ? buffers.length : newBufs.length);
            buffers = newBufs;
        }
        // validate 
        for (int i = 0; i < buffers.length; i++) {
            Buffer in = buffers[i];
            if (in == null || !out.isCompatible(in)) {
                buffers[i] = out.createBuffer();
            }
        }
    }

    protected void process(Buffer[] buffers, boolean rendering) {
        // no op
    }

    protected void writeOutput(Buffer[] inputs, Buffer output, int index) {
        if (index < inputs.length) {
            Buffer in = inputs[index];
            float[] inData = in.getData();
            float[] outData = output.getData();
            System.arraycopy(inData, 0, outData, 0, in.getSize());
        } else {
            output.clear();
        }
    }
    
    protected long getTime() {
        return time;
    }

    @Override
    protected void registerSink(Pipe sink) throws SourceIsFullException {
        if (sink == null) {
            throw new NullPointerException();
        }
        if (sinks.contains(sink)) {
            throw new IllegalArgumentException();
        }
        if (sinks.size() == maxSinks) {
            throw new SourceIsFullException();
        }
        sinks.add(sink);
    }

    @Override
    protected void unregisterSink(Pipe sink) {
        sinks.remove(sink);
    }

    @Override
    protected void registerSource(Pipe source) {
        throw new SourceIsFullException();
    }

    @Override
    protected void unregisterSource(Pipe source) {
        return;
    }

    @Override
    protected boolean isRenderRequired(Pipe source, long time) {
        return isRendering(time);
    }

    private boolean isRendering(long time) {
        if (sinks.size() == 1) {
            return simpleRenderingCheck(time);
        } else {
            return protectedRenderingCheck(time);
        }
    }

    private boolean simpleRenderingCheck(long time) {
        if (time != renderReqTime) {
            renderReqTime = time;
            renderReqCache = sinkRequiresRender(sinks.get(0), time);
        }
        return renderReqCache;
    }

    private boolean protectedRenderingCheck(long time) {
        Pipe sink;
        if (renderIdx > 0) {
            while (renderIdx < sinks.size()) {
                sink = sinks.get(renderIdx);
                renderIdx++;
                if (sinkRequiresRender(sink, time)) {
                    renderIdx = 0;
                    return true;
                }
            }
            return false;
        } else {
            if (renderReqTime != time) {
                renderReqTime = time;
                renderReqCache = false;
                while (renderIdx < sinks.size()) {
//                    if (sinks.get(renderIdx++).isRenderRequired(this, time)) {
                    sink = sinks.get(renderIdx);
                    renderIdx++;
                    if (sinkRequiresRender(sink, time)) {
                        renderReqCache = true;
                        break;
                    }
                }
                renderIdx = 0;
            }
            return renderReqCache;
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
        return sinks.size();
    }

    @Override
    public int getSinkCapacity() {
        return maxSinks;
    }

    @Override
    public Pipe getSink(int idx) {
        return sinks.get(idx);
    }
}