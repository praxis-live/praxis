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
package net.neilcsmith.praxis.components.math;

import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractExecutionContextComponent;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.FloatInputPort;
import net.neilcsmith.praxis.impl.NumberProperty;
import net.neilcsmith.praxis.impl.TriggerControl;
import net.neilcsmith.praxis.util.Normalizer;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class Normalize extends AbstractExecutionContextComponent {
    
    private Normalizer normalizer;
    private ControlPort.Output output;
    
    public Normalize() {
        normalizer = new Normalizer();
        registerPort(Port.IN, FloatInputPort.create(new InputBinding()));
        output = new DefaultControlOutputPort(this);
        registerPort(Port.OUT, output);
        registerControl("average", NumberProperty.create(new AverageBinding(), 0, 1, 0));
        registerControl("correction", NumberProperty.create(new CorrectionBinding(), 0, 1, 0));
        TriggerControl reset = TriggerControl.create(new ResetBinding());
        registerControl("reset", reset);
    }

    public void stateChanged(ExecutionContext source) {
        normalizer.reset();
    }
    
    
    private class InputBinding implements FloatInputPort.Binding {

        public void receive(long time, double value) {
            output.send(time, normalizer.normalize(value));
        }
        
    }
    
    private class AverageBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            normalizer.setAverage(value);
        }

        public double getBoundValue() {
            return normalizer.getAverage();
        }
        
    }
    
    private class CorrectionBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            normalizer.setCorrection(value);
        }

        public double getBoundValue() {
            return normalizer.getCorrection();
        }
        
    }
    
    private class ResetBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            normalizer.reset();
        }
        
    }
    
    
}
