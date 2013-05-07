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
package net.neilcsmith.praxis.video.opengl.ops;

import java.util.EnumSet;
import net.neilcsmith.praxis.video.opengl.internal.Color;
import net.neilcsmith.praxis.video.opengl.internal.GLRenderer;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.ops.Blend;
import net.neilcsmith.praxis.video.render.ops.BlendMode;
import static net.neilcsmith.praxis.video.render.ops.BlendMode.*;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
abstract class AbstractBlitOp extends GLOp {
    
    private final static EnumSet<BlendMode> supportedBlends = 
            EnumSet.of(Normal, Add, Multiply, Mask);

    protected AbstractBlitOp(Class<? extends SurfaceOp> opClass) {
        super(opClass);
    }

    boolean canProcess(BlendMode mode) {
        return supportedBlends.contains(mode);
    }

    void setupBlending(GLRenderer renderer, BlendMode mode, float opacity) {// boolean srcAlpha, boolean dstAlpha) {

//        renderer.enableBlending();

        switch (mode) {
            case Normal:
                renderer.setBlendFunction(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                break;
            case Add:
                renderer.setBlendFunction(GL11.GL_ONE, GL11.GL_ONE);
                break;
            case Multiply:
                renderer.setBlendFunction(GL11.GL_DST_COLOR, GL11.GL_ONE_MINUS_SRC_ALPHA);
                break;
            case Mask:
                renderer.setBlendFunction(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
                break;
            default:
                throw new IllegalArgumentException();
        }

        renderer.setColor(new Color(opacity, opacity, opacity, opacity));
    }
}
