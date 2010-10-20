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
import java.util.Collections;
import java.util.LinkedHashMap;
import net.neilcsmith.praxis.core.types.PMap;
import java.util.Map;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.Component;

/**
 *
 * @author Neil C Smith
 */
public class ComponentInfo extends Argument {

    private Class<? extends Component> type;
    private Map<String, ControlInfo> controls;
    private Map<String, PortInfo> ports;
    private PMap properties;

    private ComponentInfo(Class<? extends Component> type, Map<String, ControlInfo> controls,
            Map<String, PortInfo> ports, PMap properties) {
        this.type = type;
        this.controls = controls;
        this.ports = ports;
        this.properties = properties;
    }

    public Class<? extends Component> getType() {
        return type;
    }

    public String[] getControls() {
        return controls.keySet().toArray(new String[controls.size()]);
    }

    public ControlInfo getControlInfo(String control) {
        return controls.get(control);
    }

    public String[] getPorts() {
        return ports.keySet().toArray(new String[controls.size()]);
    }

    public PMap getProperties() {
        return properties;
    }
//
//    public boolean isContainer() {
//        return (children != null);
//    }
//
//    public String[] getChildren() {
//        if (children == null) {
//            return new String[0];
//        } else {
//            return Arrays.copyOf(children, children.length);
//        }
//    }

    //@TODO implement toString
    @Override
    public String toString() {
        return "ComponentInfo toString() not implemented yet";
    }



    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ComponentInfo) {
            ComponentInfo o = (ComponentInfo) obj;
            return type.equals(o.type) &&
                    controls.equals(o.controls) &&
                    ports.equals(o.ports) &&
                    properties.equals(o.properties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 29 * hash + (this.controls != null ? this.controls.hashCode() : 0);
        hash = 29 * hash + (this.ports != null ? this.ports.hashCode() : 0);
        hash = 29 * hash + (this.properties != null ? this.properties.hashCode() : 0);
        return hash;
    }

    public static ComponentInfo create(
            Class<? extends Component> clas,
            Map<String, ControlInfo> controls,
            Map<String, PortInfo> ports,
            PMap properties) {
        return create(clas, controls, ports, null, properties);
    }

    @Deprecated
    public static ComponentInfo create(
            Class<? extends Component> type,
            Map<String, ControlInfo> controls,
            Map<String, PortInfo> ports,
            String[] children,
            PMap properties) {
        if (type == null) {
            throw new NullPointerException();
        }
        if (controls == null) {
            controls = Collections.emptyMap();
        } else {
            controls = new LinkedHashMap<String, ControlInfo>(controls);
        }
        if (ports == null) {
            ports = Collections.emptyMap();
        } else {
            ports = new LinkedHashMap<String, PortInfo>(ports);
        }
        if (properties == null) {
            properties = PMap.EMPTY;
        }

        return new ComponentInfo(type, controls, ports, properties);

    }

    public static ComponentInfo coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof ComponentInfo) {
            return (ComponentInfo) arg;
        }
        throw new ArgumentFormatException();
    }

    public static ArgumentInfo info() {
        return ArgumentInfo.create(ComponentInfo.class, null);
    }
}
