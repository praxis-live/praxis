/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008/09 - Neil C Smith. All rights reserved.
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

import java.awt.Dimension;
import java.awt.Rectangle;
import net.neilcsmith.ripl.core.PixelData;
import net.neilcsmith.ripl.core.Surface;
import net.neilcsmith.ripl.core.SurfaceCapabilities;
import net.neilcsmith.ripl.core.SurfaceOp;
import net.neilcsmith.ripl.core.impl.SingleInOut;
import static net.neilcsmith.ripl.rgbmath.RGBMath.*;


/**
 *
 * @author Neil C Smith
 * @TODO implement Reset()
 */
public class ChangeMeasure extends SingleInOut {

    public static enum Mode {
        Mean, Maximum
    };
    
    private final static SurfaceCapabilities CAPS = new SurfaceCapabilities(true);
    
    private Op op;
    private double x;
    private double y;
    private double width;
    private double height;
    private Surface background;
    private double change;
    private Rectangle bounds;
    private Dimension outputDim;

    public ChangeMeasure() {
        op = new Op();
        x = 0;
        y = 0;
        width = 1;
        height = 1;
        change = 0;
        outputDim = new Dimension(0, 0);
    }

    @Override
    protected void process(Surface surface, boolean rendering) {
        if (rendering) {
            if (getSourceCount() == 0) {
                surface.clear();
                if (background != null) {
                    background.release();
                    background = null;
                }
                return;
            }
            if (bounds == null || outputDim.width != surface.getWidth() ||
                    outputDim.height != surface.getHeight()) {
                resetBounds(surface);
            }
            if (background == null || bounds.width != background.getWidth() ||
                    bounds.height != background.getHeight() || 
                    !surface.checkCompatible(background, false, true)) {
                background = surface.createSurface(bounds.width, bounds.height,
                        surface.hasAlpha(), CAPS);
                change = 0;
                return;
            }
            op.setOffsetX(bounds.x);
            op.setOffsetY(bounds.y);
            surface.process(op, background);
            change = op.getChange();
        }
    }

    private void resetBounds(Surface surface) {
        int outWidth = surface.getWidth();
        int outHeight = surface.getHeight();
        int iX = (int) (x * outWidth);
        int iY = (int) (y * outHeight);
        int iWidth = (int) (width * outWidth);
        int iHeight = (int) (height * outHeight);
        if (iX >= outWidth) {
            iX = outWidth - 1;
        }
        if (iY >= outHeight) {
            iY = outHeight - 1;
        }
        if (iWidth <= 0) {
            iWidth = 1;
        }
        if (iHeight <= 0) {
            iHeight = 1;
        }
        outputDim.width = outWidth;
        outputDim.height = outHeight;
        bounds = new Rectangle(iX, iY, iWidth, iHeight);
    }

//    public void setX(double x) {
//        if (x < 0 || x > 1) {
//            throw new IllegalArgumentException();
//        }
//        this.x = x;
//        bounds = null;
//    }
//
    public double getX() {
        return x;
    }
//
//    public void setY(double y) {
//        if (y < 0 || y > 1) {
//            throw new IllegalArgumentException();
//        }
//        this.y = y;
//        bounds = null;
//    }
//
    public double getY() {
        return y;
    }
//
//    public void setWidth(double width) {
//        if (width < 0 || width > 1) {
//            throw new IllegalArgumentException();
//        }
//        this.width = width;
//        bounds = null;
//    }
//
    public double getWidth() {
        return width;
    }
//
//    public void setHeight(double height) {
//        if (height < 0 || height > 1) {
//            throw new IllegalArgumentException();
//        }
//        this.height = height;
//        bounds = null;
//    }
//
    public double getHeight() {
        return height;
    }

    public void setBounds(double x, double y, double width, double height) {
        if (x < 0 || x > 1 || y < 0 || y > 1 ||
                width < 0 || width > 1 || height < 0 || height > 1) {
            throw new IllegalArgumentException();
        }
        if ((x + width) > 1) {
            width = 1 - x;
        }
        if ((y + height) > 1) {
            height = 1 - y;
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        bounds = null;
    }
    
    
    public double getChange() {
        return change;
    }

    public static class Op implements SurfaceOp {

        private Mode mode;
        private double change;
        private int offsetX;
        private int offsetY;

        public Op() {
            mode = Mode.Mean;
        }
        
        public void setChange(double change) {
            if (change < 0 || change > 1) {
                throw new IllegalArgumentException();
            }
            this.change = change;
        }
        
        public double getChange() {
            return change;
        }

        public void setOffsetX(int x) {
            if (x < 0) {
                throw new IllegalArgumentException();
            }
            this.offsetX = x;
        }
        
        public int getOffsetX() {
            return offsetX;
        }
        
        public void setOffsetY(int y) {
            if (y < 0) {
                throw new IllegalArgumentException();
            }
            this.offsetY = y;
        }
        
        public int getOffsetY() {
            return offsetY;
        }
        
        public void process(PixelData output, PixelData... inputs) {
            if (inputs.length < 1) {
                change = 0;
                return;
            }
            PixelData input = inputs[0];
            int width = (Math.min(output.getWidth(),
                    input.getWidth() + offsetX)) - offsetX;
            int height = (Math.min(output.getHeight(),
                    input.getHeight() + offsetY)) - offsetY;
            int bgDelta = input.getScanline() - width;
            int fgDelta = output.getScanline() - width;
            int[] bgData = input.getData();
            int[] fgData = output.getData();
            int bgIdx = 0;
            int fgIdx = (offsetY * output.getScanline()) + offsetX;
            
            int fg, fgR, fgG, fgB;
            int bg, bgR, bgG, bgB;
            int maxDelta;
            
            double value = 0;
            
            if (mode == Mode.Mean) {
                
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

                        value += max(fgR, fgG, fgB);
                        
                        bgData[bgIdx] = fg;
//                        fgData[fgIdx] = RED_MASK;

                        bgIdx++;
                        fgIdx++;
                    }
                    bgIdx += bgDelta;
                    fgIdx += fgDelta;
                }
                
                change = value / (width * height) / 255.0;
                
            } else {
                
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
                        if (maxDelta > value) {
                            value = maxDelta;
                        }
                        
                        bgData[bgIdx] = fg;
//                        fgData[fgIdx] = RED_MASK;

                        bgIdx++;
                        fgIdx++;
                    }
                    bgIdx += bgDelta;
                    fgIdx += fgDelta;
                }
                
                change = value / 255.0;
                
            }
        }
    }
}
