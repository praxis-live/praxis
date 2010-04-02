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
package net.neilcsmith.ripl.components.temporal;

//import net.neilcsmith.ripl.core.PixelData;
import net.neilcsmith.ripl.core.Surface;
import net.neilcsmith.ripl.core.SurfaceCapabilities;
//import net.neilcsmith.ripl.core.SurfaceOp;
import net.neilcsmith.ripl.core.impl.SingleInOut;
import net.neilcsmith.ripl.ops.DifferenceOp;
import static net.neilcsmith.ripl.rgbmath.RGBMath.*;

/**
 *
 * @author Neil C Smith
 * @TODO implement as Op
 */
public class Difference extends SingleInOut {

    public static enum Mode {

        Color, Mono, Threshold
    };

    private final static SurfaceCapabilities CAPS = new SurfaceCapabilities(true);
    private Surface lastFrame;
    private DifferenceOp op;

    public Difference() {
        op = new DifferenceOp();
    }

    public void setMode(Mode mode) {
        switch (mode) {
            case Color :
                op.setMode(DifferenceOp.Mode.Color);
                break;
            case Mono :
                op.setMode(DifferenceOp.Mode.Mono);
                break;
            case Threshold :
                op.setMode(DifferenceOp.Mode.Threshold);
                break;
        }
    }

    public Mode getMode() {
        DifferenceOp.Mode opMode = op.getMode();
        switch (opMode) {
            case Color :
                return Mode.Color;
            case Mono :
                return Mode.Mono;
            case Threshold :
                return Mode.Threshold;
            default :
                return Mode.Color; // shouldn't be possible
        }
    }
    
    public void setThreshold(double threshold) {
        op.setThreshold(threshold);
    }

    public double getThreshold() {
        return op.getThreshold();
    }

    @Override
    protected void process(Surface surface, boolean rendering) {
        if (rendering) {
            if (getSourceCount() == 0) {
                surface.clear();
                if (lastFrame != null) {
                    lastFrame.release();
                    lastFrame = null;
                }
                return;
            }
            if (lastFrame == null || !surface.checkCompatible(lastFrame, true, true)) {
                lastFrame = surface.createSurface(CAPS);
                surface.process(op, lastFrame);
                surface.clear(); // output will be garbage
            } else {
                surface.process(op, lastFrame);
            }
        }
    }

//    public static class Op implements SurfaceOp {
//
//        private double threshold;
//        private Mode mode;
//
//        public Op() {
//            this.threshold = 0;
//            this.mode = Mode.Color;
//        }
//
//        public void process(PixelData output, PixelData... inputs) {
//            if (inputs.length < 1) {
//                return;
//            }
//            switch (mode) {
//                case Color:
//                    processColor(output, inputs[0]);
//                    break;
//                case Mono:
//                    processMono(output, inputs[0]);
//                    break;
//                case Threshold:
//                    processThreshold(output, inputs[0]);
//                    break;
//            }
//
//        }
//
//        public void setThreshold(double threshold) {
//            if (threshold < 0 || threshold > 1) {
//                throw new IllegalArgumentException();
//            }
//
//            this.threshold = threshold;
//        }
//
//        public double getThreshold() {
//            return threshold;
//        }
//
//        public void setMode(Mode mode) {
//            if (mode == null) {
//                throw new NullPointerException();
//            }
//            this.mode = mode;
//        }
//
//        public Mode getMode() {
//            return mode;
//        }
//
//        private void processColor(PixelData output, PixelData input) {
//            int thres = (int) Math.round(threshold * 256);
//            int width = Math.min(input.getWidth(), output.getWidth());
//            int height = Math.min(input.getHeight(), output.getHeight());
//            int bgDelta = input.getScanline() - width;
//            int fgDelta = output.getScanline() - width;
//            int[] bgData = input.getData();
//            int[] fgData = output.getData();
//            int bgIdx = 0;
//            int fgIdx = 0;
//
//            int fg, fgR, fgG, fgB;
//            int bg, bgR, bgG, bgB;
//            int maxDelta;
//
//            for (int y = 0; y < height; y++) {
//                for (int x = 0; x < width; x++) {
//                    fg = fgData[fgIdx];
//                    fgR = (fg & RED_MASK) >>> 16;
//                    fgG = (fg & GREEN_MASK) >>> 8;
//                    fgB = fg & BLUE_MASK;
//                    bg = bgData[bgIdx];
//                    bgR = (bg & RED_MASK) >>> 16;
//                    bgG = (bg & GREEN_MASK) >>> 8;
//                    bgB = bg & BLUE_MASK;
//
//                    fgR = diff(fgR, bgR);
//                    fgG = diff(fgG, bgG);
//                    fgB = diff(fgB, bgB);
//
//                    maxDelta = max(fgR, fgG, fgB);
//                    if (maxDelta < thres) {
//                        fgData[fgIdx] = 0;
//                    } else {
//                        fgData[fgIdx] = fg & ALPHA_MASK |
//                                (fgR << 16) | (fgG << 8) | fgB;
//                    }
//                    bgData[bgIdx] = fg;
//
//                    bgIdx++;
//                    fgIdx++;
//                }
//                bgIdx += bgDelta;
//                fgIdx += fgDelta;
//            }
//
//        }
//
//        private void processMono(PixelData output, PixelData input) {
//            int thres = (int) Math.round(threshold * 256);
//            int width = Math.min(input.getWidth(), output.getWidth());
//            int height = Math.min(input.getHeight(), output.getHeight());
//            int bgDelta = input.getScanline() - width;
//            int fgDelta = output.getScanline() - width;
//            int[] bgData = input.getData();
//            int[] fgData = output.getData();
//            int bgIdx = 0;
//            int fgIdx = 0;
//
//            int fg, fgR, fgG, fgB;
//            int bg, bgR, bgG, bgB;
//            int maxDelta;
//
//            for (int y = 0; y < height; y++) {
//                for (int x = 0; x < width; x++) {
//                    fg = fgData[fgIdx];
//                    fgR = (fg & RED_MASK) >>> 16;
//                    fgG = (fg & GREEN_MASK) >>> 8;
//                    fgB = fg & BLUE_MASK;
//                    bg = bgData[bgIdx];
//                    bgR = (bg & RED_MASK) >>> 16;
//                    bgG = (bg & GREEN_MASK) >>> 8;
//                    bgB = bg & BLUE_MASK;
//
//                    fgR = diff(fgR, bgR);
//                    fgG = diff(fgG, bgG);
//                    fgB = diff(fgB, bgB);
//
//                    maxDelta = max(fgR, fgG, fgB);
//                    if (maxDelta < thres) {
//                        fgData[fgIdx] = 0;
//                    } else {
//                        fgData[fgIdx] = fg & ALPHA_MASK |
//                                (maxDelta << 16) | (maxDelta << 8) | maxDelta;
//                    }
//                    bgData[bgIdx] = fg;
//
//                    bgIdx++;
//                    fgIdx++;
//                }
//                bgIdx += bgDelta;
//                fgIdx += fgDelta;
//            }
//
//        }
//
//        private void processThreshold(PixelData output, PixelData input) {
//            int thres = (int) Math.round(threshold * 256);
//            int width = Math.min(input.getWidth(), output.getWidth());
//            int height = Math.min(input.getHeight(), output.getHeight());
//            int bgDelta = input.getScanline() - width;
//            int fgDelta = output.getScanline() - width;
//            int[] bgData = input.getData();
//            int[] fgData = output.getData();
//            int bgIdx = 0;
//            int fgIdx = 0;
//
//            int fg, fgR, fgG, fgB;
//            int bg, bgR, bgG, bgB;
//            int maxDelta;
//
//            boolean alpha = output.hasAlpha();
//
//            for (int y = 0; y < height; y++) {
//                for (int x = 0; x < width; x++) {
//                    fg = fgData[fgIdx];
//                    fgR = (fg & RED_MASK) >>> 16;
//                    fgG = (fg & GREEN_MASK) >>> 8;
//                    fgB = fg & BLUE_MASK;
//                    bg = bgData[bgIdx];
//                    bgR = (bg & RED_MASK) >>> 16;
//                    bgG = (bg & GREEN_MASK) >>> 8;
//                    bgB = bg & BLUE_MASK;
//
//                    fgR = diff(fgR, bgR);
//                    fgG = diff(fgG, bgG);
//                    fgB = diff(fgB, bgB);
//
//                    maxDelta = max(fgR, fgG, fgB);
//                    if (maxDelta < thres) {
//                        fgData[fgIdx] = 0;
//                    } else {
//                        fgData[fgIdx] = alpha ? 0xFFFFFFFF : 0xFFFFFF;
//                    }
//                    bgData[bgIdx] = fg;
//
//                    bgIdx++;
//                    fgIdx++;
//                }
//                bgIdx += bgDelta;
//                fgIdx += fgDelta;
//            }
//
//        }
//    }
}
