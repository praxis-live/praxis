/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Neil C Smith.
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

package net.neilcsmith.praxis.code;

import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.info.ControlInfo;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public abstract class ControlDescriptor {
    
    public static enum Category {Internal, Synthetic, Property, Action}
    
    private final String id;
    private final Category category;
    private final int index;
    
    protected ControlDescriptor(String id, Category category, int index) {
        this.id = id;
        this.category = category;
        this.index = index;
    }
    
    public final String getID() {
        return id;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public int getIndex() {
        return index;
    }
    
    public abstract ControlInfo getInfo();
    
    public abstract void attach(CodeContext<?> context, Control previous);
    
    public abstract Control getControl();
    
    public void reset() {
    }
    
    public void stopping() {
    }
    
//    public void dispose() {
//    }
    
}
