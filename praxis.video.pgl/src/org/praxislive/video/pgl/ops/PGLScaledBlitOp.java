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

import java.awt.Rectangle;
import java.util.logging.Level;
import org.praxislive.video.pgl.PGLGraphics;
import org.praxislive.video.pgl.PGLSurface;
import org.praxislive.video.render.Surface;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.ops.BlendMode;
import org.praxislive.video.render.ops.ScaledBlit;
import processing.core.PImage;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class PGLScaledBlitOp extends AbstractBlitOp {
    
    private Rectangle rect;

    PGLScaledBlitOp() {
        super(ScaledBlit.class);
    }

    @Override
    public void process(SurfaceOp op, PGLSurface output, Bypass bypass, Surface... inputs) {
        if (inputs.length > 0) {
            if (process((ScaledBlit) op, output, inputs[0])) {
                return;
            }
        }
        bypass.process(op, inputs);
    }

    private boolean process(ScaledBlit blit, PGLSurface dst, Surface src) {
        try {
            BlendMode mode = blit.getBlendMode();
            if (canProcessDirect(mode)) {
                PGLGraphics g = dst.getGraphics();
                PImage img = dst.getContext().asImage(src);
                g.beginDraw();
                setupBlending(g, mode, (float) blit.getOpacity());
                
                Rectangle bnds = blit.getSourceRegion(rect);      
                int srcX = bnds == null ? 0 : bnds.x;
                int srcW = bnds == null ? src.getWidth() : bnds.width;          
                int srcY = bnds == null ? 0 : bnds.y;
                int srcH = bnds == null ? src.getHeight() : bnds.height;
                
                bnds = blit.getDestinationRegion(rect);
                int dstW = bnds == null ? dst.getWidth() : bnds.width;
                int dstX = bnds == null ? 0 : bnds.x;
                int dstH = bnds == null ? dst.getHeight() : bnds.height;
                int dstY = bnds == null ? 0 : bnds.y;
                
                g.image(img, dstX, dstY, dstW, dstH,
                        srcX, srcY, srcX + srcW, srcY + srcH);
                return true;
            }
        } catch (Exception ex) {
            // fall through
            LOG.log(Level.FINE, "Scaled blit threw exception", ex);
        }
        return false;
    }
}
