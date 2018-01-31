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
import org.jaudiolibs.pipes.Buffer;
import org.jaudiolibs.pipes.impl.SingleInOut;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public final class OpGen extends SingleInOut implements Resettable {
    
    private final static Function PASSTHROUGH = new Function() {

        @Override
        public double process(double in) {
            return in;
        }
    };

//    private double[] scratch;
    private Function fn;
    
    public OpGen() {
        reset();
    }
    
    
    @Override
    protected void process(Buffer buffer, boolean rendering) {
        Function f = fn == null ? PASSTHROUGH : fn;
        int size = buffer.getSize();
        float[] data = buffer.getData();
        for (int i=0; i < size; i++) {
            data[i] = (float) f.process(data[i]);
        }
    }

    public OpGen function(Function fn) {
        this.fn = fn == null ? PASSTHROUGH : fn;
        return this;
    }
    
    @Override
    public void reset() {
        this.fn = null;
    }
    
    
    
}
