/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
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
package net.neilcsmith.ripl;

/**
 *
 * @author Neil C Smith
 */
public abstract class Surface {

    private final static Surface[] EMPTY = new Surface[0];

    private final int width;
    private final int height;
    private final boolean alpha;

    public Surface(int width, int height, boolean alpha) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException();
        }
        this.width = width;
        this.height = height;
        this.alpha = alpha;
    }

    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    public final boolean hasAlpha() {
        return alpha;
    }

    protected final PixelData getPixelData(Surface surface) {
        return surface.getPixelData();
    }

    protected abstract PixelData getPixelData();

    public void process(SurfaceOp op) {
        process(op, EMPTY);
    }

    public void process(SurfaceOp op, Surface input) {
        process(op, new Surface[] {input} );
    }

    public abstract void process(SurfaceOp op, Surface... inputs);

    public abstract void clear();

    public abstract void release();

    public abstract void copy(Surface source);

    public abstract boolean checkCompatible(Surface surface, boolean checkDimensions, boolean checkAlpha);

    public abstract Surface createSurface(
            int width, int height, boolean alpha, SurfaceCapabilities caps);

    public Surface createSurface(SurfaceCapabilities caps) {
        return createSurface(width, height, alpha, caps);
    }
}
