/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.ripl.components.temporal;

import net.neilcsmith.ripl.PixelData;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.SurfaceOp;
import net.neilcsmith.ripl.impl.MultiInputInOut;
import net.neilcsmith.ripl.rgbmath.RGBMath;

/**
 *
 * @author Neil C Smith
 */
public class Ripple extends MultiInputInOut {
    
    private Op op;

    public Ripple() {
        super(2, false);
        op = new Op();

    }

    @Override
    protected void process(Surface surface, boolean rendering) {
        if (getSourceCount() == 0) {
            if (rendering) {
                surface.clear();
            }
            return;
        } else if (getSourceCount() == 1) {
            if (rendering) {
                if (surface.hasAlpha()) {
                    surface.clear();
                }
//                surface.getGraphics().drawSurface(getInputSurface(0), 0, 0);
                surface.copy(getInputSurface(0));
            }
            return;
        } else {
            if (rendering) {
                surface.process(op, getInputSurface(0), getInputSurface(1));
            }

        }



//        int dSL = pd.getScanline();
//        Bounds dBnds = pd.getBounds();
//        int x = dBnds.getX();
//        int y = dBnds.getY();
//        int width = dBnds.getWidth();
//        int height = dBnds.getHeight();
//        int mIdx = width;
//        int iy = y + 1;
//        int ky = y + height - 1;
//        int pix, ix, kx;
//        for (; iy < ky; iy++) {
//            ix = (iy * dSL) + x + 1;
//            kx = ix + width - 2;
//            mIdx++;
//            for (;ix<kx; ix++) {
//                pix = data[ix];
//                pix = RGBMath.max(pix >>> 16 & 0xFF,
//                        pix >>> 8 & 0xFF,
//                        pix & 0xFF);
//                previousMap[mIdx] += pix >>> 2;
//                pix = (
//                        previousMap[mIdx - width] +
//                        previousMap[mIdx + width] +
//                        previousMap[mIdx - 1] +
//                        previousMap[mIdx + 1]
//                        ) >> 1;
//                pix -= currentMap[mIdx];
//                pix -= pix >> 5;
//                currentMap[mIdx] = pix;
//            }
//            mIdx++;
//        }




    }

    public static class Op implements SurfaceOp {

        private int[] currentMap;
        private int[] previousMap;
//    private int damp = 16;
        private int rad = 256;

        public Op() {
            currentMap = new int[0];
            previousMap = new int[0];
        }

        public void process(PixelData output, PixelData... inputs) {
            processRipples(output, inputs);
        }

        private void processRipples(PixelData output, PixelData... inputs) {
            if (inputs.length != 2) {
                return; //@TODO fix this ?
            }

            PixelData pd = output;

            int width = pd.getWidth();
            int height = pd.getHeight();
            int hWidth = width / 2;
            int hHeight = height / 2;
            int area = width * height;

            // ensure ripple maps match surface area
            if (currentMap.length != area) {
                currentMap = new int[area];
                previousMap = new int[area];
            }

            // get surface data
            int[] out = pd.getData();
            int outSL = pd.getScanline();
            pd = inputs[0];
            int[] in = pd.getData();
            int inSL = pd.getScanline();
            pd = inputs[1];
            int[] dis = pd.getData();
            int disSL = pd.getScanline();

            // copy top edge
            for (int x = 0; x < width; x++) {
                out[x] = in[x];
            }

            // main ripple loop
            int data, pix, inX, inY;
            int disOffset = disSL;
            int outOffset = outSL;
            int index;
            for (int y = 1,  endY = (height - 1); y < endY; y++) {
                out[outOffset] = in[y * inSL];
                index = (y * width) + 1;
                for (int x = 1,  endX = (width - 1); x < endX; x++) {
                    // calculate ripples and disturbance
                    data = (previousMap[index - width] +
                            previousMap[index + width] +
                            previousMap[index - 1] +
                            previousMap[index + 1]) / 2;
                    pix = dis[x + disOffset];
                    pix = RGBMath.max((pix >>> 16) & 0xFF,
                            (pix >>> 8) & 0xFF, pix & 0xFF);
                    previousMap[index] += pix;
                    data -= currentMap[index];
//                data -= currentMap[index] + (pix / 2);
//                data -= (data >> 5);
                    data -= (data < 0 ? (data / 32) - 1 : data / 32);
                    currentMap[index] = data;
                    index++;

                    // draw texture
                    data = rad - data;
                    inX = clamp(((x - hWidth) * data / rad) + hWidth, width);
                    inY = clamp(((y - hHeight) * data / rad) + hHeight, height);

                    out[x + outOffset] = in[(inY * inSL) + inX];
                }
                out[outOffset + width - 1] = in[(y * inSL) + width - 1];

                disOffset += disSL;
                outOffset += outSL;

            }

            // copy bottom edge
            int inOffset = (height - 1) * inSL;
            outOffset = (height - 1) * outSL;
            for (int x = 0; x < width; x++) {
                out[x + outOffset] = in[x + inOffset];
            }

            int[] temp = currentMap;
            currentMap = previousMap;
            previousMap = temp;

//            disX = y * disSL;
//            outX = y * outSL;
//            mapIdx = y * width;
//            int endX = outX + width - 2;
//            // copy left edge
//            out[outX] = in[y * inSL];
//            while (outX < endX) {
//                // calculate and disturb
//                outX++;
//                disX++;
//                mapIdx++;
//                data = (
//                        previousMap[mapIdx - width] +
//                        previousMap[mapIdx + width] +
//                        previousMap[mapIdx - 1] +
//                        previousMap[mapIdx + 1]
//                        ) >>> 1;
//                pix = dis[disX];
//                pix = RGBMath.max((pix >>> 16) & 0xFF,
//                        (pix >>> 8) & 0xFF, pix & 0xFF);
//                data -= currentMap[mapIdx] + pix;
//                data -= data / damp;
//                currentMap[mapIdx] = data;
//                
//                // draw texture
//                data = rad - data;
////                inX = ((x - hWidth) * data / 512) + hWidth;
//                


        }

        private int clamp(int input, int max) {
            return (input < 0) ? 0 : (input >= max) ? max - 1 : input;
        }
    }
}
