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
package org.praxislive.video.render.ops;


import org.praxislive.video.render.PixelData;
import org.praxislive.video.render.SurfaceOp;
import static org.praxislive.video.render.utils.RGBMath.*;

/**
 *
 * @author Neil C Smith
 */
public class DifferenceOp implements SurfaceOp {
    
    public static enum Mode {Color, Mono, Threshold};

    private double threshold;
    private Mode mode;

    public DifferenceOp() {
        this.threshold = 0;
        this.mode = Mode.Color;
    }

    public void process(PixelData output, PixelData... inputs) {
        if (inputs.length < 1) {
            return;
        }
        switch (mode) {
            case Color :
                processColor(output, inputs[0]);
                break;
            case Mono :
                processMono(output, inputs[0]);
                break;
            case Threshold :
                processThreshold(output, inputs[0]);
                break;
        }

    }

    public void setThreshold(double threshold) {
        if (threshold < 0 || threshold > 1) {
            throw new IllegalArgumentException();
        }

        this.threshold = threshold;
    }

    public double getThreshold() {
        return threshold;
    }
    
    public void setMode(Mode mode) {
        if (mode == null) {
            throw new NullPointerException();
        }
        this.mode = mode;
    } 

    public Mode getMode() {
        return mode;
    }

    private void processColor(PixelData output, PixelData input) {
        int thres = (int) Math.round(threshold * 256);
        int width = Math.min(input.getWidth(), output.getWidth());
        int height = Math.min(input.getHeight(), output.getHeight());
        int bgDelta = input.getScanline() - width;
        int fgDelta = output.getScanline() - width;
        int[] bgData = input.getData();
        int[] fgData = output.getData();
        int bgIdx = 0;
        int fgIdx = 0;

        int fg, fgR, fgG, fgB;
        int bg, bgR, bgG, bgB;
        int maxDelta;
        
        for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    fg = fgData[fgIdx];
                    fgR = (fg & RED_MASK) >>> 16;
                    fgG = (fg & GREEN_MASK) >>> 8;
                    fgB = fg & BLUE_MASK;
                    bg = bgData[bgIdx];
                    bgR = (bg & RED_MASK) >>> 16;
                    bgG = (bg & GREEN_MASK) >>> 8;
                    bgB = bg & BLUE_MASK;

                    fgR = diff(fgR, bgR);
                    fgG = diff(fgG, bgG);
                    fgB = diff(fgB, bgB);

                    maxDelta = max(fgR, fgG, fgB);
                    if (maxDelta < thres) {
                        fgData[fgIdx] = 0;
                    } else {
                        fgData[fgIdx] = fg & ALPHA_MASK |
                                (fgR << 16) | (fgG << 8) | fgB;
                    }
//                    bgData[bgIdx] = fg;

                    bgIdx++;
                    fgIdx++;
                }
                bgIdx += bgDelta;
                fgIdx += fgDelta;
            }
        
    }

    private void processMono(PixelData output, PixelData input) {
        int thres = (int) Math.round(threshold * 256);
        int width = Math.min(input.getWidth(), output.getWidth());
        int height = Math.min(input.getHeight(), output.getHeight());
        int bgDelta = input.getScanline() - width;
        int fgDelta = output.getScanline() - width;
        int[] bgData = input.getData();
        int[] fgData = output.getData();
        int bgIdx = 0;
        int fgIdx = 0;

        int fg, fgR, fgG, fgB;
        int bg, bgR, bgG, bgB;
        int maxDelta;
        
        for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    fg = fgData[fgIdx];
                    fgR = (fg & RED_MASK) >>> 16;
                    fgG = (fg & GREEN_MASK) >>> 8;
                    fgB = fg & BLUE_MASK;
                    bg = bgData[bgIdx];
                    bgR = (bg & RED_MASK) >>> 16;
                    bgG = (bg & GREEN_MASK) >>> 8;
                    bgB = bg & BLUE_MASK;

                    fgR = diff(fgR, bgR);
                    fgG = diff(fgG, bgG);
                    fgB = diff(fgB, bgB);

                    maxDelta = max(fgR, fgG, fgB);
                    if (maxDelta < thres) {
                        fgData[fgIdx] = 0;
                    } else {
                        fgData[fgIdx] = fg & ALPHA_MASK |
                                (maxDelta << 16) | (maxDelta << 8) | maxDelta;
                    }
//                    bgData[bgIdx] = fg;

                    bgIdx++;
                    fgIdx++;
                }
                bgIdx += bgDelta;
                fgIdx += fgDelta;
            }
        
    }

    private void processThreshold(PixelData output, PixelData input) {
        int thres = (int) Math.round(threshold * 256);
        int width = Math.min(input.getWidth(), output.getWidth());
        int height = Math.min(input.getHeight(), output.getHeight());
        int bgDelta = input.getScanline() - width;
        int fgDelta = output.getScanline() - width;
        int[] bgData = input.getData();
        int[] fgData = output.getData();
        int bgIdx = 0;
        int fgIdx = 0;

        int fg, fgR, fgG, fgB;
        int bg, bgR, bgG, bgB;
        int maxDelta;
        
        boolean alpha = output.hasAlpha();
        
        for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    fg = fgData[fgIdx];
                    fgR = (fg & RED_MASK) >>> 16;
                    fgG = (fg & GREEN_MASK) >>> 8;
                    fgB = fg & BLUE_MASK;
                    bg = bgData[bgIdx];
                    bgR = (bg & RED_MASK) >>> 16;
                    bgG = (bg & GREEN_MASK) >>> 8;
                    bgB = bg & BLUE_MASK;

                    fgR = diff(fgR, bgR);
                    fgG = diff(fgG, bgG);
                    fgB = diff(fgB, bgB);

                    maxDelta = max(fgR, fgG, fgB);
                    if (maxDelta < thres) {
                        fgData[fgIdx] = 0;
                    } else {
                        fgData[fgIdx] = alpha ? 0xFFFFFFFF : 0xFFFFFF;
                    }
//                    bgData[bgIdx] = fg;

                    bgIdx++;
                    fgIdx++;
                }
                bgIdx += bgDelta;
                fgIdx += fgDelta;
            }
        
    }
}
