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
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.FloatInputPort;
import net.neilcsmith.praxis.impl.FloatProperty;

/**
 *
 * @author Neil C Smith
 */
public class Threshold extends AbstractComponent {

    private FloatProperty threshold;
    private ControlPort.Output outputLow;
    private ControlPort.Output outputHigh;

    public Threshold() {
        threshold = FloatProperty.create(this, 0);
        registerControl("threshold", threshold);
        registerPort("threshold", threshold.createPort());
        registerPort(Port.IN, FloatInputPort.create(this, new InputBinding()));
        outputLow = new DefaultControlOutputPort(this);
        outputHigh = new DefaultControlOutputPort(this);
        registerPort("output-low", outputLow);
        registerPort("output-high", outputHigh);
    }


    private class InputBinding implements FloatInputPort.Binding {

        public void receive(long time, double value) {
            if (value >= threshold.getValue()) {
                outputHigh.send(time, value);
            } else {
                outputLow.send(time, value);
            }
        }
    }
}
