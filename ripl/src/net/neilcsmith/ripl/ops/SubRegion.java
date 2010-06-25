/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 - Neil C Smith. All rights reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */

package net.neilcsmith.ripl.ops;

import java.awt.Rectangle;
import net.neilcsmith.ripl.PixelData;
import net.neilcsmith.ripl.SurfaceOp;
import net.neilcsmith.ripl.utils.SubPixels;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class SubRegion implements SurfaceOp {
    
    private Bounds bounds;
    private SurfaceOp op;

    private SubRegion(Bounds bounds, SurfaceOp op) {
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

    public static SurfaceOp op(Bounds bounds, SurfaceOp op) {
        if (bounds == null || op == null) {
            throw new NullPointerException();
        }
        return new SubRegion(bounds, op);
    }

}
