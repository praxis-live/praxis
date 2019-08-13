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
package org.praxislive.impl.components;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.praxislive.core.Component;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ComponentInfo;
import org.praxislive.impl.ContainerContext;
import org.praxislive.core.Lookup;
import org.praxislive.core.Port;
import org.praxislive.core.PortAddress;
import org.praxislive.core.PortConnectionException;
import org.praxislive.core.PortInfo;
import org.praxislive.core.PortListener;
import org.praxislive.core.Value;
import org.praxislive.core.VetoException;
import org.praxislive.core.types.PMap;
import org.praxislive.impl.RegistrationException;
import org.praxislive.impl.AbstractContainer;
import org.praxislive.impl.ArgumentProperty;
import org.praxislive.impl.InstanceLookup;

/**
 *
 * @author Neil C Smith
 */
public class UserContainer extends AbstractContainer {
    
    private final Context context;
    private Lookup lookup;
    
    
    public UserContainer() {
        context = new Context();
        markDynamic();
        registerControl("ports", 
                ArgumentProperty.builder()
                        .type(PMap.class)
                        .binding(new PortsBinding())
                        .build());
    }

    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = InstanceLookup.create(super.getLookup(), context);
        }
        return lookup;
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        lookup = null;
    }

    @Override
    public void addChild(String id, Component child) throws VetoException {
        super.addChild(id, child);
        refreshPortInfo(null);
    }

    @Override
    public Component removeChild(String id) {
        refreshPortInfo(null);
        return super.removeChild(id);
    }

    @Override
    public Port getPort(String id) {
        Port port = super.getPort(id);
        if (port instanceof PortProxy) {
            return ((PortProxy) port).unproxy();
        } else {
            return port;
        }
    }
    
    
    
    private class PortsBinding implements ArgumentProperty.Binding {
        
        private PMap ports = PMap.EMPTY;

        @Override
        public void setBoundValue(long time, Value value) throws Exception {
            PMap newPorts = PMap.from(value).orElseThrow(IllegalArgumentException::new);
            if (ports.equals(newPorts)) {
                return;
            }
            List<PortProxy> lst = new ArrayList<>();
            List<String> newPortNames = newPorts.keys();
            for (String key : newPorts.keys()) {
                if (!PortAddress.isValidID(key)) {
                    throw new IllegalArgumentException("" + key + " : is not a valid port ID");
                }
                String s = newPorts.get(key).toString();
                String[] parts = s.split("!");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("" + s + " : is not a valid relative port");
                }
                String child = parts[0];
                String port = parts[1];
                if (!ComponentAddress.isValidID(child)) {
                    throw new IllegalArgumentException("" + child + " : is not a valid component ID");
                }
                if (!PortAddress.isValidID(port)) {
                    throw new IllegalArgumentException("" + key + " : is not a valid port ID");
                }
                lst.add(new PortProxy(key, child, port));
            }
            for (String id : ports.keys()) {
                if (!newPortNames.contains(id)) {
                    Port p = getPort(id);
                    p.disconnectAll();
                }
                unregisterPort(id);
            }
            for (PortProxy p : lst) {
                registerPort(p.id, p);
            }
            ports = newPorts;
        }

        @Override
        public Value getBoundValue() {
            return ports;
        }
        
    }
     
    private class PortProxy implements PortEx {
        
        private final String id;
        private final String childID;
        private final String portID;
        
        PortProxy(String id, String childID, String portID) {
            this.id = id;
            this.childID = childID;
            this.portID = portID;
        }

        @Override
        public PortInfo getInfo() {
            Component child = getChild(childID);
            if (child != null) {
                ComponentInfo info = child.getInfo();
                if (info != null) {
                    PortInfo portInfo = info.portInfo(portID);
                    if (portInfo != null) {
                        return portInfo;
                    }
                }
            }
            return PortInfo.create(Port.class, PortInfo.Direction.BIDI, PMap.EMPTY);
        }
        
        private Port unproxy() {
            Component child = getChild(childID);
            if (child != null) {
                Port port = child.getPort(portID);
                if (port != null) {
                    return port;
                }
            }
            return this;
        }
        
        @Override
        public void connect(Port port) throws PortConnectionException {
            throw new PortConnectionException();
        }

        @Override
        public void disconnect(Port port) {
        }

        @Override
        public void disconnectAll() {
        }

        @Override
        public Port[] getConnections() {
            return new Port[0];
        }

        @Override
        public void addListener(PortListener listener) {
        }

        @Override
        public void removeListener(PortListener listener) {
        }
        
    }
    
    private class Context extends ContainerContext {

        @Override
        public void registerControl(String id, ControlEx control) throws RegistrationException {
            try {
                UserContainer.this.registerControl(id, control);
            } catch (Exception ex) {
                throw new RegistrationException(ex);
            }
        }

        @Override
        public void unregisterControl(String id, ControlEx control) {
            // check control is correct
            UserContainer.this.unregisterControl(id);
        }

        @Override
        public void registerPort(String id, PortEx port) throws RegistrationException {
            try {
                UserContainer.this.registerPort(id, port);
            } catch (Exception ex) {
                throw new RegistrationException(ex);
            }
        }

        @Override
        public void unregisterPort(String id, PortEx port) {
            // check port is correct
            UserContainer.this.unregisterPort(id);
        }

        @Override
        public void refreshControlInfo(String id, ControlEx control) {
            UserContainer.this.refreshControlInfo(id);
        }

        @Override
        public void refreshPortInfo(String id, PortEx port) {
            UserContainer.this.refreshPortInfo(id);
        }
        
    }
    
}
