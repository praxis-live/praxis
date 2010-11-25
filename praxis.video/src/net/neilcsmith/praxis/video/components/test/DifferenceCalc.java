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
package net.neilcsmith.praxis.video.components.test;

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.ArrayProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.TriggerControl;
import net.neilcsmith.praxis.video.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.DefaultVideoOutputPort;
import net.neilcsmith.ripl.components.temporal.ChangeMeasure;

/**
 *
 * @author Neil C Smith
 */
public class DifferenceCalc extends AbstractComponent {
    
    private final static PNumber ZERO = PNumber.valueOf(0);
    private final static PNumber ONE = PNumber.valueOf(1);
    private final static PArray DEFAULTS = PArray.valueOf(new Argument[] {ZERO, ZERO, ONE, ONE});
    
    private ChangeMeasure changeMeasure;
    private ControlPort.Output diffOutput;

    public DifferenceCalc() {
        changeMeasure = new ChangeMeasure();
        registerPort(Port.IN, new DefaultVideoInputPort(this, changeMeasure));
        registerPort(Port.OUT, new DefaultVideoOutputPort(this, changeMeasure));
        diffOutput = new DefaultControlOutputPort(this);

        TriggerControl trigger = TriggerControl.create( new TriggerBinding());
        registerControl("bounds", ArrayProperty.create( new RegionBinding(), DEFAULTS));   
        registerControl("trigger", trigger);
        registerPort("trigger", trigger.createPort());
        registerPort("measurement", diffOutput);



    }

    private class RegionBinding implements ArrayProperty.Binding {

        private PArray value;

        private RegionBinding() {

            value = DEFAULTS;
        }

        public void setBoundValue(long time, PArray value) {
            if (value.getSize() != 4) {
                throw new IllegalArgumentException();
            }
            try {
                double x = PNumber.coerce(value.get(0)).value();
                double y = PNumber.coerce(value.get(1)).value();
                double width = PNumber.coerce(value.get(2)).value();
                double height = PNumber.coerce(value.get(3)).value();
                changeMeasure.setBounds(x, y, width, height);
                this.value = value;
            } catch (ArgumentFormatException ex) {
                throw new IllegalArgumentException(ex);
            }

        }

        public PArray getBoundValue() {
            return value;
        }
    }

    private class TriggerBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            diffOutput.send(time, changeMeasure.getChange());
        }
    }
}
