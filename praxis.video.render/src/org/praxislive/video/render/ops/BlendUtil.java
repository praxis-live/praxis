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
import org.praxislive.video.render.utils.RGBMath;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class BlendUtil {
    
    static void process(PixelData src, PixelData dst, BlendMode mode, double opacity) {
        RGBComposite cmp = getComposite(mode);
        int alpha = getExtraAlpha(opacity);
        int width = Math.min(src.getWidth(), dst.getWidth());
        int height = Math.min(src.getHeight(), dst.getHeight());
        int srcOffset = src.getOffset();
        int srcSL = src.getScanline();
        boolean srcAlpha = src.hasAlpha();
        int dstOffset = dst.getOffset();
        int dstSL = dst.getScanline();
        boolean dstAlpha = dst.hasAlpha();
        if (srcAlpha == dstAlpha) {
            if (dstAlpha) {
                composeARGB(cmp, alpha, width, height, src.getData(), srcSL, srcOffset,
                        dst.getData(), dstSL, dstOffset);
            } else {
                composeRGB(cmp, alpha, width, height, src.getData(), srcSL, srcOffset,
                        dst.getData(), dstSL, dstOffset);
            }
        } else {
            composeMixed(cmp, alpha, width, height, src.getData(), srcSL, srcOffset, srcAlpha,
                    dst.getData(), dstSL, dstOffset, dstAlpha);
        }
    }
    
    private static RGBComposite getComposite(BlendMode mode) {
        switch (mode) {
            case Add:
                return RGBComposite.ADD;
            case BitXor:
                return RGBComposite.BITXOR;
            case Difference:
                return RGBComposite.DIFFERENCE;
            case Mask:
                return RGBComposite.MASK;
            case Multiply:
                return RGBComposite.MULTIPLY;
            case Normal:
                return RGBComposite.NORMAL;
            case Screen:
                return RGBComposite.SCREEN;
            case Sub:
                return RGBComposite.SUB;
        }
        throw new IllegalArgumentException("Unknown blend mode");
    }
    
    private static int getExtraAlpha(double opacity) {
        if (opacity < 0.0 || opacity > 1.0) {
            throw new IllegalArgumentException("Opacity must be between 0 and 1");
        }
        return (int) Math.round(opacity * 255);
    }
    
    private static void composeRGB(RGBComposite cmp, int alpha,
            int width, int height,
            int[] srcData, int srcSL, int srcOffset,
            int[] dstData, int dstSL, int dstOffset) {
        if (srcSL == width && dstSL == width) {
            cmp.rgb(srcData, srcOffset, dstData, dstOffset, width * height, alpha);
        } else {
            for (int y = 0; y < height; y++) {
                cmp.rgb(srcData, srcOffset, dstData, dstOffset, width, alpha);
                srcOffset += srcSL;
                dstOffset += dstSL;
            }
        }
        
    }
    
    private static void composeARGB(RGBComposite cmp, int alpha,
            int width, int height,
            int[] srcData, int srcSL, int srcOffset,
            int[] dstData, int dstSL, int dstOffset) {
//            System.out.println("Composing ARGB");
        if (srcSL == width && dstSL == width) {
//                System.out.println("Composing full array");
            cmp.argb(srcData, srcOffset, dstData, dstOffset, width * height, alpha);
        } else {
//                System.out.println("Composing line by line");
            for (int y = 0; y < height; y++) {
                cmp.argb(srcData, srcOffset, dstData, dstOffset, width, alpha);
                srcOffset += srcSL;
                dstOffset += dstSL;
            }
        }
        
    }
    
    private static void composeMixed(RGBComposite cmp, int alpha,
            int width, int height,
            int[] srcData, int srcSL, int srcOffset, boolean srcAlpha,
            int[] dstData, int dstSL, int dstOffset, boolean dstAlpha) {
        int[] src = new int[width];
        int[] dst = new int[width];
        
        for (int y = 0; y < height; y++) {
            if (srcAlpha) {
                System.arraycopy(srcData, srcOffset, src, 0, width);
            } else {
                for (int i = 0; i < width; i++) {
                    src[i] = srcData[i + srcOffset] | RGBMath.ALPHA_MASK;
                }
            }
            if (dstAlpha) {
                System.arraycopy(dstData, dstOffset, dst, 0, width);
            } else {
                for (int i = 0; i < width; i++) {
                    dst[i] = dstData[i + dstOffset] | RGBMath.ALPHA_MASK;
                }
            }
            cmp.argb(src, 0, dst, 0, width, alpha);
            if (dstAlpha) {
                System.arraycopy(dst, 0, dstData, dstOffset, width);
            } else {
                for (int i = 0; i < width; i++) {
                    dstData[i + dstOffset] = dst[i] & 0x00FFFFFF;
                }
            }
            
            srcOffset += srcSL;
            dstOffset += dstSL;
            
        }
        
    }
}
