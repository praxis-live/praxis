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
 */
package net.neilcsmith.praxis.audio.components.test;

import net.neilcsmith.praxis.audio.impl.DefaultAudioOutputPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.NumberProperty;
import org.jaudiolibs.pipes.Buffer;
import org.jaudiolibs.pipes.impl.SingleInOut;
import org.jaudiolibs.pipes.impl.SingleOut;

/**
 *
 * @author Neil C Smith
 */
public class Sine extends AbstractComponent {

    private SineUG sine;

    public Sine() {
        sine = new SineUG(440);
        registerPort(Port.OUT, new DefaultAudioOutputPort(this, sine));
        NumberProperty freq = NumberProperty.create(new FrequencyBinding(),
                110, 4 * 440, 440);
        registerControl("frequency", freq);
        registerPort("frequency", freq.createPort());

    }

    private class FrequencyBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            sine.setFrequency((float) value);
        }

        public double getBoundValue() {
            return sine.getFrequency();
        }
    }

    private class SineUG extends SingleInOut {

        private final static float TWOPI = (float) (2 * Math.PI);
        private float phase = 0;
        private float volume = 0.6f;
        private float freq = 440;
        private float srate = 0;

        public SineUG() {
            this(440);
        }

        public SineUG(float freq) {
            this.freq = freq;
        }

        public void setFrequency(float frequency) {
            this.freq = frequency;
        }

        public float getFrequency() {
            return this.freq;
        }

        @Override
        protected void process(Buffer buffer, boolean rendering) {
            if (srate != buffer.getSampleRate()) {
                srate = buffer.getSampleRate();
                phase = 0;
            }
            float[] out = buffer.getData();
            int bufsz = buffer.getSize();
            for (int i = 0; i < bufsz; i++) {
                phase += TWOPI * freq / srate;
//           buffer.set(i, (float) (volume * Math.sin(phase)));
                out[i] = volume * (float) Math.sin(phase);
            }
            while (phase > TWOPI) {
                phase -= TWOPI;
            }
        }
    }
}
