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
 *
 */

package net.neilcsmith.praxis.audio.components.time;

import net.neilcsmith.praxis.audio.impl.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.impl.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.FloatProperty;
import org.jaudiolibs.audioops.impl.ContainerOp;
import org.jaudiolibs.audioops.impl.VariableDelayOp;
import org.jaudiolibs.pipes.impl.OpHolder;

/**
 *
 * @author Neil C Smith
 */
public class MonoDelay2s extends AbstractComponent {

    private VariableDelayOp delay;
    private ContainerOp container;
    private FloatProperty time;
    private FloatProperty feedback;
    private FloatProperty mix;

    public MonoDelay2s() {
        delay = new VariableDelayOp(2);
        container = new ContainerOp(delay);
        OpHolder holder = new OpHolder(container);
        registerPort(Port.IN, new DefaultAudioInputPort(this, holder));
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, holder));
        time =  FloatProperty.create( new TimeBinding(),
                0, 2, 0);
        registerControl("time", time);
        registerPort("time", time.createPort());
        feedback = FloatProperty.create( new FeedbackBinding(),
                0, 1, 0);
        registerControl("feedback", feedback);
        registerPort("feedback", feedback.createPort());
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

    private class TimeBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            delay.setDelay((float) value);
        }

        public double getBoundValue() {
            return delay.getDelay();
        }

    }

    private class FeedbackBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            delay.setFeedback((float) value);
        }

        public double getBoundValue() {
            return delay.getFeedback();
        }

    }

}
