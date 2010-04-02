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

package net.neilcsmith.rapl.core.impl;

import net.neilcsmith.rapl.core.Buffer;

/**
 *
 * @author Neil C Smith
 */
public abstract class SingleInOut extends AbstractInOut {
    
    boolean ensureClear = true;

    
    public SingleInOut() {
        this(true);
    }
    
    public SingleInOut(boolean ensureClear) {
        super(1,1);
        this.ensureClear = ensureClear;
    }
    
    
    @Override
    protected void callSources(Buffer buffer, long time, boolean rendering) {
        if (sources.size() == 1) {
            sources.get(0).process(buffer, this, time);
        } else if (ensureClear && rendering) {
            buffer.clear();
        }
    }
}
