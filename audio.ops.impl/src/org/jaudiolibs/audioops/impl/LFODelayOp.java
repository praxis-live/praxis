/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
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

import org.jaudiolibs.audioops.AudioOp;

/**
 *
 * @author Neil C Smith
 */
public class LFODelayOp implements AudioOp {

    private final static float PI2 = (float) Math.PI * 2;
    private final float max_delay;
    private final VariableDelayOp delayOp;
    private float delay = 0;
    private float range = 0;
    private float rate = 0;
    private float phase = 0;
    private int lfocount;
    private float samplerate;

    public LFODelayOp() {
        this.max_delay = 1;
        this.delayOp = new VariableDelayOp(2);
    }

    public void initialize(float samplerate, int maxBufferSize) {
        delayOp.initialize(samplerate, maxBufferSize);
        this.samplerate = samplerate;
    }

    public void processReplace(int buffersize, float[][] outputs, float[][] inputs) {
        processImpl(buffersize, outputs, inputs, true);
    }

    public void processAdd(int buffersize, float[][] outputs, float[][] inputs) {
        processImpl(buffersize, outputs, inputs, false);
    }

    private void processImpl(int buffersize, float[][] outputs, float[][] inputs, boolean replace) {
        if (rate > 0.0001 && range > 0) {
            lfocount += buffersize;
            float lfolength = samplerate / rate;
            lfocount %= (int) (lfolength);
            float r = lfocount / lfolength;
            r *= PI2;
            r += phase * PI2;
            if (r > PI2) {
                r -= PI2;
            }
            r = delay * range * (float) Math.sin(r);
            delayOp.setDelay(delay + r);
        } else {
            lfocount = 0;
            delayOp.setDelay(delay);
        }

        if (replace) {
            delayOp.processReplace(buffersize, outputs, inputs);
        } else {
            delayOp.processAdd(buffersize, outputs, inputs);
        }


    }

    public void reset(int skipped) {
        delayOp.reset(skipped);
        lfocount = 0;
    }

    public boolean isInputRequired(boolean outputRequired) {
        return outputRequired;
    }

    public void setDelay(float depth) {
        if (depth < 0 || depth > max_delay) {
            throw new IllegalArgumentException();
        }
        this.delay = depth;
    }

    public float getDelay() {
        return delay;
    }

    public void setRange(float range) {
        if (range < 0 || range > 1) {
            throw new IllegalArgumentException();
        }
        this.range = range;
    }

    public float getRange() {
        return range;
    }

    public void setRate(float rate) {
        if (rate < 0) {
            throw new IllegalArgumentException();
        }
        this.rate = rate;
    }

    public float getRate() {
        return rate;
    }

    public void setPhase(float phase) {
        if (phase < 0 || phase > 1) {
            throw new IllegalArgumentException();
        }
        this.phase = phase;
    }

    public float getPhase() {
        return phase;
    }

    public void setFeedback(float feedback) {
        delayOp.setFeedback(feedback);
    }

    public float getFeedback() {
        return delayOp.getFeedback();
    }
}
