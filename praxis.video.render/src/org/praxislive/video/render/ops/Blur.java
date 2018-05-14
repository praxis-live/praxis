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
 *
 *
 *
 *
 *
 * Partially derived from code by Florent Dupont
 *
 * /*
 * Copyright (c) 2008, Florent Dupont
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of the PulpCore project nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.praxislive.video.render.ops;

import java.util.logging.Logger;
import org.praxislive.video.render.PixelData;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.utils.PixelArrayCache;


/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Blur implements SurfaceOp {

    // private final static Logger LOG = Logger.getLogger(Blur.class.getName());
    
    private int xRadius;
    private int yRadius;
    private TempData tmp;

    private Blur(int radius) {
        this.xRadius = radius;
        this.yRadius = radius;
    }

    public void process(PixelData output, PixelData... inputs) {
        int dstH = output.getWidth();
        int dstW = output.getHeight();
        if (tmp == null || tmp.getWidth() != dstW || tmp.getHeight() != dstH) {
            tmp = TempData.create(dstW, dstH, true);
        }
        blur(output, tmp, xRadius);
        blur(tmp, output, yRadius);
        tmp.release();
    }

    private void blur(PixelData src, PixelData dst, int radius) {

	    final int windowSize = radius * 2 + 1;
	    final int radiusPlusOne = radius + 1;

	    int sumAlpha;
	    int sumRed;
	    int sumGreen;
	    int sumBlue;

	    int srcIndex = src.getOffset();
            int srcSL = src.getScanline();
            int dstSL = dst.getScanline();
	    int dstIndex;
	    int pixel;

//	    int[] sumLookupTable = new int[256 * windowSize];
//	    for (int i = 0; i < sumLookupTable.length; i++) {
//	        sumLookupTable[i] = i / windowSize;
//	    }
            int lookupSize = 256 * windowSize;
            int[] sumLookupTable = PixelArrayCache.acquire(lookupSize, false);
            for (int i=0; i < lookupSize; i++) {
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
	            dstIndex += dstSL;

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

	        srcIndex += srcSL;
	    }

            PixelArrayCache.release(sumLookupTable);
	}

    public static SurfaceOp op(int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException();
        }
        return new Blur(radius);
    }

}
