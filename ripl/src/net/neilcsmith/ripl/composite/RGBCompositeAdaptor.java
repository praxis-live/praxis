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

import net.neilcsmith.ripl.j2d.*;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.WritableRaster;
import net.neilcsmith.ripl.rgbmath.RGBComposite;
import net.neilcsmith.ripl.rgbmath.RGBMath;

/**
 *
 * @author Neil C Smith
 */
public class RGBCompositeAdaptor implements Composite {

    private RGBComposite comp;
    
    public RGBCompositeAdaptor(RGBComposite comp) {
        if (comp == null) {
            throw new NullPointerException();
        }
        this.comp = comp;
    } 
    
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        boolean srcAlpha = checkAlpha(srcColorModel);
        boolean destAlpha = checkAlpha(dstColorModel);
        return new Context(srcAlpha, destAlpha);
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
    
    private class Context implements CompositeContext {
        
        private boolean srcAlpha;
        private boolean destAlpha;

        
        private Context(boolean srcAlpha, boolean destAlpha) {
            this.srcAlpha = srcAlpha;
            this.destAlpha = destAlpha;

        }

        public void dispose() {
            // no op
        }

        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            int width = Math.min(src.getWidth(), dstIn.getWidth());
            int height = Math.min(src.getHeight(), dstIn.getHeight());
            int[] srcPix = new int[width];
            int[] destPix = new int[width];
            RGBComposite cmp = comp;
            if (srcAlpha && destAlpha) {
                for (int y = 0; y < height; y++) {
                    src.getDataElements(0, y, width, 1, srcPix);
                    dstIn.getDataElements(0, y, width, 1, destPix);
                    cmp.composeARGB(srcPix, 0, destPix, 0, destPix, 0, width);
                    dstOut.setDataElements(0, y, width, 1, destPix);
                }
            } else if (!srcAlpha && !destAlpha) {
                for (int y = 0; y < height; y++) {
                    src.getDataElements(0, y, width, 1, srcPix);
                    dstIn.getDataElements(0, y, width, 1, destPix);
                    cmp.composeRGB(srcPix, 0, destPix, 0, destPix, 0, width);
                    dstOut.setDataElements(0, y, width, 1, destPix);
                }
            } else {
                for (int y = 0; y < height; y++) {
                    src.getDataElements(0, y, width, 1, srcPix);
                    if (!srcAlpha) {
                        for (int i=0; i < width; i++) {
                            srcPix[i] |= RGBMath.ALPHA_MASK;
                        }
                    }
                    dstIn.getDataElements(0, y, width, 1, destPix);
                    if (!destAlpha) {
                        for (int i=0; i < width; i++) {
                            destPix[i] |= RGBMath.ALPHA_MASK;
                        }
                    }
                    cmp.composeARGB(srcPix, 0, destPix, 0, destPix, 0, width);
                    if (!destAlpha) {
                        for (int i=0; i < width; i++) {
                            destPix[i] &= 0x00FFFFFF;
                        }
                    }
                    dstOut.setDataElements(0, y, width, 1, destPix);
                }
            }
            
        }
        
    }

}
