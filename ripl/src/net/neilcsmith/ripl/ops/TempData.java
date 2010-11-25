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

package net.neilcsmith.ripl.ops;

import net.neilcsmith.ripl.PixelData;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class TempData implements PixelData {
    
    int[] data;
    int offset;
    int scanline;
    int width;
    int height;
    boolean alpha;

    public int[] getData() {
        return data;
    }

    public int getOffset() {
        return offset;
    }

    public int getScanline() {
        return scanline;
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

    static TempData create(int width, int height, boolean alpha) {
        return create(width, height, alpha, null);
    }
    
    static TempData create(int width, int height, boolean alpha, int[] data) {
        TempData tmp = new TempData();
        tmp.width = width;
        tmp.height = height;
        tmp.scanline = width;
        tmp.offset = 0;
        tmp.alpha = alpha;
        if (data == null || data.length < (width * height)) {
            data = new int[width * height];
        }
        tmp.data = data;
        return tmp;
    }

}
