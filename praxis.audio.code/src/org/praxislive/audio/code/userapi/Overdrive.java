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
package org.praxislive.audio.code.userapi;

import org.praxislive.audio.code.Resettable;
import org.jaudiolibs.audioops.impl.gpl.OverdriveOp;
import org.jaudiolibs.pipes.impl.OpHolder;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public final class Overdrive extends OpHolder<OverdriveOp> implements Resettable {
    
    private final OverdriveOp op;
    
    public Overdrive() {
        this.op = new OverdriveOp();
        reset();
        setOp(op);
    }
    
    public Overdrive drive(double amt) {
        op.setDrive((float) amt);
        return this;
    }
    
    public double drive() {
        return op.getDrive();
    }

    @Override
    public void reset() {
        op.setDrive(0);
    }
    
    
    
}
