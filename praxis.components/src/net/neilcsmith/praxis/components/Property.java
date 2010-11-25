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

package net.neilcsmith.praxis.components;

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;

/**
 *
 * @author Neil C Smith
 */
public class Property extends AbstractComponent {
    
    private ControlPort.Output output;
    private Argument arg;

    public Property() {
        output = new DefaultControlOutputPort(this);
        arg = PString.EMPTY;
        ArgumentProperty value = ArgumentProperty.create( new Binding(), arg);
        registerControl("value", value);
        registerPort(Port.OUT, output);
    }
    
    private class Binding implements ArgumentProperty.Binding {

        // @TODO should binding only send value if root state is running?
        public void setBoundValue(long time, Argument value) {
            arg = value;
            output.send(time, arg);
        }

        public Argument getBoundValue() {
            return arg;
        }
        
    }

}
