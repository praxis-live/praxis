/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
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
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */

package net.neilcsmith.rapl.components;

import net.neilcsmith.rapl.core.Buffer;
import net.neilcsmith.rapl.core.impl.SingleInOut;
import net.neilcsmith.rapl.ops.GainOp;

/**
 * @TODO implement check for rendering at 0 - must take interpolation into account
 * @author Neil C Smith
 */
public class Gain extends SingleInOut {
    
    private GainOp gainOp;
    
    public Gain() {
        gainOp = new GainOp();
        gainOp.setGain(1.0f);
    }
    
    public void setGain(float gain) {
        gainOp.setGain(gain);
    }
    
    public float getGain() {
        return gainOp.getGain();
    }

    @Override
    protected void process(Buffer buffer, boolean rendering) {
        if (rendering) {
            gainOp.processReplace(buffer, buffer);
        }
    }
}
