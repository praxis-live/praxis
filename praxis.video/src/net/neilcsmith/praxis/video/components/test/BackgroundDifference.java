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

package net.neilcsmith.praxis.video.components.test;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.impl.StringProperty;
import net.neilcsmith.praxis.video.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.DefaultVideoOutputPort;
import net.neilcsmith.ripl.components.Placeholder;
import net.neilcsmith.ripl.components.temporal.BackgroundDifference.Mode;
import net.neilcsmith.ripl.SinkIsFullException;
import net.neilcsmith.ripl.SourceIsFullException;

/**
 *
 * @author Neil C Smith
 */
public class BackgroundDifference extends AbstractComponent {
    
    private net.neilcsmith.ripl.components.temporal.BackgroundDifference diff;
    
    public BackgroundDifference() {
        try {
            diff = new net.neilcsmith.ripl.components.temporal.BackgroundDifference();
            Placeholder pl1 = new Placeholder();
            Placeholder pl2 = new Placeholder();
            diff.addSource(pl1);
            diff.addSource(pl2);
            registerPort(Port.IN, new DefaultVideoInputPort(this, pl1));
            registerPort("background", new DefaultVideoInputPort(this, pl2));
            registerPort(Port.OUT, new DefaultVideoOutputPort(this, diff));
            StringProperty mode = StringProperty.create( new ModeBinding(), getModeStrings(), diff.getMode().name());
            registerControl("mode", mode);
            FloatProperty threshold = FloatProperty.create( new ThresholdBinding(), 0, 1, 0);
            registerControl("threshold", threshold);
            registerPort("threshold", threshold.createPort());
        } catch (SinkIsFullException ex) {
            Logger.getLogger(BackgroundDifference.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SourceIsFullException ex) {
            Logger.getLogger(BackgroundDifference.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String[] getModeStrings() {
        Mode[] modes = Mode.values();
        String[] strings = new String[modes.length];
        for (int i=0; i < modes.length; i++) {
            strings[i] = modes[i].name();
        }
        return strings;
    }
    
    private class ModeBinding implements StringProperty.Binding {

        public void setBoundValue(long time, String value) {
            diff.setMode(Mode.valueOf(value));
        }

        public String getBoundValue() {
            return diff.getMode().name();
        }
        
    }
    
    private class ThresholdBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            diff.setThreshold(value);
        }

        public double getBoundValue() {
            return diff.getThreshold();
        }
        
    }

}
