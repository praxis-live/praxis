/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2013 Neil C Smith.
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
package org.praxislive.components.container;

import org.praxislive.core.Argument;
import org.praxislive.impl.AbstractControlInputPort;

/**
 *
 * @author Neil C Smith
 */
class LinkedInputPort extends AbstractControlInputPort {
    
    private final Output output;

    public LinkedInputPort(Output output) {
        this.output = output;
    }

    @Override
    public void receive(long time, double value) {
        output.send(time, value);
    }

    @Override
    public void receive(long time, Argument value) {
        output.send(time, value);
    }
    
}
