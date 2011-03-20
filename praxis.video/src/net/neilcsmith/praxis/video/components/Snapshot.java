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

package net.neilcsmith.praxis.video.components;

import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.impl.TriggerControl;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;

/**
 *
 * @author Neil C Smith
 */
public class Snapshot extends AbstractComponent {
    
    private net.neilcsmith.ripl.components.Snapshot snap;
    
    public Snapshot() {
        snap = new net.neilcsmith.ripl.components.Snapshot();
        registerPort(Port.IN, new DefaultVideoInputPort(this, snap));
        registerPort(Port.OUT, new DefaultVideoOutputPort(this, snap));
        FloatProperty time = FloatProperty.create( new TimeBinding(), 0, 60 * 60, 0);
        registerControl("fade-time", time);
        registerPort("fade-time", time.createPort());
        FloatProperty mix = FloatProperty.create( new MixBinding(), 0, 1, 1);
        registerControl("mix", mix);
        registerPort("mix", mix.createPort());
        TriggerControl trigger = TriggerControl.create( new TriggerControl.Binding() {

            public void trigger(long time) {
                snap.trigger();
            }

        });
        registerControl("trigger", trigger);
        registerPort("trigger", trigger.createPort());
        TriggerControl reset = TriggerControl.create(new TriggerControl.Binding() {

            public void trigger(long time) {
                snap.reset();
            }

        });
        registerControl("reset", reset);
        registerPort("reset", reset.createPort());
    }
    
    private class MixBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            snap.setMix(value);
        }

        public double getBoundValue() {
            return snap.getMix();
        }
        
    }
    
    private class TimeBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            snap.setFadeTime(value);
        }

        public double getBoundValue() {
            return snap.getFadeTime();
        }
        
    }
    
}
