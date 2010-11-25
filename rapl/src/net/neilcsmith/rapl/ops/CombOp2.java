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

package net.neilcsmith.rapl.ops;

import net.neilcsmith.rapl.core.Buffer;

/**
 *
 * @author Neil C Smith
 */
public class CombOp2 implements AudioOpOld {
    
    public final static float MIN_FREQ = 20;
    public final static float MAX_FREQ = 20000;
    private final static float MAX_TIME = (1 / MIN_FREQ) * 2;
    
    private float frequency;
    private VariableDelayOpOld delay;
    
    public CombOp2() {
        delay = new VariableDelayOpOld(MAX_TIME);
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
    
    public void setGain(float gain) {
        delay.setGain(gain);
    }
    
    public float getGain() {
        return delay.getGain();
    }
    
    public void setFeedback(float feedback) {
        delay.setFeedback(feedback);
    }
    
    public float getFeedback() {
        return delay.getFeedback();
    }
    
    public void processReplace(Buffer bufferIn, Buffer bufferOut) {
        delay.processReplace(bufferIn, bufferOut);
    }

    public void processAdd(Buffer bufferIn, Buffer bufferOut) {
        delay.processAdd(bufferIn, bufferOut);
    }

    public void reset() {
        delay.reset();
    }

}
