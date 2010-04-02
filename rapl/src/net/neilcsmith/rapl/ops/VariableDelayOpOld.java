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
package net.neilcsmith.rapl.ops;

import java.util.Arrays;
import net.neilcsmith.rapl.core.Buffer;

/**
 *
 * @author Neil C Smith
 */
public class VariableDelayOpOld implements AudioOpOld {

    private float[] delaybuffer;
    private int rovepos = 0;
    private float gain;
    private float samplerate;
    private float delaytime;
    private float maxdelay;

    private float lastdelay;
    private float feedback;


    public VariableDelayOpOld(float maxdelay) {
        if (maxdelay > 0) {
            this.maxdelay = maxdelay;
        }
        this.gain = 1;
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

//    public void setInterpolationTime(float time) {
//        if (time < 0) {
//            throw new IllegalArgumentException();
//        }
//        this.interpTime = time;
//    }
//
//    public float getInterpolationTime() {
//        return interpTime;
//    }

    public void processReplace(Buffer bufferIn, Buffer bufferOut) {
        process(bufferIn, bufferOut, true);
    }

    public void processAdd(Buffer bufferIn, Buffer bufferOut) {
        process(bufferIn, bufferOut, false);
    }

    public void reset() {
        if (delaybuffer != null) {
            Arrays.fill(delaybuffer, 0);
        }
    }

    private void process(Buffer bufferIn, Buffer bufferOut, boolean replace) {
        float srate = bufferOut.getSampleRate();
        if (srate != this.samplerate) {
            init(srate);
        }
        float g = this.gain;
        float delay = this.delaytime * srate;
        float ldelay = this.lastdelay;
        float fb = this.feedback;
        float[] buf = this.delaybuffer;
        int len = bufferOut.getSize();
        int rnlen = buf.length;
        int pos = this.rovepos;
        float delta = (delay - ldelay) / len;
        float[] in = bufferIn.getData();
        float[] out = bufferOut.getData();


        float r, s, a, b, o;
        int ri;
        if (replace) {
            for (int i = 0; i < len; i++) {
                r = pos - (ldelay + 2) + rnlen;
                ri = (int) r;
                s = r - ri;
                a = buf[ri % rnlen];
                b = buf[(ri + 1) % rnlen];
                o = a * (1 - s) + b * s;
                buf[pos] = in[i] + o * fb;
                out[i] = o * g;
                pos = (pos + 1) % rnlen;
                ldelay += delta;
            }

        } else {
            for (int i = 0; i < len; i++) {
                r = pos - (ldelay + 2) + rnlen;
                ri = (int) r;
                s = r - ri;
                a = buf[ri % rnlen];
                b = buf[(ri + 1) % rnlen];
                o = a * (1 - s) + b * s;
                out[i] += o * g;
                buf[pos] = in[i] + o * fb;
                pos = (pos + 1) % rnlen;
                ldelay += delta;
            }
        }
        this.rovepos = pos;
        this.lastdelay = delay;
    }

    private void init(float samplerate) {
        this.samplerate = samplerate;
        int bufferSize = (int) (maxdelay * samplerate) + 10;
        delaybuffer = new float[bufferSize];
        rovepos = 0;
        lastdelay = 0;
    }
}



