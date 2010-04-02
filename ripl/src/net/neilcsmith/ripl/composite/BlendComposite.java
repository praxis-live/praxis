/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
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
package net.neilcsmith.ripl.composite;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import net.neilcsmith.ripl.rgbmath.RGBComposite;
import net.neilcsmith.ripl.rgbmath.RGBMath;

/**
 *
 * @author Neil C Smith
 * @TODO reimplement composites through this class, with direct databuffer access
 */
public class BlendComposite implements Composite {

    public static enum Mode {

        AddPin,
        SubPin,
        Difference,
        Multiply,
        Screen,
        BitXor
    }
    private Mode mode;
    private float alpha;

    private BlendComposite(Mode mode, float alpha) {
        this.mode = mode;
        this.alpha = alpha;
    }

    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        boolean srcAlpha = checkAlpha(srcColorModel);
        boolean destAlpha = checkAlpha(dstColorModel);
        switch (mode) {
            case AddPin:
                return new RGBContext(new RGBComposite.AddPin(alpha), srcAlpha, destAlpha);
            case BitXor:
                return new RGBContext(new RGBComposite.BitXor(alpha), srcAlpha, destAlpha);
            case Difference:
                return new RGBContext(new RGBComposite.Difference(alpha), srcAlpha, destAlpha);
            case Multiply:
                return new RGBContext(new RGBComposite.Multiply(alpha), srcAlpha, destAlpha);
            case Screen:
                return new RGBContext(new RGBComposite.Screen(alpha), srcAlpha, destAlpha);
            case SubPin:
                return new RGBContext(new RGBComposite.SubPin(alpha), srcAlpha, destAlpha);
            default:
                throw new IllegalArgumentException();
            }
    }

    private boolean checkAlpha(ColorModel cm) {
        if (cm instanceof DirectColorModel && cm.getTransferType() == DataBuffer.TYPE_INT) {
            DirectColorModel dCm = (DirectColorModel) cm;
            if (dCm.getNumComponents() == 3 &&
                    dCm.getRedMask() == RGBMath.RED_MASK &&
                    dCm.getGreenMask() == RGBMath.GREEN_MASK &&
                    dCm.getBlueMask() == RGBMath.BLUE_MASK) {
                return false;
            } else if (dCm.getNumComponents() == 4 &&
                    dCm.getRedMask() == RGBMath.RED_MASK &&
                    dCm.getGreenMask() == RGBMath.GREEN_MASK &&
                    dCm.getBlueMask() == RGBMath.BLUE_MASK &&
                    dCm.getAlphaMask() == RGBMath.ALPHA_MASK &&
                    dCm.isAlphaPremultiplied()) {
                return true;
            }
        }
        throw new RasterFormatException("Incompatible colour models");
    }

    public BlendComposite derive(float alpha) {
        return new BlendComposite(mode, alpha);
    }

    public static BlendComposite getInstance(Mode mode) {
        return getInstance(mode, 1);
    }

    public static BlendComposite getInstance(Mode mode, float alpha) {
        if (mode == null) {
            throw new NullPointerException();
        }
        if (alpha < 0 || alpha > 1) {
            throw new IllegalArgumentException();
        }
        return new BlendComposite(mode, alpha);
    }

    private class RGBContext implements CompositeContext {

        private boolean srcAlpha;
        private boolean destAlpha;
        private RGBComposite cmp;

        private RGBContext(RGBComposite cmp, boolean srcAlpha, boolean destAlpha) {
            this.cmp = cmp;
            this.srcAlpha = srcAlpha;
            this.destAlpha = destAlpha;
        }

        public void dispose() {
        // no op
        }

        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {

            if (srcAlpha != destAlpha) {
                composeMixed(src, dstIn, dstOut);
                return;
            }

            if (dstIn != dstOut) {
                // should we just do compose mixed?
                dstOut.setDataElements(0, 0, dstIn);
            }

            int width = Math.min(src.getWidth(), dstIn.getWidth());
            int height = Math.min(src.getHeight(), dstIn.getHeight());
            DataBufferInt db = (DataBufferInt) src.getDataBuffer();
            SinglePixelPackedSampleModel m = (SinglePixelPackedSampleModel) src.getSampleModel();
            int[] srcPix = db.getData();
            int srcSL = m.getScanlineStride();
            int srcOffset = db.getOffset() - src.getSampleModelTranslateX() -
                    (srcSL * src.getSampleModelTranslateY());
            db = (DataBufferInt) dstOut.getDataBuffer();
            m = (SinglePixelPackedSampleModel) dstOut.getSampleModel();
            int[] dstPix = db.getData();
            int dstSL = m.getScanlineStride();
            int dstOffset = db.getOffset() - dstOut.getSampleModelTranslateX() -
                    (dstSL * dstOut.getSampleModelTranslateY());
            
            if (destAlpha) {
                composeARGB(width, height, srcPix, srcSL, srcOffset, dstPix, dstSL, dstOffset);
            } else {
                composeRGB(width, height, srcPix, srcSL, srcOffset, dstPix, dstSL, dstOffset);
            }

        }

        private void composeRGB(int width, int height, int[] src, int srcSL, int srcOffset,
                int[] dst, int dstSL, int dstOffset) {
//            System.out.println("Composing RGB");
            if (srcSL == width && dstSL == width) {
//                System.out.println("Composing full array");
                cmp.composeRGB(src, srcOffset, dst, dstOffset, dst, dstOffset, width * height);
            } else {
//                System.out.println("Composing line by line");
                for (int y=0; y < height; y++) {
                    cmp.composeRGB(src, srcOffset, dst, dstOffset, dst, dstOffset, width);
                    srcOffset += srcSL;
                    dstOffset += dstSL;
                }
            }
            
        }
        
        private void composeARGB(int width, int height, int[] src, int srcSL, int srcOffset,
                int[] dst, int dstSL, int dstOffset) {
//            System.out.println("Composing ARGB");
            if (srcSL == width && dstSL == width) {
//                System.out.println("Composing full array");
                cmp.composeARGB(src, srcOffset, dst, dstOffset, dst, dstOffset, width * height);
            } else {
//                System.out.println("Composing line by line");
                for (int y=0; y < height; y++) {
                    cmp.composeARGB(src, srcOffset, dst, dstOffset, dst, dstOffset, width);
                    srcOffset += srcSL;
                    dstOffset += dstSL;
                }
            }
            
        }

        private void composeMixed(Raster src, Raster dstIn, WritableRaster dstOut) {

            System.out.println("Composing Mixed");
            
            int width = Math.min(src.getWidth(), dstIn.getWidth());
            int height = Math.min(src.getHeight(), dstIn.getHeight());

            int[] srcPix = new int[width];
            int[] destPix = new int[width];

            for (int y = 0; y < height; y++) {
                src.getDataElements(0, y, width, 1, srcPix);
                if (!srcAlpha) {
                    for (int i = 0; i < width; i++) {
                        srcPix[i] |= RGBMath.ALPHA_MASK;
                    }
                }
                dstIn.getDataElements(0, y, width, 1, destPix);
                if (!destAlpha) {
                    for (int i = 0; i < width; i++) {
                        destPix[i] |= RGBMath.ALPHA_MASK;
                    }
                }
                cmp.composeARGB(srcPix, 0, destPix, 0, destPix, 0, width);
                if (!destAlpha) {
                    for (int i = 0; i < width; i++) {
                        destPix[i] &= 0x00FFFFFF;
                    }
                }
                dstOut.setDataElements(0, y, width, 1, destPix);
            }


        }

//        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
//
//            int width = Math.min(src.getWidth(), dstIn.getWidth());
//            int height = Math.min(src.getHeight(), dstIn.getHeight());
//
//            int[] srcPix = new int[width];
//            int[] destPix = new int[width];
//
//            if (srcAlpha && destAlpha) {
//                for (int y = 0; y < height; y++) {
//                    src.getDataElements(0, y, width, 1, srcPix);
//                    dstIn.getDataElements(0, y, width, 1, destPix);
//                    cmp.composeARGB(srcPix, 0, destPix, 0, destPix, 0, width);
//                    dstOut.setDataElements(0, y, width, 1, destPix);
//                }
//            } else if (!srcAlpha && !destAlpha) {
//                for (int y = 0; y < height; y++) {
//                    src.getDataElements(0, y, width, 1, srcPix);
//                    dstIn.getDataElements(0, y, width, 1, destPix);
//                    cmp.composeRGB(srcPix, 0, destPix, 0, destPix, 0, width);
//                    dstOut.setDataElements(0, y, width, 1, destPix);
//                }
//            } else {
//                for (int y = 0; y < height; y++) {
//                    src.getDataElements(0, y, width, 1, srcPix);
//                    if (!srcAlpha) {
//                        for (int i = 0; i < width; i++) {
//                            srcPix[i] |= RGBMath.ALPHA_MASK;
//                        }
//                    }
//                    dstIn.getDataElements(0, y, width, 1, destPix);
//                    if (!destAlpha) {
//                        for (int i = 0; i < width; i++) {
//                            destPix[i] |= RGBMath.ALPHA_MASK;
//                        }
//                    }
//                    cmp.composeARGB(srcPix, 0, destPix, 0, destPix, 0, width);
//                    if (!destAlpha) {
//                        for (int i = 0; i < width; i++) {
//                            destPix[i] &= 0x00FFFFFF;
//                        }
//                    }
//                    dstOut.setDataElements(0, y, width, 1, destPix);
//                }
//            }
//
//        }
        }
    }
