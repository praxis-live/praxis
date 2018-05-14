/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 *
 */
package org.praxislive.audio.code.userapi;

import org.praxislive.audio.code.Resettable;
import org.jaudiolibs.audioops.AudioOp;
import org.jaudiolibs.pipes.impl.OpHolder;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public final class Phasor extends OpHolder<AudioOp> implements Resettable {

    private final static float DEFAULT_FREQUENCY = 1;

    private final Op op;

    public Phasor() {
        op = new Op();
        reset();
        setOp(op);
    }

    public Phasor frequency(double frequency) {
        op.setFrequency((float) frequency);
        return this;
    }

    public double frequency() {
        return op.getFrequency();
    }
    
    public Phasor minimum(double minimum) {
        op.setMinimum((float) minimum);
        return this;
    }
    
    public double minimum() {
        return op.getMinimum();
    }
    
    public Phasor maximum(double maximum) {
        op.setMaximum((float) maximum);
        return this;
    }
    
    public double maximum() {
        return op.getMaximum();
    }
    
    public Phasor phase(double phase) {
        op.setPhase((float) phase);
        return this;
    }
    
    public double phase() {
        return op.getPhase();
    }

    @Override
    public void reset() {
        op.setFrequency(DEFAULT_FREQUENCY);
        op.setMinimum(0);
        op.setMaximum(1);
    }

    private static class Op implements AudioOp {

        private final static float TWOPI = (float) (2 * Math.PI);

        private float phase;
        private float phaseIncrement;
        private float freq;
        private float srate;
        private float minimum;
        private float maximum;

        private Op() {
            this.freq = DEFAULT_FREQUENCY;
            this.minimum = 0;
            this.maximum = 1;
        }

        public void setFrequency(float frequency) {
            this.freq = frequency;
            updateIncrement();
        }

        public float getFrequency() {
            return this.freq;
        }
        
        public void setMinimum(float minimum) {
            this.minimum = minimum;
        }
        
        public float getMinimum() {
            return minimum;
        }
        
        public void setMaximum(float maximum) {
            this.maximum = maximum;
        } 
        
        public float getMaximum() {
            return maximum;
        }

        public void setPhase(float phase) {
            this.phase = Math.abs(phase) % TWOPI;
        }
        
        public float getPhase() {
            return phase;
        }
        
        @Override
        public void initialize(float samplerate, int buffersize) {
            this.srate = samplerate;
            this.phase = 0;
            updateIncrement();
        }

        @Override
        public void reset(int i) {
            phase = 0; // increment phase using i
        }

        @Override
        public boolean isInputRequired(boolean bln) {
            return false;
        }

        @Override
        public void processReplace(int buffersize, float[][] outputs, float[][] inputs) {
            float[] out = outputs[0];
            for (int i = 0; i < buffersize; i++) {
                out[i] = nextSample();
            }
        }

        @Override
        public void processAdd(int buffersize, float[][] outputs, float[][] inputs) {
            float[] out = outputs[0];
            for (int i = 0; i < buffersize; i++) {
                out[i] += nextSample();
            }
        }

        private void updateIncrement() {
            float inc = 0;
            if (srate > 0) {
                inc = TWOPI * freq / srate;
                if (inc < 0) {
                    inc = 0;
                }
            }
            phaseIncrement = inc;
        }

        private float nextSample() {
            float value = (phase / TWOPI) * (maximum - minimum) + minimum;
            phase += phaseIncrement;
            while (phase >= TWOPI) {
                phase -= TWOPI;
            }
            return value;
        }

    }

}
