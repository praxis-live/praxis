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
import net.neilcsmith.rapl.components.filters.Comb;

/**
 *
 * @author Neil C Smith
 */
public class CombFilter extends AbstractComponent {

    private Comb comb;
    private FloatProperty frequency;
    private FloatProperty feedback;
    private FloatProperty mix;

    public CombFilter() {
        comb = new Comb();
        frequency =  FloatProperty.create( new FrequencyBinding(),
                Comb.MIN_FREQ, Comb.MAX_FREQ, comb.getFrequency(),
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
        registerPort(Port.IN, new DefaultAudioInputPort(this, comb));
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, comb));
    }

    private class MixBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            comb.setMix((float) value);
        }

        public double getBoundValue() {
            return comb.getMix();
        }

    }

    private class FrequencyBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            comb.setFrequency((float) value);
        }

        public double getBoundValue() {
            return comb.getFrequency();
        }

    }

    private class FeedbackBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            comb.setFeedback((float) value);
        }

        public double getBoundValue() {
            return comb.getFeedback();
        }

    }

}
