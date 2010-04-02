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

package net.neilcsmith.praxis.video.components;

import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.impl.TriggerControl;
import net.neilcsmith.praxis.video.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.DefaultVideoOutputPort;

/**
 *
 * @author Neil C Smith
 */
public class Snapshot extends AbstractComponent {
    
    private net.neilcsmith.ripl.components.Snapshot snap;
    
    public Snapshot() {
        snap = new net.neilcsmith.ripl.components.Snapshot();
        registerPort("input", new DefaultVideoInputPort(this, snap));
        registerPort("output", new DefaultVideoOutputPort(this, snap));
        FloatProperty time = FloatProperty.create(this, new TimeBinding(), 0, 60 * 60, 0);
        registerControl("fade-time", time);
        registerPort("fade-time", time.createPort());
        FloatProperty mix = FloatProperty.create(this, new MixBinding(), 0, 1, 1);
        registerControl("mix", mix);
        registerPort("mix", mix.createPort());
        TriggerControl trigger = TriggerControl.create(this, new TriggerBinding());
        registerControl("trigger", trigger);
        registerPort("trigger", trigger.getPort());
    }
    
    private class TriggerBinding implements TriggerControl.Binding {

        public void trigger(long time) {
            snap.trigger();
        }
        
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
