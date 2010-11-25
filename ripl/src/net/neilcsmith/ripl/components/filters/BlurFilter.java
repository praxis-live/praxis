/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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

package net.neilcsmith.ripl.components.filters;

import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.SurfaceOp;
import net.neilcsmith.ripl.impl.SingleInOut;
import net.neilcsmith.ripl.ops.Blur;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class BlurFilter extends SingleInOut {
    
    private SurfaceOp op;
    private int radius;

    public BlurFilter() {
        
    }
    
    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException();
        }
        this.op = null;
        this.radius = radius;
    }
    
    

    @Override
    protected void process(Surface surface, boolean rendering) {
        if (rendering) {
            if (op == null) {
                op = Blur.op(radius);
            }
            surface.process(op);
        }
    }

}
