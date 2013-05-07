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

import java.awt.Rectangle;
import net.neilcsmith.praxis.video.opengl.internal.GLRenderer;
import net.neilcsmith.praxis.video.opengl.internal.GLSurface;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.ops.BlendMode;
import net.neilcsmith.praxis.video.render.ops.Blit;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class GLBlitOp extends AbstractBlitOp {
    
    private Rectangle rect;

    GLBlitOp() {
        super(Blit.class);
        rect = new Rectangle();
    }

    @Override
    public void process(SurfaceOp op, GLSurface output, Bypass bypass, Surface... inputs) {
        if (inputs.length > 0) {
            if (process((Blit) op, output, inputs[0])) {
                return;
            }
        }
        bypass.process(op, inputs);
    }

    private boolean process(Blit blit, GLSurface dst, Surface src) {
        try {
            BlendMode mode = blit.getBlendMode();
            if (canProcess(mode)) {
                GLRenderer renderer = dst.getGLContext().getRenderer();
                renderer.target(dst);
                setupBlending(renderer, mode, (float) blit.getOpacity());
                Rectangle bounds = blit.getSourceRegion(rect);
                if (bounds == null) {
                    int x = blit.getX();
                    int y = dst.getHeight() - (blit.getY() + src.getHeight());
                    renderer.draw(src, x, y);
                } else {
                    int x = blit.getX();
                    int sh = bounds.height;
                    int y = dst.getHeight() - (blit.getY() + sh);
                    renderer.draw(src, bounds.x, bounds.y, bounds.width, sh, x, y);
                }
                return true;
            }
        } catch (Exception ex) {
            // fall through
        }
        return false;
    }

 
}
