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
import net.neilcsmith.praxis.impl.FloatProperty;
import org.jaudiolibs.audioops.impl.ChorusOp;
import org.jaudiolibs.audioops.impl.ContainerOp;
import org.jaudiolibs.pipes.impl.OpHolder;

/**
 *
 * @author Neil C Smith
 */
public class MonoChorus extends AbstractComponent {

    private ChorusOp chorus;
    private ContainerOp container;
    private FloatProperty depth;
    private FloatProperty rate;
//    private FloatProperty phase;
    private FloatProperty feedback;
    private FloatProperty mix;

    public MonoChorus() {
        chorus = new ChorusOp();
        chorus.setDepth(1.9f);
        chorus.setRate(0.4f);
        container = new ContainerOp(chorus);
        OpHolder holder = new OpHolder(container);
        registerPort(Port.IN, new DefaultAudioInputPort(this, holder));
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, holder));
        depth =  FloatProperty.create( new DepthBinding(),
                1, 40, 1);
        registerControl("depth", depth);
        registerPort("depth", depth.createPort());
        rate = FloatProperty.create(new RateBinding(), 0, 15, 0);
        registerControl("rate", rate);
        registerPort("rate", rate.createPort());
        feedback = FloatProperty.create( new FeedbackBinding(),
                0, 1, 0);
        
        registerControl("feedback", feedback);
        registerPort("feedback", feedback.createPort());
//        phase = FloatProperty.create(new PhaseBinding(), 0, 1, 0);
//        registerControl("phase", phase);
        mix = FloatProperty.create( new MixBinding(), 0, 1, 0);
        registerControl("mix", mix);
        registerPort("mix", mix.createPort());
        
    }

    private class MixBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            container.setMix((float) value);
        }

        public double getBoundValue() {
            return container.getMix();
        }

    }

    private class DepthBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            chorus.setDepth((float)value);
        }

        public double getBoundValue() {
            return chorus.getDepth();
        }

    }
    
    private class RateBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            chorus.setRate((float) value);
        }

        public double getBoundValue() {
            return chorus.getRate();
        }
        
    }
    
    private class PhaseBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            chorus.setPhase((float)value);
        }

        public double getBoundValue() {
            return chorus.getPhase();
        }
        
    }

    private class FeedbackBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            chorus.setFeedback((float) value);
        }

        public double getBoundValue() {
            return chorus.getFeedback();
        }

    }

}
