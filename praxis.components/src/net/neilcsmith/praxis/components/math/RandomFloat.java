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

package net.neilcsmith.praxis.components.math;

import java.util.Random;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.NumberProperty;
import net.neilcsmith.praxis.impl.TriggerControl;

/**
 *
 * @author Neil C Smith
 */
public class RandomFloat extends AbstractComponent {
    
    private Random rdm;
    private ControlPort.Output output;
    private NumberProperty minimum;
    private NumberProperty range;
    private TriggerControl trigger;
    
    public RandomFloat() {
        rdm = new Random();
        minimum = NumberProperty.create( 0);
        range = NumberProperty.create( 0, PNumber.MAX_VALUE, 1);
        output = new DefaultControlOutputPort(this);
        trigger = TriggerControl.create( new TriggerBinding());
        registerControl("trigger", trigger);
        registerPort("trigger", trigger.createPort());
        registerPort(Port.OUT, output);
        registerControl("minimum", minimum);
        registerPort("minimum", minimum.createPort());
        registerControl("range", range);
        registerPort("range", range.createPort());     
    }
    
    
    private class TriggerBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            double min = minimum.getValue();
            double r = range.getValue();
            if (r > 0) {
                output.send(time, (rdm.nextDouble() * r) + min);
            } else {
                output.send(time, min);
            }
        }
        
    }
    
}
