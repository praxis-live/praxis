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

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.video.pgl.PGLGraphics;
import org.praxislive.video.pgl.PGLSurface;
import org.praxislive.video.render.Surface;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.ops.BlendMode;
import org.praxislive.video.render.ops.ShapeRender;
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
        if (process((ShapeRender) op, output)) {
            return;
        }
        bypass.process(op, inputs);
    }

    private boolean process(ShapeRender op, PGLSurface dst) {
        try {
            Shape shape = op.getShape();
            if (shape == null) {
                return true;
            }
            BlendMode mode = op.getBlendMode();
            if (canProcessDirect(mode)) {
                PGLGraphics pg = dst.getGraphics();
                pg.beginDraw();
                configure(pg, mode, (float) op.getOpacity(), op.getFillColor(), op.getStrokeColor());
                BasicStroke bs = op.getStroke();
                if (bs != null) {
                    pg.strokeWeight(bs.getLineWidth());
                } else {
                    pg.noStroke();
                }
                PathIterator itr = shape.getPathIterator(op.getTransform(), 1);
                drawShape(pg, itr, shape instanceof Line2D);
                return true;
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error during shape blit", ex);
        }
        return false;
    }

    private void drawShape(PGLGraphics pg, PathIterator itr, boolean line) {
        boolean inShape = false;
        while (!itr.isDone()) {
            int type = itr.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    if (inShape) {
                        pg.endShape();
                    }
                    if (line) {
                        pg.beginShape(PConstants.LINES);
                    } else {
                        pg.beginShape();
                    }
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
            pg.endShape();
        }

    }

}
