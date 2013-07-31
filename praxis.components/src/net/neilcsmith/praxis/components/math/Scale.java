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
package net.neilcsmith.praxis.components.math;

import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.FloatInputPort;
import net.neilcsmith.praxis.impl.NumberProperty;

/**
 *
 * @author Neil C Smith
 */
public class Scale extends AbstractComponent {

    private NumberProperty x1;
    private NumberProperty x2;
    private NumberProperty y1;
    private NumberProperty y2;
    private ControlPort.Output output;

    public Scale() {
        x1 = NumberProperty.create( 0);
        x2 = NumberProperty.create( 1);
        y1 = NumberProperty.create( 0);
        y2 = NumberProperty.create( 1);
        FloatInputPort input = FloatInputPort.create( new InputBinding());
        output = new DefaultControlOutputPort(this);
        registerPort(Port.IN, input);
        registerPort(Port.OUT, output);
        registerControl("x1", x1);
        registerPort("x1", x1.createPort());
        registerControl("x2", x2);
        registerPort("x2", x2.createPort());
        registerControl("y1", y1);
        registerPort("y1", y1.createPort());
        registerControl("y2", y2);
        registerPort("y2", y2.createPort());   
    }

    private class InputBinding implements FloatInputPort.Binding {

        public void receive(long time, double value) {
            double _x1, _x2, _y1, _y2, _xMin, _xMax, ratio, out;
            _x1 = x1.getValue();
            _x2 = x2.getValue();
            _y1 = y1.getValue();
            _y2 = y2.getValue();

            _xMin = Math.min(_x1, _x2);
            if (value < _xMin) {
                value = _xMin;
            }
            _xMax = Math.max(_x1, _x2);
            if (value > _xMax) {
                value = _xMax;
            }

            ratio = (value - _x1) / (_x2 - _x1);
            out = ratio * (_y2 - _y1) + _y1;

            output.send(time, out);
        }
    }
}
