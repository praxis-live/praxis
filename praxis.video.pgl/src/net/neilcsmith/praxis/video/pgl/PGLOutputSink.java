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
package net.neilcsmith.praxis.video.pgl;

import net.neilcsmith.praxis.video.pipes.SinkIsFullException;
import net.neilcsmith.praxis.video.pipes.VideoPipe;
import net.neilcsmith.praxis.video.render.Surface;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class PGLOutputSink extends VideoPipe {

    private VideoPipe source; // only allow one connection

    void process(PGLSurface surface, long time) {
        if (source != null) {
            callSource(source, surface, time);
        }
    }
    
    VideoPipe getSource() {
        return source;
    }
    
    void disconnect() {
        if (source != null) {
            removeSource(source);
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
    public VideoPipe getSource(int idx) {
        if (idx == 0 && source != null) {
            return source;
        } else {
            throw new IndexOutOfBoundsException();
        }
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
    public VideoPipe getSink(int idx) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    protected void registerSource(VideoPipe source) {
        if (this.source == null) {
            this.source = source;
        } else {
            throw new SinkIsFullException();
        }
    }

    @Override
    protected void unregisterSource(VideoPipe source) {
        if (this.source == source) {
            this.source = null;
        }
    }

    @Override
    protected boolean isRenderRequired(VideoPipe source, long time) {
        return true;
    }

    @Override
    protected void process(VideoPipe sink, Surface buffer, long time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void registerSink(VideoPipe sink) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void unregisterSink(VideoPipe sink) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
