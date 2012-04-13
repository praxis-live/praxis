/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
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
package net.neilcsmith.praxis.components.routing;

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractControlInputPort;
import net.neilcsmith.praxis.impl.AbstractExecutionContextComponent;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.FloatProperty;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class Inhibitor extends AbstractExecutionContextComponent {

    private long lastMessageTime;
    private boolean inited;
    private long timeNS;
    private double timeS;
    private ControlPort.Output out;

    public Inhibitor() {
        registerPort(Port.IN, new InputPort());
        out = new DefaultControlOutputPort(this);
        registerPort(Port.OUT, out);
        FloatProperty time = FloatProperty.create(new TimeBinding(), 0, 60 * 60, 1);
        registerControl("time", time);
        registerPort("time", time.createPort());
    }

    public void stateChanged(ExecutionContext source) {
        inited = false;
    }

    private class TimeBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            timeS = value;
            timeNS = secsToNanos(value);
        }

        public double getBoundValue() {
            return timeS;
        }

        private long secsToNanos(double secs) {
            if (secs > 0) {
                return (long) (secs * 1e9);
            } else {
                return 1;
            }
        }
    }

    private class InputPort extends AbstractControlInputPort {

        @Override
        public void receive(long time, double value) {
            if (!inited) {
                inited = true;
            } else if (((lastMessageTime + timeNS) - time) > 0) {
                return;
            }
            out.send(time, value);
            lastMessageTime = time;
        }

        @Override
        public void receive(long time, Argument value) {
            if (!inited) {
                inited = true;
            } else if (((lastMessageTime + timeNS) - time) > 0) {
                return;
            }
            out.send(time, value);
            lastMessageTime = time;
        }
    }
}
