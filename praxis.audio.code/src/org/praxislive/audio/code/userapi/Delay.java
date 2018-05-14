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
import org.jaudiolibs.audioops.impl.VariableDelayOp;
import org.jaudiolibs.pipes.impl.OpHolder;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public final class Delay extends OpHolder<VariableDelayOp> implements Resettable {

    private final OpImpl op;
    
    public Delay() {
        op = new OpImpl(2);
        reset();
        setOp(op);
    }
    
    public Delay time(double time) {
        op.setDelay((float) Utils.constrain(time, 0, 2));
        return this;
    }
    
    public double time() {
        return op.getDelay();
    }
    
    public Delay feedback(double amt) {
        op.setFeedback((float) Utils.constrain(amt, 0, 1));
        return this;
    }
    
    public double feedback() {
        return op.getFeedback();
    }
    
    public Delay level(double amt) {
        op.setGain((float) Utils.constrain(amt, 0, 1));
        return this;
    }
    
    public double level() {
        return op.getGain();
    }
    
    public Delay passthrough(boolean passthrough) {
        op.passthrough = passthrough;
        return this;
    }
    
    public boolean passthrough() {
        return op.passthrough;
    }
    
    public double maxDelay() {
        return op.getMaxDelay();
    }
    
    @Override
    public void reset() {
        op.setDelay(0);
        op.setGain(1);
        op.setFeedback(0);
        op.passthrough = true;
    }
    
    private static class OpImpl extends VariableDelayOp {
        
        private boolean passthrough;
        
        private OpImpl(float maxDelay) {
            super(maxDelay);
        }

        @Override
        public void processReplace(int buffersize, float[][] outputs, float[][] inputs) {
            if (passthrough) {
                super.processAdd(buffersize, outputs, inputs);
            } else {
                super.processReplace(buffersize, outputs, inputs);
            }
        }
        
        
        
        
    }
    
}
