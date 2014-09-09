/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2014 Neil C Smith.
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
package net.neilcsmith.praxis.core.info;

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PMap;

/**
 *
 * @author Neil C Smith
 */
public final class PortInfo extends Argument {

    public static enum Direction { IN, OUT, BIDI };

    private final Class<? extends Port> type;
    private final Direction direction;
    private final PMap properties;
    
    private volatile String string;

    private PortInfo(Class<? extends Port> type,
            Direction direction,
            PMap properties,
            String string) {
        this.type = type;
        this.direction = direction;
        this.properties = properties;
        this.string = string;
    }

    public Class<? extends Port> getType() {
        return type;
    }

    public Direction getDirection() {
        return direction;
    }

    public PMap getProperties() {
        return properties;
    }

    @Override
    public String toString() {
//        StringBuilder str = new StringBuilder();
//        str.append(type.getName());
//        str.append(" ");
//        str.append(direction.name());
//        str.append(" ");
//        str.append("{");
//        str.append(properties.toString());
//        str.append("}");
//        return str.toString();
        String str = string;
        if (str == null) {
            str = type.getName() + " " + direction.name() + " {" + properties.toString() + "}";
            string = str;
        }
        return str;
    }

//    @Override
//    public boolean isEquivalent(Argument arg) {
//        return equals(arg);
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PortInfo) {
            PortInfo o = (PortInfo) obj;
            return type.equals(o.type) && direction.equals(o.direction)
                    && properties.equals(o.properties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 89 * hash + (this.direction != null ? this.direction.hashCode() : 0);
        hash = 89 * hash + (this.properties != null ? this.properties.hashCode() : 0);
        return hash;
    }

    public static PortInfo create(Class<? extends Port> typeClass,
            Direction direction, PMap properties) {
        if (typeClass == null || direction == null) {
            throw new NullPointerException();
        }
//        String type = typeClass.getName();
        if (properties == null) {
            properties = PMap.EMPTY;
        }
        return new PortInfo(typeClass, direction, properties, null);

    }
    
    /**
     * Coerce the given Argument into an ArgumentInfo object.
     * 
     * @param arg Argument to be coerced.
     * @return ArgumentInfo
     * @throws ArgumentFormatException if Argument cannot be coerced.
     */
    public static PortInfo coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof PortInfo) {
            return (PortInfo) arg;
        } else {
            return valueOf(arg.toString());
        }
    }
    
    private static PortInfo valueOf(String string) throws ArgumentFormatException {
        PArray arr = PArray.valueOf(string);
        try {
            Class<? extends Port> cls = (Class<? extends Port>) Class.forName(arr.get(0).toString());
            Direction direction = Direction.valueOf(arr.get(1).toString());
            PMap properties = PMap.coerce(arr.get(2));
            return new PortInfo(cls, direction, properties, string);
        } catch (Exception ex) {
            throw new ArgumentFormatException(ex);
        }
    }
    
}
