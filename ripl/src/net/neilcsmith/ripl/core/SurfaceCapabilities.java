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

package net.neilcsmith.ripl.core;

/**
 *
 * @author Neil C Smith
 */
public class SurfaceCapabilities {

//    public static enum Stability {STABLE, UNSTABLE, UNDEFINED};
//    
//    public static enum PixelAccess {READABLE, FETCHABLE, INACCESSIBLE, UNDEFINED};
//    
//    private Stability st;
//    private PixelAccess ac;
    
//    public SurfaceCapabilities(Stability stable, PixelAccess access) {
//        this.stable = stable;
//        this.access = access;
//    }
    
    
    private boolean stable;
//    private boolean inSystemMemory;
    
    public SurfaceCapabilities(boolean stable) {
        this.stable = stable;
//        this.inSystemMemory = inSystemMemory;
    }
    
    public final boolean isStable() {
        return stable;
    }
//    
//    public final boolean isInSystemMemory() {
//        return inSystemMemory;
//    }
    
//    public Stability getStability() {
//        return this.stable;
//    }
//    
//    public PixelAccess getPixelAccess() {
//        return this.access;
//    }

//    @Override
//    public boolean equals(Object obj) {
//        if (obj instanceof SurfaceCapabilities) {
//            SurfaceCapabilities o = (SurfaceCapabilities) obj;
//            if (this.stable == o.getStability() && this.access == o.getPixelAccess()) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 3;
//        hash = 41 * hash + (this.stable != null ? this.stable.hashCode() : 0);
//        hash = 41 * hash + (this.access != null ? this.access.hashCode() : 0);
//        return hash;
//    }
    
    
    
}
