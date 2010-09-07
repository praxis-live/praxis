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

package net.neilcsmith.praxis.components.array;

import java.util.Random;
import net.neilcsmith.praxis.core.ControlPort;
//import net.neilcsmith.praxis.core.impl.MultiArgProperty;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.ArrayProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.TriggerControl;

/**
 *
 * @author Neil C Smith
 */
public class ArrayRandom extends AbstractComponent {

//    private MultiArgProperty args;
    private ArrayProperty args;
    private ControlPort.Output output;
    private Random random;
    
    public ArrayRandom() {
        random = new Random();
        args = ArrayProperty.create(this);
        registerControl("values", args);
        TriggerControl trigger = TriggerControl.create(this, new TriggerBinding());
        registerControl("trigger", trigger);
        registerPort("trigger", trigger.createPort());
        output = new DefaultControlOutputPort(this);
        registerPort(Port.OUT, output);
    }
    
    private class TriggerBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            
            PArray arr = args.getValue();
            int count = arr.getSize();
            if (count == 0) {
                output.send(time, PString.EMPTY);
            } else if (count == 1) {
                output.send(time, arr.get(0));
            } else {
                output.send(time, arr.get(random.nextInt(count)));
            }
            
        }
        
    }
    
}
