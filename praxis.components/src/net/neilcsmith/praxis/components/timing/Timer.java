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
package net.neilcsmith.praxis.components.timing;

import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractClockComponent;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.NumberProperty;

/**
 *
 * @author Neil C Smith
 */
public class Timer extends AbstractClockComponent {

    private long lastTime = 0;
    private long periodNS = 1000000000;
    private double periodS = 1;
    private ControlPort.Output output;
    private boolean first;
    
    public Timer() {
        NumberProperty period = NumberProperty.create( new PeriodBinding(), 0, 60, 1);
        output = new DefaultControlOutputPort();
        registerPort(Port.OUT, output);
        registerControl("period", period);
        registerPort("period", period.createPort());
        
                
    }

    public void tick(ExecutionContext source) {
        long time = source.getTime();
        if (first) {
            lastTime = time;
            first = false;
            output.send(time);
        } else if (((lastTime + periodNS) - time) <= 0) {
            output.send(time);
            lastTime += ((time - lastTime) / periodNS) * periodNS;
        }
    }

    @Override
    public void stateChanged(ExecutionContext source) {
        first = true;
    }
    
//
//    @Override
//    public void nextControlFrame(AbstractRoot root) {
//        long time = root.getTime();
//        if (((lastTime + periodNS) - time) <= 0) {
//            output.send(time);
//            lastTime += ((time - lastTime) / periodNS) * periodNS;
//        }
//    }
//
//    @Override
//    public void rootStateChanged(AbstractRoot source, RootState state) {
//        super.rootStateChanged(source, state);
//        if (state == RootState.ACTIVE_RUNNING) {
//            lastTime = source.getTime();
//            output.send(lastTime);
//        }
//    }
//
//    @Override
//    public void hierarchyChanged() {
//        super.hierarchyChanged();
//        Root root = getRoot();
//        if (root instanceof AbstractRoot) {
//            lastTime = ((AbstractRoot) root).getTime();
//        }
//    }



    
    
    

    private class PeriodBinding implements NumberProperty.Binding {

        public void setBoundValue(long time, double value) {
            periodS = value;
            periodNS = secsToNanos(value);
        }

        public double getBoundValue() {
            return periodS;
        }

        private long secsToNanos(double secs) {
            if (secs > 0) {
                return (long) (secs * 1e9);
            } else {
                return 1;
            }
        }
    }
}
