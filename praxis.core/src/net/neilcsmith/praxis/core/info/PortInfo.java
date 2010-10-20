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
package net.neilcsmith.praxis.core.info;

import java.util.Arrays;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.PortAddress;
import net.neilcsmith.praxis.core.types.PMap;

/**
 *
 * @author Neil C Smith
 */
public final class PortInfo extends Argument {

    private String type;
    private Port.Direction direction;
    private PMap properties;

    private PortInfo(String type, Port.Direction direction, PMap properties) {
        this.type = type;
        this.direction = direction;
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    @Deprecated
    public PortAddress[] getConnections() {
        return new PortAddress[0];
    }

    public Port.Direction getDirection() {
        return direction;
    }

    public PMap getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(type);
        str.append(" ");
        str.append(direction.name());
        str.append(" ");
        str.append("{");
        str.append(properties.toString());
        str.append("}");
        return str.toString();
    }

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
            Port.Direction direction, PMap properties) {
        if (typeClass == null || direction == null) {
            throw new NullPointerException();
        }
        String type = typeClass.getName();
        if (properties == null) {
            properties = PMap.EMPTY;
        }
        return new PortInfo(type, direction, properties);

    }

    @Deprecated
    public static PortInfo create(Class<? extends Port> typeClass, Port.Direction direction,
            PortAddress[] connections, PMap properties) {
        return create(typeClass, direction, properties);

    }

    @Deprecated
    public static PortInfo create(PortInfo oldInfo, PortAddress[] connections) {

        return oldInfo;
    }
}
