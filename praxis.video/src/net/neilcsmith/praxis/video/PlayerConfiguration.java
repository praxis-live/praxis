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
package net.neilcsmith.praxis.video;

import net.neilcsmith.praxis.core.Lookup;

/**
 *
 * @author nsigma
 */
public final class PlayerConfiguration {
    
    private final int width;
    private final int height;
    private final double fps;
    private final Lookup lookup;
    
    public PlayerConfiguration(int width, int height, double fps) {
        this(width, height, fps, Lookup.EMPTY);
    }
    
    public PlayerConfiguration(int width, int height, double fps, Lookup lookup) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Illegal dimensions");
        }
        if (lookup == null) {
            throw new NullPointerException();
        } 
        this.width = width;
        this.height = height;
        this.fps = fps;
        this.lookup = lookup;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public double getFPS() {
        return fps;
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
}
