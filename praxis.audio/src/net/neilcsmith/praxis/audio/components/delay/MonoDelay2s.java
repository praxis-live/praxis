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

package net.neilcsmith.praxis.audio.components.delay;

import net.neilcsmith.praxis.audio.impl.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.impl.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.NumberProperty;
import net.neilcsmith.praxis.impl.LinkPort;
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
    private NumberProperty time;
    private NumberProperty feedback;
    private NumberProperty mix;
    private LinkPort<MonoDelay2s> link;

    public MonoDelay2s() {
        delay = new VariableDelayOp(2);
        container = new ContainerOp(delay);
        OpHolder holder = new OpHolder(container);
        registerPort(Port.IN, new DefaultAudioInputPort(this, holder));
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, holder));
        time =  NumberProperty.create( new TimeBinding(),
                0, 2, 0);
        registerControl("time", time);
        registerPort("time", time.createPort());
        feedback = NumberProperty.create( new FeedbackBinding(),
                0, 1, 0);
        registerControl("feedback", feedback);
        registerPort("feedback", feedback.createPort());
        mix = NumberProperty.create( new MixBinding(), 0, 1, 0);
        registerControl("mix", mix);
        registerPort("mix", mix.createPort());
        link = new LinkPort<MonoDelay2s>(MonoDelay2s.class, new LinkHandler(), this);
        registerPort(LinkPort.ID, link);
    }

    private class MixBinding implements NumberProperty.Binding {
        
        public void setBoundValue(long time, double value) {
            container.setMix((float) value);
            link.fireUpdate();
        }

        public double getBoundValue() {
            return container.getMix();
        }

    }

    private class TimeBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            delay.setDelay((float) value);
            link.fireUpdate();
        }

        public double getBoundValue() {
            return delay.getDelay();
        }

    }

    private class FeedbackBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            delay.setFeedback((float) value);
            link.fireUpdate();
        }

        public double getBoundValue() {
            return delay.getFeedback();
        }

    }

    
    private class LinkHandler implements LinkPort.Handler<MonoDelay2s> {

        public void update(MonoDelay2s source) {
            delay.setDelay(source.delay.getDelay());
            delay.setFeedback(source.delay.getFeedback());
            container.setMix(source.container.getMix());
        }
        
    }
    
    
}
