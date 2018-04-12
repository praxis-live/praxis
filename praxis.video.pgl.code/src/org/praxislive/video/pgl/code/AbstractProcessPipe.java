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
package org.praxislive.video.pgl.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.praxislive.video.pipes.SourceIsFullException;
import org.praxislive.video.pipes.VideoPipe;
import org.praxislive.video.render.Surface;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
abstract class AbstractProcessPipe extends VideoPipe {

    private int maxSources;
    private List<VideoPipe> sources;
    private VideoPipe sink;
    private long renderReqTime;
    private boolean renderReqCache;

    protected AbstractProcessPipe(int maxSources) {
        if (maxSources < 0) {
            throw new IllegalArgumentException();
        }
        this.maxSources = maxSources;
        this.sources = new ArrayList<>(maxSources);
    }

    @Override
    protected void process(VideoPipe sink, Surface output, long time) {
        if (this.sink == sink) {
            update(time);
            callSources(output, time);
            if (isRendering(time)) {
                render(output, time);
            }
        }
    }
        
    protected abstract void update(long time);
    
    protected abstract void callSources(Surface output, long time);
    
    protected abstract void render(Surface output, long time);
    
    @Override
    protected void registerSink(VideoPipe sink) throws SourceIsFullException {
        if (this.sink != null) {
            throw new SourceIsFullException();
        }
        this.sink = Objects.requireNonNull(sink);
    }

    @Override
    protected void unregisterSink(VideoPipe sink) {
        if (this.sink == sink) {
            this.sink = null;
        }
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

    protected boolean isRendering(long time) {
        if (time != renderReqTime) {
            if (sink == null) {
                renderReqCache = false;
            } else {
                renderReqCache = sinkRequiresRender(sink, time);
            }
            renderReqTime = time;
        }
        return renderReqCache;
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
    
    protected int getSourceIndex(VideoPipe source) {
        return sources.indexOf(source);
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
    public VideoPipe getSink(int idx) {
        if (idx == 0 && sink != null) {
            return sink;
        }
        throw new IndexOutOfBoundsException();
    }
}
