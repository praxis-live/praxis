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
import org.jaudiolibs.audioops.impl.ChorusOp;
import org.jaudiolibs.pipes.impl.OpHolder;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public final class Chorus extends OpHolder<ChorusOp> implements Resettable {

    private final ChorusOp op;
    
    public Chorus() {
        op = new ChorusOp();
        reset();
        setOp(op);
    }
    
    public Chorus depth(double depth) {
        op.setDepth((float) depth);
        return this;
    }
    
    public double depth() {
        return op.getDepth();
    }
    
    public Chorus feedback(double feedback) {
        op.setFeedback((float) feedback);
        return this;
    }
    
    public double feedback() {
        return op.getFeedback();
    }
//    
//    public Chorus phase(double phase) {
//        op.setPhase((float) phase);
//        return this;
//    }
//    
//    public double phase() {
//        return op.getPhase();
//    }
    
    public Chorus rate(double rate) {
        op.setRate((float) rate);
        return this;
    }
    
    public double rate() {
        return op.getRate();
    }
    
    @Override
    public void reset() {
        op.setDepth(0);
//        op.setPhase(0);
        op.setFeedback(0);
        op.setRate(0);
    }
    
}
