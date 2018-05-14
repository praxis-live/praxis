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

package org.praxislive.video.impl.components;

import org.praxislive.video.render.PixelData;
import org.praxislive.video.render.SurfaceOp;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
abstract class SWTransform implements SurfaceOp {

    final static SWTransform ROTATE_90 = new Rotate90();
    final static SWTransform ROTATE_180 = new Rotate180();
    final static SWTransform ROTATE_270 = new Rotate270();

    private static class Rotate90 extends SWTransform {

        public void process(PixelData output, PixelData... inputs) {
            PixelData input = inputs[0];
            int inW = input.getWidth();
            int inH = input.getHeight();
            int inDelta = input.getScanline() - inW;
            int inIdx = input.getOffset();
            int[] inData = input.getData();
            int outW = output.getWidth();
            int outH = output.getHeight();
            int outSL = output.getScanline();
            int outOS = output.getOffset();
            int[] outData = output.getData();

            int outIdx = 0;
            for (int y=0; y < inH; y++) {
                for (int x=0; x < inW; x++) {
                    outIdx = outW - y + (x * outSL) + outOS;
                    outData[outIdx] = inData[inIdx];

                    inIdx++;
                }
                inIdx += inDelta;
            }
        }

    }

     private static class Rotate180 extends SWTransform {

        public void process(PixelData output, PixelData... inputs) {
            PixelData input = inputs[0];
            int inW = input.getWidth();
            int inH = input.getHeight();
            int inDelta = input.getScanline() - inW;
            int inIdx = input.getOffset();
            int[] inData = input.getData();
            int outW = output.getWidth();
            int outH = output.getHeight();
            int outSL = output.getScanline();
            int outOS = output.getOffset();
            int[] outData = output.getData();

            int outIdx = 0;
            for (int y=0; y < inH; y++) {
                for (int x=0; x < inW; x++) {
                    outIdx = ((outH - y) * outSL) + (outW - x) + outOS;
                    outData[outIdx] = inData[inIdx];

                    inIdx++;
                }
                inIdx += inDelta;
            }
        }

    }

      private static class Rotate270 extends SWTransform {

        public void process(PixelData output, PixelData... inputs) {
            PixelData input = inputs[0];
            int inW = input.getWidth();
            int inH = input.getHeight();
            int inDelta = input.getScanline() - inW;
            int inIdx = input.getOffset();
            int[] inData = input.getData();
            int outW = output.getWidth();
            int outH = output.getHeight();
            int outSL = output.getScanline();
            int outOS = output.getOffset();
            int[] outData = output.getData();

            int outIdx = 0;
            for (int y=0; y < inH; y++) {
                for (int x=0; x < inW; x++) {
                    //outIdx = outW - y + (x * outSL) + outOS;
                    outIdx = ((outH - x) * outSL) + y + outOS;
                    outData[outIdx] = inData[inIdx];

                    inIdx++;
                }
                inIdx += inDelta;
            }
        }

    }

}
