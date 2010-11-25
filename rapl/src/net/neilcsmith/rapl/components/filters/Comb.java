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

package net.neilcsmith.rapl.components.filters;

import net.neilcsmith.audioops.impl.ContainerOp;
import net.neilcsmith.audioops.impl.CombOp;
import net.neilcsmith.rapl.components.SingleInOutOpComponent;


/**
 *
 * @author Neil C Smith
 */
public class Comb extends SingleInOutOpComponent {

    public final static float MIN_FREQ = CombOp.MIN_FREQ;
    public final static float MAX_FREQ = CombOp.MAX_FREQ;
    private CombOp combOp;
    private ContainerOp container;
    
    public Comb() {
        combOp = new CombOp();
        container = new ContainerOp(combOp);
        setOp(container);
    }
    
    public void setFrequency(float freq) {
        combOp.setFrequency(freq);
    }
    
    public float getFrequency() {
        return combOp.getFrequency();
    }
    
    public void setFeedback(float amt) {
        combOp.setFeedback(amt);
    }
    
    public float getFeedback() {
        return combOp.getFeedback();
    }

    public void setMix(float mix) {
        container.setMix(mix);
    }

    public float getMix() {
        return container.getMix();
    }

}
