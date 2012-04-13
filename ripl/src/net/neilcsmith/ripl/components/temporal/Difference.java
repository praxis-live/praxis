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

//import net.neilcsmith.ripl.core.PixelData;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.SurfaceCapabilities;
//import net.neilcsmith.ripl.core.SurfaceOp;
import net.neilcsmith.ripl.impl.SingleInOut;
import net.neilcsmith.ripl.ops.DifferenceOp;

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
                Surface tmp = lastFrame.createSurface(null);
                tmp.copy(surface);
                surface.process(op, lastFrame);
                lastFrame.copy(tmp);
                tmp.release();
            }
        }
    }


}
