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
package net.neilcsmith.praxis.video.components.mix;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.impl.StringProperty;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.ripl.components.Placeholder;
import net.neilcsmith.ripl.SinkIsFullException;
import net.neilcsmith.ripl.SourceIsFullException;
import net.neilcsmith.ripl.components.mix.Composite.Mode;

/**
 *
 * @author Neil C Smith
 */
public class Composite extends AbstractComponent {

    private net.neilcsmith.ripl.components.mix.Composite comp;
    private Placeholder dst;
    private Placeholder src;

    public Composite() {
        try {
            comp = new net.neilcsmith.ripl.components.mix.Composite();
            dst = new Placeholder();
            src = new Placeholder();
            comp.addSource(dst);
            comp.addSource(src);
            
            registerPort(Port.IN, new DefaultVideoInputPort(this, dst));
            registerPort("src", new DefaultVideoInputPort(this, src));
            registerPort(Port.OUT, new DefaultVideoOutputPort(this, comp));
                 
            StringProperty mode = createModeControl();
            registerControl("mode", mode);
            registerPort("mode", mode.createPort());

            FloatProperty mix = createMixControl();
            registerControl("mix", mix);
            registerPort("mix", mix.createPort());
            
            registerControl("force-alpha", createAlphaControl());


        } catch (SinkIsFullException ex) {
            Logger.getLogger(Composite.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SourceIsFullException ex) {
            Logger.getLogger(Composite.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    private FloatProperty createMixControl() {
        FloatProperty.Binding binding = new FloatProperty.Binding() {

            @Override
            public void setBoundValue(long time, double value) {
                comp.setMix(value);
            }

            @Override
            public double getBoundValue() {
                return comp.getMix();
            }
            };
        return FloatProperty.create(binding, 0, 1, 0);
    }

    private StringProperty createModeControl() {
        Mode[] modes = Mode.values();
        String[] allowed = new String[modes.length];
        for (int i=0; i < modes.length; i++) {
            allowed[i] = modes[i].name();
        }
        StringProperty.Binding binding = new StringProperty.Binding() {

            @Override
            public void setBoundValue(long time, String value) {
                comp.setMode(Mode.valueOf(value));
            }

            @Override
            public String getBoundValue() {
                return comp.getMode().name();
            }
        };
        return StringProperty.create(binding, allowed, comp.getMode().name());
    }
    
    private BooleanProperty createAlphaControl() {
        BooleanProperty.Binding binding = new BooleanProperty.Binding() {

            @Override
            public void setBoundValue(long time, boolean value) {
                comp.setForceAlpha(value);
            }

            @Override
            public boolean getBoundValue() {
                return comp.getForceAlpha();
            }
        };
        return BooleanProperty.create(this, binding, comp.getForceAlpha());
    }
}
