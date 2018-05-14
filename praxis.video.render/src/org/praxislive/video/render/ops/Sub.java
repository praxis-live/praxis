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

package org.praxislive.video.render.ops;

import java.awt.Rectangle;
import org.praxislive.video.render.PixelData;
import org.praxislive.video.render.SurfaceOp;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Sub implements SurfaceOp {
    
    private Bounds bounds;
    private SurfaceOp op;

    private Sub(Bounds bounds, SurfaceOp op) {
        this.bounds = bounds;
        this.op = op;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public SurfaceOp getOp() {
        return op;
    }

    public void process(PixelData output, PixelData... inputs) {
        Rectangle bnds = bounds.asRectangle();
        Rectangle out = new Rectangle(output.getWidth(), output.getHeight());
        bnds = out.intersection(bnds);
        if (bnds.isEmpty()) {
            return;
        }
        op.process(SubPixels.create(output, bnds), inputs);
    }

    public static SurfaceOp op(SurfaceOp op, Bounds bounds) {
        if (bounds == null || op == null) {
            throw new NullPointerException();
        }
        return new Sub(bounds, op);
    }

    public static SurfaceOp op(SurfaceOp op, int x, int y, int width, int height) {
        return op(op, new Bounds(x, y, width, height));
    }

}
