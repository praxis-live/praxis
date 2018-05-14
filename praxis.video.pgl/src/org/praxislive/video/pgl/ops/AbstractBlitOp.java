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

import org.praxislive.video.pgl.PGLGraphics;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.ops.BlendMode;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
abstract class AbstractBlitOp extends AbstractBlendOp {

    protected AbstractBlitOp(Class<? extends SurfaceOp> opClass) {
        super(opClass);
    }

    void setupBlending(PGLGraphics g, BlendMode mode, float opacity) {// boolean srcAlpha, boolean dstAlpha) {

//        renderer.enableBlending();
        setupBlending(g, mode);
        opacity *= 255;
        g.tint(255, 255, 255, opacity);
        
    }
}
