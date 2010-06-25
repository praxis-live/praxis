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
package net.neilcsmith.ripl.ops.impl;

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
import net.neilcsmith.ripl.PixelData;
import net.neilcsmith.ripl.ops.BlendFunction;
import net.neilcsmith.ripl.rgbmath.RGBMath;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class BlendComposite implements Composite {

    private BlendFunction blend;

    private BlendComposite(BlendFunction blend) {
        this.blend = blend;
    }

    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        System.out.println(srcColorModel);
        System.out.println(dstColorModel);
        boolean srcAlpha = checkAlpha(srcColorModel);
        boolean dstAlpha = checkAlpha(dstColorModel);
        return new Context(blend, srcAlpha, dstAlpha);

    }

    private boolean checkAlpha(ColorModel cm) {
        if (cm instanceof DirectColorModel && cm.getTransferType() == DataBuffer.TYPE_INT) {
            DirectColorModel dCm = (DirectColorModel) cm;
            if (dCm.getNumComponents() == 3
                    && dCm.getRedMask() == RGBMath.RED_MASK
                    && dCm.getGreenMask() == RGBMath.GREEN_MASK
                    && dCm.getBlueMask() == RGBMath.BLUE_MASK) {
                return false;
            } else if (dCm.getNumComponents() == 4
                    && dCm.getRedMask() == RGBMath.RED_MASK
                    && dCm.getGreenMask() == RGBMath.GREEN_MASK
                    && dCm.getBlueMask() == RGBMath.BLUE_MASK
                    && dCm.getAlphaMask() == RGBMath.ALPHA_MASK
                    && dCm.isAlphaPremultiplied()) {
                return true;
            }
        }
        throw new RasterFormatException("Incompatible colour models");
    }

    public static BlendComposite create(BlendFunction blend) {
        if (blend == null) {
            throw new NullPointerException();
        }
        return new BlendComposite(blend);
    }

    private static class Context implements CompositeContext {

        private boolean srcAlpha;
        private boolean dstAlpha;
        private BlendFunction blend;

        private Context(BlendFunction blend, boolean srcAlpha, boolean dstAlpha) {
            this.blend = blend;
            this.srcAlpha = srcAlpha;
            this.dstAlpha = dstAlpha;
        }

        public void dispose() {
            // no op
        }

        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            if (dstIn != dstOut) {
                dstOut.setDataElements(0, 0, dstIn);
            }
            DataBufferInt db;
            int[] data;
            int scanline, offset;
            // source
            db = (DataBufferInt) src.getDataBuffer();
            data = db.getData();
            scanline = ((SinglePixelPackedSampleModel) src.getSampleModel()).getScanlineStride();
            offset = db.getOffset() - src.getSampleModelTranslateX()
                    - (scanline * src.getSampleModelTranslateY());
            RasterWrapper srcPD = new RasterWrapper(data, src.getWidth(), src.getHeight(),
                    scanline, offset, srcAlpha);

            // dest
            db = (DataBufferInt) dstOut.getDataBuffer();
            data = db.getData();
            scanline = ((SinglePixelPackedSampleModel) dstOut.getSampleModel()).getScanlineStride();
            offset = db.getOffset() - dstOut.getSampleModelTranslateX()
                    - (scanline * dstOut.getSampleModelTranslateY());
            RasterWrapper dstPD = new RasterWrapper(data, src.getWidth(), src.getHeight(),
                    scanline, offset, dstAlpha);

            blend.process(srcPD, dstPD);
        }
    }

    private static class RasterWrapper implements PixelData {

        private int[] data;
        private int offset;
        private int scanline;
        private int width;
        private int height;
        private boolean alpha;

        private RasterWrapper(int[] data, int width, int height, int scanline,
                int offset, boolean alpha) {
            this.data = data;
            this.width = width;
            this.height = height;
            this.scanline = scanline;
            this.offset = offset;
            this.alpha = alpha;
        }

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
    }
}
