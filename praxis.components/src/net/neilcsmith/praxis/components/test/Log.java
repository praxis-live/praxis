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

package net.neilcsmith.praxis.components.test;

import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.ArgumentInputPort;

/**
 *
 * @author Neil C Smith
 */
public class Log extends AbstractComponent {

    private static Logger logger = Logger.getLogger(Log.class.getName());
   
    
    public Log() {
        registerPort(Port.IN, ArgumentInputPort.create( new InputBinding()));
    }
    
    private class InputBinding implements ArgumentInputPort.Binding {

        public void receive(long time, Argument arg) {
            ComponentAddress ad = getAddress();
            logger.info("Log component at " + ad + "\n received " + arg);
        }
        
    }
    
}
