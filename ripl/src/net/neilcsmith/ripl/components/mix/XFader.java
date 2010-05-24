package net.neilcsmith.ripl.components.mix;

import net.neilcsmith.ripl.Source;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.SurfaceGraphics;
import net.neilcsmith.ripl.impl.MultiInputInOut;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import net.neilcsmith.ripl.ops.Blend;
import net.neilcsmith.ripl.ops.Blit;

/**
 *
 * @author Neil C Smith
 */
public class XFader extends MultiInputInOut {

    public static enum MixMode {

        Blend,
        AddPin,
        Difference,
        BitXor
    }
    private double mix;
    private MixMode mode;

    public XFader() {
        this(MixMode.Blend);
    }

    public XFader(MixMode mode) {
        super(2);
        this.mode = mode;
    }

    @Override
    protected void process(Surface surface, boolean rendering) {
        if (rendering) {
            switch (getSourceCount()) {
                case 0:
//                    surface.clear();
                    break;
                case 1:
                    drawSingle(surface);
                    break;
                default:
                    drawComposite(surface);
            }
        }
        releaseSurfaces();
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

    private void drawSingle(Surface surface) {
//        SurfaceGraphics g = surface.getGraphics();
        if (mix == 0.0) {
//            if (surface.hasAlpha()) {
//                surface.clear();
//            }
//            g.drawSurface(getInputSurface(0), 0, 0);
            surface.process(Blit.op(), getInputSurface(0));
        } else if (mix == 1.0) {
//            surface.clear();
        } else {
//            surface.clear();
//            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - (float) mix));
//            g.drawSurface(getInputSurface(0), 0, 0);
            surface.process(Blit.op(Blend.NORMAL.opacity(1 - mix)), getInputSurface(0));
        }

    }

    private void drawComposite(Surface surface) {
//        if (surface.hasAlpha()) {
//            surface.clear();
//        }
        if (mix == 0.0) {
//            surface.getGraphics().drawSurface(getInputSurface(0), 0, 0);
            surface.process(Blit.op(), getInputSurface(0));
        } else if (mix == 1.0) {
//            surface.getGraphics().drawSurface(getInputSurface(1), 0, 0);
            surface.process(Blit.op(), getInputSurface(1));
        } else {

            switch (mode) {
                case AddPin:
                    renderAddPin(surface);
                    break;
                case Difference:
                    renderDifference(surface);
                    break;
                case BitXor:
                    renderBitXor(surface);
                    break;
                default:
                    renderBlend(surface);
            }


        }
    }

    private void renderBlend(Surface surface) {
//        SurfaceGraphics g = surface.getGraphics();
        if (surface.hasAlpha()) {
//            SurfaceComposite comp = SurfaceComposite.create(Mode.AddPin, 1 - mix);
//            comp.process(getInputSurface(0), surface, surface, 0, 0, true);
//            comp = SurfaceComposite.create(SurfaceComposite.Mode.AddPin, mix);
//            comp.process(getInputSurface(1), surface, surface, 0, 0, true);
            surface.process(Blit.op(Blend.ADD.opacity(1 - mix)), getInputSurface(0));
            surface.process(Blit.op(Blend.ADD.opacity(mix)), getInputSurface(1));

        } else {
//            g.drawSurface(getInputSurface(0), 0, 0);
//            SurfaceComposite comp = SurfaceComposite.create(Mode.SrcOver, mix);
//            comp.process(getInputSurface(1), surface, surface, 0, 0, true);
            surface.process(Blit.op(), getInputSurface(0));
            surface.process(Blit.op(Blend.NORMAL.opacity(mix)), getInputSurface(1));
        }

//        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (1 - mix) ));
//        g.drawSurface(getInputSurface(0), 0, 0);
//        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) mix ));
//        g.drawSurface(getInputSurface(1), 0, 0);
    }

    private void renderBitXor(Surface surface) {
        double alpha;
        Surface src;
        Surface dst;
        if (mix >= 0.5) {
            alpha = (1.0 - mix) * 2;
            src = getInputSurface(0);
            dst = getInputSurface(1);
        } else {
            alpha = mix * 2;
            src = getInputSurface(1);
            dst = getInputSurface(0);
        }
//        SurfaceComposite comp = SurfaceComposite.create(Mode.BitXor, alpha);
//        comp.process(src, dst, surface, 0, 0, true);
//        surface.getGraphics().drawSurface(dst, 0, 0);
        dst.process(Blit.op(Blend.BITXOR.opacity(alpha)), src);
        surface.copy(dst);
    }

    private void renderAddPin(Surface surface) {
        double alpha;
        Surface src;
        Surface dst;
        if (mix >= 0.5) {
            alpha = (1.0 - mix) * 2;
            src = getInputSurface(0);
            dst = getInputSurface(1);
        } else {
            alpha = mix * 2;
            src = getInputSurface(1);
            dst = getInputSurface(0);
        }
//        SurfaceComposite comp = SurfaceComposite.create(SurfaceComposite.Mode.AddPin, alpha);
//        comp.process(src, dst, surface, 0, 0, true);
//        surface.getGraphics().drawSurface(dst, 0, 0);
        dst.process(Blit.op(Blend.ADD.opacity(alpha)), src);
        surface.copy(dst);
    }

    private void renderDifference(Surface surface) {
        double alpha;
        Surface src;
        Surface dst;
        if (mix >= 0.5) {
            alpha = (1.0 - mix) * 2;
            src = getInputSurface(0);
            dst = getInputSurface(1);
        } else {
            alpha = mix * 2;
            src = getInputSurface(1);
            dst = getInputSurface(0);
        }
//        SurfaceComposite comp = SurfaceComposite.create(SurfaceComposite.Mode.Difference, alpha);
//        comp.process(src, dst, surface, 0, 0, true);
//        surface.getGraphics().drawSurface(dst, 0, 0);
        dst.process(Blit.op(Blend.DIFFERENCE.opacity(alpha)), src);
        surface.copy(dst);
    }

    @Override
    public boolean isRenderRequired(Source source, long time) {
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
