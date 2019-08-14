/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2018 Neil C Smith.
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
package org.praxislive.impl;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.praxislive.core.Value;
import org.praxislive.core.Component;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.Container;
import org.praxislive.core.Control;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.Lookup;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Port;
import org.praxislive.core.VetoException;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.PortInfo;
import org.praxislive.core.Protocol;
import org.praxislive.core.protocols.ComponentProtocol;
import org.praxislive.core.services.Service;
import org.praxislive.core.services.ServiceUnavailableException;
import org.praxislive.core.services.Services;
import org.praxislive.core.types.PMap;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractComponent implements Component {

    private final Map<String, ControlEx> controlMap;
    private final Map<String, PortEx> portMap;
    private final Set<Class<? extends Protocol>> interfaceSet;
    
    private Container parent;
    private ComponentAddress address;
    private ComponentInfo info;
    private PacketRouter router;
    private boolean dynamic;

    /**
     *
     */
    protected AbstractComponent() {
        this(true);
    }

    protected AbstractComponent(boolean componentInterface) {
        controlMap = new LinkedHashMap<>();
        portMap = new LinkedHashMap<>();
        interfaceSet = new LinkedHashSet<>();
        if (componentInterface) {
            buildComponentInterface();
        }
    }

    protected void markDynamic() {
        dynamic = true;
    }

    private void buildComponentInterface() {
        registerControl(ComponentProtocol.INFO,
                ArgumentProperty.createReadOnly(ComponentInfo.info(),
                new ArgumentProperty.ReadBinding() {
                    public Value getBoundValue() {
                        return getInfo();
                    }
                }));
        registerProtocol(ComponentProtocol.class);
    }

    @Override
    public Control getControl(String id) {
        return controlMap.get(id);
    }

    public ControlAddress getAddress(ControlEx control) {
        ComponentAddress thisAddress = getAddress();
        if (thisAddress != null) {
            for (Map.Entry<String, ControlEx> entry : controlMap.entrySet()) {
                if (entry.getValue() == control) {
                    return ControlAddress.of(thisAddress, entry.getKey());
                }
            }
        }
        return null;
    }

    public final String[] getControlIDs() {
        Set<String> keyset = controlMap.keySet();
        return keyset.toArray(new String[keyset.size()]);
    }

    /**
     *
     * @param id
     * @param control
     */
    // @TODO only nullify info if control is visible
    protected void registerControl(String id, ControlEx control) {
        if (id == null || control == null) {
            throw new NullPointerException();
        }
        if (controlMap.containsKey(id)) {
            throw new IllegalArgumentException();
        }
        controlMap.put(id, control);
        if (control instanceof ControlEx) {
            ControlEx exc = (ControlEx) control;
            exc.addNotify(this);
            exc.hierarchyChanged();
        }
        info = null;
    }

    /**
     *
     * @param id
     * @return
     */
    protected ControlEx unregisterControl(String id) {
        ControlEx control = controlMap.remove(id);
        if (control instanceof ControlEx) {
            ControlEx exc = (ControlEx) control;
            exc.removeNotify(this);
            exc.hierarchyChanged();
        }
        if (control != null) {
            info = null;
        }
        return control;
    }

    protected void refreshControlInfo(String id) {
        info = null;
    }

    @Override
    public Port getPort(String id) {
        return portMap.get(id);
    }

    public final String[] getPortIDs() {
        Set<String> keyset = portMap.keySet();
        return keyset.toArray(new String[keyset.size()]);
    }

    /**
     *
     * @param id
     * @param port
     */
    protected void registerPort(String id, PortEx port) {
        if (id == null || port == null) {
            throw new NullPointerException();
        }
        if (portMap.containsKey(id)) {
            throw new IllegalArgumentException();
        }
        portMap.put(id, port);
        info = null;
    }

    /**
     *
     * @param id
     * @return
     */
    protected PortEx unregisterPort(String id) {
        PortEx port = portMap.remove(id);
        if (port != null) {
//            port.removeConnectionListener(portListener);
            port.disconnectAll();
            info = null;
        }
        return port;
    }

    protected void refreshPortInfo(String id) {
        info = null;
    }

    protected void registerProtocol(Class<? extends Protocol> type) {
        if (type == null) {
            throw new NullPointerException();
        }
        interfaceSet.add(type);
        info = null;
    }

    public Container getParent() {
        return parent;
    }

    public ComponentAddress getAddress() {
        if (parent != null) {
            return parent.getAddress(this);
        } else {
            return null;
        }
    }

    public void parentNotify(Container parent) throws VetoException {
        if (parent == null) {
            if (this.parent != null) {
                this.parent = null;
                disconnectAll();
                // hierarchyChanged(); // defer to hierarchy changed for all uncaching
            }
        } else {
            if (this.parent != null) {
                throw new VetoException();
            }
            this.parent = parent;
            //  hierarchyChanged(); // as above
        }
//        address = null;
//        router = null;
    }

    private void disconnectAll() {
        for (PortEx p : portMap.values()) {
            p.disconnectAll();
        }
    }

    public void hierarchyChanged() {
        address = null;
        router = null;
        for (Map.Entry<String, ControlEx> entry : controlMap.entrySet()) {
            ControlEx c = entry.getValue();
            if (c instanceof ControlEx) {
                ((ControlEx) c).hierarchyChanged();
            }
        }
    }

    public final ComponentInfo getInfo() {
        if (info == null) {
            Map<String, ControlInfo> controls = buildControlInfoMap();
            Map<String, PortInfo> ports = buildPortInfoMap();
            info = ComponentInfo.create(
                    controls,
                    ports,
                    interfaceSet,
                    dynamic ? PMap.of(ComponentInfo.KEY_DYNAMIC, true) : PMap.EMPTY);
        }
        return info;
    }

    private Map<String, ControlInfo> buildControlInfoMap() {
        Map<String, ControlInfo> infos = new LinkedHashMap<String, ControlInfo>();
        Set<Map.Entry<String, ControlEx>> controls = controlMap.entrySet();
        for (Map.Entry<String, ControlEx> entry : controls) {
            ControlInfo inf = entry.getValue().getInfo();
            if (inf != null) {
                infos.put(entry.getKey(), inf);
            }
        }
        return infos;
    }

    private Map<String, PortInfo> buildPortInfoMap() {
        Map<String, PortInfo> infos = new LinkedHashMap<String, PortInfo>();
        Set<Map.Entry<String, PortEx>> ports = portMap.entrySet();
        for (Map.Entry<String, PortEx> entry : ports) {
            PortInfo inf = entry.getValue().getInfo();
            if (inf != null) {
                infos.put(entry.getKey(), inf);
            }
        }
        return infos;
    }

    protected Lookup getLookup() {
        if (parent == null) {
            return Lookup.EMPTY;
        } else {
            return parent.getLookup();
        }
    }

    protected PacketRouter getPacketRouter() {
        if (router == null) {
            router = getLookup().find(PacketRouter.class).orElse(null);
        }
        return router;
    }
    
    protected ComponentAddress findService(Class<? extends Service> service)
            throws ServiceUnavailableException {
        return getLookup().find(Services.class)
                .flatMap(sm -> sm.locate(service))
                .orElseThrow(ServiceUnavailableException::new);
    }

    public static interface ControlEx extends Control {

        public default void addNotify(AbstractComponent component) {}

        public default void removeNotify(AbstractComponent component) {}

        public default void hierarchyChanged() {};

        public ControlInfo getInfo();
        
    }
    
    public static interface PortEx extends Port {
        
        public PortInfo getInfo();
        
    }
}
