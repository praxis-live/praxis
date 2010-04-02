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
package net.neilcsmith.ripl.delegates;

import java.awt.AlphaComposite;
import net.neilcsmith.ripl.core.Surface;
import net.neilcsmith.ripl.core.SurfaceGraphics;

/**
 *
 * @author Neil C Smith
 */
//public class AlphaCompositeContext implements CompositeSurfaceFilter {
public class AlphaCompositeContext extends CompositeDelegate {

    private AlphaComposite comp;

    public AlphaCompositeContext(AlphaComposite comp) {
        if (comp == null) {
            throw new NullPointerException();
        }
        this.comp = comp;
    }

//    public void process(Surface src, Surface dstIn, Surface dstOut, int x, int y) {
    public void process(Surface src, Surface dstIn, Surface dstOut, int x, int y, boolean rendering) {
        if (rendering) {
            SurfaceGraphics g = dstOut.getGraphics();
            if (dstIn != dstOut) {
                g.drawSurface(dstIn, x, y);
            }
            g.setComposite(comp);
            g.drawSurface(src, x, y);
        }


    }
}
