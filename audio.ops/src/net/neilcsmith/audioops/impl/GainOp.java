/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
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
 */
package net.neilcsmith.audioops.impl;

import net.neilcsmith.audioops.AudioOp;


/**
 * Basic gain op with smooth interpolation.
 *
 * Interpolation of gain happens over full length of buffer.
 *
 * @author Neil C Smith
 */
public class GainOp implements AudioOp {

    private float gain = 1;
    private float oldGain = 0;
    

    public void setGain(float gain) {
        if (gain < 0) {
            throw new IllegalArgumentException();
        }
        this.gain = gain;
    }

    public float getGain() {
        return gain;
    }

    public void reset() {
        oldGain = 0;
    }

    public void processReplace(int buffersize, float[][] outputs, float[][] inputs) {
        if (gain != 0 || oldGain != 0) {
            float[] in = inputs[0];
            float[] out = outputs[0];
            float g = oldGain;
            float delta = (gain - g) / buffersize;
            for (int i=0; i < buffersize; i++) {
                out[i] = g * in[i];
                g += delta;
            }
            oldGain = gain;
        } else {
            float[] out = outputs[0];
            for (int i=0; i<buffersize; i++) {
                out[i] = 0;
            }
        }
    }

    public void processAdd(int buffersize, float[][] outputs, float[][] inputs) {
        if (gain != 0 || oldGain != 0) {
            float[] in = inputs[0];
            float[] out = outputs[0];
            float g = oldGain;
            float delta = (gain - g) / buffersize;
            for (int i=0; i < buffersize; i++) {
                out[i] += g * in[i];
                g += delta;
            }
            oldGain = gain;
        }
    }

    public void initialize(float samplerate, int maxBufferSize) {
        // no op
    }

    public boolean isInputRequired() {
        return (gain != 0 || oldGain != 0);
    }
}
