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

package net.neilcsmith.praxis.components.timing;

import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.impl.RootState;
import net.neilcsmith.praxis.impl.AbstractControlFrameComponent;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.ArgumentInputPort;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.FloatProperty;

/**
 *
 * @author Neil C Smith
 */
public class SimpleDelay extends AbstractControlFrameComponent {
    
    private static final Logger logger = Logger.getLogger(SimpleDelay.class.getName());
    
    private double delayS = 0;
    private long delayNS = 1;
    private long messageTime;
    private Argument message;
    private DefaultControlOutputPort output;

    
    public SimpleDelay() {
        FloatProperty delay = createDelayControl();
        registerControl("time", delay);
        registerPort("time", delay.createPort());
        registerPort(Port.IN, createInputPort());
        output = new DefaultControlOutputPort(this);
        registerPort(Port.OUT, output);
    }
    
    private FloatProperty createDelayControl() {
        FloatProperty.Binding binding = new FloatProperty.Binding() {

            public void setBoundValue(long time, double value) {
                delayS = value;
                delayNS = secsToNS(value);
            }

            public double getBoundValue() {
                return delayS;
            }
        };
        return FloatProperty.create(binding, 0, 60 * 60, 0);
    }
    
    private ArgumentInputPort createInputPort() {
        ArgumentInputPort.Binding binding = new ArgumentInputPort.Binding() {

            public void receive(long time, Argument arg) {
                Root root = getRoot();
                if (root != null) {
                    message = arg;
                    messageTime = time;
                }
            }
        };
        return ArgumentInputPort.create(binding);
    }
    
    private long secsToNS(double secs) {
        if (secs > 0) {
            return (long) (secs * 1e9);
        } else {
            return 1;
        }
    }

    public void nextControlFrame(AbstractRoot root) {
        if (message != null) {
            long time = root.getTime();
            if (((messageTime + delayNS) - time) <= 0) { // protect against overflow
                Argument arg = message;
                // null before sending in case feedback places back into delay
                message = null;
                output.send(time, arg);  
            }
        }
    }

    @Override
    public void rootStateChanged(AbstractRoot source, RootState state) {
        super.rootStateChanged(source, state);
        message = null;
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        message = null;
    }
    
    
    
    
    



    
    
}
