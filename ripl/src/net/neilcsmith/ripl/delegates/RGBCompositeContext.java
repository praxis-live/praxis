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
package net.neilcsmith.ripl.delegates;

import java.awt.Rectangle;
import net.neilcsmith.ripl.core.Bounds;
import net.neilcsmith.ripl.core.PixelData;
import net.neilcsmith.ripl.core.Surface;
import net.neilcsmith.ripl.rgbmath.RGBComposite;

/**
 *
 * @author Neil C Smith
 */
public class RGBCompositeContext extends CompositeDelegate {

    private RGBComposite comp;

    public RGBCompositeContext(RGBComposite comp) {
        if (comp == null) {
            throw new NullPointerException();
        }
        this.comp = comp;
    }

    public void process(Surface src, Surface dstIn, Surface dstOut, int x, int y, boolean rendering) {
//        boolean acceleratedOut = destIn != destOut &&
//                !destOut.getCapabilities().isInSystemMemory();
//        System.out.println("Process called");

//        PixelData destOutPx;
//        if (acceleratedOut) {
//            destOutPx = destInPx;
//        } else {
//            destOutPx = destOut.getPixelData();
//        }
        
        // this copies src to dest first, which could unnecessarily punt any acceleration
        // however, otherwise we break major contract of API - don't alter the source!
        if (rendering) {
            if (dstIn != dstOut) {
                dstOut.getGraphics().drawSurface(dstIn, 0, 0);
            }
            PixelData srcPx = src.getPixelData();
            PixelData destPx = dstOut.getPixelData();
            processData(srcPx, destPx, x, y, dstOut.hasAlpha());
        }





    }


//    private boolean checkContiguous(PixelData src, PixelData destIn, PixelData destOut) {
//        if (destIn == destOut) {
//            int line = src.getScanline();
//            if (line == destIn.getScanline()) {
//                Bounds sb = src.getBounds();
//                Bounds db = destIn.getBounds();
//                return sb.getX() == 0 &&
//                        sb.getX() == 0 &&
//                        
//            }
//            return destIn.getScanline() == line &&
//                    src.getBounds().getWidth() == line &&
//                    destIn.getBounds().getWidth() == line;
//        } else {
//            int line = src.getScanline();
//            return destIn.getScanline() == line &&
//                    destOut.getScanline() == line &&
//                    src.getBounds().getX() == line &&
//                    destIn.getBounds().getX() == line &&
//                    destOut.getBounds().getX() == line;
//        }
//    }
    public void processData(PixelData src, PixelData dest, int offsetX, int offsetY, boolean alpha) {
        int sSL = src.getScanline();
        int dSL = dest.getScanline();
//        Bounds sB = src.getBounds();
//        Bounds dB = dest.getBounds();

//        System.out.println("Checking Intersection");

        // @TODO optimise this now Bounds are not used
        //rectangles created translated to 0,0
//        Rectangle sRct = new Rectangle(0, 0, sB.getWidth(), sB.getHeight());
        Rectangle sRct = new Rectangle(0, 0, src.getWidth(), src.getHeight());
        sRct.translate(offsetX, offsetY);
//        Rectangle dRct = new Rectangle(0, 0, dB.getWidth(), dB.getHeight());
        Rectangle dRct = new Rectangle(0, 0, dest.getWidth(), dest.getHeight());
        Rectangle intersection = dRct.intersection(sRct);
        if (intersection.isEmpty()) {
//            System.out.println("Intersection Empty");
            return;
        }
        sRct.setBounds(intersection);
        dRct.setBounds(intersection);


//        sRct.translate(sB.getX() - offsetX, sB.getY() - offsetY);
//        dRct.translate(dB.getX(), dB.getY());
        sRct.translate(-offsetX, -offsetY);

        int[] srcData = src.getData();
        int[] destData = dest.getData();

        if (dRct.x == 0 && sRct.x == 0 && sSL == sRct.width && dSL == dRct.width) {
            int sOff = sRct.y * sSL;
            int dOff = dRct.y * dSL;
            int length = sRct.height * sSL;
            if (alpha) {
                comp.composeARGB(srcData, sOff, destData, dOff, destData, dOff, length);
            } else {
                comp.composeRGB(srcData, sOff, destData, dOff, destData, dOff, length);
            }
        } else {
            int sLineStart = sRct.y * sSL;
            int dLineStart = dRct.y * dSL;

            if (alpha) {
                for (int i = 0,  k = sRct.height; i < k; i++) {
                    comp.composeARGB(srcData, sLineStart + sRct.x, destData, dLineStart + dRct.x,
                            destData, dLineStart + dRct.x, sRct.width);
                    sLineStart += sSL;
                    dLineStart += dSL;
                }
            } else {
                for (int i = 0,  k = sRct.height; i < k; i++) {
                    comp.composeRGB(srcData, sLineStart + sRct.x, destData, dLineStart + dRct.x,
                            destData, dLineStart + dRct.x, sRct.width);
                    sLineStart += sSL;
                    dLineStart += dSL;
                }
            }
        }





    }
}
