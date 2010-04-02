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

import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.Root.State;
import net.neilcsmith.praxis.impl.AbstractControlFrameComponent;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.FloatProperty;

/**
 *
 * @author Neil C Smith
 */
public class Timer extends AbstractControlFrameComponent {

    private long lastTime = 0;
    private long periodNS = 1000000000;
    private double periodS = 1;
    private ControlPort.Output output;
    
    public Timer() {
        FloatProperty period = FloatProperty.create(this, new PeriodBinding(), 0, 60 * 60, 1);
        registerControl("period", period);
        registerPort("period", period.createPort());
        output = new DefaultControlOutputPort(this);
        registerPort("output", output);
                
    }
    

    @Override
    public void nextControlFrame(Root root) {
        long time = root.getTime();
        if (((lastTime + periodNS) - time) <= 0) {
            output.send(time);
            lastTime += ((time - lastTime) / periodNS) * periodNS;
        }
    }

    @Override
    public void rootStateChanged(Root source, State state) {
        super.rootStateChanged(source, state);
        if (state == Root.State.ACTIVE_RUNNING) {
            lastTime = source.getTime();
            output.send(lastTime);
        }
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        Root root = getRoot();
        if (root != null) {
            lastTime = root.getTime();
        }
    }
    
    
    
    

    private class PeriodBinding implements FloatProperty.Binding {

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
