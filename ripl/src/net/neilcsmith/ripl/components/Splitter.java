/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 Neil C Smith.
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
package net.neilcsmith.ripl.components;


import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.impl.CachedInOut;


/**
 *
 * @author Neil C Smith
 */
public class Splitter extends CachedInOut {

    public Splitter() {
        super(1,2,false);
    }

    @Override
    protected void process(Surface surface, boolean rendering) {
        if (rendering) {
            if (getSourceCount() == 0) {
                surface.clear();
            } else {
                Surface input = getInputSurface(0);
                if (surface == input) {
                    return;
                }
                if (surface.hasAlpha()) {
                    surface.clear();
                }
                surface.copy(getInputSurface(0));
            }
        }
    }
    
}
