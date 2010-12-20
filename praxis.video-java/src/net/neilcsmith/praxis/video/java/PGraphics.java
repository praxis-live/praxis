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

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class PGraphics {

    private PImage image;
    private Surface surface;
    private BlendFunction blend;

    public PGraphics(PImage image) {
        if (image == null) {
            throw new NullPointerException();
        }
        this.image = image;
        this.surface = image.getSurface();
        this.blend = Blend.NORMAL;
    }

    public void setImage(PImage image) {
        if (image == null) {
            throw new NullPointerException();
        }
        this.image = image;
        this.surface = image.getSurface();
    }

    public PImage getImage() {
        return image;
    }


    public void image(PImage image, double x, double y) {
        surface.process(Blit.op(blend, (int) x, (int) y), image.getSurface());
    }

    public void op(SurfaceOp op) {
        surface.process(op);
    }

    public void op(SurfaceOp op, Surface src) {
        surface.process(op, src);
    }


}
