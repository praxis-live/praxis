/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2016 Neil C Smith.
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
package org.praxislive.video.components.mix;

import org.praxislive.core.Port;
import org.praxislive.impl.AbstractComponent;
import org.praxislive.impl.BooleanProperty;
import org.praxislive.impl.NumberProperty;
import org.praxislive.impl.StringProperty;
import org.praxislive.video.impl.VideoInputPortEx;
import org.praxislive.video.impl.VideoOutputPortEx;
import org.praxislive.video.pipes.impl.MultiInOut;
import org.praxislive.video.pipes.impl.Placeholder;
import org.praxislive.video.render.Surface;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.ops.BlendMode;
import org.praxislive.video.render.ops.Blit;

/**
 *
 *  @author Neil C Smith
 */
public class Composite extends AbstractComponent {

    public static enum Mode {

        Normal,
        Add,
        Sub,
        Difference,
        Multiply,
        Screen,
        BitXor,
        Mask
    }
    private CompositePipe comp;
    private Placeholder dst;
    private Placeholder src;

    public Composite() {
//        try {
        comp = new CompositePipe();
        dst = new Placeholder();
        src = new Placeholder();
        comp.addSource(dst);
        comp.addSource(src);

        registerPort(PortEx.IN, new VideoInputPortEx(dst));
        registerPort("src", new VideoInputPortEx(src));
        registerPort(PortEx.OUT, new VideoOutputPortEx(comp));

        StringProperty mode = createModeControl();
        registerControl("mode", mode);
        registerPort("mode", mode.createPort());

        NumberProperty mix = createMixControl();
        registerControl("mix", mix);
        registerPort("mix", mix.createPort());

        registerControl("force-alpha", createAlphaControl());


//        } catch (SinkIsFullException ex) {
//            Logger.getLogger(Composite.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (SourceIsFullException ex) {
//            Logger.getLogger(Composite.class.getName()).log(Level.SEVERE, null, ex);
//        }


    }

    private NumberProperty createMixControl() {
        NumberProperty.Binding binding = new NumberProperty.Binding() {
            @Override
            public void setBoundValue(long time, double value) {
                comp.setMix(value);
            }

            @Override
            public double getBoundValue() {
                return comp.getMix();
            }
        };
        return NumberProperty.create(binding, 0, 1, 0);
    }

    private StringProperty createModeControl() {
        Mode[] modes = Mode.values();
        String[] allowed = new String[modes.length];
        for (int i = 0; i < modes.length; i++) {
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
        return BooleanProperty.create(binding, comp.getForceAlpha());
    }

    private class CompositePipe extends MultiInOut {

        private Mode mode = Mode.Normal;
        private double mix = 1.0;
        private boolean forceAlpha;
        private SurfaceOp blit;

        private CompositePipe() {
            super(2, 1);
        }

        public void setMix(double mix) {
            if (mix < 0 || mix > 1) {
                throw new IllegalArgumentException();
            }
            this.mix = mix;
            blit = null;
        }

        public double getMix() {
            return mix;
        }

        public void setMode(Mode mode) {
            if (mode == null) {
                throw new NullPointerException();
            }
            this.mode = mode;
            blit = null;
        }

        public Mode getMode() {
            return mode;
        }

        public void setForceAlpha(boolean forceAlpha) {
            this.forceAlpha = forceAlpha;
        }

        public boolean getForceAlpha() {
            return forceAlpha;
        }

        @Override
        protected Surface validateInput(Surface input, Surface output, int index) {
            if (forceAlpha && index == 1) {
                if (input == null || 
                        !output.checkCompatible(input, true, false) ||
                        !input.hasAlpha()) {
                    input = output.createSurface(output.getWidth(), output.getHeight(), true);
                }
                return input;
            } else {
                return super.validateInput(input, output, index);
            }            
        }
        
        @Override
        protected void process(Surface[] inputs, Surface output, int outputIndex, boolean rendering) {
            if (!rendering) {
                return;
            }
            Surface in;
            int count = inputs.length;
            if (count == 0) {
                output.clear();
                return;
            }

            in = inputs[0];
            output.copy(inputs[0]);
            in.release();

            if (count == 2) {
                in = inputs[1];
                if (mix > 0) {
                    if (blit == null) {
                        blit = createBlit(mode, mix);
                    }
                    output.process(blit, in);
                }

                in.release();

            }
        }

//        @Override
//        public boolean isRenderRequired(VideoPipe source, long time) {
//            if (mix == 0.0) {
//                if (getSourceCount() > 1 && source == getSource(1)) {
//                    return false;
//                }
//            }
//            return super.isRenderRequired(source, time);
//
//        }
        private SurfaceOp createBlit(Mode mode, double mix) {
            switch (mode) {
                case Normal:
                    return new Blit().setBlendMode(BlendMode.Normal).setOpacity(mix);
                case Add:
                    return new Blit().setBlendMode(BlendMode.Add).setOpacity(mix);
                case Sub:
                    return new Blit().setBlendMode(BlendMode.Sub).setOpacity(mix);
                case Difference:
                    return new Blit().setBlendMode(BlendMode.Difference).setOpacity(mix);
                case Multiply:
                    return new Blit().setBlendMode(BlendMode.Multiply).setOpacity(mix);
                case Screen:
                    return new Blit().setBlendMode(BlendMode.Screen).setOpacity(mix);
                case BitXor:
                    return new Blit().setBlendMode(BlendMode.BitXor).setOpacity(mix);
                case Mask:
                    return new Blit().setBlendMode(BlendMode.Mask).setOpacity(mix);
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}
