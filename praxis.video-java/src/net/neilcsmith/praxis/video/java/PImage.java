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

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class PImage {

    private Surface surface;
    public int width;
    public int height;

    public PImage(Surface surface) {
        setSurfaceImpl(surface);
    }

    public void setSurface(Surface surface) {
        setSurfaceImpl(surface);
    }

    private void setSurfaceImpl(Surface surface) {
        if (surface == null) {
            throw new NullPointerException();
        }
        this.surface = surface;
        this.width = surface.getWidth();
        this.height = surface.getHeight();
    }

    public Surface getSurface() {
        return surface;
    }

    public void process(SurfaceOp op) {
        surface.process(op);
    }

    public void process(SurfaceOp op, PImage image) {
        surface.process(op, image.surface);
    }
    

}
