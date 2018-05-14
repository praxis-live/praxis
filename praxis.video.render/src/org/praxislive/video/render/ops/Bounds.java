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

/**
 *
 * @author Neil C Smith
 */
@Deprecated
public final class Bounds {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public Bounds(int x, int y, int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException();
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

    }

    public Bounds(Rectangle rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    public final int getX() {
        return x;
    }

    public final int getY() {
        return y;
    }

    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    public final Rectangle asRectangle() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Bounds) {
            Bounds o = (Bounds) obj;
            return o.width == width &&
                    o.height == height &&
                    o.x == x &&
                    o.y == y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.x;
        hash = 67 * hash + this.y;
        hash = 67 * hash + this.width;
        hash = 67 * hash + this.height;
        return hash;
    }




}