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

package net.neilcsmith.praxis.audio.components.time;

import net.neilcsmith.praxis.audio.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.rapl.components.time.MonoDelay;

/**
 *
 * @author Neil C Smith
 */
public class MonoDelay2s extends AbstractComponent {

    private MonoDelay delay;
    private FloatProperty time;
    private FloatProperty feedback;
    private FloatProperty mix;

    public MonoDelay2s() {
        delay = new MonoDelay(2);
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
        registerPort(Port.IN, new DefaultAudioInputPort(this, delay));
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, delay));
    }

    private class MixBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            delay.setMix((float) value);
        }

        public double getBoundValue() {
            return delay.getMix();
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
