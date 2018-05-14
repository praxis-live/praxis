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
package org.praxislive.video.pgl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.video.pgl.ops.PGLOp;
import org.praxislive.video.render.PixelData;
import org.praxislive.video.render.Surface;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.ops.Blit;
import org.praxislive.video.render.ops.Reverse;
import org.praxislive.video.render.utils.PixelArrayCache;
import processing.core.PConstants;
import processing.core.PImage;

/**
 *
 * @author Neil C Smith
 */
public final class PGLSurface extends Surface {

    private final static Logger LOG = Logger.getLogger(PGLSurface.class.getName());
    private final static PixelData[] EMPTY_DATA = new PixelData[0];

    private final PGLContext context;
    private final FallbackProcessor fallback;

    private Data data;
    private boolean clear = true;
    private int modCount;

    PGLSurface(PGLContext context, int width, int height, boolean alpha) {
        super(width, height, alpha);
        this.context = context;
        fallback = new FallbackProcessor();
    }

    @Override
    public void process(SurfaceOp op, Surface... inputs) {
        modCount++;
        PGLOp glop = context.getOpCache().find(op);
        if (glop != null) {
            try {
                glop.process(op, this, fallback, inputs);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        } else {
            fallback.process(op, inputs);
        }
        clear = false;
    }

    public PGLGraphics getGraphics() {
        if (data == null) {
            LOG.fine("Data null - clearing graphics");
            data = new Data(width, height, alpha);
            data.graphics = context.acquireGraphics(width, height);
            data.graphics.beginDraw();
            data.graphics.background(0, 0, 0, alpha ? 0 : 255);
        } else if (data.usage > 1) {
            Data tmp = new Data(width, height, alpha);
            tmp.graphics = context.acquireGraphics(width, height);
            tmp.graphics.beginDraw();
            tmp.graphics.background(0, 0, 0, alpha ? 0 : 255);
            if (data.graphics == null) {
                LOG.fine("Data shared - creating copy from pixels");
                tmp.graphics.writePixelsARGB(data.pixels, alpha);
            } else {
                LOG.fine("Data shared - creating copy from graphics");
                tmp.graphics.blendMode(PConstants.REPLACE);
                tmp.graphics.tint(255,255,255,255);
                tmp.graphics.image(data.graphics,0,0);
            }
            data.usage--;
            data = tmp;
        } else if (data.graphics == null) {
            LOG.fine("Graphics null - copying data from pixels");
            data.graphics = context.acquireGraphics(width, height);
            data.graphics.beginDraw();
            data.graphics.background(0, 0, 0, alpha ? 0 : 255);
            data.graphics.writePixelsARGB(data.pixels, alpha);
            PixelArrayCache.release(data.pixels);
        }
        data.pixels = null;
        return data.graphics;
    }

    protected PImage asImage() {
        if (data != null && data.graphics != null) {
            return data.graphics;
        } else {
            LOG.fine("Creating PGLGraphics for pixel data image");
            return null;
        }
    }

    public PGLContext getContext() {
        return context;
    }

    private void makeSWReadable() {
        if (data == null) {
            data = new Data(width, height, alpha);
            data.pixels = PixelArrayCache.acquire(width * height, true);
        } else if (data.pixels == null) {
            data.pixels = PixelArrayCache.acquire(width * height, false);
            data.graphics.readPixelsARGB(data.pixels);
        }
    }

    private void makeSWWritable() {
        makeSWReadable();
        if (data.usage > 1) {
            Data tmp = new Data(width, height, alpha);
            tmp.pixels = PixelArrayCache.acquire(width * height, false);
            System.arraycopy(data.pixels, 0, tmp.pixels, 0, width * height);
            data.usage--;
            data = tmp;
        }
        // invalidate texture
        if (data.graphics != null) {
            context.releaseGraphics(data.graphics);
            data.graphics = null;
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
        if (data != null) {
            data.usage--;
            if (data.usage <= 0) {
                assert data.usage == 0;
                LOG.log(Level.FINEST, "Releasing Data");
                if (data.pixels != null) {
                    LOG.log(Level.FINEST, "Releasing Pixels");
                    PixelArrayCache.release(data.pixels);
                    data.pixels = null;
                }
                if (data.graphics != null) {
                    LOG.log(Level.FINEST, "Releasing Texture");
                    context.releaseGraphics(data.graphics);
                    data.graphics = null;
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
            PGLSurface src = (PGLSurface) source;
            if (src.data != null) {
                data = src.data;
                data.usage++;
                clear = false;
            } else {
                LOG.log(Level.FINE, "Copied surface has no data");
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
        if (!(surface instanceof PGLSurface)) {
            return false;
        }
        if (((PGLSurface) surface).context != context) {
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
    public PGLSurface createSurface() {
        return createSurface(width, height, alpha);
    }

    @Override
    public PGLSurface createSurface(int width, int height, boolean alpha) {
        return context.createSurface(width, height, alpha);
    }

    private class FallbackProcessor implements PGLOp.Bypass {

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
            if (input instanceof PGLSurface) {
                PGLSurface in = (PGLSurface) input;
                in.makeSWReadable();
                op.process(data, in.data);
            } else {
                SurfaceOp rev = Reverse.op(op, data);
                input.process(rev);
            }
        }

        private void processSW(SurfaceOp op, Surface[] inputs) {
            PixelData[] pixelInputs = new PixelData[inputs.length];
            PGLSurface in;
            for (int i = 0; i < inputs.length; i++) {
                if (inputs[i] instanceof PGLSurface) {
                    in = (PGLSurface) inputs[i];
                    in.makeSWReadable();
                    pixelInputs[i] = in.data;
                } else {
                    throw new UnsupportedOperationException("not yet implemented");
                }
            }
            op.process(data, pixelInputs);
        }
    }

    public static class Data implements PixelData {

        private final int width;
        private final int height;
        private final boolean alpha;

        private int[] pixels;
        private PGLGraphics graphics;
        private int usage;

        Data(int w, int h, boolean a) {
            width = w;
            height = h;
            alpha = a;
            usage = 1;
        }

        @Override
        public int[] getData() {
            return pixels;
        }

        @Override
        public int getOffset() {
            return 0;
        }

        @Override
        public int getScanline() {
            return width;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public boolean hasAlpha() {
            return alpha;
        }

    }

}
