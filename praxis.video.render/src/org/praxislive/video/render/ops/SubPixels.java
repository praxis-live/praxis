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

import java.awt.Rectangle;
import org.praxislive.video.render.PixelData;


/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class SubPixels implements PixelData {

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

    @Override
    public int[] getData() {
        return pd.getData();
    }

    @Override
    public int getOffset() {
        return pd.getOffset() + (y * pd.getScanline()) + x;
    }

    @Override
    public int getScanline() {
        return pd.getScanline();
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

    public static SubPixels create(PixelData pd, Rectangle bnds) {
        return create(pd, bnds.x, bnds.y, bnds.width, bnds.height);
    }



}
