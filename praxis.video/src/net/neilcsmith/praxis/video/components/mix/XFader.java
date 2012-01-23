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
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.impl.StringProperty;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;

import net.neilcsmith.ripl.components.Placeholder;
import net.neilcsmith.ripl.SinkIsFullException;
import net.neilcsmith.ripl.SourceIsFullException;

import net.neilcsmith.ripl.components.mix.XFader.MixMode;

/**
 *
 * @author Neil C Smith
 */
public class XFader extends AbstractComponent {

    private net.neilcsmith.ripl.components.mix.XFader mixer;
    private Placeholder pl1;
    private Placeholder pl2;

    public XFader() {
        try {
            mixer = new net.neilcsmith.ripl.components.mix.XFader();
            pl1 = new Placeholder();
            pl2 = new Placeholder();
            mixer.addSource(pl1);
            mixer.addSource(pl2);
            registerPort(Port.IN + "-1", new DefaultVideoInputPort(this, pl1));
            registerPort(Port.IN + "-2", new DefaultVideoInputPort(this, pl2));
            registerPort(Port.OUT, new DefaultVideoOutputPort(this, mixer));
            StringProperty mode = createModeControl();
            registerControl("mode", mode);
            registerPort("mode", mode.createPort());
            FloatProperty mix = createMixControl();
            registerControl("mix", mix);
            registerPort("mix", mix.createPort());         
        } catch (SinkIsFullException ex) {
            Logger.getLogger(XFader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SourceIsFullException ex) {
            Logger.getLogger(XFader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private FloatProperty createMixControl() {
        FloatProperty.Binding binding = new FloatProperty.Binding() {

            @Override
            public void setBoundValue(long time, double value) {
                mixer.setMix(value);
            }

            @Override
            public double getBoundValue() {
                return mixer.getMix();
            }
            };
        return FloatProperty.create(binding, 0, 1, 0);
    }

    private StringProperty createModeControl() {
        MixMode[] modes = MixMode.values();
        String[] allowed = new String[modes.length];
        for (int i=0; i < modes.length; i++) {
            allowed[i] = modes[i].name();
        }
        StringProperty.Binding binding = new StringProperty.Binding() {

            @Override
            public void setBoundValue(long time, String value) {
                mixer.setMode(MixMode.valueOf(value));
            }

            @Override
            public String getBoundValue() {
                return mixer.getMode().name();
            }
        };
        return StringProperty.create(binding, allowed, mixer.getMode().name());
    }
//    private class MixControl extends BasicControl {
//
//        private MixControl() {
//            super(XFader.this);
//        }
//        
//        @Override
//        protected Call processInvoke(Call call, boolean quiet) throws Exception {
//            CallArguments args = call.getArgs();
//            if (args.getCount() == 1) {
//                float val = Float.valueOf(args.getArg(0).toString()).floatValue();
//                if (val < 0 || val > 1) {
//                    return createError(call, "Invalid Value");
//                }
//                mixer.setMix(val);
//                
//            }
//            if (!quiet) {
//                return Call.createReturnCall(call, PNumber.valueOf(mixer.getMix()));
//            }
//            return null;
//        }
//
//        public ControlInfo getInfo() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }
//    private class ModeControl extends BasicControl {
//
//        private ModeControl() {
//            super(XFader.this);
//        }
//        
//        @Override
//        protected Call processInvoke(Call call, boolean quiet) throws Exception {
//            CallArguments args = call.getArgs();
//            if (args.getCount() == 1) {
//                String val = args.getArg(0).toString();
//                XFader.MixMode mode = XFader.MixMode.valueOf(val);
//                mixer.setMode(mode);
//                
//            }
//            if (!quiet) {
//                return Call.createReturnCall(call, PString.valueOf(mixer.getMode()));
//            }
//            return null;
//        }
//
//        public ControlInfo getInfo() {
//            return null;
//        }
//        
//    }
}
