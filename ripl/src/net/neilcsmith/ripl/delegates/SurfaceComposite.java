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
package net.neilcsmith.ripl.delegates;

import java.awt.AlphaComposite;
import net.neilcsmith.ripl.core.Surface;
import net.neilcsmith.ripl.rgbmath.RGBComposite;

/**
 *
 * @author Neil C Smith
 */
public class SurfaceComposite extends CompositeDelegate {

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
        BitXor,
        Blend
    }

    private static CompositeDelegate getContext(Mode mode, double extraAlpha) {
        switch (mode) {
            case SrcOver:
                return new AlphaCompositeContext(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) extraAlpha));
            case DstOver:
                return new AlphaCompositeContext(AlphaComposite.getInstance(AlphaComposite.DST_OVER, (float) extraAlpha));
            case SrcIn:
                return new AlphaCompositeContext(AlphaComposite.getInstance(AlphaComposite.SRC_IN, (float) extraAlpha));
            case DstIn:
                return new AlphaCompositeContext(AlphaComposite.getInstance(AlphaComposite.DST_IN, (float) extraAlpha));
            case SrcOut:
                return new AlphaCompositeContext(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, (float) extraAlpha));
            case DstOut:
                return new AlphaCompositeContext(AlphaComposite.getInstance(AlphaComposite.DST_OUT, (float) extraAlpha));
            case SrcAtop:
                return new AlphaCompositeContext(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, (float) extraAlpha));
            case DstAtop:
                return new AlphaCompositeContext(AlphaComposite.getInstance(AlphaComposite.DST_ATOP, (float) extraAlpha));
            case Xor:
                return new AlphaCompositeContext(AlphaComposite.getInstance(AlphaComposite.XOR, (float) extraAlpha));
            case AddPin:
                return new RGBCompositeContext(new RGBComposite.AddPin(extraAlpha));
            case SubPin:
                return new RGBCompositeContext(new RGBComposite.SubPin(extraAlpha));
            case Difference:
                return new RGBCompositeContext(new RGBComposite.Difference(extraAlpha));
            case Multiply:
                return new RGBCompositeContext(new RGBComposite.Multiply(extraAlpha));
            case Screen:
                return new RGBCompositeContext(new RGBComposite.Screen(extraAlpha));
            case BitXor:
                return new RGBCompositeContext(new RGBComposite.BitXor(extraAlpha));
            case Blend:
                return new RGBCompositeContext(new RGBComposite.Blend(extraAlpha));
            default:
                // should not be possible!
                return new AlphaCompositeContext(AlphaComposite.SrcOver);
        }
    }
    private Mode mode;
    private CompositeDelegate swContext;
    private double extraAlpha;

    private SurfaceComposite(Mode mode, double extraAlpha) {
        if (extraAlpha < 0 || extraAlpha > 1) {
            throw new IllegalArgumentException("Extra Alpha out of range");
        }
        this.mode = mode;
        this.extraAlpha = extraAlpha;
        swContext = getContext(mode, extraAlpha);
    }

    public void process(Surface src, Surface dstIn, Surface dstOut, int x, int y, boolean rendering) {
        swContext.process(src, dstIn, dstOut, x, y, rendering);
    }


    public double getAlpha() {
        return extraAlpha;
    }

    public Mode getMode() {
        return mode;
    }

    public SurfaceComposite derive(double alpha) {
        return new SurfaceComposite(this.mode, alpha);
    }
    
    public static SurfaceComposite create(Mode mode, double extraAlpha) {
        return new SurfaceComposite(mode, extraAlpha);
    }
}
