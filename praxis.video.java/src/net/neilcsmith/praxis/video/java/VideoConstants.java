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

import net.neilcsmith.praxis.video.render.ops.BlendMode;



/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class VideoConstants {

    private VideoConstants() {}

    public final static Blend NORMAL = new Blend(BlendMode.Normal);
    public final static Blend BLEND = NORMAL;
    public final static Blend ADD = new Blend(BlendMode.Add);
    public final static Blend SUB = new Blend(BlendMode.Sub);
    public final static Blend SUBTRACT = SUB;
    public final static Blend DIFFERENCE = new Blend(BlendMode.Difference);
    public final static Blend MULTIPLY = new Blend(BlendMode.Multiply);
    public final static Blend SCREEN = new Blend(BlendMode.Screen);
    public final static Blend BITXOR = new Blend(BlendMode.BitXor);
    public final static Blend MASK = new Blend(BlendMode.Mask);

    public final static boolean OPEN = false;
    public final static boolean CLOSE = true;
    
    
    public static class Blend {
        
        private final BlendMode mode;
        private final double opacity;
        
        private Blend(BlendMode mode) {
            this(mode, 1);
        }
        
        private Blend(BlendMode mode, double opacity) {
            this.mode = mode;
            this.opacity = opacity;
        }
        
        BlendMode getMode() {
            return mode;
        }
        
        double getOpacity() {
            return opacity;
        }
        
        @Deprecated
        public Blend opacity(double opacity) {
            if (opacity < 0) {
                opacity = 0;
            } else if (opacity > 1) {
                opacity = 1;
            }
            return new Blend(mode, opacity);
        }
        
    }
    

}
