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
package org.praxislive.video.impl.components;

import org.praxislive.video.render.PixelData;
import org.praxislive.video.render.utils.PixelArrayCache;

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
    private boolean clear;

    private SWSurfaceData(int w, int h, boolean a, boolean c) {
        width = w;
        height = h;
        alpha = a;
        clear = c;
        usage = 1;
    }

    public int[] getData() {
        if (pixels == null) {
            pixels = PixelArrayCache.acquire(width * height, clear);
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

    SWSurfaceData acquire() {
        usage++;
        return this;
    }

    void release() {
        usage--;
        if (usage <= 0 && pixels != null) {
            PixelArrayCache.release(pixels);
            pixels = null;
        }  
    }

    SWSurfaceData getUnshared(SWSurface owner) {
        if (usage > 1) {
            SWSurfaceData copy = new SWSurfaceData(width, height, alpha, false);
            if (pixels != null) {
                System.arraycopy(pixels, 0, copy.getData(), 0, width * height);
            }
            release();
            return copy;

        } else if (usage == 1) {
            return this;
        } else {
            throw new IllegalStateException();
        }
    }

    static SWSurfaceData createSurfaceData(SWSurface owner, int width, int height,
            boolean alpha, boolean clear) {
        return new SWSurfaceData(width, height, alpha, clear);
    }
}
