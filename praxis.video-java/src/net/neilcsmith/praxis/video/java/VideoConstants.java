/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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

package net.neilcsmith.praxis.video.java;

import net.neilcsmith.ripl.ops.Blend;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class VideoConstants {

    private VideoConstants() {}

    public final static Blend NORMAL = Blend.NORMAL;
    public final static Blend BLEND = NORMAL;
    public final static Blend ADD = Blend.ADD;
    public final static Blend SUB = Blend.SUB;
    public final static Blend SUBTRACT = SUB;
    public final static Blend DIFFERENCE = Blend.DIFFERENCE;
    public final static Blend MULTIPLY = Blend.MULTIPLY;
    public final static Blend SCREEN = Blend.SCREEN;
    public final static Blend BITXOR = Blend.BITXOR;

    public final static boolean OPEN = false;
    public final static boolean CLOSE = true;

}
