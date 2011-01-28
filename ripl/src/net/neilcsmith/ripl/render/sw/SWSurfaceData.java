/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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
package net.neilcsmith.ripl.render.sw;

import net.neilcsmith.ripl.PixelData;
import net.neilcsmith.ripl.utils.PixelArrayCache;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class SWSurfaceData implements PixelData {

    private int width;
    private int height;
    private boolean alpha;
    private int[] pixels;
    private int usage;

    private SWSurfaceData(int w, int h, boolean a) {
        width = w;
        height = h;
        alpha = a;
        usage = 1;
    }

    public int[] getData() {
        if (pixels == null) {
            pixels = PixelArrayCache.acquire(width * height, true);
        }
        return pixels;
    }

    public int getOffset() {
        return 0;
    }

    public int getScanline() {
        return width;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean hasAlpha() {
        return alpha;
    }

    SWSurfaceData acquire(SWSurface surface) {
        usage++;
        return this;
    }

    void release(SWSurface surface) {
        usage--;
        if (usage <= 0 && pixels != null) {
            PixelArrayCache.release(pixels);
            pixels = null;
        }  
    }

    SWSurfaceData getUnshared(SWSurface owner) {
        if (usage > 1) {
            SWSurfaceData copy = new SWSurfaceData(width, height, alpha);
            if (pixels != null) {
                copy.pixels = PixelArrayCache.acquire(width * height, false);
                System.arraycopy(pixels, 0, copy.pixels, 0, width * height);
            }
            release(null);
            return copy;

        } else if (usage == 1) {
            return this;
        } else {
            throw new IllegalStateException();
        }
    }

    static SWSurfaceData createSurfaceData(SWSurface owner, int width, int height, boolean alpha) {
        return new SWSurfaceData(width, height, alpha);
    }
}
