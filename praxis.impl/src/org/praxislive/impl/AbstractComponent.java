/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2013 Neil C Smith.
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
package org.praxislive.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.praxislive.core.Value;
import org.praxislive.core.Component;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.Container;
import org.praxislive.core.Control;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.InterfaceDefinition;
import org.praxislive.core.Lookup;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Port;
import org.praxislive.core.Root;
import org.praxislive.core.VetoException;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.PortInfo;
import org.praxislive.core.interfaces.ComponentInterface;
import org.praxislive.core.services.Service;
import org.praxislive.core.services.ServiceUnavailableException;
import org.praxislive.core.services.Services;
import org.praxislive.core.types.PMap;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractComponent implements Component {

    private final Map<String, Control> controlMap;
    private final Map<String, Port> portMap;
    private final Set<Class<? extends InterfaceDefinition>> interfaceSet;
    
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
        registerControl(ComponentInterface.INFO,
                ArgumentProperty.createReadOnly(ComponentInfo.info(),
                new ArgumentProperty.ReadBinding() {
                    public Value getBoundValue() {
                        return getInfo();
                    }
                }));
        registerInterface(ComponentInterface.INSTANCE);
    }

    public Control getControl(String id) {
        return controlMap.get(id);
    }

    public ControlAddress getAddress(Control control) {
        ComponentAddress thisAddress = getAddress();
        if (thisAddress != null) {
            for (Map.Entry<String, Control> entry : controlMap.entrySet()) {
                if (entry.getValue() == control) {
                    return ControlAddress.create(thisAddress, entry.getKey());
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
    protected void registerControl(String id, Control control) {
        if (id == null || control == null) {
            throw new NullPointerException();
        }
        if (controlMap.containsKey(id)) {
            throw new IllegalArgumentException();
        }
        controlMap.put(id, control);
        if (control instanceof ExtendedControl) {
            ExtendedControl exc = (ExtendedControl) control;
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
    protected Control unregisterControl(String id) {
        Control control = controlMap.remove(id);
        if (control instanceof ExtendedControl) {
            ExtendedControl exc = (ExtendedControl) control;
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
    protected void registerPort(String id, Port port) {
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
    protected Port unregisterPort(String id) {
        Port port = portMap.remove(id);
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

    @Deprecated
    protected void registerInterface(InterfaceDefinition id) {
        registerInterface(id.getClass());
    }
    
    protected void registerInterface(Class<? extends InterfaceDefinition> type) {
        if (type == null) {
            throw new NullPointerException();
        }
        interfaceSet.add(type);
        info = null;
    }

    @Deprecated
    public final InterfaceDefinition[] getInterfaces() {
        List<InterfaceDefinition> ids = new ArrayList<>();
        for (Class<? extends InterfaceDefinition> idClass : interfaceSet) {
            try {
                ids.add(idClass.newInstance());
            } catch (Exception ex) {
            }
        }
        return ids.toArray(new InterfaceDefinition[ids.size()]);
    }

    public Container getParent() {
        return parent;
    }

    @Deprecated
    public Root getRoot() {
        Container c = getParent();
        if (c == null) {
            return null;
        }
        do {
            if (c.getParent() == null && c instanceof Root) {
                return (Root) c;
            }

        } while ((c = c.getParent()) != null);
        return null;
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
        for (Port p : portMap.values()) {
            p.disconnectAll();
        }
    }

    public void hierarchyChanged() {
        address = null;
        router = null;
        for (Map.Entry<String, Control> entry : controlMap.entrySet()) {
            Control c = entry.getValue();
            if (c instanceof ExtendedControl) {
                ((ExtendedControl) c).hierarchyChanged();
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
                    dynamic ? PMap.create(ComponentInfo.KEY_DYNAMIC, true) : PMap.EMPTY);
        }
        return info;
    }

    private Map<String, ControlInfo> buildControlInfoMap() {
        Map<String, ControlInfo> infos = new LinkedHashMap<String, ControlInfo>();
        Set<Map.Entry<String, Control>> controls = controlMap.entrySet();
        for (Map.Entry<String, Control> entry : controls) {
            ControlInfo inf = entry.getValue().getInfo();
            if (inf != null) {
                infos.put(entry.getKey(), inf);
            }
        }
        return infos;
    }

    private Map<String, PortInfo> buildPortInfoMap() {
        Map<String, PortInfo> infos = new LinkedHashMap<String, PortInfo>();
        Set<Map.Entry<String, Port>> ports = portMap.entrySet();
        for (Map.Entry<String, Port> entry : ports) {
            PortInfo inf = entry.getValue().getInfo();
            if (inf != null) {
                infos.put(entry.getKey(), inf);
            }
        }
        return infos;
    }

    protected Lookup getLookup() {
        if (parent == null) {
            return EmptyLookup.getInstance();
        } else {
            return parent.getLookup();
        }
    }

    protected PacketRouter getPacketRouter() {
        if (router == null) {
            router = getLookup().get(PacketRouter.class);
        }
        return router;
    }

    @Deprecated
    protected ComponentAddress findService(InterfaceDefinition service)
            throws ServiceUnavailableException {
        return findService((Class<? extends Service>) service.getClass());
    }
    
    @Deprecated
    protected ComponentAddress findService(Class<? extends Service> service)
            throws ServiceUnavailableException {
        try {
            Services srvs = getLookup().get(Services.class);
            return srvs.locate(service).get();
        } catch (Exception ex) {
            throw new ServiceUnavailableException();
        }
    }

    public static interface ExtendedControl extends Control {

        public void addNotify(AbstractComponent component);

        public void removeNotify(AbstractComponent component);

        public void hierarchyChanged();
    }
}
