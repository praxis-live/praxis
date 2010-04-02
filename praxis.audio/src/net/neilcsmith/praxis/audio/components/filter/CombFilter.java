/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Neil C Smith. All rights reserved.
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
 *
 */

package net.neilcsmith.praxis.audio.components.filter;

import net.neilcsmith.praxis.audio.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.DefaultAudioOutputPort;
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
        frequency =  FloatProperty.create(this, new FrequencyBinding(),
                Comb.MIN_FREQ, Comb.MAX_FREQ, comb.getFrequency(),
                PMap.valueOf("scale-hint", "Exponential"));
        registerControl("frequency", frequency);
        registerPort("frequency", frequency.createPort());
        feedback = FloatProperty.create(this, new FeedbackBinding(),
                0, 1, comb.getFeedback());
        registerControl("feedback", feedback);
        registerPort("feedback", feedback.createPort());
        mix = FloatProperty.create(this, new MixBinding(), 0, 1, 0);
        registerControl("mix", mix);
        registerPort("mix", mix.createPort());
        registerPort("input", new DefaultAudioInputPort(this, comb));
        registerPort("output", new DefaultAudioOutputPort(this, comb));
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
