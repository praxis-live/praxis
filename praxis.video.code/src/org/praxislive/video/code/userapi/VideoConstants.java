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

package org.praxislive.video.code.userapi;

import org.praxislive.video.render.ops.BlendMode;



/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class VideoConstants {

    private VideoConstants() {}

    @Deprecated public final static BlendMode NORMAL = BlendMode.Normal;
    public final static BlendMode BLEND = BlendMode.Normal;
    public final static BlendMode ADD = BlendMode.Add;
    @Deprecated public final static BlendMode SUB = BlendMode.Sub;
    public final static BlendMode SUBTRACT = BlendMode.Sub;
    public final static BlendMode DIFFERENCE = BlendMode.Difference;
    public final static BlendMode MULTIPLY = BlendMode.Multiply;
    public final static BlendMode SCREEN = BlendMode.Screen;
    public final static BlendMode BITXOR = BlendMode.BitXor;
    public final static BlendMode MASK = BlendMode.Mask;

    public final static boolean OPEN = false;
    public final static boolean CLOSE = true;
    
    

    

}
