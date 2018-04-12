/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */

package org.praxislive.code;

import org.praxislive.core.Control;
import org.praxislive.core.ControlInfo;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public abstract class ControlDescriptor {
    
    public static enum Category {Internal, Synthetic, In, Action, Property, AuxIn, Function}
    
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
    
    @SuppressWarnings("deprecated")
    public void reset(boolean full) {
        reset();
    }
    
    @Deprecated
    public void reset() {
    }
    
    public void stopping() {
    }
    
//    public void dispose() {
//    }
    
}
