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

import net.neilcsmith.praxis.video.components.test.*;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.NumberProperty;
import net.neilcsmith.praxis.impl.StringProperty;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.praxis.video.pipes.impl.SingleInOut;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.ops.DifferenceOp;

/**
 *
 * @author Neil C Smith
 */
public class FrameDifference extends AbstractComponent {

    private static enum Mode {

        Color, Mono, Threshold
    };
    private Difference diff;

    public FrameDifference() {
        diff = new Difference();
        registerPort(Port.IN, new DefaultVideoInputPort(this, diff));
        registerPort(Port.OUT, new DefaultVideoOutputPort(this, diff));
        StringProperty mode = StringProperty.create(new ModeBinding(),
                getModeStrings(), diff.getMode().name());
        registerControl("mode", mode);
        NumberProperty threshold = NumberProperty.create(new ThresholdBinding(), 0, 1, 0);
        registerControl("threshold", threshold);
        registerPort("threshold", threshold.createPort());
    }

    private String[] getModeStrings() {
        Mode[] modes = Mode.values();
        String[] strings = new String[modes.length];
        for (int i = 0; i < modes.length; i++) {
            strings[i] = modes[i].name();
        }
        return strings;
    }

    private class ModeBinding implements StringProperty.Binding {

        public void setBoundValue(long time, String value) {
            diff.setMode(Mode.valueOf(value));
        }

        public String getBoundValue() {
            return diff.getMode().name();
        }
    }

    private class ThresholdBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            diff.setThreshold(value);
        }

        public double getBoundValue() {
            return diff.getThreshold();
        }
    }

    private class Difference extends SingleInOut {

        private Surface lastFrame;
        private DifferenceOp op;

        public Difference() {
            op = new DifferenceOp();
        }

        public void setMode(Mode mode) {
            switch (mode) {
                case Color:
                    op.setMode(DifferenceOp.Mode.Color);
                    break;
                case Mono:
                    op.setMode(DifferenceOp.Mode.Mono);
                    break;
                case Threshold:
                    op.setMode(DifferenceOp.Mode.Threshold);
                    break;
            }
        }

        public Mode getMode() {
            DifferenceOp.Mode opMode = op.getMode();
            switch (opMode) {
                case Color:
                    return Mode.Color;
                case Mono:
                    return Mode.Mono;
                case Threshold:
                    return Mode.Threshold;
                default:
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
            if (!rendering) {
                return;
            }

            if (getSourceCount() == 0) {
                surface.clear();
                if (lastFrame != null) {
                    lastFrame.release();
                    lastFrame = null;
                }
                return;
            }
            if (lastFrame == null || !surface.checkCompatible(lastFrame, true, true)) {
                lastFrame = surface.createSurface();
                surface.process(op, lastFrame);
                surface.clear(); // output will be garbage
            } else {
                Surface tmp = lastFrame.createSurface();
                tmp.copy(surface);
                surface.process(op, lastFrame);
                lastFrame.copy(tmp);
                tmp.release();
            }

        }
    }
}
