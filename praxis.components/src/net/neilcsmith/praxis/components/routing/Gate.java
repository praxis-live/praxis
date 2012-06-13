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

import net.neilcsmith.praxis.core.*;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.impl.*;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Gate extends AbstractExecutionContextComponent {

    private BooleanProperty active;
    private double[] pattern;
    private int idx;
    private ControlPort.Output out;
    private ControlPort.Output discard;

    public Gate() {
        registerPort(Port.IN, new InputPort());
        out = new DefaultControlOutputPort();
        registerPort(Port.OUT, out);
        discard = new DefaultControlOutputPort();
        registerPort("discard", discard);
        active = BooleanProperty.create(this, false);
        registerControl("active", active);
        registerPort("active", active.createPort());
        ArrayProperty pt = ArrayProperty.create(new PatternBinding(), PArray.EMPTY);
        registerControl("pattern", pt);
        registerPort("pattern", pt.createPort());
        TriggerControl retrigger = TriggerControl.create(new TriggerControl.Binding() {

            public void trigger(long time) {
                retrigger();
            }
        });
        registerControl("retrigger", retrigger);
        registerPort("retrigger", retrigger.createPort());
        
        pattern = new double[0];
        idx = 0;
        
    }

    public void stateChanged(ExecutionContext source) {
        retrigger();
    }
    
    private void retrigger() {
        idx = 0;
    }

    private class InputPort extends AbstractControlInputPort {

        @Override
        public void receive(long time, double value) {
            if (checkSend()) {
                out.send(time, value);
            } else {
                discard.send(time, value);
            }
        }

        @Override
        public void receive(long time, Argument value) {
            if (checkSend()) {
                out.send(time, value);
            } else {
                discard.send(time, value);
            }
        }

        private boolean checkSend() {
            if (active.getValue()) {
                if (pattern.length == 0) {
                    return true;
                }
                double p = pattern[idx];
                idx++;
                idx %= pattern.length;
                if (p == 1) {
                    return true;
                } else if (p == 0) {
                    return false;
                } else {
                    return Math.random() < p;
                }
            } else {
                return false;
            }
        }
    }

    private class PatternBinding implements ArrayProperty.Binding {

        private PArray value = PArray.EMPTY;

        public void setBoundValue(long time, PArray value) {

            double[] pt = new double[value.getSize()];
            double d;
            for (int i = 0; i < pt.length; i++) {
                try {
                    d = PNumber.coerce(value.get(i)).value();
                } catch (ArgumentFormatException argumentFormatException) {
                    throw new IllegalArgumentException(argumentFormatException);
                }
                if (d < 0 || d > 1) {
                    throw new IllegalArgumentException("Value " + d + " out of range");
                }
                pt[i] = d;
            }
            pattern = pt;
            this.value = value;
        }

        public PArray getBoundValue() {
            return value;
        }
    }
}
