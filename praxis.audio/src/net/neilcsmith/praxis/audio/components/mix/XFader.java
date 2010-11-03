/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith. All rights reserved.
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

package net.neilcsmith.praxis.audio.components.mix;

import net.neilcsmith.praxis.audio.DefaultAudioInputPort;
import net.neilcsmith.praxis.audio.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.rapl.components.Gain;
import net.neilcsmith.rapl.components.Mixer;

/**
 *
 * @author Neil C Smith
 */
public class XFader extends AbstractComponent {

    Gain g1;
    Gain g2;
    Mixer mixer;

    public XFader() {
        g1 = new Gain();
        g1.setGain(1);
        g2 = new Gain();
        g2.setGain(0);
        mixer = new Mixer(2);
        try {
            mixer.addSource(g1);
            mixer.addSource(g2);
        } catch (Exception ex) {
            throw new Error();
        }
        FloatProperty mix = FloatProperty.create( new MixBinding(), 0, 1, 0);
        registerControl("mix", mix);
        registerPort("mix", mix.createPort());
        registerPort(Port.IN + "-1", new DefaultAudioInputPort(this, g1));
        registerPort(Port.IN + "-2", new DefaultAudioInputPort(this, g2));
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, mixer));
    }

    private class MixBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            g1.setGain((float) (1 - value));
            g2.setGain((float) value);
        }

        public double getBoundValue() {
            return g2.getGain();
        }

    }

}
