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

package net.neilcsmith.rapl.components.test;

import net.neilcsmith.rapl.core.Buffer;
import net.neilcsmith.rapl.core.impl.SingleOut;
import net.neilcsmith.rapl.ops.NoiseOp;

/**
 *
 * @author Neil C Smith
 */
public class Noise extends SingleOut {

    private NoiseOp op;
    private float[][] dataHolder = new float[1][];
    
    public Noise() {
        op = new NoiseOp();
    }
    
    public void setGain(float gain) {
        op.setGain(gain);
    }
    
    public float getGain() {
        return op.getGain();
    }
    
    
    @Override
    protected void process(Buffer buffer, boolean rendering) {
        if (rendering) {
            dataHolder[0] = buffer.getData();
            op.processReplace(buffer.getSize(), dataHolder, null);
        }
    }

}
