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

package org.praxislive.video.render.ops;

import org.praxislive.video.render.PixelData;
import org.praxislive.video.render.utils.PixelArrayCache;


/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class TempData implements PixelData {
    
    private int[] data;
    private int width;
    private int height;
    private boolean alpha;

    public int[] getData() {
        if (data == null) {
            data = PixelArrayCache.acquire(width*height, true);
        }
        return data;
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

    void release() {
        PixelArrayCache.release(data);
        data = null;
    }

    static TempData create(int width, int height, boolean alpha) {
        TempData tmp = new TempData();
        tmp.width = width;
        tmp.height = height;
        tmp.alpha = alpha;
        return tmp;
    }

}
