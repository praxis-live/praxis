/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 - Neil C Smith. All rights reserved.
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
package net.neilcsmith.praxis.core;

import net.neilcsmith.praxis.core.info.ControlInfo;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class InterfaceDefinition {
    
    public abstract String[] getControls();

    public abstract ControlInfo getControlInfo(String control);

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return obj == null ? false : this.getClass().equals(obj.getClass());
    }

    @Override
    public final int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public final String toString() {
        return this.getClass().getName();
    }



// @TODO This belongs in ComponentInfo

//    public static InterfaceDefinition valueOf(String str) throws ArgumentFormatException {
//        try {
//            Class<?> cl = Class.forName(str);
//            Class<? extends InterfaceDefinition> c = cl.asSubclass(InterfaceDefinition.class);
//            return c.newInstance();
//        } catch (Exception ex) {
//            throw new ArgumentFormatException(ex);
//        }
//    }
//
//    public static InterfaceDefinition coerce(Argument arg) throws ArgumentFormatException {
//        if (arg instanceof InterfaceDefinition) {
//            return (InterfaceDefinition) arg;
//        } else {
//            return valueOf(arg.toString());
//        }
//    }
}
