/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Neil C Smith.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.InterfaceDefinition;
import net.neilcsmith.praxis.core.types.PMap;

/**
 *
 * @author Neil C Smith
 */
public class ComponentInfo extends Argument {
    
    public final static String KEY_DYNAMIC = "dynamic";

    private Class<? extends Component> type;
    private InterfaceDefinition[] interfaces;
    private Map<String, ControlInfo> controls;
    private Map<String, PortInfo> ports;
    private PMap properties;

    private ComponentInfo(Class<? extends Component> type, InterfaceDefinition[] interfaces,
            Map<String, ControlInfo> controls, Map<String, PortInfo> ports, PMap properties) {
        this.type = type;
        this.interfaces = interfaces;
        this.controls = controls;
        this.ports = ports;
        this.properties = properties;
    }

    public Class<? extends Component> getType() {
        return type;
    }

    public InterfaceDefinition[] getInterfaces() {
        return interfaces.clone();
    }

    public String[] getControls() {
        return controls.keySet().toArray(new String[controls.size()]);
    }

    public ControlInfo getControlInfo(String control) {
        return controls.get(control);
    }

    public String[] getPorts() {
        return ports.keySet().toArray(new String[ports.size()]);
    }

    public PortInfo getPortInfo(String port) {
        return ports.get(port);
    }

    public PMap getProperties() {
        return properties;
    }

    //@TODO implement toString
    @Override
    public String toString() {
        return "ComponentInfo toString() not implemented yet";
    }

    @Override
    public boolean isEquivalent(Argument arg) {
        return equals(arg);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ComponentInfo) {
            ComponentInfo o = (ComponentInfo) obj;
            return type.equals(o.type) &&
                    Arrays.equals(interfaces, interfaces) &&
                    controls.equals(o.controls) &&
                    ports.equals(o.ports) &&
                    properties.equals(o.properties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 79 * hash + Arrays.deepHashCode(this.interfaces);
        hash = 79 * hash + (this.controls != null ? this.controls.hashCode() : 0);
        hash = 79 * hash + (this.ports != null ? this.ports.hashCode() : 0);
        hash = 79 * hash + (this.properties != null ? this.properties.hashCode() : 0);
        return hash;
    }


//    public static ComponentInfo create(
//            Class<? extends Component> clas,
//            Map<String, ControlInfo> controls,
//            Map<String, PortInfo> ports,
//            PMap properties) {
//        return create(clas, controls, ports, null, properties);
//    }


    public static ComponentInfo create(
            Class<? extends Component> type,
            Set<InterfaceDefinition> interfaces,
            Map<String, ControlInfo> controls,
            Map<String, PortInfo> ports,
            PMap properties) {
        if (type == null) {
            throw new NullPointerException();
        }
        InterfaceDefinition[] ids = new InterfaceDefinition[0];
        if (interfaces != null && !interfaces.isEmpty()) {
            ids = interfaces.toArray(ids);
        }
        if (controls == null || controls.isEmpty()) {
            controls = Collections.emptyMap();
        } else {
            controls = new LinkedHashMap<String, ControlInfo>(controls);
        }
        if (ports == null || ports.isEmpty()) {
            ports = Collections.emptyMap();
        } else {
            ports = new LinkedHashMap<String, PortInfo>(ports);
        }
        if (properties == null) {
            properties = PMap.EMPTY;
        }

        return new ComponentInfo(type, ids, controls, ports, properties);

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
