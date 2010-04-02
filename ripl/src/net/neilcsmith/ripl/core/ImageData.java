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

import java.awt.Image;

/**
 *
 * @author Neil C Smith
 */
public interface ImageData {
    
    public Image getImage();
    
    public Bounds getBounds();
    
//    private Image img;
//    private Bounds bounds;
//    
//    public ImageData(Image img, Bounds bounds) {
//        if (img == null || bounds == null) {
//            throw new NullPointerException();
//        }
//        this.img = img;
//        this.bounds = bounds;
//    }
//    
//    public final Image getImage() {
//        return img;
//    }
//    
//    public final Bounds getBounds() {
//        return bounds;
//    }

}
