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

import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractExecutionContextComponent;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;

/**
 *
 * @author Neil C Smith
 */
public class StartTrigger extends AbstractExecutionContextComponent {

    private DefaultControlOutputPort output;
    
    public StartTrigger() {
        output = new DefaultControlOutputPort(this);
        registerPort(Port.OUT, output);
    }

    public void stateChanged(ExecutionContext source) {
        if (source.getState() == ExecutionContext.State.ACTIVE) {
            output.send(source.getTime());
        }
    }

}
