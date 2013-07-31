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
package net.neilcsmith.praxis.video.components.mix;

import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.NumberProperty;
import net.neilcsmith.praxis.impl.StringProperty;
import net.neilcsmith.praxis.video.impl.DefaultVideoInputPort;
import net.neilcsmith.praxis.video.impl.DefaultVideoOutputPort;
import net.neilcsmith.praxis.video.pipes.VideoPipe;
import net.neilcsmith.praxis.video.pipes.impl.MultiInOut;
import net.neilcsmith.praxis.video.pipes.impl.Placeholder;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.ops.Blend;
import net.neilcsmith.praxis.video.render.ops.Blit;

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

        registerPort(Port.IN, new DefaultVideoInputPort(this, dst));
        registerPort("src", new DefaultVideoInputPort(this, src));
        registerPort(Port.OUT, new DefaultVideoOutputPort(this, comp));

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
                        !output.checkCompatible(output, true, false) ||
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
                    return Blit.op(Blend.NORMAL.opacity(mix));
                case Add:
                    return Blit.op(Blend.ADD.opacity(mix));
                case Sub:
                    return Blit.op(Blend.SUB.opacity(mix));
                case Difference:
                    return Blit.op(Blend.DIFFERENCE.opacity(mix));
                case Multiply:
                    return Blit.op(Blend.MULTIPLY.opacity(mix));
                case Screen:
                    return Blit.op(Blend.SCREEN.opacity(mix));
                case BitXor:
                    return Blit.op(Blend.BITXOR.opacity(mix));
                case Mask:
                    return Blit.op(Blend.MASK.opacity(mix));
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}
