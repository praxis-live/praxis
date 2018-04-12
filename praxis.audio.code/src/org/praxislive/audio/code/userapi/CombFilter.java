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
import org.jaudiolibs.audioops.impl.CombOp;
import org.jaudiolibs.pipes.impl.OpHolder;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public final class CombFilter extends OpHolder<CombOp> implements Resettable {
    
    private final CombOp op;
    
    public CombFilter() {
        this.op = new CombOp();
        reset();
        setOp(op);
    }

    public CombFilter frequency(double frequency) {
        op.setFrequency((float) Utils.constrain(frequency, CombOp.MIN_FREQ, CombOp.MAX_FREQ));
        return this;
    }

    public double frequency() {
        return op.getFrequency();
    }

    public CombFilter feedback(double feedback) {
        op.setFeedback((float) Utils.constrain(feedback, 0, 1));
        return this;
    }

    public double feedback() {
        return op.getFeedback();
    }
    
    @Override
    public void reset() {
        op.setFrequency(CombOp.MIN_FREQ);
        op.setFeedback(0);
    }
    
}
