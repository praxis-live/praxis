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

import java.awt.Rectangle;
import net.neilcsmith.praxis.video.pgl.PGLGraphics;
import net.neilcsmith.praxis.video.pgl.PGLSurface;

import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.ops.BlendMode;
import net.neilcsmith.praxis.video.render.ops.Blit;
import processing.core.PImage;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class PGLBlitOp extends AbstractBlitOp {
    
    private Rectangle rect;

    PGLBlitOp() {
        super(Blit.class);
        rect = new Rectangle();
    }

    @Override
    public void process(SurfaceOp op, PGLSurface output, Bypass bypass, Surface... inputs) {
        if (inputs.length > 0) {
            if (process((Blit) op, output, inputs[0])) {
                return;
            }
        }
        bypass.process(op, inputs);
    }

    private boolean process(Blit blit, PGLSurface dst, Surface src) {
        try {
            BlendMode mode = blit.getBlendMode();
            if (canProcess(mode)) {
                PGLGraphics g = dst.getGraphics();
                PImage img = dst.getContext().asImage(src);
                assert g != img;
                g.beginDraw();
                setupBlending(g, mode, (float) blit.getOpacity());
                Rectangle bounds = blit.getSourceRegion(rect);
                if (bounds == null) {
                    g.image(img, blit.getX(), blit.getY());
                } else {
                    int x = blit.getX();
                    int y = blit.getY();
                    int w = bounds.width;
                    int h = bounds.height;
                    g.image(img, x, y, w, h, bounds.x, bounds.y, bounds.x + w, bounds.y + h);
                }
//                g.endDraw();
                return true;
            }
        } catch (Exception ex) {
            // fall through
        }
        return false;
    }


 
}
