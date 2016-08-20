/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2016 Neil C Smith.
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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.InterfaceDefinition;
import net.neilcsmith.praxis.core.PortAddress;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith
 */
public class ComponentInfo extends Argument {

    public final static String KEY_DYNAMIC = "dynamic";

    private final static String INFO_PREFIX = "INFO:";

//    private Class<? extends Component> type;
    private final Set<Class<? extends InterfaceDefinition>> interfaces;
    private final PMap controls;
    private final PMap ports;
    private final PMap properties;

    private volatile String string;

    private ComponentInfo(Set<Class<? extends InterfaceDefinition>> interfaces,
            PMap controls,
            PMap ports,
            PMap properties,
            String string
    ) {
//        this.type = type;
        this.interfaces = interfaces;
        this.controls = controls;
        this.ports = ports;
        this.properties = properties;
        this.string = string;
    }

    public boolean hasInterface(Class<? extends InterfaceDefinition> type) {
        return interfaces.contains(type);
    }

    public Set<Class<? extends InterfaceDefinition>> getAllInterfaces() {
        return interfaces;
    }

    public String[] getControls() {
        return controls.getKeys();
    }

    public ControlInfo getControlInfo(String control) {
        try {
            return ControlInfo.coerce(controls.get(control));
        } catch (ArgumentFormatException ex) {
            Logger.getLogger(ComponentInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public String[] getPorts() {
        return ports.getKeys();
    }

    public PortInfo getPortInfo(String port) {
        try {
            return PortInfo.coerce(ports.get(port));
        } catch (ArgumentFormatException ex) {
            Logger.getLogger(ComponentInfo.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public PMap getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        String str = string;
        if (str == null) {
            str = buildString();
            string = str;
        }
        return str;
    }

    private String buildString() {
        PString[] ints = new PString[interfaces.size()];
        int i=0;
        for (Class<? extends InterfaceDefinition> id : interfaces) {
            ints[i++] = PString.valueOf(id.getName());
        }
        PArray arr = PArray.valueOf(
                PString.valueOf(INFO_PREFIX),
                controls,
                ports,
                PArray.valueOf(ints),
                properties
        );
        return arr.toString();           
    }

    @Override
    public boolean isEquivalent(Argument arg) {
        try {
            if (this == arg) {
                return true;
            }
            ComponentInfo other = ComponentInfo.coerce(arg);
            return interfaces.equals(other.interfaces)
                    && controls.isEquivalent(other.controls)
                    && ports.isEquivalent(other.ports)
                    && properties.isEquivalent(other.properties);
        } catch (ArgumentFormatException ex) {
            return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ComponentInfo) {
            ComponentInfo o = (ComponentInfo) obj;
            return interfaces.equals(o.interfaces)
                    && controls.equals(o.controls)
                    && ports.equals(o.ports)
                    && properties.equals(o.properties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
//        hash = 79 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 79 * hash + interfaces.hashCode();
        hash = 79 * hash + (this.controls != null ? this.controls.hashCode() : 0);
        hash = 79 * hash + (this.ports != null ? this.ports.hashCode() : 0);
        hash = 79 * hash + (this.properties != null ? this.properties.hashCode() : 0);
        return hash;
    }

    public static ComponentInfo create(
            Map<String, ControlInfo> controls,
            Map<String, PortInfo> ports,
            Set<Class<? extends InterfaceDefinition>> interfaces,
            PMap properties) {

        PMap ctrls;
        if (controls == null || controls.isEmpty()) {
            ctrls = PMap.EMPTY;
        } else {
            PMap.Builder cBld = PMap.builder(controls.size());
            for (Map.Entry<String, ControlInfo> control : controls.entrySet()) {
                cBld.put(control.getKey(), control.getValue());
            }
            ctrls = cBld.build();
        }
        PMap prts;
        if (ports == null || ports.isEmpty()) {
            prts = PMap.EMPTY;
        } else {
            PMap.Builder pBld = PMap.builder(ports.size());
            for (Map.Entry<String, PortInfo> port : ports.entrySet()) {
                pBld.put(port.getKey(), port.getValue());
            }
            prts = pBld.build();
        }
        if (interfaces == null || interfaces.isEmpty()) {
            interfaces = Collections.emptySet();
        } else {
            interfaces = Collections.unmodifiableSet(new LinkedHashSet<>(interfaces));
        }
        if (properties == null) {
            properties = PMap.EMPTY;
        }

        return new ComponentInfo(interfaces, ctrls, prts, properties, null);

    }

    public static ComponentInfo coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof ComponentInfo) {
            return (ComponentInfo) arg;
        } else {
            return valueOf(arg.toString());
        }
    }
    
    public static Optional<ComponentInfo> from(Argument arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ArgumentFormatException ex) {
            return Optional.empty();
        }
    }

    public static ArgumentInfo info() {
        return ArgumentInfo.create(ComponentInfo.class, null);
    }

    private static ComponentInfo valueOf(String string) throws ArgumentFormatException {
        try {
            PArray arr = PArray.valueOf(string);
            if (arr.getSize() < 4 || !INFO_PREFIX.equals(arr.get(0).toString())) {
                throw new ArgumentFormatException();
            }
            // arr(1) is controls
            PArray ctrls = PArray.coerce(arr.get(1));
            int len = ctrls.getSize();
            PMap controls;
            if (len == 0) {
                controls = PMap.EMPTY;
            } else {
                PMap.Builder cBld = PMap.builder(ctrls.getSize() / 2);
                for (int i = 0; i < len; i += 2) {
                    String id = ctrls.get(i).toString();
                    if (!ControlAddress.isValidID(id)) {
                        throw new ArgumentFormatException("Invalid control ID " + id);
                    }
                    cBld.put(id, ControlInfo.coerce(ctrls.get(i + 1)));
                }
                controls = cBld.build();
            }

            // arr(2) is ports
            PArray pts = PArray.coerce(arr.get(2));
            len = pts.getSize();
            PMap ports;
            if (len == 0) {
                ports = PMap.EMPTY;
            } else {
                PMap.Builder pBld = PMap.builder(pts.getSize() / 2);
                for (int i = 0; i < len; i += 2) {
                    String id = pts.get(i).toString();
                    if (!PortAddress.isValidID(id)) {
                        throw new ArgumentFormatException("Invalid port ID: " + id);
                    }
                    pBld.put(id, PortInfo.coerce(pts.get(i + 1)));
                }
                ports = pBld.build();
            }

            // optional arr(3) is interfaces
            Set<Class<? extends InterfaceDefinition>> interfaces;
            if (arr.getSize() > 3) {
                PArray ints = PArray.coerce(arr.get(3));
                interfaces = new LinkedHashSet<>();
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                for (int i = 0; i < ints.getSize(); i++) {
                    interfaces.add((Class<? extends InterfaceDefinition>) cl.loadClass(ints.get(i).toString()));
                }
                interfaces = Collections.unmodifiableSet(interfaces);
            } else {
                interfaces = Collections.emptySet();
            }

            // optional arr(4) is properties
            PMap properties;
            if (arr.getSize() > 4) {
                properties = PMap.coerce(arr.get(4));
            } else {
                properties = PMap.EMPTY;
            }

            return new ComponentInfo(interfaces, controls, ports, properties, null);
        } catch (Exception ex) {
            throw new ArgumentFormatException(ex);
        }
    }
}
