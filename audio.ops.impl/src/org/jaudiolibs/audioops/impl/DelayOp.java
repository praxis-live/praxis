/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 *
 * Linking this work statically or dynamically with other modules is making a
 * combined work based on this work. Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this work give you permission
 * to link this work with independent modules to produce an executable,
 * regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that
 * you also meet, for each linked independent module, the terms and conditions of
 * the license of that module. An independent module is a module which is not
 * derived from or based on this work. If you modify this work, you may extend
 * this exception to your version of the work, but you are not obligated to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package org.jaudiolibs.audioops.impl;

import java.util.Arrays;
import org.jaudiolibs.audioops.AudioOp;

/**
 * A basic delay op. Delay time and feedback are variable. Unlike VariableDelayOp
 * this op does not interpolate changes in delay time over the buffer; this op is
 * more useful for longer period fixed delays.
 *
 * This is a mono op and requires one input and output channel. Input and output
 * buffers may be the same.
 *
 * @author Neil C Smith
 */
public class DelayOp implements AudioOp {

    private final static float DEF_SRATE = 44100;
    private final static float DEF_MAX_DELAY = 2;
    private float[] delaybuffer;
    private int rovepos = 0;
    private float gain;
    private float samplerate;
    private float delaytime;
    private float maxdelay;
    private float lastdelay;
    private float feedback;

    public DelayOp() {
        this(DEF_MAX_DELAY);
    }

    public DelayOp(float maxdelay) {
        if (maxdelay > 0) {
            this.maxdelay = maxdelay;
        }
        this.samplerate = DEF_SRATE;
        this.gain = 1;
        this.feedback = 0;

    }

    public void setDelay(float time) {
        if (time < 0 || time > maxdelay) {
            throw new IllegalArgumentException();
        }
        this.delaytime = time;
    }

    public float getDelay() {
        return this.delaytime;
    }

    public float getMaxDelay() {
        return this.maxdelay;
    }

    public void setFeedback(float feedback) {
        if (feedback < 0 || feedback > 1) {
            throw new IllegalArgumentException();
        }
        this.feedback = feedback;
    }

    public float getFeedback() {
        return this.feedback;
    }

    public void setGain(float gain) {
        if (gain < 0) {
            throw new IllegalArgumentException();
        }
        this.gain = gain;
    }

    public float getGain() {
        return this.gain;
    }

    public void reset() {
        if (delaybuffer != null) {
            Arrays.fill(delaybuffer, 0);
        }
    }

    public void processReplace(int buffersize, float[][] outputs, float[][] inputs) {
        process(buffersize, outputs, inputs, true);
    }

    public void processAdd(int buffersize, float[][] outputs, float[][] inputs) {
        process(buffersize, outputs, inputs, false);
    }

    private void process(int buffersize, float[][] outputs, float[][] inputs, boolean replace) {

        float[] buf = this.delaybuffer;
        if (buf == null) {
            // not been initialized
            throw new IllegalStateException();
        }
        float[] in = inputs[0];
        float[] out = outputs[0];
        float srate = this.samplerate;
        float g = this.gain;
        float delay = this.delaytime * srate;
        float fb = this.feedback;
        int len = buffersize;
        int rnlen = buf.length;
        int pos = this.rovepos;

        float r, s, a, b, o;
        int ri;
        if (replace) {
            for (int i = 0; i < len; i++) {
                r = pos - (delay + 2) + rnlen;
                ri = (int) r;
                s = r - ri;
                a = buf[ri % rnlen];
                b = buf[(ri + 1) % rnlen];
                o = a * (1 - s) + b * s;
                out[i] = o * g;
                buf[pos] = in[i] + o * fb;
                pos = (pos + 1) % rnlen;
            }

        } else {
            for (int i = 0; i < len; i++) {
                r = pos - (delay + 2) + rnlen;
                ri = (int) r;
                s = r - ri;
                a = buf[ri % rnlen];
                b = buf[(ri + 1) % rnlen];
                o = a * (1 - s) + b * s;
                out[i] += o * g;
                buf[pos] = in[i] + o * fb;
                pos = (pos + 1) % rnlen;
            }
        }
        this.rovepos = pos;
        this.lastdelay = delay;
    }

    public void initialize(float samplerate, int maxBufferSize) {
        if (samplerate < 1) {
            throw new IllegalArgumentException();
        }
        this.samplerate = samplerate;
        int bufferSize = (int) (maxdelay * samplerate) + 10;
        this.delaybuffer = new float[bufferSize];
        this.rovepos = 0;
        this.lastdelay = 0;
    }

    public boolean isInputRequired() {
        return true;
    }
}
