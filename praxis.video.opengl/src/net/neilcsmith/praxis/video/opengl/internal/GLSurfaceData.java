/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Neil C Smith.
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
package net.neilcsmith.praxis.video.opengl.internal;

import net.neilcsmith.praxis.video.render.PixelData;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class GLSurfaceData implements PixelData {

    final int width;
    final int height;
    final boolean alpha;
    int[] pixels;
    Texture texture;
    int usage;

    GLSurfaceData(int w, int h, boolean a) {
        width = w;
        height = h;
        alpha = a;
        usage = 1;
    }

    @Override
    public int[] getData() {
        return pixels;
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public int getScanline() {
        return width;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public boolean hasAlpha() {
        return alpha;
    }


}
