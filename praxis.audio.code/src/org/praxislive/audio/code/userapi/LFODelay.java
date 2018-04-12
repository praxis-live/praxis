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
import org.jaudiolibs.audioops.impl.LFODelayOp;
import org.jaudiolibs.pipes.impl.OpHolder;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public final class LFODelay extends OpHolder<LFODelayOp> implements Resettable {

    private final LFODelayOp op;

    public LFODelay() {
        op = new LFODelayOp();
        reset();
        setOp(op);
    }

    public LFODelay time(double time) {
        op.setDelay((float) Utils.constrain(time, 0, 1));
        return this;
    }

    public double time() {
        return op.getDelay();
    }

    public LFODelay feedback(double feedback) {
        op.setFeedback((float) Utils.constrain(feedback, 0, 1));
        return this;
    }

    public double feedback() {
        return op.getFeedback();
    }

//    public LFODelay phase(double phase) {
//        op.setPhase((float) phase);
//        return this;
//    }
//
//    public double phase() {
//        return op.getPhase();
//    }

    public LFODelay range(double range) {
        op.setRange((float) Utils.constrain(range, 0, 1));
        return this;
    }

    public double range() {
        return op.getRange();
    }

    public LFODelay rate(double rate) {
        op.setRate((float) rate);
        return this;
    }

    public double rate() {
        return op.getRate();
    }

    @Override
    public void reset() {
        op.setDelay(0);
        op.setRange(0);
//        op.setPhase(0);
        op.setFeedback(0);
        op.setRate(0);
    }

}
