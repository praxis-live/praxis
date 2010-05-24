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
import net.neilcsmith.ripl.rgbmath.RGBComposite;
import net.neilcsmith.ripl.rgbmath.RGBMath;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Blend implements BlendFunction {

    public static enum Type {Normal, Add, Sub, Difference, Multiply, Screen, BitXor};

    public final static Blend NORMAL = new Blend(Type.Normal, 1);
    public final static Blend ADD = new Blend(Type.Add, 1);
    public final static Blend SUB = new Blend(Type.Sub, 1);
    public final static Blend DIFFERENCE = new Blend(Type.Difference, 1);
    public final static Blend MULTIPLY = new Blend(Type.Multiply, 1);
    public final static Blend SCREEN = new Blend(Type.Screen, 1);
    public final static Blend BITXOR = new Blend(Type.BitXor, 1);


    public static Blend ofType(Type type) {
        switch (type) {
            case Normal :
                return NORMAL;
            case Add :
                return ADD;
            case Sub :
                return SUB;
            case Difference :
                return DIFFERENCE;
            case Multiply :
                return MULTIPLY;
            case Screen :
                return SCREEN;
            case BitXor :
                return BITXOR;
            default :
                throw new IllegalArgumentException();
        }
    }


    private Type type;
    private double extraAlpha;
    private RGBComposite cmp;

    private Blend(Type type, double extraAlpha) {
        switch (type) {
            case Normal :
                cmp = new RGBComposite.Normal(extraAlpha);
                break;
            case Add :
                cmp = new RGBComposite.Add(extraAlpha);
                break;
            case Sub :
                cmp = new RGBComposite.Sub(extraAlpha);
                break;
            case Difference :
                cmp = new RGBComposite.Difference(extraAlpha);
                break;
            case Multiply :
                cmp = new RGBComposite.Multiply(extraAlpha);
                break;
            case Screen :
                cmp = new RGBComposite.Screen(extraAlpha);
                break;
            case BitXor :
                cmp = new RGBComposite.BitXor(extraAlpha);
                break;
            default :
                throw new IllegalArgumentException();
        }
        this.type = type;
        this.extraAlpha = extraAlpha;

    }

    public Type getType() {
        return type;
    }

    public double getExtraAlpha() {
        return extraAlpha;
    }

    public Blend opacity(double extraAlpha) {
        return new Blend(type, extraAlpha);
    }

    public void process(PixelData src, PixelData dst) {
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
                composeARGB(width, height, src.getData(), srcSL, srcOffset,
                        dst.getData(), dstSL, dstOffset);
            } else {
                composeRGB(width, height, src.getData(), srcSL, srcOffset,
                        dst.getData(), dstSL, dstOffset);
            }
        } else {
            composeMixed(width, height, src.getData(), srcSL, srcOffset, srcAlpha,
                    dst.getData(), dstSL, dstOffset, dstAlpha);
        }



    }

    private void composeRGB(int width, int height, int[] srcData, int srcSL, int srcOffset,
                int[] dstData, int dstSL, int dstOffset) {
//            System.out.println("Composing RGB");
            if (srcSL == width && dstSL == width) {
//                System.out.println("Composing full array");
                cmp.composeRGB(srcData, srcOffset, dstData, dstOffset, width * height);
            } else {
//                System.out.println("Composing line by line");
                for (int y=0; y < height; y++) {
                    cmp.composeRGB(srcData, srcOffset, dstData, dstOffset, width);
                    srcOffset += srcSL;
                    dstOffset += dstSL;
                }
            }

        }

    private void composeARGB(int width, int height, int[] srcData, int srcSL, int srcOffset,
                int[] dstData, int dstSL, int dstOffset) {
//            System.out.println("Composing ARGB");
            if (srcSL == width && dstSL == width) {
//                System.out.println("Composing full array");
                cmp.composeARGB(srcData, srcOffset, dstData, dstOffset, width * height);
            } else {
//                System.out.println("Composing line by line");
                for (int y=0; y < height; y++) {
                    cmp.composeARGB(srcData, srcOffset, dstData, dstOffset, width);
                    srcOffset += srcSL;
                    dstOffset += dstSL;
                }
            }

        }

    private void composeMixed(int width, int height, int[] srcData, int srcSL, int srcOffset,
            boolean srcAlpha, int[] dstData, int dstSL, int dstOffset, boolean dstAlpha) {
        int[] src = new int[width];
        int[] dst = new int[width];

        for (int y=0; y < height; y++) {
            if (srcAlpha) {
                System.arraycopy(srcData, srcOffset, src, 0, width);
            } else {
                for (int i=0; i < width; i++) {
                    src[i] = srcData[i + srcOffset] | RGBMath.ALPHA_MASK;
                }
            }
            if (dstAlpha) {
                System.arraycopy(dstData, dstOffset, dst, 0, width);
            } else {
                for (int i=0; i < width; i++) {
                    dst[i] = dstData[i + dstOffset] | RGBMath.ALPHA_MASK;
                }
            }
            cmp.composeARGB(src, 0, dst, 0, width);
            if (dstAlpha) {
                System.arraycopy(dst, 0, dstData, dstOffset, width);
            } else {
                for (int i=0; i < width; i++) {
                    dstData[i + dstOffset] = dst[i] & 0x00FFFFFF;
                }
            }

            srcOffset += srcSL;
            dstOffset += dstSL;

        }

    }



}
