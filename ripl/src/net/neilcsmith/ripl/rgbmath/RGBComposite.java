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
package net.neilcsmith.ripl.rgbmath;

import static net.neilcsmith.ripl.rgbmath.RGBMath.*;

/**
 *
 * @author Neil C Smith
 * @TODO Further optimisations - remove alpha check out of inner loop where possible
 */
public abstract class RGBComposite {

    protected int extraAlpha;

    protected RGBComposite(double extraAlpha) {
        if (extraAlpha < 0.0 || extraAlpha > 1.0) {
            throw new IllegalArgumentException("Extra alpha multiplier must be between 0 and 1");
        }
        this.extraAlpha = (int) (extraAlpha * 255);
    }

    public abstract void composeRGB(int[] src, int srcPos,
            int[] dest, int destPos, int length);
    
    public abstract void composeARGB(int[] src, int srcPos,
            int[] dest, int destPos, int length);

    public static class Add extends RGBComposite {

        public Add(double extraAlpha) {
            super(extraAlpha);
        }

        public void composeRGB(int[] src, int srcPos, int[] dest, int destPos, int length) {
//            int srcPx, b;
            int alpha = extraAlpha;
            int srcPx, destPx, srcR, srcG, srcB, destR, destG, destB;
            for (int i = 0; i < length; i++) {
                srcPx = src[srcPos];
                destPx = dest[destPos];
                if (alpha == 255) {
                    srcR = (srcPx & RED_MASK) >>> 16;
                    srcG = (srcPx & GREEN_MASK) >>> 8;
                    srcB = srcPx & BLUE_MASK;
                } else {
                    srcR = mult((srcPx & RED_MASK) >>> 16, alpha);
                    srcG = mult((srcPx & GREEN_MASK) >>> 8, alpha);
                    srcB = mult(srcPx & BLUE_MASK, alpha);
                }
                destR = (destPx & RED_MASK) >>> 16;
                destG = (destPx & GREEN_MASK) >>> 8;
                destB = (destPx & BLUE_MASK);
                
                dest[destPos] = min(srcR + destR, 0xFF) << 16 |
                        min(srcG + destG, 0xFF) << 8 |
                        min(srcB + destB, 0xFF);

                srcPos++;
                destPos++;

            }
        }

        public void composeARGB(int[] src, int srcPos, int[] dest, int destPos, int length) {
//            int a, b;
            int alpha = extraAlpha;
            for (int i = 0; i < length; i++) {
                int srcPx = src[srcPos];
                int destPx = dest[destPos];
                int srcA = (alpha == 255) ? (srcPx & ALPHA_MASK) >>> 24
                        : mult((srcPx & ALPHA_MASK) >>> 24, alpha);
                int srcR = (alpha == 255) ? (srcPx & RED_MASK) >>> 16
                        : mult((srcPx & RED_MASK) >>> 16, alpha);
                int srcG = (alpha == 255) ? (srcPx & GREEN_MASK) >>> 8
                        : mult((srcPx & GREEN_MASK) >>> 8, alpha);
                int srcB = (alpha == 255) ? (srcPx & BLUE_MASK)
                        : mult(srcPx & BLUE_MASK, alpha);
                int destA = (destPx & ALPHA_MASK) >>> 24;
                int destR = (destPx & RED_MASK) >>> 16;
                int destG = (destPx & GREEN_MASK) >>> 8;
                int destB = (destPx & BLUE_MASK);

//                destOut[destOutPos] = min(srcA + destA, 0xFF) << 24 |
//                        min(srcR + destR, 0xFF) << 16 |
//                        min(srcG + destG, 0xFF) << 8 |
//                        min(srcB + destB, 0xFF);

                int a = min(srcA + destA, 0xFF);
                dest[destPos] = a << 24 |
                        min(srcR + destR, a) << 16 |
                        min(srcG + destG, a) << 8 |
                        min(srcB + destB, a);
                
                srcPos++;
                destPos++;
            }
        }
    }

    public static class Normal extends RGBComposite {

        public Normal(double extraAlpha) {
            super(extraAlpha);
        }

        public void composeRGB(int[] src, int srcPos, int[] dest, int destPos, int length) {
            int alpha = extraAlpha;
            for (int i = 0; i < length; i++) {
                int srcPx = src[srcPos];
                int destPx = dest[destPos];
                int srcR = (alpha == 255) ? (srcPx & RED_MASK) >>> 16
                        : mult((srcPx & RED_MASK) >>> 16, alpha);
                int srcG = (alpha == 255) ? (srcPx & GREEN_MASK) >>> 8
                        : mult((srcPx & GREEN_MASK) >>> 8, alpha);
                int srcB = (alpha == 255) ? (srcPx & BLUE_MASK)
                        : mult(srcPx & BLUE_MASK, alpha);
                int destR = (destPx & RED_MASK) >>> 16;
                int destG = (destPx & GREEN_MASK) >>> 8;
                int destB = (destPx & BLUE_MASK);

                dest[destPos] = blend(srcR, destR, alpha) << 16 |
                        blend(srcG, destG, alpha) << 8 |
                        blend(srcB, destB, alpha);

                srcPos++;
                destPos++;

            }
        }

        public void composeARGB(int[] src, int srcPos, int[] dest, int destPos, int length) {
            int alpha = extraAlpha;
            for (int i = 0; i < length; i++) {
                int srcPx = src[srcPos];
                int destPx = dest[destPos];
                int srcA = (alpha == 255) ? (srcPx & ALPHA_MASK) >>> 24
                        : mult((srcPx & ALPHA_MASK) >>> 24, alpha);
                int srcR = (alpha == 255) ? (srcPx & RED_MASK) >>> 16
                        : mult((srcPx & RED_MASK) >>> 16, alpha);
                int srcG = (alpha == 255) ? (srcPx & GREEN_MASK) >>> 8
                        : mult((srcPx & GREEN_MASK) >>> 8, alpha);
                int srcB = (alpha == 255) ? (srcPx & BLUE_MASK)
                        : mult(srcPx & BLUE_MASK, alpha);
                int destA = (destPx & ALPHA_MASK) >>> 24;
                int destR = (destPx & RED_MASK) >>> 16;
                int destG = (destPx & GREEN_MASK) >>> 8;
                int destB = (destPx & BLUE_MASK);

                dest[destPos] = blend(srcA, destA, srcA) << 24 |
                        blend(srcR, destR, srcA) << 16 |
                        blend(srcG, destG, srcA) << 8 |
                        blend(srcB, destB, srcA);

                srcPos++;
                destPos++;
            }

        }
    }

    public static class Sub extends RGBComposite {

        public Sub(double extraAlpha) {
            super(extraAlpha);
        }

        public void composeRGB(int[] src, int srcPos, int[] dest, int destPos, int length) {
            int alpha = extraAlpha;
            for (int i = 0; i < length; i++) {
                int srcPx = src[srcPos];
                int destPx = dest[destPos];
                int srcR = (alpha == 255) ? (srcPx & RED_MASK) >>> 16
                        : mult((srcPx & RED_MASK) >>> 16, alpha);
                int srcG = (alpha == 255) ? (srcPx & GREEN_MASK) >>> 8
                        : mult((srcPx & GREEN_MASK) >>> 8, alpha);
                int srcB = (alpha == 255) ? (srcPx & BLUE_MASK)
                        : mult(srcPx & BLUE_MASK, alpha);
                int destR = (destPx & RED_MASK) >>> 16;
                int destG = (destPx & GREEN_MASK) >>> 8;
                int destB = (destPx & BLUE_MASK);

                dest[destPos] = max(destR - srcR, 0) << 16 |
                        max(destG - srcG, 0) << 8 |
                        max(destB - srcB, 0);

                srcPos++;
                destPos++;

            }
        }

        public void composeARGB(int[] src, int srcPos, int[] dest, int destPos, int length) {
            int alpha = extraAlpha;
            for (int i = 0; i < length; i++) {
                int srcPx = src[srcPos];
                int destPx = dest[destPos];
                int srcA = (alpha == 255) ? (srcPx & ALPHA_MASK) >>> 24
                        : mult((srcPx & ALPHA_MASK) >>> 24, alpha);
                int srcR = (alpha == 255) ? (srcPx & RED_MASK) >>> 16
                        : mult((srcPx & RED_MASK) >>> 16, alpha);
                int srcG = (alpha == 255) ? (srcPx & GREEN_MASK) >>> 8
                        : mult((srcPx & GREEN_MASK) >>> 8, alpha);
                int srcB = (alpha == 255) ? (srcPx & BLUE_MASK)
                        : mult(srcPx & BLUE_MASK, alpha);
                int destA = (destPx & ALPHA_MASK) >>> 24;
                int destR = (destPx & RED_MASK) >>> 16;
                int destG = (destPx & GREEN_MASK) >>> 8;
                int destB = (destPx & BLUE_MASK);

                dest[destPos] = blend(srcA, destA, srcA) << 24 |
                        max(destR - srcR, 0) << 16 |
                        max(destG - srcG, 0) << 8 |
                        max(destB - srcB, 0);

                srcPos++;
                destPos++;
            }

        }
    }

    public static class Difference extends RGBComposite {

        public Difference(double extraAlpha) {
            super(extraAlpha);
        }

        public void composeRGB(int[] src, int srcPos, int[] dest, int destPos, int length) {
            int alpha = extraAlpha;
            for (int i = 0; i < length; i++) {
                int srcPx = src[srcPos];
                int destPx = dest[destPos];
                int srcR = (alpha == 255) ? (srcPx & RED_MASK) >>> 16
                        : mult((srcPx & RED_MASK) >>> 16, alpha);
                int srcG = (alpha == 255) ? (srcPx & GREEN_MASK) >>> 8
                        : mult((srcPx & GREEN_MASK) >>> 8, alpha);
                int srcB = (alpha == 255) ? (srcPx & BLUE_MASK)
                        : mult(srcPx & BLUE_MASK, alpha);
                int destR = (destPx & RED_MASK) >>> 16;
                int destG = (destPx & GREEN_MASK) >>> 8;
                int destB = (destPx & BLUE_MASK);

                dest[destPos] = alpha == 255 ? diff(srcR, destR) << 16 |
                        diff(srcG, destG) << 8 |
                        diff(srcB, destB) : (srcR + destR - (2 * min(srcR, mult(destR, alpha)))) << 16 |
                        (srcG + destG - (2 * min(srcG, mult(destG, alpha)))) << 8 |
                        (srcB + destB - (2 * min(srcB, mult(destB, alpha))));

                srcPos++;
                destPos++;

            }
        }

        public void composeARGB(int[] src, int srcPos, int[] dest, int destPos, int length) {
            int alpha = extraAlpha;
            for (int i = 0; i < length; i++) {
                int srcPx = src[srcPos];
                int destPx = dest[destPos];
                int srcA = (alpha == 255) ? (srcPx & ALPHA_MASK) >>> 24
                        : mult((srcPx & ALPHA_MASK) >>> 24, alpha);
                int srcR = (alpha == 255) ? (srcPx & RED_MASK) >>> 16
                        : mult((srcPx & RED_MASK) >>> 16, alpha);
                int srcG = (alpha == 255) ? (srcPx & GREEN_MASK) >>> 8
                        : mult((srcPx & GREEN_MASK) >>> 8, alpha);
                int srcB = (alpha == 255) ? (srcPx & BLUE_MASK)
                        : mult(srcPx & BLUE_MASK, alpha);
                int destA = (destPx & ALPHA_MASK) >>> 24;
                int destR = (destPx & RED_MASK) >>> 16;
                int destG = (destPx & GREEN_MASK) >>> 8;
                int destB = (destPx & BLUE_MASK);

//                destOut[destOutPos] = blend(srcA, destA, srcA) << 24 |
//                        diff(srcR, destR) << 16 |
//                        diff(srcG, destG) << 8 |
//                        diff(srcB, destB);

                dest[destPos] = blend(srcA, destA, srcA) << 24 |
                        (srcR + destR - (2 * min(mult(srcR, destA), mult(destR, srcA)))) << 16 |
                        (srcG + destG - (2 * min(mult(srcG, destA), mult(destG, srcA)))) << 8 |
                        (srcB + destB - (2 * min(mult(srcB, destA), mult(destB, srcA))));

                srcPos++;
                destPos++;
            }

        }
    }

    public static class BitXor extends RGBComposite {

        public BitXor(double extraAlpha) {
            super(extraAlpha);
        }

        public void composeRGB(int[] src, int srcPos, int[] dest, int destPos, int length) {
            int alpha = extraAlpha;
            for (int i = 0; i < length; i++) {
                int srcPx = src[srcPos];
                int destPx = dest[destPos];
                int srcR = (alpha == 255) ? (srcPx & RED_MASK) >>> 16
                        : mult((srcPx & RED_MASK) >>> 16, alpha);
                int srcG = (alpha == 255) ? (srcPx & GREEN_MASK) >>> 8
                        : mult((srcPx & GREEN_MASK) >>> 8, alpha);
                int srcB = (alpha == 255) ? (srcPx & BLUE_MASK)
                        : mult(srcPx & BLUE_MASK, alpha);
                int destR = (destPx & RED_MASK) >>> 16;
                int destG = (destPx & GREEN_MASK) >>> 8;
                int destB = (destPx & BLUE_MASK);

                dest[destPos] = (srcR ^ destR) << 16 |
                        (srcG ^ destG) << 8 |
                        (srcB ^ destB);

                srcPos++;
                destPos++;

            }
        }

        public void composeARGB(int[] src, int srcPos, int[] dest, int destPos, int length) {
            int alpha = extraAlpha;
            for (int i = 0; i < length; i++) {
                int srcPx = src[srcPos];
                int destPx = dest[destPos];
                int srcA = (alpha == 255) ? (srcPx & ALPHA_MASK) >>> 24
                        : mult((srcPx & ALPHA_MASK) >>> 24, alpha);
                int srcR = (alpha == 255) ? (srcPx & RED_MASK) >>> 16
                        : mult((srcPx & RED_MASK) >>> 16, alpha);
                int srcG = (alpha == 255) ? (srcPx & GREEN_MASK) >>> 8
                        : mult((srcPx & GREEN_MASK) >>> 8, alpha);
                int srcB = (alpha == 255) ? (srcPx & BLUE_MASK)
                        : mult(srcPx & BLUE_MASK, alpha);
                int destA = (destPx & ALPHA_MASK) >>> 24;
                int destR = (destPx & RED_MASK) >>> 16;
                int destG = (destPx & GREEN_MASK) >>> 8;
                int destB = (destPx & BLUE_MASK);

                dest[destPos] = blend(srcA, destA, srcA) << 24 |
                        (srcR ^ destR) << 16 |
                        (srcG ^ destG) << 8 |
                        (srcB ^ destB);

                srcPos++;
                destPos++;
            }

        }
    }

    public static class Screen extends RGBComposite {

        public Screen(double extraAlpha) {
            super(extraAlpha);
        }

        public void composeRGB(int[] src, int srcPos, int[] dest, int destPos, int length) {
            int alpha = extraAlpha;
            for (int i = 0; i < length; i++) {
                int srcPx = src[srcPos];
                int destPx = dest[destPos];
                int srcR = (alpha == 255) ? (srcPx & RED_MASK) >>> 16
                        : mult((srcPx & RED_MASK) >>> 16, alpha);
                int srcG = (alpha == 255) ? (srcPx & GREEN_MASK) >>> 8
                        : mult((srcPx & GREEN_MASK) >>> 8, alpha);
                int srcB = (alpha == 255) ? (srcPx & BLUE_MASK)
                        : mult(srcPx & BLUE_MASK, alpha);
                int destR = (destPx & RED_MASK) >>> 16;
                int destG = (destPx & GREEN_MASK) >>> 8;
                int destB = (destPx & BLUE_MASK);

                dest[destPos] = (255 - mult(255 - srcR, 255 - destR)) << 16 |
                        (255 - mult(255 - srcG, 255 - destG)) << 8 |
                        (255 - mult(255 - srcB, 255 - destB));

                srcPos++;
                destPos++;

            }
        }

        public void composeARGB(int[] src, int srcPos, int[] dest, int destPos, int length) {
            int alpha = extraAlpha;
            for (int i = 0; i < length; i++) {
                int srcPx = src[srcPos];
                int destPx = dest[destPos];
                int srcA = (alpha == 255) ? (srcPx & ALPHA_MASK) >>> 24
                        : mult((srcPx & ALPHA_MASK) >>> 24, alpha);
                int srcR = (alpha == 255) ? (srcPx & RED_MASK) >>> 16
                        : mult((srcPx & RED_MASK) >>> 16, alpha);
                int srcG = (alpha == 255) ? (srcPx & GREEN_MASK) >>> 8
                        : mult((srcPx & GREEN_MASK) >>> 8, alpha);
                int srcB = (alpha == 255) ? (srcPx & BLUE_MASK)
                        : mult(srcPx & BLUE_MASK, alpha);
                int destA = (destPx & ALPHA_MASK) >>> 24;
                int destR = (destPx & RED_MASK) >>> 16;
                int destG = (destPx & GREEN_MASK) >>> 8;
                int destB = (destPx & BLUE_MASK);

                dest[destPos] = blend(srcA, destA, srcA) << 24 |
                        (255 - mult(255 - srcR, 255 - destR)) << 16 |
                        (255 - mult(255 - srcG, 255 - destG)) << 8 |
                        (255 - mult(255 - srcB, 255 - destB));

                srcPos++;
                destPos++;

            }

        }
    }

    public static class Multiply extends RGBComposite {

        public Multiply(double extraAlpha) {
            super(extraAlpha);
        }

        public void composeRGB(int[] src, int srcPos, int[] dest, int destPos, int length) {
            int alpha = extraAlpha;
            for (int i = 0; i < length; i++) {
                int srcPx = src[srcPos];
                int destPx = dest[destPos];
                int srcR = (alpha == 255) ? (srcPx & RED_MASK) >>> 16
                        : mult((srcPx & RED_MASK) >>> 16, alpha);
                int srcG = (alpha == 255) ? (srcPx & GREEN_MASK) >>> 8
                        : mult((srcPx & GREEN_MASK) >>> 8, alpha);
                int srcB = (alpha == 255) ? (srcPx & BLUE_MASK)
                        : mult(srcPx & BLUE_MASK, alpha);
                int destR = (destPx & RED_MASK) >>> 16;
                int destG = (destPx & GREEN_MASK) >>> 8;
                int destB = (destPx & BLUE_MASK);

                dest[destPos] = (alpha == 255) ? mult(srcR, destR) << 16 |
                        mult(srcG, destG) << 8 |
                        mult(srcB, destB)
                        : (mult(srcR, destR) + mult(destR, 255 - alpha)) << 16 |
                        (mult(srcG, destG) + mult(destG, 255 - alpha)) << 8 |
                        mult(srcB, destB) + mult(destB, 255 - alpha);

                srcPos++;
                destPos++;

            }
        }

        public void composeARGB(int[] src, int srcPos, int[] dest, int destPos, int length) {
            int alpha = extraAlpha;
            for (int i = 0; i < length; i++) {
                int srcPx = src[srcPos];
                int destPx = dest[destPos];
                int srcA = (alpha == 255) ? (srcPx & ALPHA_MASK) >>> 24
                        : mult((srcPx & ALPHA_MASK) >>> 24, alpha);
                int srcR = (alpha == 255) ? (srcPx & RED_MASK) >>> 16
                        : mult((srcPx & RED_MASK) >>> 16, alpha);
                int srcG = (alpha == 255) ? (srcPx & GREEN_MASK) >>> 8
                        : mult((srcPx & GREEN_MASK) >>> 8, alpha);
                int srcB = (alpha == 255) ? (srcPx & BLUE_MASK)
                        : mult(srcPx & BLUE_MASK, alpha);
                int destA = (destPx & ALPHA_MASK) >>> 24;
                int destR = (destPx & RED_MASK) >>> 16;
                int destG = (destPx & GREEN_MASK) >>> 8;
                int destB = (destPx & BLUE_MASK);

                dest[destPos] = blend(srcA, destA, srcA) << 24 |
                        (mult(srcR, destR) + mult(destR, 255 - srcA) + mult(srcR, 255 - destA)) << 16 |
                        (mult(srcG, destG) + mult(destG, 255 - srcA) + mult(srcG, 255 - destA)) << 8 |
                        mult(srcB, destB) + mult(destB, 255 - srcA) + mult(srcB, 255 - destA);

                srcPos++;
                destPos++;
            }

        }
    }

//    public static class AddTest extends RGBComposite {
//
//        public AddTest(double extraAlpha) {
//            super(extraAlpha);
//        }
//
//        public void composeRGB(int[] src, int srcPos, int[] destIn, int destInPos, int[] destOut, int destOutPos, int length) {
//            int a, b;
//            int af = extraAlpha;
//            for (int i = 0; i < length; i++) {
////                a = af == 256 ? src[srcPos] : multRGB(src[srcPos], af);
//                a = src[srcPos];
//                b = destIn[destInPos];
//                destOut[destOutPos] = min((((a & RED_MASK) >>> 8) * af) + (b & RED_MASK), RED_MASK) & RED_MASK |
//                        min((((a & GREEN_MASK) >>> 8) * af) + (b & GREEN_MASK), GREEN_MASK) & GREEN_MASK |
//                        min((((a & BLUE_MASK) * af) >>> 8) + (b & BLUE_MASK), BLUE_MASK);
////                destOut[destOutPos] = min((a & RED_MASK) + (b & RED_MASK), RED_MASK) & RED_MASK |
////                        min((a & GREEN_MASK) + (b & GREEN_MASK), GREEN_MASK) & GREEN_MASK |
////                        min((a & BLUE_MASK) + (b & BLUE_MASK), BLUE_MASK);
////                a = src[srcPos];
////                b = destIn[destInPos];
////                int srcA = (af == 256) ? (a & ALPHA_MASK) >>> 24
////                        : (((a & ALPHA_MASK >>> 24) * af) >> 8);
////                int srcR = (af == 256) ? (a & RED_MASK) >>> 16
////                        : (((a & RED_MASK) * af) >>> 24);
////                int srcG = (af == 256) ? (a & GREEN_MASK) >>> 8
////                        : (((a & GREEN_MASK) * af) >>> 16);
////                int srcB = (af == 256) ? (a & BLUE_MASK)
////                        : (((a & BLUE_MASK) * af) >>> 8);
////                int destA = (b & ALPHA_MASK) >>> 24;
////                int destR = (b & RED_MASK) >>> 16;
////                int destG = (b & GREEN_MASK) >>> 8;
////                int destB = (b & BLUE_MASK);
////
////                destOut[destOutPos] = min(srcA + destA, 0xFF) << 24 |
////                        min(srcR + destR, 0xFF) << 16 |
////                        min(srcG + destG, 0xFF) << 8 |
////                        min(srcB + destB, 0xFF);
//
//                srcPos++;
//                destInPos++;
//                destOutPos++;
//
//            }
//        }
//
//        public void composeARGB(int[] src, int srcPos, int[] destIn, int destInPos, int[] destOut, int destOutPos, int length) {
////            int a, b;
//            int alpha = extraAlpha;
//            for (int i = 0; i < length; i++) {
//                int srcPx = alpha == 255 ? src[srcPos] : multARGB(src[srcPos], alpha);
//                int destPx = destIn[destInPos];
//                int srcA = (srcPx & ALPHA_MASK) >>> 24;
//                int srcR = (srcPx & RED_MASK) >>> 16;
//                int srcG = (srcPx & GREEN_MASK) >>> 8;
//                int srcB = (srcPx & BLUE_MASK);
//                int destA = (destPx & ALPHA_MASK) >>> 24;
//                int destR = (destPx & RED_MASK) >>> 16;
//                int destG = (destPx & GREEN_MASK) >>> 8;
//                int destB = (destPx & BLUE_MASK);
//
//                destOut[destOutPos] = min(srcA + destA, 0xFF) << 24 |
//                        min(srcR + destR, 0xFF) << 16 |
//                        min(srcG + destG, 0xFF) << 8 |
//                        min(srcB + destB, 0xFF);
//
//                srcPos++;
//                destInPos++;
//                destOutPos++;
//            }
//
//        }
//    }
}
