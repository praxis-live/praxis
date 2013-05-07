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
package net.neilcsmith.praxis.video.opengl.internal;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.video.opengl.ops.GLOp;
import net.neilcsmith.praxis.video.opengl.ops.GLOpCache;
import net.neilcsmith.praxis.video.render.PixelData;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.ops.Blit;
import net.neilcsmith.praxis.video.render.ops.Reverse;
import net.neilcsmith.praxis.video.render.utils.PixelArrayCache;

/**
 *
 * @author Neil C Smith
 */
public class GLSurface extends Surface {

    private final static Logger LOGGER = Logger.getLogger(GLSurface.class.getName());
    private final static PixelData[] EMPTY_DATA = new PixelData[0];
    private GLSurfaceData data;
    private boolean clear = true;
    private GLContext context;
    private FallbackProcessor fallback;
    private int modCount;

    GLSurface(GLContext context, int width, int height, boolean alpha) {
        super(width, height, alpha);
        this.context = context;
        fallback = new FallbackProcessor();
    }

    @Override
    public void process(SurfaceOp op, Surface... inputs) {
        modCount++;
        GLOp glop = GLOpCache.getInstance().find(op);
        if (glop != null) {
            try {
                glop.process(op, this, fallback, inputs);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        } else {
            fallback.process(op, inputs);
        }
    }

    public GLContext getGLContext() {
        return context;
    }
    
    void setData(GLSurfaceData data) {
        this.data = data;
    }
    
    GLSurfaceData getData() {
        return data;
    }

    private void makeSWReadable() {
        if (data == null) {
            data = new GLSurfaceData(width, height, alpha);
            data.pixels = PixelArrayCache.acquire(width * height, true);
        } else if (data.pixels == null) {
            data.pixels = PixelArrayCache.acquire(width * height, false);
            context.getRenderer().syncTextureToPixels(data);
        }
    }

    private void makeSWWritable() {
        context.getRenderer().invalidate(this);
        makeSWReadable();
        if (data.usage > 1) {
            GLSurfaceData tmp = new GLSurfaceData(width, height, alpha);
            tmp.pixels = PixelArrayCache.acquire(width * height, false);
            System.arraycopy(data.pixels, 0, tmp.pixels, 0, width * height);
            data.usage--;
            data = tmp;
        }
        // invalidate texture
        if (data.texture != null) {
            context.getTextureManager().release(data.texture);
            data.texture = null;
        }
        

    }

    private class FallbackProcessor implements GLOp.Bypass {

        @Override
        public void process(SurfaceOp op, Surface... inputs) {
            makeSWWritable();
            switch (inputs.length) {
                case 0:
                    processSW(op);
                    break;
                case 1:
                    processSW(op, inputs[0]);
                    break;
                default:
                    processSW(op, inputs);
                    break;
            }
        }

        private void processSW(SurfaceOp op) {
            op.process(data, EMPTY_DATA);
        }

        private void processSW(SurfaceOp op, Surface input) {
            if (input instanceof GLSurface) {
                GLSurface in = (GLSurface) input;
                in.makeSWReadable();
                op.process(data, in.data);
            } else {
                SurfaceOp rev = Reverse.op(op, data);
                input.process(rev);
            }
        }

        private void processSW(SurfaceOp op, Surface[] inputs) {
            PixelData[] pixelInputs = new PixelData[inputs.length];
            GLSurface in;
            for (int i = 0; i < inputs.length; i++) {
                if (inputs[i] instanceof GLSurface) {
                    in = (GLSurface) inputs[i];
                    in.makeSWReadable();
                    pixelInputs[i] = in.data;
                } else {
                    throw new UnsupportedOperationException("not yet implemented");
                }
            }
            op.process(data, pixelInputs);
        }
    }

    @Override
    public void clear() {
        release();
        clear = true;
    }

    @Override
    public boolean isClear() {
        return clear;
    }

    @Override
    public void release() {
        modCount++;
        context.getRenderer().invalidate(this);
        if (data != null) {
            data.usage--;
            if (data.usage <= 0) {
                assert data.usage == 0;
                LOGGER.log(Level.FINEST, "Releasing Data");
                if (data.pixels != null) {
                    LOGGER.log(Level.FINEST, "Releasing Pixels");
                    PixelArrayCache.release(data.pixels);
                    data.pixels = null;
                }
                if (data.texture != null) {
                    LOGGER.log(Level.FINEST, "Releasing Texture");
                    context.getTextureManager().release(data.texture);
                    data.texture = null;
                }
            }
            data = null;
        }
    }

    @Override
    public void copy(Surface source) {
        assert source != this;
        modCount++;
        if (checkCompatible(source, true, true)) {
            release();
            GLSurface src = (GLSurface) source;
            if (src.data != null) {
                data = src.data;
                data.usage++;
                clear = false;
            } else {
                LOGGER.log(Level.FINE, "Copied surface has no data");
                data = null;
                clear = true;
            }
        } else {
            process(Blit.op(), source);
        }
    }

    @Override
    public int getModCount() {
        return modCount;
    }
    

    @Override
    public boolean checkCompatible(Surface surface, boolean checkDimensions, boolean checkAlpha) {
        if (!(surface instanceof GLSurface)) {
            return false;
        }
        if (((GLSurface) surface).context != context) {
            return false;
        }
        if (checkDimensions && (surface.getWidth() != getWidth()
                || surface.getHeight() != getHeight())) {
            return false;
        }
        if (checkAlpha && (surface.hasAlpha() != hasAlpha())) {
            return false;
        }
        return true;
    }

    @Override
    public GLSurface createSurface(int width, int height, boolean alpha) {
        return context.createSurface(width, height, alpha);
    }
}