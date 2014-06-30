/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2013 Neil C Smith.
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
import java.awt.geom.AffineTransform;
import java.util.logging.Level;
import net.neilcsmith.praxis.video.pgl.PGLGraphics;
import net.neilcsmith.praxis.video.pgl.PGLSurface;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.ops.BlendMode;
import net.neilcsmith.praxis.video.render.ops.TransformBlit;
import processing.core.PImage;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class PGLTransformBlitOp extends AbstractBlitOp {

    private final Rectangle rect;
    private final AffineTransform transform;
    private final float[] vertices;
    private final double[] matrix;

    PGLTransformBlitOp() {
        super(TransformBlit.class);
        rect = new Rectangle();
        transform = new AffineTransform();
        vertices = new float[8];
        matrix = new double[6];
    }

    @Override
    public void process(SurfaceOp op, PGLSurface output, Bypass bypass, Surface... inputs) {
        if (inputs.length > 0) {
            LOG.fine("Processing OpenGL transformed blit");
            if (process((TransformBlit) op, output, inputs[0])) {
                LOG.fine("Success!?");
                return;
            }
        }
        bypass.process(op, inputs);
    }

    private boolean process(TransformBlit blit, PGLSurface dst, Surface src) {
        try {
            BlendMode mode = blit.getBlendMode();
            if (canProcess(mode)) {
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

                AffineTransform tr = blit.getTransform(transform);
                tr.getMatrix(matrix);
                g.pushMatrix();
                g.applyMatrix((float) matrix[0], (float) matrix[2], (float) matrix[4],
                        (float) matrix[1], (float) matrix[3], (float) matrix[5]);
                g.image(img, dstX, dstY, dstW, dstH,
                        srcX, srcY, srcX + srcW, srcY + srcH);
                g.popMatrix();
                
                return true;
            }
        } catch (Exception ex) {
            // fall through
            LOG.log(Level.FINE, "Transform blit threw exception", ex);
        }
        return false;
    }
}
