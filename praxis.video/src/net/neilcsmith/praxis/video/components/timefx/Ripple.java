/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 Neil C Smith.
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
package net.neilcsmith.praxis.video.components.timefx;

import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.praxis.video.pipes.impl.MultiInOut;
import net.neilcsmith.praxis.video.pipes.impl.Placeholder;
import net.neilcsmith.praxis.video.render.PixelData;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.rgbmath.RGBMath;

/**
 *
 * @author Neil C Smith
 */
public class Ripple extends AbstractComponent {

    private RipplePipe rip;
    private Placeholder input;
    private Placeholder disturbance;

    public Ripple() {
//        try {
        rip = new RipplePipe();
        input = new Placeholder();
        disturbance = new Placeholder();
        rip.addSource(input);
        rip.addSource(disturbance);
        registerPort(Port.IN, new DefaultVideoInputPort(this, input));
        registerPort("disturbance", new DefaultVideoInputPort(this, disturbance));
        registerPort(Port.OUT, new DefaultVideoOutputPort(this, rip));
//        } catch (SinkIsFullException ex) {
//            Logger.getLogger(Ripple.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (SourceIsFullException ex) {
//            Logger.getLogger(Ripple.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    class RipplePipe extends MultiInOut implements SurfaceOp {

        private int[] currentMap;
        private int[] previousMap;
//    private int damp = 16;
        private int rad = 256;

        public RipplePipe() {
            super(2, 1);
            currentMap = new int[0];
            previousMap = new int[0];
        }

        @Override
        protected void process(Surface[] inputs, Surface surface, int index, boolean rendering) {
            if (!rendering) {
                return;
            }
            if (inputs.length == 0) {
                surface.clear();
            } else if (inputs.length == 1) {
                surface.copy(inputs[0]);
            } else {
                surface.process(this, inputs[0], inputs[1]);
            }
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
            for (int y = 1, endY = (height - 1); y < endY; y++) {
                out[outOffset] = in[y * inSL];
                index = (y * width) + 1;
                for (int x = 1, endX = (width - 1); x < endX; x++) {
                    // calculate ripples and disturbance
                    data = (previousMap[index - width]
                            + previousMap[index + width]
                            + previousMap[index - 1]
                            + previousMap[index + 1]) / 2;
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
        }

        private int clamp(int input, int max) {
            return (input < 0) ? 0 : (input >= max) ? max - 1 : input;
        }
    }
}
