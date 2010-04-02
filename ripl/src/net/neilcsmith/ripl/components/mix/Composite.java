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
package net.neilcsmith.ripl.components.mix;

import java.awt.AlphaComposite;
import net.neilcsmith.ripl.composite.BlendComposite;
import net.neilcsmith.ripl.core.Source;
import net.neilcsmith.ripl.core.Surface;
import net.neilcsmith.ripl.core.SurfaceGraphics;
import net.neilcsmith.ripl.core.impl.MultiInputInOut;

/**
 *
 * @author Neil C Smith
 */
public class Composite extends MultiInputInOut {

    public static enum Mode {

        SrcOver,
        DstOver,
        SrcIn,
        DstIn,
        SrcOut,
        DstOut,
        SrcAtop,
        DstAtop,
        Xor,
        AddPin,
        SubPin,
        Difference,
        Multiply,
        Screen,
        BitXor
    }
    private Mode mode = Mode.SrcOver;
    private double mix = 1.0;
    private boolean forceAlpha;
    private java.awt.Composite comp;

    public Composite() {
        super(2);
//        comp = SurfaceComposite.create(Mode.SrcOver, 1.0);
    }

    public void setMix(double mix) {
        if (mix < 0 || mix > 1) {
            throw new IllegalArgumentException();
        }
        this.mix = mix;
        comp = null;
    }

    public double getMix() {
        return mix;
    }

    public void setMode(Mode mode) {
        if (mode == null) {
            throw new NullPointerException();
        }
        this.mode = mode;
        comp = null;
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

    @Override
    public boolean isRenderRequired(Source source, long time) {
        if (mix == 0.0) {
            if (getSourceCount() > 1 && source == getSource(1)) {
                return false;
            }
        }
        return super.isRenderRequired(source, time);

    }

    private void drawComposite(Surface surface) {
//        if (surface.hasAlpha() || forceAlpha) {
//            surface.clear();
//        }
        if (comp == null) {
            comp = createComposite(mode, (float) mix);
        }
        Surface src = getInputSurface(1);
        Surface dst = getInputSurface(0);
        // composite into dst in case surface doesn't have alpha
        SurfaceGraphics g = dst.getGraphics();
        g.setComposite(comp);
        g.drawSurface(src, 0, 0);
        surface.getGraphics().drawSurface(dst, 0, 0);
    }

    private void drawSingle(Surface surface) {
//        if (surface.hasAlpha() || forceAlpha) {
//            surface.clear();
//        }
        SurfaceGraphics g = surface.getGraphics();
        g.drawSurface(getInputSurface(0), 0, 0);
    }

    @Override
    protected Surface validateInputSurface(Surface input, Surface output, int index) {
        if (forceAlpha) {
            if (input == null || !input.hasAlpha() || !output.checkCompatible(input, true, false)) {
                return output.createSurface(output.getWidth(), output.getHeight(), true, null);
            } else {
                return input;
            }
        } else {
            return super.validateInputSurface(input, output, index);
        }

    }

    private java.awt.Composite createComposite(Mode mode, float mix) {
        switch (mode) {
            case SrcOver:
                return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, mix);
            case DstOver:
                return AlphaComposite.getInstance(AlphaComposite.DST_OVER, mix);
            case SrcIn:
                return AlphaComposite.getInstance(AlphaComposite.SRC_IN, mix);
            case DstIn:
                return AlphaComposite.getInstance(AlphaComposite.DST_IN, mix);
            case SrcOut:
                return AlphaComposite.getInstance(AlphaComposite.SRC_OUT, mix);
            case DstOut:
                return AlphaComposite.getInstance(AlphaComposite.DST_OUT, mix);
            case SrcAtop:
                return AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, mix);
            case DstAtop:
                return AlphaComposite.getInstance(AlphaComposite.DST_ATOP, mix);
            case Xor:
                return AlphaComposite.getInstance(AlphaComposite.XOR, mix);
            case AddPin:
                return BlendComposite.getInstance(BlendComposite.Mode.AddPin, mix);
            case SubPin:
                return BlendComposite.getInstance(BlendComposite.Mode.SubPin, mix);
            case Difference:
                return BlendComposite.getInstance(BlendComposite.Mode.Difference, mix);
            case Multiply:
                return BlendComposite.getInstance(BlendComposite.Mode.Multiply, mix);
            case Screen:
                return BlendComposite.getInstance(BlendComposite.Mode.Screen, mix);
            case BitXor:
                return BlendComposite.getInstance(BlendComposite.Mode.BitXor, mix);
            default:
                // should not be possible!
                return AlphaComposite.SrcOver;
        }
    }
}

