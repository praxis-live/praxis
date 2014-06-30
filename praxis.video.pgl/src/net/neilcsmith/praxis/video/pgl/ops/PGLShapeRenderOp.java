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

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.video.pgl.PGLGraphics;
import net.neilcsmith.praxis.video.pgl.PGLSurface;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.ops.BlendMode;
import net.neilcsmith.praxis.video.render.ops.RectFill;
import net.neilcsmith.praxis.video.render.ops.ShapeRender;
import processing.core.PConstants;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class PGLShapeRenderOp extends AbstractDrawOp {

    private final static Logger LOG = Logger.getLogger(PGLShapeRenderOp.class.getName());

    private final float[] coords;

    PGLShapeRenderOp() {
        super(ShapeRender.class);
        coords = new float[6];
    }

    @Override
    public void process(SurfaceOp op, PGLSurface output, Bypass bypass, Surface... inputs) {
//        if (inputs.length > 0) {
        if (process((ShapeRender) op, output)) {
            return;
        }
//        }
        bypass.process(op, inputs);
    }

    private boolean process(ShapeRender op, PGLSurface dst) {
        try {
            Shape shape = op.getShape();
            if (shape == null) {
                return true;
            }
            BlendMode mode = op.getBlendMode();
            if (canProcess(mode)) {
                PGLGraphics pg = dst.getGraphics();
                pg.beginDraw();
                configure(pg, mode, (float) op.getOpacity(), op.getFillColor(), op.getStrokeColor());
                BasicStroke bs = op.getStroke();
                if (bs != null) {
                    pg.strokeWeight(bs.getLineWidth());
                } else {
                    pg.noStroke();
                }
                PathIterator itr = shape.getPathIterator(null, 1);
                drawShape(pg, itr);
                return true;
            }
        } catch (Exception ex) {
            LOG.log(Level.FINE, "Error during rect blit", ex);
        }
        return false;
    }

    private void drawShape(PGLGraphics pg, PathIterator itr) {
        boolean inShape = false;
        while (!itr.isDone()) {
            int type = itr.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    if (inShape) {
                        pg.endShape(PConstants.CLOSE);
                    }
                    pg.beginShape();
                    inShape = true;
                    pg.vertex(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    pg.vertex(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_CLOSE:
                    if (inShape) {
                        pg.endShape(PConstants.CLOSE);
                        inShape = false;
                    }
                    break;
                default :
                    LOG.log(Level.FINE, "Unexpected PathIterator segment type");
            }
            itr.next();
        }
        if (inShape) {
            LOG.log(Level.FINE, "Shape not closed by PathIterator");
            pg.endShape(PConstants.CLOSE);
        }

    }

}
