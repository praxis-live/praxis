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
import java.awt.geom.AffineTransform;
import java.util.logging.Level;
import org.praxislive.video.pgl.PGLGraphics;
import org.praxislive.video.pgl.PGLSurface;
import static org.praxislive.video.pgl.ops.PGLOp.LOG;
import org.praxislive.video.render.Surface;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.ops.TextRender;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class PGLTextRenderOp extends PGLOp {

    private final double[] matrix;
    
    public PGLTextRenderOp() {
        super(TextRender.class);
        matrix = new double[6];
    }

    @Override
    public void process(SurfaceOp op, PGLSurface output, Bypass bypass, Surface... inputs) {
        if (process((TextRender) op, output)) {
            return;
        }
        bypass.process(op, inputs);
    }
    
    private boolean process(TextRender op, PGLSurface dst) {
        try {
            PGLGraphics pg = dst.getGraphics();
            pg.beginDraw();
            Color color = op.getColor();
            pg.fill(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            pg.textFont(dst.getContext().asPFont(op.getFont()));
            AffineTransform tr = op.getTransform();
            if (tr != null) {
                tr.getMatrix(matrix);
                pg.pushMatrix();
                pg.applyMatrix((float) matrix[0], (float) matrix[2], (float) matrix[4],
                        (float) matrix[1], (float) matrix[3], (float) matrix[5]);
                pg.text(op.getText(), (float) op.getX(), (float) op.getY());
                pg.popMatrix();
            } else {
                pg.text(op.getText(), (float) op.getX(), (float) op.getY());
            }
            return true;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Text blit threw exception", ex);
            return false;
        }
    }
    
    
    
}
