/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2016 Neil C Smith.
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

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.jogamp.opengl.GL2;
import net.neilcsmith.praxis.video.pgl.ops.PGLOpCache;
import net.neilcsmith.praxis.video.render.NativePixelData;
import net.neilcsmith.praxis.video.render.PixelData;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.utils.PixelArrayCache;
import processing.core.PApplet;
import processing.core.PConstants;
import static processing.core.PConstants.ARGB;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.Texture;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public final class PGLContext {

    private final static Logger LOG = Logger.getLogger(PGLContext.class.getName());

    private final PApplet applet;
    private final PGLProfile profile;
    private final int width;
    private final int height;
    private final int cacheMax = 8;
    private final PGLOpCache opCache;
    private final List<PGLGraphics> cache;
    private final List<AlienImageReference> aliens;
    private final WeakHashMap<PGLSurface, Boolean> surfaces;
    private final ReadPixelsOp readOp;
    private final PImage CLEAR_RGB;
    private final PImage CLEAR_ARGB;
    PGraphics current; //@TODO use primary().getCurrent?
    private IntBuffer scratchBuffer;

    PGLContext(PApplet applet, PGLProfile profile, int width, int height) {
        this.applet = applet;
        this.profile = profile;
        this.width = width;
        this.height = height;
        cache = new ArrayList<>(cacheMax);
        aliens = new ArrayList<>(cacheMax);
        surfaces = new WeakHashMap<>();
        readOp = new ReadPixelsOp();
        CLEAR_RGB = new PImage(width, height, PImage.RGB);
        CLEAR_ARGB = new PImage(width, height, PImage.ARGB);
        opCache = new PGLOpCache(this);
    }

    public PImage asImage(Surface surface) {
        if (surface instanceof PGLSurface) {
            PImage img = ((PGLSurface) surface).asImage();
            if (img != null) {
                return img;
            }
            if (surface.isClear()
                    && surface.getWidth() == width
                    && surface.getHeight() == height) {
                // common occurence - clear surface at frame width / height
                return surface.hasAlpha()
                        ? CLEAR_ARGB
                        : CLEAR_RGB;
            }
            // fall through for data in pixels or custom dimensions
        }
        return asAlienImage(surface);
    }

    public PGLGraphics createGraphics(int width, int height) {
        return (PGLGraphics) applet.createGraphics(width, height, PGLGraphics.ID);
    }

    public PApplet parent() {
        return applet;
    }

    public PGLGraphics primary() {
        return (PGLGraphics) applet.g;
    }

    public PGLGraphics3D create3DGraphics(int width, int height) {
        return (PGLGraphics3D) applet.createGraphics(width, height, PGLGraphics3D.ID);
    }

    public PGLSurface createSurface(int width, int height, boolean alpha) {
        PGLSurface s = new PGLSurface(this, width, height, alpha);
        surfaces.put(s, Boolean.TRUE);
        return s;
    }

    PGLOpCache getOpCache() {
        return opCache;
    }

    PGLJOGL getPGL() {
        return (PGLJOGL) primary().pgl;
    }

    PGLGraphics acquireGraphics(int width, int height) {
        PGLGraphics pgl = null;
        for (int i = 0; i < cache.size(); i++) {
            PGLGraphics g = cache.get(i);
            if (g.width == width && g.height == height) {
                pgl = g;
                cache.remove(i);
                break;
            }
        }
        if (pgl != null) {
            return pgl;
        }
        while (cache.size() >= cacheMax) {
            LOG.log(Level.FINE, "Trimming graphics cache");
            cache.remove(cache.size() - 1).dispose();
        }
        LOG.log(Level.FINE, "Creating new graphics of size {0}x{1}", new Object[]{width, height});
        pgl = createGraphics(width, height);
        return pgl;
    }

    void releaseGraphics(PGLGraphics pgl) {
        pgl.endDraw();
        while (cache.size() >= cacheMax) {
            LOG.log(Level.FINE, "Trimming graphics cache");
            cache.remove(cache.size() - 1).dispose();
        }
        cache.add(0, pgl);
    }

    void writePixels(int[] data, Texture tex) {
        int size = tex.width * tex.height;
        IntBuffer buffer = getScratchBuffer(size);
        if (profile != PGLProfile.GLES2) {
            buffer.put(data, 0, size);
            buffer.rewind();
            writePixelsARGB(buffer, tex);
        } else {
            for (int i = 0; i < size; i++) {
                int color = data[i];
                int rb = color & 0x00FF00FF;
                data[i] = (color & 0xFF00FF00) | (rb << 16) | (rb >> 16);
            }
            buffer.put(data, 0, size);
            buffer.rewind();
            writePixelsRGBA(buffer, tex);
        }
    }

    private void writePixelsARGB(IntBuffer data, Texture tex) {
        PGL pgl = ((PGLGraphics) primary()).pgl;
        boolean enabledTex = false;
        if (!pgl.isEnabled(tex.glTarget)) {
            pgl.enable(tex.glTarget);
            enabledTex = true;
        }
        pgl.bindTexture(tex.glTarget, tex.glName);
        pgl.texSubImage2D(tex.glTarget,
                0,
                0,
                0,
                tex.width,
                tex.height,
                GL2.GL_BGRA,
                GL2.GL_UNSIGNED_INT_8_8_8_8_REV,
                data);
        if (tex.usingMipmaps() && PGraphicsOpenGL.autoMipmapGenSupported) {
            pgl.generateMipmap(tex.glTarget);
        }
        pgl.bindTexture(tex.glTarget, 0);
        if (enabledTex) {
            pgl.disable(tex.glTarget);
        }
        tex.updateTexels();
    }

    private void writePixelsRGBA(IntBuffer data, Texture tex) {
        PGL pgl = ((PGLGraphics) primary()).pgl;
        boolean enabledTex = false;
        if (!pgl.isEnabled(tex.glTarget)) {
            pgl.enable(tex.glTarget);
            enabledTex = true;
        }
        pgl.bindTexture(tex.glTarget, tex.glName);
        pgl.texSubImage2D(tex.glTarget,
                0,
                0,
                0,
                tex.width,
                tex.height,
                PGL.RGBA,
                PGL.UNSIGNED_BYTE,
                data);
        if (tex.usingMipmaps() && PGraphicsOpenGL.autoMipmapGenSupported) {
            pgl.generateMipmap(tex.glTarget);
        }
        pgl.bindTexture(tex.glTarget, 0);
        if (enabledTex) {
            pgl.disable(tex.glTarget);
        }
        tex.updateTexels();
    }

    IntBuffer getScratchBuffer(int size) {
        if (scratchBuffer == null || scratchBuffer.capacity() < size) {
            scratchBuffer = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        }
        scratchBuffer.clear();
        return scratchBuffer;
    }

    private PImage asAlienImage(Surface alien) {
        AlienImageReference ref = null;
        for (int i = aliens.size() - 1; i >= 0; i--) {
            AlienImageReference r = aliens.get(i);
            Surface s = r.alien.get();
            if (s == null) {
                LOG.fine("Releasing alien image");
//                release(r.region.texture);
                aliens.remove(i);
            } else if (s == alien) {
                LOG.fine("Found cached alien image");
                ref = r;
                aliens.remove(i);
            }
        }
        if (ref == null) {
            LOG.fine("Creating new alien image");
            PGLTexture tex = createAlienTexture(alien.getWidth(),
                    alien.getHeight(),
                    alien.hasAlpha());
            readOp.readToTexture(tex, alien);
            ref = new AlienImageReference();
            ref.image = wrapAlienTexture(tex);
            ref.alien = new WeakReference<>(alien);
            ref.modCount = alien.getModCount();
        } else if (ref.modCount != alien.getModCount()) {
            LOG.fine("Updating existing alien texture");
            PGLTexture tex = (PGLTexture) primary().getCache(ref.image);
            if (tex.contextIsOutdated()) {
                LOG.fine("Creating new alien texture - CONTEXT OUTDATED");
                tex = createAlienTexture(alien.getWidth(),
                        alien.getHeight(),
                        alien.hasAlpha());
                ref.image = wrapAlienTexture(tex);
            }
            readOp.readToTexture(tex, alien);
            ref.modCount = alien.getModCount();
        } else {
            LOG.fine("Existing alien texture in sync");
        }
        aliens.add(ref);
        return ref.image;
    }

    private PGLTexture createAlienTexture(int width, int height, boolean alpha) {
        Texture.Parameters params = new Texture.Parameters();
        params.mipmaps = false;
        if (!alpha) {
            params.format = PConstants.RGB;
        }
        PGLTexture texture = new PGLTexture(primary(), width, height, params);
//        texture.invertedY(true);
//        texture.colorBuffer(true);
        return texture;
    }

    private PImage wrapAlienTexture(Texture tex) {
        PImage img = new PImage();
        //img.parent = parent;
        img.width = tex.width;
        img.height = tex.height;
        img.format = ARGB; // @TODO ???
        primary().setCache(img, tex);
        return img;
    }

    protected void dispose() {
        for (PGLSurface surface : surfaces.keySet()) {
            surface.clear();
        }
//        for (PGLGraphics graphics : cache) {
//            graphics.dispose();
//        }
        opCache.dispose();
        cache.clear();
        aliens.clear();
    }

    private static class AlienImageReference {

        private WeakReference<Surface> alien;
        private int modCount;
        private PImage image;
    }

    private class ReadPixelsOp implements SurfaceOp {

        private Texture texture;

        private void readToTexture(Texture texture, Surface alien) {
            this.texture = texture;
            alien.process(this);
            this.texture = null;
        }

        @Override
        public void process(PixelData output, PixelData... inputs) {
            LOG.fine("Reading Pixel Data");
            if (output instanceof NativePixelData && output.getScanline() == output.getWidth()) {
                LOG.fine("PixelData is native");
                NativePixelData nOut = (NativePixelData) output;
                if (profile != PGLProfile.GLES2) {
                    writePixelsARGB(nOut.getNativeData().asIntBuffer(), texture);
                } else {
                    writePixels(output.getData(), texture);
                }

            } else if (output.getScanline() == output.getWidth() && output.getOffset() == 0) {
                writePixels(output.getData(), texture);
            } else {
                int size = output.getWidth() * output.getHeight();
                int[] pixels = PixelArrayCache.acquire(size, false);
                IntBuffer buffer = IntBuffer.wrap(pixels);
                int i = output.getOffset();
                int w = output.getWidth();
                int h = output.getHeight();
                int sl = output.getScanline();
                int[] data = output.getData();
                for (int v = 0; v < h; v++) {
                    buffer.put(data, i, w);
                    i += sl;
                }
                writePixels(data, texture);
                PixelArrayCache.release(pixels);
            }
        }
    }

}
