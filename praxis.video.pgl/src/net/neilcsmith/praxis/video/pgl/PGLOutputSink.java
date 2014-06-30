/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    
    @Override
    public int getSourceCount() {
        return source == null ? 0 : 1;
    }

    @Override
    public int getSourceCapacity() {
        return 1;
    }

    public VideoPipe getSource() {
        return source;
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
