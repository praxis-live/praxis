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
 *
 *
 * Parts of the API of this package, as well as some of the code, is derived from
 * the Processing project (http://processing.org)
 *
 * Copyright (c) 2004-09 Ben Fry and Casey Reas
 * Copyright (c) 2001-04 Massachusetts Institute of Technology
 *
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
        this.surface = surface;
        if (surface == null) {
            this.width = 0;
            this.height = 0;
        } else {
            this.width = surface.getWidth();
            this.height = surface.getHeight();
        }
    }

    public int getWidth() {
        if (surface != null) {
            return surface.getWidth();
        } else {
            return 0;
        }
    }

    public int getHeight() {
        if (surface != null) {
            return surface.getHeight();
        } else {
            return 0;
        }
    }

    public void clear() {
        if (surface != null) {
            surface.clear();
        }
    }

    public void process(SurfaceOp op) {
        if (surface != null) {
            surface.process(op);
        }
    }

    public void process(SurfaceOp op, PImage image) {
        if (surface != null && image.surface != null) {
            surface.process(op, image.surface);
        }
    }
}
