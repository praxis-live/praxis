/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * Copying and distribution of this file, with or without modification,
 * are permitted in any medium without royalty provided the copyright
 * notice and this notice are preserved.  This file is offered as-is,
 * without any warranty.
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 *
 */
package org.praxislive.video.render.ops;

import static org.praxislive.video.render.utils.RGBMath.*;

/**
 *
 * @author Neil C Smith
 */
abstract class RGBComposite {

    final static RGBComposite ADD = new RGBComposite.Add();
    final static RGBComposite BITXOR = new RGBComposite.BitXor();
    final static RGBComposite DIFFERENCE = new RGBComposite.Difference();
    final static RGBComposite MASK = new RGBComposite.Mask();
    final static RGBComposite MULTIPLY = new RGBComposite.Multiply();
    final static RGBComposite NORMAL = new RGBComposite.Normal();
    final static RGBComposite SCREEN = new RGBComposite.Screen();
    final static RGBComposite SUB = new RGBComposite.Sub();
    
    

    private RGBComposite() {
    }

    abstract void rgb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha);

    abstract void argb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha);

    static class Add extends RGBComposite {

        private Add() {
        }

        @Override
        void rgb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha) {
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

                dest[destPos] = min(srcR + destR, 0xFF) << 16
                        | min(srcG + destG, 0xFF) << 8
                        | min(srcB + destB, 0xFF);

                srcPos++;
                destPos++;

            }
        }

        @Override
        void argb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha) {
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
                dest[destPos] = a << 24
                        | min(srcR + destR, a) << 16
                        | min(srcG + destG, a) << 8
                        | min(srcB + destB, a);

                srcPos++;
                destPos++;
            }
        }
    }

    static class Normal extends RGBComposite {

        private Normal() {
        }

        @Override
        void rgb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha) {
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

                dest[destPos] = blend(srcR, destR, alpha) << 16
                        | blend(srcG, destG, alpha) << 8
                        | blend(srcB, destB, alpha);

                srcPos++;
                destPos++;

            }
        }

        @Override
        void argb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha) {
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

                dest[destPos] = blend(srcA, destA, srcA) << 24
                        | blend(srcR, destR, srcA) << 16
                        | blend(srcG, destG, srcA) << 8
                        | blend(srcB, destB, srcA);

                srcPos++;
                destPos++;
            }

        }
    }

    static class Sub extends RGBComposite {

        private Sub() {
        }

        @Override
        void rgb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha) {
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

                dest[destPos] = max(destR - srcR, 0) << 16
                        | max(destG - srcG, 0) << 8
                        | max(destB - srcB, 0);

                srcPos++;
                destPos++;

            }
        }

        @Override
        void argb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha) {
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

                dest[destPos] = blend(srcA, destA, srcA) << 24
                        | max(destR - srcR, 0) << 16
                        | max(destG - srcG, 0) << 8
                        | max(destB - srcB, 0);

                srcPos++;
                destPos++;
            }

        }
    }

    static class Difference extends RGBComposite {

        private Difference() {
        }

        @Override
        void rgb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha) {
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

                dest[destPos] = alpha == 255 ? diff(srcR, destR) << 16
                        | diff(srcG, destG) << 8
                        | diff(srcB, destB) : (srcR + destR - (2 * min(srcR, mult(destR, alpha)))) << 16
                        | (srcG + destG - (2 * min(srcG, mult(destG, alpha)))) << 8
                        | (srcB + destB - (2 * min(srcB, mult(destB, alpha))));

                srcPos++;
                destPos++;

            }
        }

        @Override
        void argb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha) {
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

                dest[destPos] = blend(srcA, destA, srcA) << 24
                        | (srcR + destR - (2 * min(mult(srcR, destA), mult(destR, srcA)))) << 16
                        | (srcG + destG - (2 * min(mult(srcG, destA), mult(destG, srcA)))) << 8
                        | (srcB + destB - (2 * min(mult(srcB, destA), mult(destB, srcA))));

                srcPos++;
                destPos++;
            }

        }
    }

    static class BitXor extends RGBComposite {

        private BitXor() {
        }

        @Override
        void rgb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha) {
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

                dest[destPos] = (srcR ^ destR) << 16
                        | (srcG ^ destG) << 8
                        | (srcB ^ destB);

                srcPos++;
                destPos++;

            }
        }

        @Override
        void argb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha) {
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

                dest[destPos] = blend(srcA, destA, srcA) << 24
                        | (srcR ^ destR) << 16
                        | (srcG ^ destG) << 8
                        | (srcB ^ destB);

                srcPos++;
                destPos++;
            }

        }
    }

    static class Screen extends RGBComposite {

        private Screen() {
        }

        @Override
        void rgb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha) {
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

                dest[destPos] = (255 - mult(255 - srcR, 255 - destR)) << 16
                        | (255 - mult(255 - srcG, 255 - destG)) << 8
                        | (255 - mult(255 - srcB, 255 - destB));

                srcPos++;
                destPos++;

            }
        }

        @Override
        void argb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha) {
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

                dest[destPos] = blend(srcA, destA, srcA) << 24
                        | (255 - mult(255 - srcR, 255 - destR)) << 16
                        | (255 - mult(255 - srcG, 255 - destG)) << 8
                        | (255 - mult(255 - srcB, 255 - destB));

                srcPos++;
                destPos++;

            }

        }
    }

    static class Multiply extends RGBComposite {

        private Multiply() {
        }

        @Override
        void rgb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha) {
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

                dest[destPos] = (alpha == 255) ? mult(srcR, destR) << 16
                        | mult(srcG, destG) << 8
                        | mult(srcB, destB)
                        : (mult(srcR, destR) + mult(destR, 255 - alpha)) << 16
                        | (mult(srcG, destG) + mult(destG, 255 - alpha)) << 8
                        | mult(srcB, destB) + mult(destB, 255 - alpha);

                srcPos++;
                destPos++;

            }
        }

        @Override
        public void argb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha) {
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

                dest[destPos] = blend(srcA, destA, srcA) << 24
                        | (mult(srcR, destR) + mult(destR, 255 - srcA) + mult(srcR, 255 - destA)) << 16
                        | (mult(srcG, destG) + mult(destG, 255 - srcA) + mult(srcG, 255 - destA)) << 8
                        | mult(srcB, destB) + mult(destB, 255 - srcA) + mult(srcB, 255 - destA);

                srcPos++;
                destPos++;
            }

        }
    }

    static class Mask extends RGBComposite {

        private Mask() {
        }

        @Override
        void rgb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha) {
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

                dest[destPos] = (alpha == 255) ? mult(srcR, destR) << 16
                        | mult(srcG, destG) << 8
                        | mult(srcB, destB)
                        : (mult(srcR, destR) + mult(destR, 255 - alpha)) << 16
                        | (mult(srcG, destG) + mult(destG, 255 - alpha)) << 8
                        | mult(srcB, destB) + mult(destB, 255 - alpha);

                srcPos++;
                destPos++;

            }
        }

        @Override
        void argb(int[] src, int srcPos, int[] dest, int destPos, int length, int alpha) {
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

//                dest[destPos] = blend(srcA, destA, srcA) << 24 |
//                        (mult(srcR, destR) + mult(destR, 255 - srcA) + mult(srcR, 255 - destA)) << 16 |
//                        (mult(srcG, destG) + mult(destG, 255 - srcA) + mult(srcG, 255 - destA)) << 8 |
//                        mult(srcB, destB) + mult(destB, 255 - srcA) + mult(srcB, 255 - destA);

                dest[destPos] = (alpha == 255) ? mult(srcA, destA) << 24
                        | mult(srcR, destR) << 16
                        | mult(srcG, destG) << 8
                        | mult(srcB, destB)
                        : (mult(srcA, destA) + mult(destA, 255 - alpha)) << 24
                        | (mult(srcR, destR) + mult(destR, 255 - alpha)) << 16
                        | (mult(srcG, destG) + mult(destG, 255 - alpha)) << 8
                        | mult(srcB, destB) + mult(destB, 255 - alpha);

                srcPos++;
                destPos++;
            }

        }
    }
}
