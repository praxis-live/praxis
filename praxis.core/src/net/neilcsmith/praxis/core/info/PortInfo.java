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

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.PortAddress;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith
 */
public final class PortInfo extends Argument {

    public final static PString TYPE_KEY = PString.valueOf("type");
    public final static PString DIRECTION_KEY = PString.valueOf("direction");
    public final static PString CONNECTIONS_KEY = PString.valueOf("connections");
    public final static PString PROPERTIES_KEY = PString.valueOf("properties");
    private PMap data;

    private PortInfo(PMap data) {
        this.data = data;
    }

    public PMap getData() {
        return data;
    }

    public PString getType() {
        return (PString) data.get(TYPE_KEY);
    }

    public PArray getConnections() {
        return (PArray) data.get(CONNECTIONS_KEY);
    }

    public PString getDirection() {
        return (PString) data.get(DIRECTION_KEY);
    }

    public PMap getProperties() {
        return (PMap) data.get(PROPERTIES_KEY);
    }

    @Override
    public String toString() {
        return data.toString();
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PortInfo) {
            PortInfo o = (PortInfo) obj;
            return data.equals(o.data);
        }
        return false;
    }
    
//    public static PortInfo create(Port port, PortAddress[] connections, PMap properties) {
//        Class<? extends Port> typeClass = port.getTypeClass();
//        Port.Direction direction = port.getDirection();
//        return create(typeClass, direction, connections, properties);
//    }

    public static PortInfo create(Class<? extends Port> typeClass, Port.Direction direction,
            PortAddress[] connections, PMap properties) {
        if (typeClass == null || direction == null || connections == null) {
            throw new NullPointerException();
        }
        PString type = PString.valueOf(typeClass.getName());
        PString dir = PString.valueOf(direction.name());
        PArray con = PArray.valueOf(connections);
        if (properties == null) {
            properties = PMap.EMPTY;
        }

        return create(type, dir, con, properties);

    }

    public static PortInfo create(PortInfo oldInfo, PortAddress[] connections) {
        
        PArray con = PArray.valueOf(connections);
        
        PString type = oldInfo.getType();
        PString dir = oldInfo.getDirection();
        PMap properties = oldInfo.getProperties();
        
        return create(type, dir, con, properties);
    }

    private static PortInfo create(PString type, PString direction,
            PArray connections, PMap properties) {

        PMap data = PMap.valueOf(TYPE_KEY, type,
                DIRECTION_KEY, direction,
                CONNECTIONS_KEY, connections,
                PROPERTIES_KEY, properties);

        return new PortInfo(data);
    }
}
