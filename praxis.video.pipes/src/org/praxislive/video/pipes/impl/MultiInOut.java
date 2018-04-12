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
 */
package org.praxislive.video.pipes.impl;

import java.util.ArrayList;
import java.util.List;
import org.praxislive.video.pipes.SourceIsFullException;
import org.praxislive.video.pipes.VideoPipe;
import org.praxislive.video.render.Surface;

/**
 *
 * @author Neil C Smith
 */
public abstract class MultiInOut extends VideoPipe {

    private int maxSources;
    private List<VideoPipe> sources;
    private int maxSinks;
    private List<VideoPipe> sinks;
    private Surface[] inputs;
    private long time;
    private long renderReqTime;
    private boolean renderReqCache;
    private int renderIdx = 0;

    protected MultiInOut(int maxSources, int maxSinks) {
        if (maxSources < 0 || maxSinks < 1) {
            throw new IllegalArgumentException();
        }
        this.maxSources = maxSources;
        this.sources = new ArrayList<VideoPipe>();
        this.maxSinks = maxSinks;
        this.sinks = new ArrayList<VideoPipe>();
        inputs = new Surface[0];
    }

    @Override
    protected final void process(VideoPipe sink, Surface output, long time) {
        int sinkIndex = sinks.indexOf(sink);
        if (sinkIndex < 0) {
            return;
        }
        if (this.time != time) {
            this.time = time;
            checkBuffers(output);
            callSources(time);
            processInputs(inputs, isRendering(time));
        }
        process(inputs, output, sinkIndex, sinkRequiresRender(sink, time));
    }

    private void checkBuffers(Surface out) {
        // check size
        if (inputs.length != sources.size()) {
            Surface[] newBufs = new Surface[sources.size()];
            System.arraycopy(inputs, 0, newBufs, 0,
                    inputs.length < newBufs.length ? inputs.length : newBufs.length);
            inputs = newBufs;
        }
        // validate 
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = validateInput(inputs[i], out, i);
        }
    }

    protected Surface validateInput(Surface input, Surface output, int index) {
        if (input == null || !output.checkCompatible(input, true, true)) {
            if (input != null) {
                input.release();
            }
            input = output.createSurface();
        }
        return input;
    }

    private void callSources(long time) {
        for (int i = 0; i < sources.size(); i++) {
            callSource(sources.get(i), inputs[i], time);
        }
    }

    protected void processInputs(Surface[] inputs, boolean rendering) {
        // no op hook
    }

    protected abstract void process(Surface[] inputs, Surface output, int index, boolean rendering);

    protected long getTime() {
        return time;
    }

    @Override
    protected void registerSink(VideoPipe sink) throws SourceIsFullException {
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
    protected void unregisterSink(VideoPipe sink) {
        sinks.remove(sink);
    }

    @Override
    protected void registerSource(VideoPipe source) {
        if (source == null) {
            throw new NullPointerException();
        }
        if (sources.contains(source)) {
            throw new IllegalArgumentException();
        }
        if (sources.size() == maxSources) {
            throw new SourceIsFullException();
        }
        sources.add(source);
    }

    @Override
    public void unregisterSource(VideoPipe source) {
        sources.remove(source);
    }

    @Override
    protected boolean isRenderRequired(VideoPipe source, long time) {
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
        VideoPipe sink;
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
        return sources.size();
    }

    @Override
    public int getSourceCapacity() {
        return maxSources;
    }

    @Override
    public VideoPipe getSource(int idx) {
        return sources.get(idx);
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
    public VideoPipe getSink(int idx) {
        return sinks.get(idx);
    }
}
