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

import org.praxislive.video.pipes.SourceIsFullException;
import org.praxislive.video.pipes.VideoPipe;
import org.praxislive.video.render.Surface;

/**
 *
 * @author Neil C Smith
 */
public abstract class SingleOut extends VideoPipe {

    VideoPipe sink;
    private long time;

    @Override
    public final void process(VideoPipe sink, Surface buffer, long time) {
        if (this.sink == sink) {
            this.time = time;
            processImpl(sink, buffer, time);
        }
    }

    void processImpl(VideoPipe sink, Surface surface, long time) {
        process(surface, sinkRequiresRender(sink, time));
    }

    @Override
    protected void registerSource(VideoPipe source) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void unregisterSource(VideoPipe source) {
    }

    @Override
    public final void registerSink(VideoPipe sink) throws SourceIsFullException {
        if (sink == null) {
            throw new NullPointerException();
        }
        if (this.sink != null) {
            throw new SourceIsFullException();
        }
        this.sink = sink;
    }

    @Override
    public final void unregisterSink(VideoPipe sink) {
        if (this.sink == sink) {
            this.sink = null;
        }
    }

    protected void nextFrame(boolean rendering) {
        // no op hook
    }

    protected abstract void process(Surface surface, boolean rendering);

    protected long getTime() {
        return time;
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
    public VideoPipe getSource(int idx) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public final int getSinkCount() {
        return sink == null ? 0 : 1;
    }

    @Override
    public final int getSinkCapacity() {
        return 1;
    }

    @Override
    public VideoPipe getSink(int idx) {
        if (idx == 0 && sink != null) {
            return sink;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    protected boolean isRenderRequired(VideoPipe source, long time) {
        return false;
    }
}
