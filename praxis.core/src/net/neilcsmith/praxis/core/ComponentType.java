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

import net.neilcsmith.praxis.core.info.ArgumentInfo;

/**
 * @TODO Enforce String with regex
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ComponentType extends Argument {

    private String typeString;

    private ComponentType(String str) {
        this.typeString = str;
    }

    @Override
    public String toString() {
        return typeString;
    }

    @Override
    public int hashCode() {
        return typeString.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ComponentType) {
            return typeString.equals(obj.toString());
        } else {
            return false;
        }
    }

    public static ComponentType create(String str) {
        return new ComponentType(str);
    }

    public static ComponentType valueOf(String str) throws ArgumentFormatException {
        return create(str);
    }

    public static ArgumentInfo info() {
        return ArgumentInfo.create(ComponentType.class, null);
    }


    public static ComponentType coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof ComponentType) {
            return (ComponentType) arg;
        } else {
            return valueOf(arg.toString());
        }
    }

}
