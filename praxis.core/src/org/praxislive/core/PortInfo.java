/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2019 Neil C Smith.
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
package org.praxislive.core;

import java.util.Optional;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PMap;

/**
 *
 * @author Neil C Smith
 */
public final class PortInfo extends Value {

    public static enum Direction { IN, OUT, BIDI };

    private final Port.Type<? extends Port> type;
    private final Direction direction;
    private final PMap properties;
    
    private volatile String string;

    PortInfo(Port.Type<? extends Port> type,
            Direction direction,
            PMap properties,
            String string) {
        this.type = type;
        this.direction = direction;
        this.properties = properties;
        this.string = string;
    }

    @Deprecated
    public Class<? extends Port> getType() {
        return type.asClass();
    }
    
    public Port.Type<? extends Port> portType() {
        return type;
    }
    
    @Deprecated
    public Port.Type<? extends Port> getPortType() {
        return type;
    }

    public Direction direction() {
        return direction;
    }
    
    @Deprecated
    public Direction getDirection() {
        return direction;
    }

    public PMap properties() {
        return properties;
    }
    
    @Deprecated
    public PMap getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        String str = string;
        if (str == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(type.name()).append(" ");
            sb.append(direction.name());
            if (!properties.isEmpty()) {
                sb.append(" {");
                sb.append(properties.toString());
                sb.append("}");
            }
            string = sb.toString();
        }
        return string;
    }

//    @Override
//    public boolean isEquivalent(Value arg) {
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
        if (properties == null) {
            properties = PMap.EMPTY;
        }
        return new PortInfo(Port.Type.of(typeClass), direction, properties, null);

    }
    
    /**
     * Coerce the given Value into an ArgumentInfo object.
     * 
     * @param arg Value to be coerced.
     * @return ArgumentInfo
     * @throws ValueFormatException if Value cannot be coerced.
     */
    @Deprecated
    public static PortInfo coerce(Value arg) throws ValueFormatException {
        if (arg instanceof PortInfo) {
            return (PortInfo) arg;
        } else {
            return parse(arg.toString());
        }
    }
    
    public static Optional<PortInfo> from(Value arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ValueFormatException ex) {
            return Optional.empty();
        }
    }
    
    public static PortInfo parse(String string) throws ValueFormatException {
        PArray arr = PArray.parse(string);
        try {
            Port.Type<? extends Port> type = Port.Type.fromName(arr.get(0).toString()).get();
            Direction direction = Direction.valueOf(arr.get(1).toString());
            PMap properties = arr.size() > 2 ?
                    PMap.coerce(arr.get(2)) :
                    PMap.EMPTY;
            return new PortInfo(type, direction, properties, string);
        } catch (Exception ex) {
            throw new ValueFormatException(ex);
        }
    }
    
}
