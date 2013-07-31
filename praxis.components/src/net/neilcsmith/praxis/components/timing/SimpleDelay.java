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
package net.neilcsmith.praxis.components.timing;

import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractClockComponent;
import net.neilcsmith.praxis.impl.ArgumentInputPort;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.NumberProperty;

/**
 *
 * @author Neil C Smith
 */
public class SimpleDelay extends AbstractClockComponent {

    private static final Logger logger = Logger.getLogger(SimpleDelay.class.getName());
    private double delayS = 0;
    private long delayNS = 1;
    private long messageTime;
    private Argument message;
    private DefaultControlOutputPort output;
    private boolean active;

    public SimpleDelay() {
        
        registerPort(Port.IN, createInputPort());
        output = new DefaultControlOutputPort(this);
        registerPort(Port.OUT, output);
        NumberProperty delay = createDelayControl();
        registerControl("time", delay);
        registerPort("time", delay.createPort());
    }

    private NumberProperty createDelayControl() {
        NumberProperty.Binding binding = new NumberProperty.Binding() {

            public void setBoundValue(long time, double value) {
                delayS = value;
                delayNS = secsToNS(value);
            }

            public double getBoundValue() {
                return delayS;
            }
        };
        return NumberProperty.create(binding, 0, 60 * 60, 0);
    }

    private ArgumentInputPort createInputPort() {
        ArgumentInputPort.Binding binding = new ArgumentInputPort.Binding() {

            public void receive(long time, Argument arg) {

                message = arg;
                messageTime = time;

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

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        message = null;
    }

    public void tick(ExecutionContext source) {
        if (message != null) {
            long time = source.getTime();
            if (((messageTime + delayNS) - time) <= 0) { // protect against overflow
                Argument arg = message;
                // null before sending in case feedback places back into delay
                message = null;
                output.send(time, arg);
            }
        }
    }

    public void stateChanged(ExecutionContext source) {
        // @TODO what to do with message on start and stop?
    }
}
