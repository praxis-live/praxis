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

import java.util.ArrayList;
import java.util.List;
import org.jaudiolibs.pipes.*;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class MultiInOut implements Source, Sink {

    private int maxSources;
    private List<Source> sources;
    private int maxSinks;
    private List<Sink> sinks;
    private Buffer[] buffers;
    private long time;
    private long renderReqTime;
    private boolean renderReqCache;
    private int renderIdx = 0;

    protected MultiInOut(int maxSources, int maxSinks) {
        if (maxSources < 0 || maxSinks < 1) {
            throw new IllegalArgumentException();
        }
        this.maxSources = maxSources;
        this.sources = new ArrayList<Source>();
        this.maxSinks = maxSinks;
        this.sinks = new ArrayList<Sink>();
        buffers = new Buffer[0];
    }

    @Override
    public void process(Buffer buffer, Sink sink, long time) {
        int sinkIndex = sinks.indexOf(sink);
        if (sinkIndex < 0) {
            return;
        }

        if (this.time != time) {
            boolean rendering = isRendering(time);
            this.time = time;
            checkBuffers(buffer);
            callSources(time);
            process(buffers, rendering);
        }
        if (sink.isRenderRequired(this, time)) {
            writeOutput(buffers, buffer, sinkIndex);
        }
    }

    private void checkBuffers(Buffer out) {
        // check size
        if (buffers.length != sources.size()) {
            Buffer[] newBufs = new Buffer[sources.size()];
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

    private void callSources(long time) {
        for (int i = 0; i < sources.size(); i++) {
            sources.get(i).process(buffers[i], this, time);
        }
    }
    
    protected void process(Buffer[] buffers, boolean rendering){
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

    @Override
    public void registerSink(Sink sink) throws SourceIsFullException {
        if (sink == null) {
            throw new NullPointerException();
        }
        if (sinks.contains(sink)) {
            return;
        }
        if (sinks.size() == maxSinks) {
            throw new SourceIsFullException();
        }
        sinks.add(sink);
    }

    @Override
    public void unregisterSink(Sink sink) {
        sinks.remove(sink);
    }

    @Override
    public Sink[] getSinks() {
        return sinks.toArray(new Sink[sinks.size()]);
    }

    @Override
    public void addSource(Source source) throws SinkIsFullException, SourceIsFullException {
        if (source == null) {
            throw new NullPointerException();
        }
        if (sources.contains(source)) {
            return;
        }
        if (sources.size() == maxSources) {
            throw new SourceIsFullException();
        }
        source.registerSink(this);
        sources.add(source);
    }

    @Override
    public void removeSource(Source source) {
        if (sources.contains(source)) {
            source.unregisterSink(this);
            sources.remove(source);
        }
    }

    @Override
    public Source[] getSources() {
        return sources.toArray(new Source[sources.size()]);
    }

    @Override
    public boolean isRenderRequired(Source source, long time) {
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
            renderReqCache = sinks.get(0).isRenderRequired(this, time);
        }
        return renderReqCache;
    }

    private boolean protectedRenderingCheck(long time) {
        if (renderIdx > 0) {
            while (renderIdx < sinks.size()) {
                if (sinks.get(renderIdx++).isRenderRequired(this, time)) {
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
                    if (sinks.get(renderIdx++).isRenderRequired(this, time)) {
                        renderReqCache = true;
                        break;
                    }
                }
                renderIdx = 0;
            }
            return renderReqCache;
        }
    }
}
