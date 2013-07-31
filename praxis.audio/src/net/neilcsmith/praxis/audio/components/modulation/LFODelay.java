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
 *
 */
package net.neilcsmith.praxis.audio.components.modulation;

import net.neilcsmith.praxis.audio.impl.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.impl.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.NumberProperty;
import org.jaudiolibs.audioops.impl.ContainerOp;
import org.jaudiolibs.audioops.impl.LFODelayOp;
import org.jaudiolibs.pipes.impl.OpHolder;

/**
 *
 * @author Neil C Smith
 */
public class LFODelay extends AbstractComponent {

    private LFODelayOp op;
    private ContainerOp container;
    private NumberProperty delay;
    private NumberProperty range;
    private NumberProperty rate;
//    private FloatProperty phase;
    private NumberProperty feedback;
    private NumberProperty mix;

    public LFODelay() {
        op = new LFODelayOp();
        container = new ContainerOp(op);
        OpHolder holder = new OpHolder(container);
        registerPort(Port.IN, new DefaultAudioInputPort(this, holder));
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, holder));
        delay = NumberProperty.create(new DelayBinding(), 0, 1, 0);
        registerControl("time", delay);
        registerPort("time", delay.createPort());
        range = NumberProperty.create(new RangeBinding(), 0, 1, 0);
        registerControl("range", range);
        registerPort("range", range.createPort());
        rate = NumberProperty.create(new RateBinding(), 0, 40, 0);
        registerControl("rate", rate);
        registerPort("rate", rate.createPort());
        feedback = NumberProperty.create(new FeedbackBinding(),
                0, 1, 0);
        registerControl("feedback", feedback);
        registerPort("feedback", feedback.createPort());
//        phase = FloatProperty.create(new PhaseBinding(), 0, 1, 0);
//        registerControl("phase", phase);
        mix = NumberProperty.create(new MixBinding(), 0, 1, 0);
        registerControl("mix", mix);
        registerPort("mix", mix.createPort());

    }

    private class MixBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            container.setMix((float) value);
        }

        public double getBoundValue() {
            return container.getMix();
        }
    }

    private class DelayBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            op.setDelay((float) value);
        }

        public double getBoundValue() {
            return op.getDelay();
        }
    }

    private class RangeBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            op.setRange((float) value);
        }

        public double getBoundValue() {
            return op.getRange();
        }
    }

    private class RateBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            op.setRate((float) value);
        }

        public double getBoundValue() {
            return op.getRate();
        }
    }

//    private class PhaseBinding implements FloatProperty.Binding {
//
//        public void setBoundValue(long time, double value) {
//            op.setPhase((float)value);
//        }
//
//        public double getBoundValue() {
//            return op.getPhase();
//        }
//        
//    }
    private class FeedbackBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            op.setFeedback((float) value);
        }

        public double getBoundValue() {
            return op.getFeedback();
        }
    }
}
