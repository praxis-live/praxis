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
package org.praxislive.video.impl.components;

import org.praxislive.core.Port;
import org.praxislive.impl.AbstractComponent;
import org.praxislive.impl.NumberProperty;
import org.praxislive.impl.StringProperty;
import org.praxislive.video.impl.VideoInputPortEx;
import org.praxislive.video.impl.VideoOutputPortEx;
import org.praxislive.video.pipes.VideoPipe;
import org.praxislive.video.pipes.impl.MultiInOut;
import org.praxislive.video.pipes.impl.Placeholder;
import org.praxislive.video.render.Surface;
import org.praxislive.video.render.ops.BlendMode;
import org.praxislive.video.render.ops.Blit;

/**
 *
 * @author Neil C Smith
 */
public class XFader extends AbstractComponent {

    private static enum MixMode {

        Normal,
        Add,
        Difference,
        BitXor
    }
    private XFaderPipe mixer;
    private Placeholder pl1;
    private Placeholder pl2;

    public XFader() {
//        try {
        mixer = new XFaderPipe();
        pl1 = new Placeholder();
        pl2 = new Placeholder();
        mixer.addSource(pl1);
        mixer.addSource(pl2);
        registerPort(PortEx.IN + "-1", new VideoInputPortEx(this, pl1));
        registerPort(PortEx.IN + "-2", new VideoInputPortEx(this, pl2));
        registerPort(PortEx.OUT, new VideoOutputPortEx(this, mixer));
        StringProperty mode = createModeControl();
        registerControl("mode", mode);
        registerPort("mode", mode.createPort());
        NumberProperty mix = createMixControl();
        registerControl("mix", mix);
        registerPort("mix", mix.createPort());
//        } catch (SinkIsFullException ex) {
//            Logger.getLogger(XFader.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (SourceIsFullException ex) {
//            Logger.getLogger(XFader.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private NumberProperty createMixControl() {
        NumberProperty.Binding binding = new NumberProperty.Binding() {
            @Override
            public void setBoundValue(long time, double value) {
                mixer.setMix(value);
            }

            @Override
            public double getBoundValue() {
                return mixer.getMix();
            }
        };
        return NumberProperty.create(binding, 0, 1, 0);
    }

    private StringProperty createModeControl() {
        MixMode[] modes = MixMode.values();
        String[] allowed = new String[modes.length];
        for (int i = 0; i < modes.length; i++) {
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

    private class XFaderPipe extends MultiInOut {

        private double mix;
        private MixMode mode;

        private XFaderPipe() {
            super(2, 1);
            this.mode = MixMode.Normal;
        }

        @Override
        protected void process(Surface[] inputs, Surface output, int idx, boolean rendering) {
            if (!rendering) {
                return;
            }
            switch (inputs.length) {
                case 0:
                    output.clear();
                    break;
                case 1:
                    drawSingle(inputs[0], output);
                    break;
                default:
                    drawComposite(inputs[0], inputs[1], output);
            }
        }

        public void setMix(double mix) {
            mix = mix < 0 ? 0.0 : (mix > 1 ? 1.0 : mix);
            this.mix = mix;
        }

        public double getMix() {
            return this.mix;
        }

        public void setMode(MixMode mode) {
            if (mode == null) {
                throw new NullPointerException();
            }
            this.mode = mode;
        }

        public MixMode getMode() {
            return mode;
        }

        private void drawSingle(Surface input, Surface output) {
            if (mix == 0.0) {
                output.copy(input);
            } else if (mix == 1.0) {
                output.clear();
            } else {
                output.process(new Blit().setBlendMode(BlendMode.Normal).setOpacity(1 - mix), input);
            }
            input.release();
        }

        private void drawComposite(Surface input1, Surface input2, Surface output) {
            if (mix == 0.0) {
                output.process(Blit.op(), input1);
            } else if (mix == 1.0) {
                output.process(Blit.op(), input2);
            } else {

                switch (mode) {
                    case Add:
                        renderAdd(input1, input2, output);
                        break;
                    case Difference:
                        renderDifference(input1, input2, output);
                        break;
                    case BitXor:
                        renderBitXor(input1, input2, output);
                        break;
                    default:
                        renderBlend(input1, input2, output);
                }
            }
        }

        private void renderBlend(Surface input1, Surface input2, Surface output) {
            if (output.hasAlpha()) {
                output.process(new Blit().setBlendMode(BlendMode.Add).setOpacity(1 - mix), input1);
                output.process(new Blit().setBlendMode(BlendMode.Add).setOpacity(mix), input2);
                input1.release();
            } else {
                output.copy(input1);
                input1.release();
                output.process(new Blit().setBlendMode(BlendMode.Normal).setOpacity(mix), input2);
            }
            input2.release();
        }

        private void renderBitXor(Surface input1, Surface input2, Surface output) {
            double alpha;
            Surface src;
            Surface dst;
            if (mix >= 0.5) {
                alpha = (1.0 - mix) * 2;
                src = input1;
                dst = input2;
            } else {
                alpha = mix * 2;
                src = input2;
                dst = input1;
            }
            output.copy(dst);
            dst.release();
            output.process(new Blit().setBlendMode(BlendMode.BitXor).setOpacity(alpha), src);
            src.release();
        }
        
        private void renderAdd(Surface input1, Surface input2, Surface output) {
            double alpha;
            Surface src;
            Surface dst;
            if (mix >= 0.5) {
                alpha = (1.0 - mix) * 2;
                src = input1;
                dst = input2;
            } else {
                alpha = mix * 2;
                src = input2;
                dst = input1;
            }
            output.copy(dst);
            dst.release();
            output.process(new Blit().setBlendMode(BlendMode.Add).setOpacity(alpha), src);
            src.release();
        }
        
        private void renderDifference(Surface input1, Surface input2, Surface output) {
            double alpha;
            Surface src;
            Surface dst;
            if (mix >= 0.5) {
                alpha = (1.0 - mix) * 2;
                src = input1;
                dst = input2;
            } else {
                alpha = mix * 2;
                src = input2;
                dst = input1;
            }
            output.copy(dst);
            dst.release();
            output.process(new Blit().setBlendMode(BlendMode.Difference).setOpacity(alpha), src);
            src.release();
        }

       

        @Override
        public boolean isRenderRequired(VideoPipe source, long time) {
            if (mix == 0.0) {
                if (getSourceCount() > 1 && source == getSource(1)) {
                    return false;
                }
            } else if (mix == 1.0) {
                if (getSourceCount() > 0 && source == getSource(0)) {
                    return false;
                }
            }
            return super.isRenderRequired(source, time);

        }
    }
}
