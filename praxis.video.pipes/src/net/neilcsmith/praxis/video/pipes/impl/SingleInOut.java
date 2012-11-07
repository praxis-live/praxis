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
package net.neilcsmith.praxis.video.pipes.impl;

import net.neilcsmith.praxis.video.pipes.SinkIsFullException;
import net.neilcsmith.praxis.video.pipes.VideoPipe;
import net.neilcsmith.praxis.video.render.Surface;

/**
 *
 * @author Neil C Smith
 */
public abstract class SingleInOut extends SingleOut {

    private VideoPipe source;
    private long renderReqTime;
    private boolean renderReqCache;

    @Override
    public final void registerSource(VideoPipe source) {
        if (source == null) {
            throw new NullPointerException();
        }
        if (this.source != null) {
            throw new SinkIsFullException();
        }
        this.source = source;
    }

    @Override
    public final void unregisterSource(VideoPipe source) {
        if (this.source == source) {
            this.source = null;
        }
    }

    @Override
    public final VideoPipe getSource(int idx) {
        if (idx == 0 && source != null) {
            return source;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public final int getSourceCount() {
        return source == null ? 0 : 1;
    }

    @Override
    public final int getSourceCapacity() {
        return 1;
    }

    @Override
    protected boolean isRenderRequired(VideoPipe source, long time) {
        return isRendering(time);
    }

    boolean isRendering(long time) {
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
    void processImpl(VideoPipe sink, Surface surface, long time) {
        if (this.sink == sink) {
            if (source == null) {
                surface.clear();
            } else {
                callSource(source, surface, time);
            }
            process(surface, isRendering(time));

        }
    }
}
