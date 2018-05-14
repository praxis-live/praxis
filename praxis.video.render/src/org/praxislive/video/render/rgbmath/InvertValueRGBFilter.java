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
package org.praxislive.video.render.rgbmath;

import static org.praxislive.video.render.utils.RGBMath.*;

/**
 *
 * @author Neil C Smith
 */
public class InvertValueRGBFilter implements RGBSinglePixelFilter {

    private static InvertValueRGBFilter instance = new InvertValueRGBFilter();

    private InvertValueRGBFilter() {
    }

    public void filterRGB(int[] src, int srcPos, int[] dest, int destPos, int length) {
        int a, r, g, b, pix;
        int value, value2, min, delta;

        for (int i = 0; i < length; i++) {
            pix = src[srcPos];
            a = pix & ALPHA_MASK;
            r = (pix & RED_MASK) >>> 16;
            g = (pix & GREEN_MASK) >>> 8;
            b = pix & BLUE_MASK;

            if (r > g) {
                value = max(r, b);
                min = min(g, b);
            } else {
                value = max(g, b);
                min = min(r, b);
            }

            delta = value - min;

            if (value == 0 || delta == 0) {

                r = 255 - value;
                g = 255 - value;
                b = 255 - value;

            } else {
                value2 = value / 2;

                if (r == value) {
                    r = 255 - r;
                    b = ((r * b) + value2) / value;
                    g = ((r * g) + value2) / value;
                } else if (g == value) {
                    g = 255 - g;
                    r = ((g * r) + value2) / value;
                    b = ((g * b) + value2) / value;
                } else {
                    b = 255 - b;
                    r = ((b * r) + value2) / value;
                    g = ((b * g) + value2) / value;
                }

            }

            dest[destPos] = a | r << 16 | g << 8 | b;
            srcPos++;
            destPos++;

        }
    }

    public void filterARGB(int[] src, int srcPos, int[] dest, int destPos, int length) {
        int a, r, g, b, pix;
        int value, value2, min, delta;

        for (int i = 0; i < length; i++) {
            pix = src[srcPos];
            a = (pix & ALPHA_MASK) >>> 24;
            r = (pix & RED_MASK) >>> 16;
            g = (pix & GREEN_MASK) >>> 8;
            b = pix & BLUE_MASK;

            if (r > g) {
                value = max(r, b);
                min = min(g, b);
            } else {
                value = max(g, b);
                min = min(r, b);
            }

            delta = value - min;

            if (value == 0 || delta == 0) {

                r = 255 - value;
                g = 255 - value;
                b = 255 - value;

            } else {
                value2 = value / 2;

                if (r == value) {
                    r = max(255 - r, a);
                    b = max(((r * b) + value2) / value, a);
                    g = max(((r * g) + value2) / value, a);
                } else if (g == value) {
                    g = max(255 - g, a);
                    r = max(((g * r) + value2) / value, a);
                    b = max(((g * b) + value2) / value, a);
                } else {
                    b = max(255 - b, a);
                    r = max(((b * r) + value2) / value, a);
                    g = max(((b * g) + value2) / value, a);
                }

            }

            dest[destPos] = a << 24 | r << 16 | g << 8 | b;
            srcPos++;
            destPos++;

        }
    }

    public static InvertValueRGBFilter getInstance() {
        return instance;
    }
}
