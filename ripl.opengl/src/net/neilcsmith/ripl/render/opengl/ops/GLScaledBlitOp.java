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
package net.neilcsmith.ripl.render.opengl.ops;

import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.SurfaceOp;
import net.neilcsmith.ripl.ops.Blend;
import net.neilcsmith.ripl.ops.Bounds;
import net.neilcsmith.ripl.ops.ScaledBlit;
import net.neilcsmith.ripl.render.opengl.internal.GLRenderer;
import net.neilcsmith.ripl.render.opengl.internal.GLSurface;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class GLScaledBlitOp extends AbstractBlitOp {

    GLScaledBlitOp() {
        super(ScaledBlit.class);
    }

    @Override
    public void process(SurfaceOp op, GLSurface output, Bypass bypass, Surface... inputs) {
        if (inputs.length > 0 && inputs[0] instanceof GLSurface) {
            if (process((ScaledBlit) op, output, (GLSurface) inputs[0])) {
                return;
            }
        }
        bypass.process(op, inputs);
    }

    private boolean process(ScaledBlit blit, GLSurface dst, GLSurface src) {
        try {
            Blend blend = (Blend) blit.getBlendFunction();
            if (canProcess(blend)) {
                GLRenderer renderer = GLRenderer.get(dst);
                setupBlending(renderer, blend, src.hasAlpha(), dst.hasAlpha());
                Bounds srcBnds = blit.getSourceRegion();
                Bounds dstBnds = blit.getDestinationRegion();
                
                int srcX = srcBnds == null ? 0 : srcBnds.getX();
                int srcW = srcBnds == null ? src.getWidth() : srcBnds.getWidth();          
                int srcY = srcBnds == null ? 0 : srcBnds.getY();
                int srcH = srcBnds == null ? src.getHeight() : srcBnds.getHeight();
                
                int dstW = dstBnds == null ? dst.getWidth() : dstBnds.getWidth();
                int dstX = dstBnds == null ? 0 : dstBnds.getX();
                int dstH = dstBnds == null ? dst.getHeight() : dstBnds.getHeight();
                int dstY = dstBnds == null ? 0 : dst.getHeight() - (dstBnds.getY() + dstH);
                
                renderer.draw(src, srcX, srcY, srcW, srcH, dstX, dstY, dstW, dstH);
                return true;
            }
        } catch (Exception ex) {
            // fall through
        }
        return false;
    }
}
