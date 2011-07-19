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

import org.jaudiolibs.audioops.AudioOp;

/**
 * Comb Filter op. This op wraps a VariableDelay to provide an interface converting
 * frequency to delay length. Like VariableDelay, changes to parameters are linearly
 * interpolated over the period of one buffer.
 *
 * This is a mono op and requires one input and output channel. Input and output
 * buffers may be the same.
 * @author Neil C Smith
 */
public class CombOp implements AudioOp {
    
    public final static float MIN_FREQ = 20;
    public final static float MAX_FREQ = 20000;
    private final static float MAX_TIME = (1 / MIN_FREQ) * 2;
    
    private float frequency;
    private VariableDelayOp delay;
    
    public CombOp() {
        delay = new VariableDelayOp(MAX_TIME);
        setFrequency(MIN_FREQ);
    }
    
    public void setFrequency(float frequency) {
        if (frequency < MIN_FREQ || frequency > MAX_FREQ) {
            throw new IllegalArgumentException();
        }
        delay.setDelay(1 / frequency);
        this.frequency = frequency;
    }
    
    public float getFrequency() {
        return frequency;
    }
    
    
    public void setFeedback(float feedback) {
        delay.setFeedback(feedback);
    }
    
    public float getFeedback() {
        return delay.getFeedback();
    }
    

    public void reset() {
        delay.reset();
    }

    public void processReplace(int buffersize, float[][] outputs, float[][] inputs) {
        delay.processReplace(buffersize, outputs, inputs);
    }

    public void processAdd(int buffersize, float[][] outputs, float[][] inputs) {
        delay.processAdd(buffersize, outputs, inputs);
    }

    public void initialize(float samplerate, int maxBufferSize) {
        delay.initialize(samplerate, maxBufferSize);
    }

    public boolean isInputRequired() {
        return delay.isInputRequired();
    }

}
