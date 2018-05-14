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
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.utils.RGBMath;



/**
 *
 * @author Neil C Smith
 */
public class Noise implements SurfaceOp {

    private final static Noise op = new Noise();

    private Noise() {}

    public void process(PixelData output, PixelData... inputs) {
        int index = 0;
        int width = output.getWidth();
        int height = output.getHeight();
        int scanline = output.getScanline();
        int delta = scanline - width;
        int[] data = output.getData();
        boolean alpha = output.hasAlpha();
        for (int y=0; y < height; y++) {
            for (int x=0; x < width; x++) {
                int value = RGBMath.random();
                data[index] = alpha ? value << 24 | value << 16 | value << 8 | value :
                    value << 16 | value << 8 | value;
                index++;
            }
            index += delta;
        }
    }

    public static SurfaceOp op() {
        return op;
    }

}
