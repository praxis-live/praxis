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

package net.neilcsmith.rapl.components.time;

import org.jaudiolibs.audioops.impl.ContainerOp;
import org.jaudiolibs.audioops.impl.VariableDelayOp;
import net.neilcsmith.rapl.components.SingleInOutOpComponent;


/**
 *
 * @author Neil C Smith
 */
public class MonoDelay extends SingleInOutOpComponent {

    private VariableDelayOp delayOp;
    private ContainerOp container;
    
    public MonoDelay(float maxDelay) {
        delayOp = new VariableDelayOp(maxDelay);
        container = new ContainerOp(delayOp);
        setOp(container);
    }
    
    public void setDelay(float time) {
        delayOp.setDelay(time);
    }
    
    public float getDelay() {
        return delayOp.getDelay();
    }
    
    public void setFeedback(float amt) {
        delayOp.setFeedback(amt);
    }
    
    public float getFeedback() {
        return delayOp.getFeedback();
    }

    public void setMix(float mix) {
        container.setMix(mix);
    }

    public float getMix() {
        return container.getMix();
    }

}
