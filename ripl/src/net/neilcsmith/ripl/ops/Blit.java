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

import net.neilcsmith.ripl.PixelData;
import net.neilcsmith.ripl.SurfaceOp;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public final class Blit implements SurfaceOp {

    private static Blit defaultBlit = new Blit(Blend.NORMAL, 0, 0);

    private BlendFunction blend;
    private int x;
    private int y;

    private Blit() {
        this(Blend.NORMAL, 0, 0);
    }

    private Blit(BlendFunction blend, int x, int y) {
        if (blend == null) {
            throw new NullPointerException();
        }
        this.blend = blend;
        this.x = x;
        this.y = y;
    }

    public BlendFunction getBlendFunction() {
        return blend;
    }

    public void process(PixelData output, PixelData... inputs) {
        if (inputs.length < 1) {
            return;
        }
        if (x == 0 && y == 0) {
            blend.process(inputs[0], output);
        } else {
            PixelData dst = SubPixels.create(output, x, y, output.getWidth() - x, output.getHeight() - y);
            blend.process(inputs[0], dst);
        }
        
    }

    public static SurfaceOp op() {
        return defaultBlit;
    }
    
    public static SurfaceOp op(int x, int y) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException();
        }
        return new Blit(Blend.NORMAL,x,y);
    }

    public static SurfaceOp op(BlendFunction blend) {
        if (blend == null) {
            throw new NullPointerException();
        }
        return new Blit(blend,0,0);
    }


}
