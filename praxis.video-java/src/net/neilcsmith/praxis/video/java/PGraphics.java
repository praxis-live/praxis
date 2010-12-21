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

package net.neilcsmith.praxis.video.java;

import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.SurfaceOp;
import net.neilcsmith.ripl.ops.Blend;
import net.neilcsmith.ripl.ops.BlendFunction;
import net.neilcsmith.ripl.ops.Blit;
import net.neilcsmith.ripl.ops.Bounds;
import net.neilcsmith.ripl.ops.ScaledBlit;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class PGraphics {

    private PImage image;
    private BlendFunction blend;

    public PGraphics(PImage image) {
        if (image == null) {
            throw new NullPointerException();
        }
        this.image = image;
        this.blend = Blend.NORMAL;
    }

    public PImage getImage() {
        return image;
    }

    public void blendMode(BlendFunction blend) {
        if (blend == null) {
            throw new NullPointerException();
        }
        this.blend = blend;
    }


    public void clear() {
        image.getSurface().clear();
    }

    public void image(PImage src, double x, double y) {
        image.process(Blit.op(blend, (int) x, (int) y), src);
    }

    public void image(PImage src, double x, double y, double c, double d) {
        image(src, x, y, c, d, 0, 0, image.width, image.height);
    }

    public void image(PImage src, double x, double y, double w, double h,
            int u1, int v1, int u2, int v2) {
        int ix = (int) x;
        int iy = (int) y;
        int iw = (int) w;
        int ih = (int) h;
        int srcW = u2 - u1;
        int srcH = v2 - v1;
        if (iw == srcW && ih == srcH) {
            image.process(Blit.op(blend, new Bounds(u1, v1, srcW, srcH), ix, iy), src);
        } else {
            image.process(ScaledBlit.op(blend, new Bounds(u1, v1, srcW, srcH),
                    new Bounds(ix, iy, iw, ih)), src);
        }
    }

    public void op(SurfaceOp op) {
        image.process(op);
    }

    public void op(SurfaceOp op, Surface src) {
        image.getSurface().process(op, src);
    }

    public void op(SurfaceOp op, PImage src) {
        image.process(op, src);
    }


}
