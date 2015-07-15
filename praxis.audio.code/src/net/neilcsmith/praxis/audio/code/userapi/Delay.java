/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Neil C Smith.
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
 *
 */
package net.neilcsmith.praxis.audio.code.userapi;

import net.neilcsmith.praxis.audio.code.Resettable;
import org.jaudiolibs.audioops.impl.VariableDelayOp;
import org.jaudiolibs.pipes.impl.OpHolder;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public final class Delay extends OpHolder<VariableDelayOp> implements Resettable {

    private final VariableDelayOp op;
    
    public Delay() {
        op = new VariableDelayOp(2);
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
    
    public Delay gain(double amt) {
        op.setGain((float) Utils.constrain(amt, 0, 1));
        return this;
    }
    
    public double gain() {
        return op.getGain();
    }
    
    public double maxDelay() {
        return op.getMaxDelay();
    }
    
    @Override
    public void reset() {
        op.setDelay(0);
        op.setGain(1);
        op.setFeedback(0);
    }
    
}
