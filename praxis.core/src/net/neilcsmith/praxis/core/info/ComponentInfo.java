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

import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PBoolean;
import net.neilcsmith.praxis.core.types.PString;
import java.util.List;
import java.util.Map;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.Component;

/**
 *
 * @author Neil C Smith
 */
public class ComponentInfo extends Argument {

    public static final PString TYPE_KEY = PString.valueOf("type");
    public static final PString CONTROLS_KEY = PString.valueOf("controls");
    public static final PString PORTS_KEY = PString.valueOf("ports");
    public static final PString PROPERTIES_KEY = PString.valueOf("properties");
    public static final PString CHILDREN_KEY = PString.valueOf("children");
    public static final PString IS_CONTAINER_KEY = PString.valueOf("is_container");
    private PMap data;

    private ComponentInfo(PMap data) {
        this.data = data;
    }

    public PString getType() {
        return (PString) data.get(TYPE_KEY);
    }

    public PMap getControlsInfo() {
        return (PMap) data.get(CONTROLS_KEY);
    }

    public PMap getPortsInfo() {
        return (PMap) data.get(PORTS_KEY);
    }

    public PMap getProperties() {
        return (PMap) data.get(PROPERTIES_KEY);
    }

    public PBoolean isContainer() {
        return (PBoolean) data.get(IS_CONTAINER_KEY);
    }

    public PArray getChildren() {
        return (PArray) data.get(CHILDREN_KEY);
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
        if (obj instanceof ComponentInfo) {
            ComponentInfo o = (ComponentInfo) obj;
            return data.equals(o.data);
        }
        return false;
    }

    public static ComponentInfo create(
            ComponentInfo oldInfo,
            Map<PString, ControlInfo> controls,
            Map<PString, PortInfo> ports,
            PMap properties) {
        return create(oldInfo, controls, ports, null, properties);
    }

    public static ComponentInfo create(
            ComponentInfo oldInfo,
            Map<PString, ControlInfo> controls,
            Map<PString, PortInfo> ports,
            PString[] children,
            PMap properties) {
        PString type = oldInfo.getType();
        PBoolean is_cont = oldInfo.isContainer();
        PMap cts;
        PMap pts;
        PArray childs;
        PMap props;
        if (controls == null) {
            cts = oldInfo.getControlsInfo();
        } else {
            cts = PMap.valueOf(controls);
        }
        if (ports == null) {
            pts = oldInfo.getPortsInfo();
        } else {
            pts = PMap.valueOf(ports);
        }
        if (children == null) {
            childs = PArray.EMPTY;
        } else {
            childs = PArray.valueOf(children);
            is_cont = PBoolean.TRUE;
        }
        if (properties == null) {
            props = oldInfo.getProperties();
        } else {
            props = properties;
        }
        PMap data = PMap.valueOf(TYPE_KEY, type,
                CONTROLS_KEY, cts,
                PORTS_KEY, pts,
                IS_CONTAINER_KEY, is_cont,
                CHILDREN_KEY, childs,
                PROPERTIES_KEY, props);

        return new ComponentInfo(data);
    }

    

    public static ComponentInfo create(
            Class<? extends Component> clas,
            Map<PString, ControlInfo> controls,
            Map<PString, PortInfo> ports,
            PMap properties) {
        return create(clas, controls, ports, null, properties);

    }

    public static ComponentInfo create(
            Class<? extends Component> clas,
            Map<PString, ControlInfo> controls,
            Map<PString, PortInfo> ports,
            PString[] children,
            PMap properties) {
                if (clas == null) {
            throw new NullPointerException();
        }
        PString type = PString.valueOf(clas.getName());
        PMap cts;
        if (controls == null) {
            cts = PMap.EMPTY;
        } else {
            cts = PMap.valueOf(controls);
        }
        PMap pts;
        if (ports == null) {
            pts = PMap.EMPTY;
        } else {
            pts = PMap.valueOf(ports);
        }
        PArray childs;
        PBoolean is_cont;
        if (children == null) {
            childs = PArray.EMPTY;
            is_cont = PBoolean.FALSE;
        } else {
            childs = PArray.valueOf(children);
            is_cont = PBoolean.TRUE;
        }
        PMap props;
        if (properties == null) {
            props = PMap.EMPTY;
        } else {
            props = properties;
        }

        PMap data = PMap.valueOf(TYPE_KEY, type,
                CONTROLS_KEY, cts,
                PORTS_KEY, pts,
                IS_CONTAINER_KEY, is_cont,
                CHILDREN_KEY, childs,
                PROPERTIES_KEY, props);

        return new ComponentInfo(data);

    }
    
    public static ComponentInfo coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof ComponentInfo) {
            return (ComponentInfo) arg;
        }
        throw new ArgumentFormatException();
    }
}
