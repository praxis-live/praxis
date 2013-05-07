/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Neil C Smith.
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

import java.lang.ref.WeakReference;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.video.render.NativePixelData;
import net.neilcsmith.praxis.video.render.PixelData;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import org.lwjgl.BufferUtils;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class TextureManager {

    private final static Logger LOG = Logger.getLogger(TextureManager.class.getName());
    private final GLContext context;
    private final int cacheMax = 8;
    private final List<Texture> cache;
    private final List<SurfaceTextureReference> aliens;
    private final ReadPixelsOp reader;

    TextureManager(GLContext context) {
        this.context = context;
        cache = new ArrayList<Texture>(cacheMax);
        aliens = new ArrayList<SurfaceTextureReference>(cacheMax);
        reader = new ReadPixelsOp();
    }

    Texture acquire(int width, int height) {
        Texture tex = null;
        for (int i = 0; i < cache.size(); i++) {
            Texture t = cache.get(i);
            if (t.getWidth() == width && t.getHeight() == height) {
                tex = t;
                cache.remove(i);
                break;
            }
        }
        if (tex != null) {
            return tex;
        }
        while (cache.size() >= cacheMax) {
            LOG.log(Level.FINE, "Trimming texture cache");
            Texture t = cache.remove(cache.size() - 1);
            t.dispose();
        }
        LOG.log(Level.FINE, "Creating new texture of size {0}x{1}", new Object[]{width, height});
        tex = new Texture(width, height);
        return tex;
    }

    void release(Texture texture) {
        while (cache.size() >= cacheMax) {
            LOG.log(Level.FINE, "Trimming texture cache");
            Texture t = cache.remove(cache.size() - 1);
            t.dispose();
        }

        cache.add(0, texture);
    }

    void clear() {
        for (Texture t : cache) {
            t.dispose();
        }
        cache.clear();
    }

    void initTextureRegion(TextureRegion region, Surface alien, int x, int y, int width, int height) {
        SurfaceTextureReference ref = null;
        for (int i = aliens.size() - 1; i >= 0; i--) {
            SurfaceTextureReference r = aliens.get(i);
            Surface s = r.alien.get();
            if (s == null) {
                LOG.fine("Releasing alien texture");
                release(r.region.texture);
                aliens.remove(i);
            } else if (s == alien) {
                LOG.fine("Found cached alien texture");
                ref = r;
                aliens.remove(i);
            }
        }
        if (ref == null) {
            LOG.fine("Creating new alien texture");
            Texture tex = acquire(alien.getWidth(), alien.getHeight());
            reader.setup(tex, 0, 0);
            alien.process(reader);
            ref = new SurfaceTextureReference();
            ref.region = new TextureRegion(tex);
            ref.alien = new WeakReference<Surface>(alien);
            ref.modCount = alien.getModCount();
        } else if (ref.modCount != alien.getModCount()) {
            LOG.fine("Updating existing alien texture");
            reader.setup(ref.region.texture, 0, 0);
            alien.process(reader);
            ref.modCount = alien.getModCount();
        } else {
            LOG.fine("Existing alien texture in sync");
        }
        region.texture = ref.region.texture;
        region.setRegion(x, y, width, height);
        aliens.add(ref);
    }

    private class SurfaceTextureReference {

        private WeakReference<Surface> alien;
        private int modCount;
        private TextureRegion region;
    }

    private class ReadPixelsOp implements SurfaceOp {

        private Texture texture;
        private int x, y;
        private IntBuffer buffer;

        private void setup(Texture texture, int x, int y) {
            this.texture = texture;
            this.x = x;
            this.y = y;
        }

        @Override
        public void process(PixelData output, PixelData... inputs) {
            LOG.fine("Reading Pixel Data");
            if (output instanceof NativePixelData && output.getScanline() == output.getWidth()) {
                LOG.fine("PixelData is native");
                NativePixelData nOut = (NativePixelData) output;
                boolean alpha = nOut.getFormat() == NativePixelData.Format.INT_ARGB_PRE;
                context.getRenderer().syncPixelBufferToTexture(nOut.getNativeData().asIntBuffer(), texture, alpha, x, y, output.getWidth(), output.getHeight());
            } else {
                int size = output.getWidth() * output.getHeight();
                if (buffer == null || buffer.capacity() < size) {
                    buffer = BufferUtils.createIntBuffer(size);
                }
                buffer.rewind();
                if (output.getScanline() == output.getWidth()) {
                    buffer.put(output.getData(), output.getOffset(), size);
                } else {
                    int i = output.getOffset();
                    int w = output.getWidth();
                    int h = output.getHeight();
                    int sl = output.getScanline();
                    int[] data = output.getData();
                    for (int v = 0; v < h; v++) {
                        buffer.put(data, i, w);
                        i += sl;
                    }
                }
                buffer.rewind();
                context.getRenderer().syncPixelBufferToTexture(buffer, texture, output.hasAlpha(), x, y, output.getWidth(), output.getHeight());
            }
        }
    }

}
