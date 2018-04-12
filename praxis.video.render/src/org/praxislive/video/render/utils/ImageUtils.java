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
package org.praxislive.video.render.utils;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.praxislive.video.render.PixelData;


/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ImageUtils {

    private final static ImageUtils instance = new ImageUtils();
    private final static DirectColorModel rgbCM = new DirectColorModel(24,
                0x00ff0000, // R
                0x0000ff00, // G
                0x000000ff, // B
                0x0); // no alpha;
    private final static DirectColorModel pre_argbCM = new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                32,
                0x00ff0000,// R
                0x0000ff00,// G
                0x000000ff,// B
                0xff000000,// A
                true, // Premultiplied
                DataBuffer.TYPE_INT);

    private ImageUtils() {}

    public static BufferedImage toImage(PixelData pd) {
        int w = pd.getWidth();
        int h = pd.getHeight();
        int offset = pd.getOffset();
        int sl = pd.getScanline();
        DataBufferInt db = new DataBufferInt(pd.getData(), sl * h, offset);
        DirectColorModel cm = getColorModel(pd.hasAlpha());
        WritableRaster raster = Raster.createPackedRaster(db, w, h, sl, cm.getMasks(), null);
        return new BufferedImage(cm, raster, pd.hasAlpha(), null);
    }

    private static DirectColorModel getColorModel(boolean alpha) {
        if (alpha) {
            return pre_argbCM;
        } else {
            return rgbCM;
        }
    }

}
