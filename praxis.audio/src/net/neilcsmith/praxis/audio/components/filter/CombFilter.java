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

package net.neilcsmith.praxis.audio.components.filter;

import net.neilcsmith.praxis.audio.impl.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.impl.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.impl.LinkPort;
import org.jaudiolibs.audioops.impl.CombOp;
import org.jaudiolibs.audioops.impl.ContainerOp;
import org.jaudiolibs.pipes.impl.OpHolder;

/**
 *
 * @author Neil C Smith
 */
public class CombFilter extends AbstractComponent {

    private ContainerOp container;
    private CombOp comb;
    private FloatProperty frequency;
    private FloatProperty feedback;
    private FloatProperty mix;
    private LinkPort<CombFilter> link;

    public CombFilter() {
        comb = new CombOp();
        container = new ContainerOp(comb);
        OpHolder holder = new OpHolder(container);
        registerPort(Port.IN, new DefaultAudioInputPort(this, holder));
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, holder));
        frequency =  FloatProperty.create( new FrequencyBinding(),
                CombOp.MIN_FREQ, CombOp.MAX_FREQ, comb.getFrequency(),
                PMap.create("scale-hint", "Exponential"));
        registerControl("frequency", frequency);
        registerPort("frequency", frequency.createPort());
        feedback = FloatProperty.create( new FeedbackBinding(),
                0, 1, comb.getFeedback());
        registerControl("feedback", feedback);
        registerPort("feedback", feedback.createPort());
        mix = FloatProperty.create( new MixBinding(), 0, 1, 0);
        registerControl("mix", mix);
        registerPort("mix", mix.createPort());
        link = new LinkPort<CombFilter>(CombFilter.class, new LinkHandler(), this);
        registerPort(LinkPort.ID, link);
    }

    private class MixBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            container.setMix((float) value);
            link.fireUpdate();
        }

        public double getBoundValue() {
            return container.getMix();
        }

    }

    private class FrequencyBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            comb.setFrequency((float) value);
            link.fireUpdate();
        }

        public double getBoundValue() {
            return comb.getFrequency();
        }

    }

    private class FeedbackBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            comb.setFeedback((float) value);
            link.fireUpdate();
        }

        public double getBoundValue() {
            return comb.getFeedback();
        }

    }
    
    private class LinkHandler implements LinkPort.Handler<CombFilter> {

        public void update(CombFilter source) {
            comb.setFrequency(source.comb.getFrequency());
            comb.setFeedback(source.comb.getFeedback());
            container.setMix(source.container.getMix());
        }
        
    }

}
