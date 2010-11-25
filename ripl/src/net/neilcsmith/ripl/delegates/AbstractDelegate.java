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

package net.neilcsmith.ripl.delegates;

import net.neilcsmith.ripl.Surface;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class AbstractDelegate implements Delegate {

    public abstract void process(Surface surface);

    public void update(long time) {
         // no op - can be overridden for delegates that need to update on non rendering frames
    }

    public boolean forceRender() {
        return false;
    }

    public boolean usesInput() {
        return true;
    }

}
