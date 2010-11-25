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

package net.neilcsmith.ripl.ops;

import java.util.logging.Logger;
import net.neilcsmith.ripl.PixelData;
import net.neilcsmith.ripl.SurfaceOp;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Blur implements SurfaceOp {

    // private final static Logger LOG = Logger.getLogger(Blur.class.getName());
    
    private int xRadius;
    private int yRadius;
    private TmpData tmp;

    private Blur(int radius) {
        this.xRadius = radius;
        this.yRadius = radius;
    }

    public void process(PixelData output, PixelData... inputs) {
        int dstH = output.getWidth();
        int dstW = output.getHeight();
        if (tmp == null) {
            tmp = new TmpData(dstW, dstH, null);
        } else if (tmp.width != dstW || tmp.height != dstH) {
            tmp = new TmpData(dstW, dstH, tmp.data);
        }
        blur(output, tmp, xRadius);
        blur(tmp, output, yRadius);

    }

    private void blur(PixelData src, PixelData dst, int radius) {

	    final int windowSize = radius * 2 + 1;
	    final int radiusPlusOne = radius + 1;

	    int sumAlpha;
	    int sumRed;
	    int sumGreen;
	    int sumBlue;

	    int srcIndex = src.getOffset();
	    int dstIndex;
	    int pixel;

	    int[] sumLookupTable = new int[256 * windowSize];
	    for (int i = 0; i < sumLookupTable.length; i++) {
	        sumLookupTable[i] = i / windowSize;
	    }

	    int[] indexLookupTable = new int[radiusPlusOne];
            int width = src.getWidth();
	    if (radius < width) {
	        for (int i = 0; i < indexLookupTable.length; i++) {
	            indexLookupTable[i] = i;
	        }
	    } else {
	        for (int i = 0; i < width; i++) {
	            indexLookupTable[i] = i;
	        }
	        for (int i = width; i < indexLookupTable.length; i++) {
	            indexLookupTable[i] = width - 1;
	        }
	    }

            int height = src.getHeight();
//            int dstSL = dst.getScanline();
            int[] srcPixels = src.getData();
            int[] dstPixels = dst.getData();

	    for (int y = 0; y < height; y++) {
	        sumAlpha = sumRed = sumGreen = sumBlue = 0;
	        dstIndex = y + dst.getOffset();

	        pixel = srcPixels[srcIndex];
	        sumAlpha += radiusPlusOne * ((pixel >> 24) & 0xFF);
	        sumRed   += radiusPlusOne * ((pixel >> 16) & 0xFF);
	        sumGreen += radiusPlusOne * ((pixel >>  8) & 0xFF);
	        sumBlue  += radiusPlusOne * ( pixel        & 0xFF);

	        for (int i = 1; i <= radius; i++) {
	        	 pixel = srcPixels[srcIndex + indexLookupTable[i]];
	            sumAlpha += (pixel >> 24) & 0xFF;
	            sumRed   += (pixel >> 16) & 0xFF;
	            sumGreen += (pixel >>  8) & 0xFF;
	            sumBlue  +=  pixel        & 0xFF;
	        }

	        for  (int x = 0; x < width; x++) {
	            dstPixels[dstIndex] = sumLookupTable[sumAlpha] << 24 |
                					  sumLookupTable[sumRed]   << 16 |
                					  sumLookupTable[sumGreen] <<  8 |
                					  sumLookupTable[sumBlue];
	            dstIndex += dst.getScanline();

	            int nextPixelIndex = x + radiusPlusOne;
	            if (nextPixelIndex >= width) {
	                nextPixelIndex = width - 1;
	            }

	            int previousPixelIndex = x - radius;
	            if (previousPixelIndex < 0) {
	                previousPixelIndex = 0;
	            }

	            int nextPixel = srcPixels[srcIndex + nextPixelIndex];
	            int previousPixel = srcPixels[srcIndex + previousPixelIndex];

	            sumAlpha += (nextPixel     >> 24) & 0xFF;
	            sumAlpha -= (previousPixel >> 24) & 0xFF;

	            sumRed += (nextPixel     >> 16) & 0xFF;
	            sumRed -= (previousPixel >> 16) & 0xFF;

	            sumGreen += (nextPixel     >> 8) & 0xFF;
	            sumGreen -= (previousPixel >> 8) & 0xFF;

	            sumBlue += nextPixel & 0xFF;
	            sumBlue -= previousPixel & 0xFF;
	        }

	        srcIndex += src.getScanline();
	    }
	}



    private class TmpData implements PixelData {

        private int[] data;
        private int width;
        private int height;

        private TmpData(int width, int height, int[] data){
            this.width = width;
            this.height = height;
            if (data == null || data.length < (width * height)) {
                data = new int[width * height];
            }
            this.data = data;
        }

        public int[] getData() {
            return data;
        }

        public int getOffset() {
            return 0;
        }

        public int getScanline() {
            return width;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean hasAlpha() {
            return true;
        }

    }

    public static SurfaceOp op(int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException();
        }
        return new Blur(radius);
    }

}
