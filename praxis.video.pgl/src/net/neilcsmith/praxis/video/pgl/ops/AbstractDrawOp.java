/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2014 Neil C Smith.
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
package net.neilcsmith.praxis.video.pgl.ops;

import java.awt.Color;
import java.util.EnumSet;
import net.neilcsmith.praxis.video.pgl.PGLGraphics;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.ops.BlendMode;
import static net.neilcsmith.praxis.video.render.ops.BlendMode.*;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
abstract class AbstractDrawOp extends AbstractBlendOp {


    protected AbstractDrawOp(Class<? extends SurfaceOp> opClass) {
        super(opClass);
    }

    void configure(PGLGraphics pg, BlendMode mode, float opacity, Color fillColor) {// boolean srcAlpha, boolean dstAlpha) {

        configure(pg, mode, opacity, fillColor, null);
        
    }
    
    void configure(PGLGraphics pg, BlendMode mode, float opacity, Color fillColor, Color strokeColor) {
        
        setupBlending(pg, mode);
        if (fillColor != null) {
            int a = fillColor.getAlpha();
            int r = fillColor.getRed();
            int g = fillColor.getGreen();
            int b = fillColor.getBlue();
            float xa = a == 255 ? opacity : opacity * (a / 255.f);
            pg.fill(r * xa, g * xa, b * xa, a * opacity);
        } else {
            pg.noFill();
        }
        
        if (strokeColor != null) {
            int a = strokeColor.getAlpha();
            int r = strokeColor.getRed();
            int g = strokeColor.getGreen();
            int b = strokeColor.getBlue();
            float xa = a == 255 ? opacity : opacity * (a / 255.f);
            pg.stroke(r * xa, g * xa, b * xa, a * opacity);
        } else {
            pg.noStroke();
        }
        
    }
}
