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
package net.neilcsmith.praxis.video.components.test;

import java.awt.Dimension;
import java.awt.Rectangle;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.ArrayProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.TriggerControl;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.praxis.video.pipes.impl.SingleInOut;
import net.neilcsmith.praxis.video.render.PixelData;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import static net.neilcsmith.praxis.video.render.rgbmath.RGBMath.*;

/**
 *
 * @author Neil C Smith
 */
public class DifferenceCalc extends AbstractComponent {

    private static enum Mode {

        Mean, Maximum
    };
    private final static PNumber ZERO = PNumber.valueOf(0);
    private final static PNumber ONE = PNumber.valueOf(1);
    private final static PArray DEFAULTS = PArray.valueOf(new Argument[]{ZERO, ZERO, ONE, ONE});
    private ChangeMeasure changeMeasure;
    private ControlPort.Output diffOutput;

    public DifferenceCalc() {
        changeMeasure = new ChangeMeasure();
        registerPort(Port.IN, new DefaultVideoInputPort(this, changeMeasure));
        registerPort(Port.OUT, new DefaultVideoOutputPort(this, changeMeasure));
        diffOutput = new DefaultControlOutputPort(this);

        TriggerControl trigger = TriggerControl.create(new TriggerBinding());
        registerControl("bounds", ArrayProperty.create(new RegionBinding(), DEFAULTS));
        registerControl("trigger", trigger);
        registerPort("trigger", trigger.createPort());
        registerPort("measurement", diffOutput);



    }

    private class RegionBinding implements ArrayProperty.Binding {

        private PArray value;

        private RegionBinding() {

            value = DEFAULTS;
        }

        public void setBoundValue(long time, PArray value) {
            if (value.getSize() != 4) {
                throw new IllegalArgumentException();
            }
            try {
                double x = PNumber.coerce(value.get(0)).value();
                double y = PNumber.coerce(value.get(1)).value();
                double width = PNumber.coerce(value.get(2)).value();
                double height = PNumber.coerce(value.get(3)).value();
                changeMeasure.setBounds(x, y, width, height);
                this.value = value;
            } catch (ArgumentFormatException ex) {
                throw new IllegalArgumentException(ex);
            }

        }

        public PArray getBoundValue() {
            return value;
        }
    }

    private class TriggerBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            diffOutput.send(time, changeMeasure.getChange());
        }
    }

    public class ChangeMeasure extends SingleInOut implements SurfaceOp {

        private double x;
        private double y;
        private double width;
        private double height;
        private Surface background;
        private double change;
        private Rectangle bounds;
        private Dimension outputDim;
        
        private Mode mode = Mode.Mean;
        private int offsetX;
        private int offsetY;

        public ChangeMeasure() {
            x = 0;
            y = 0;
            width = 1;
            height = 1;
            change = 0;
            outputDim = new Dimension(0, 0);
        }

        @Override
        protected void process(Surface surface, boolean rendering) {

            if (!rendering) {
                return;
            }
            
            if (getSourceCount() == 0) {
                surface.clear();
                if (background != null) {
                    background.release();
                    background = null;
                }
                return;
            }
            if (bounds == null || outputDim.width != surface.getWidth()
                    || outputDim.height != surface.getHeight()) {
                resetBounds(surface);
            }
            if (background == null || bounds.width != background.getWidth()
                    || bounds.height != background.getHeight()
                    || !surface.checkCompatible(background, false, true)) {
                background = surface.createSurface(bounds.width, bounds.height,
                        surface.hasAlpha());
                change = 0;
                return;
            }
//            op.setOffsetX(bounds.x);
//            op.setOffsetY(bounds.y);
            offsetX = bounds.x;
            offsetY = bounds.y;
            surface.process(this, background);
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

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getWidth() {
            return width;
        }

        public double getHeight() {
            return height;
        }

        public void setBounds(double x, double y, double width, double height) {
            if (x < 0 || x > 1 || y < 0 || y > 1
                    || width < 0 || width > 1 || height < 0 || height > 1) {
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
