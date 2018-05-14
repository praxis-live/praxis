/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2018 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.video.pgl.ops;

import java.awt.Color;
import org.praxislive.video.pgl.PGLGraphics;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.ops.BlendMode;

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
            pg.fill(
                    fillColor.getRed(),
                    fillColor.getGreen(),
                    fillColor.getBlue(),
                    fillColor.getAlpha() * opacity);
        } else {
            pg.noFill();
        }

        if (strokeColor != null) {
            pg.stroke(
                    strokeColor.getRed(),
                    strokeColor.getGreen(),
                    strokeColor.getBlue(),
                    strokeColor.getAlpha() * opacity);
        } else {
            pg.noStroke();
        }

    }
}
