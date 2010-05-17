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

package net.neilcsmith.ripl.utils;

/**
 *
 * @author Neil C Smith
 */
public final class ResizeMode {
    
    public enum Type {Crop, Stretch, Scale}; // , ScaleWidth, ScaleHeight};
    
    private Type type;
    private double horizontalAlignment;
    private double verticalAlignment;
    
    public ResizeMode(Type type, double hAlign, double vAlign) {
        if (type == null) {
            throw new NullPointerException();
        }
        if (hAlign < 0 || hAlign > 1 || vAlign < 0 || vAlign > 1) {
            throw new IllegalArgumentException();
        }
        this.type = type;
        this.horizontalAlignment = hAlign;
        this.verticalAlignment = vAlign;
    }
    
    public Type getType() {
        return type;
    }
    
    public double getHorizontalAlignment() {
        return horizontalAlignment;
    }
    
    public double getVerticalAlignment() {
        return verticalAlignment;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ResizeMode) {
            ResizeMode o = (ResizeMode) obj;
            return (o.type == type &&
                    o.horizontalAlignment == horizontalAlignment &&
                    o.verticalAlignment == verticalAlignment);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.horizontalAlignment) ^ (Double.doubleToLongBits(this.horizontalAlignment) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.verticalAlignment) ^ (Double.doubleToLongBits(this.verticalAlignment) >>> 32));
        return hash;
    }
    
    
    

}
