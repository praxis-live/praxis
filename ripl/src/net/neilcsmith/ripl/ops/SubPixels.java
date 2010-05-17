/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 - Neil C Smith. All rights reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
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
public class SubPixels implements PixelData {

    private PixelData pd;
    private int x;
    private int y;
    private int width;
    private int height;

    private SubPixels(PixelData pd, int x, int y, int width, int height) {
        this.pd = pd;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }



    public int[] getData() {
        return pd.getData();
    }

    public int getOffset() {
        return pd.getOffset() + (y * pd.getScanline()) + x;
    }

    public int getScanline() {
        return pd.getScanline();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean hasAlpha() {
        return pd.hasAlpha();
    }


    public static SubPixels create(PixelData pd, int x, int y, int width, int height) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException();
        }
        if ( ((x + width) > pd.getWidth()) || ((y + height) > pd.getHeight()) ) {
            throw new IllegalArgumentException();
        }
        return new SubPixels(pd, x, y, width, height);
    }

}
