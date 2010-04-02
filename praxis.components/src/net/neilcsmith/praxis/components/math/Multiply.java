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

package net.neilcsmith.praxis.components.math;

import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.FloatInputPort;
import net.neilcsmith.praxis.impl.FloatProperty;

/**
 *
 * @author Neil C Smith
 */
public class Multiply extends AbstractComponent {
    
    private FloatProperty value;
    private ControlPort.Output output;
    
    public Multiply() {
        value = FloatProperty.create(this, 0);
        registerControl("value", value);
        registerPort("value", value.createPort());
        registerPort("input", FloatInputPort.create(this, new InputBinding()));
        output = new DefaultControlOutputPort(this);
        registerPort("output", output);
    }
    
    private class InputBinding implements FloatInputPort.Binding {

        public void receive(long time, double input) {
            double out = value.getValue() * input;
            if (out > PNumber.MAX_VALUE) {
                out = PNumber.MAX_VALUE;
            } else if (out < PNumber.MIN_VALUE) {
                out = PNumber.MIN_VALUE;
            }
            output.send(time, out);
        }
        
    }

}
