/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 Neil C Smith.
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
package net.neilcsmith.praxis.impl;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.Container;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.InterfaceDefinition;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.VetoException;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.info.ComponentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.info.PortInfo;
import net.neilcsmith.praxis.core.interfaces.ComponentInterface;
import net.neilcsmith.praxis.core.interfaces.ServiceManager;
import net.neilcsmith.praxis.core.interfaces.ServiceUnavailableException;
import net.neilcsmith.praxis.core.types.PMap;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractComponent implements Component {

    private Map<String, Control> controlMap;
    private Map<String, Port> portMap;
    private Set<InterfaceDefinition> interfaceSet;
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
        controlMap = new LinkedHashMap<String, Control>();
        portMap = new LinkedHashMap<String, Port>();
        interfaceSet = new LinkedHashSet<InterfaceDefinition>();
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

            public Argument getBoundValue() {
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
            ((ExtendedControl) control).addNotify(this);
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
            ((ExtendedControl) control).removeNotify(this);
        }
        if (control != null) {
            info = null;
        }
        return control;
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

    protected void registerInterface(InterfaceDefinition id) {
        if (id == null) {
            throw new NullPointerException();
        }
        interfaceSet.add(id);
    }

    public final InterfaceDefinition[] getInterfaces() {
        return interfaceSet.toArray(new InterfaceDefinition[interfaceSet.size()]);
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
                hierarchyChanged(); // defer to hierarchy changed for all uncaching
            }
        } else {
            if (this.parent != null) {
                throw new VetoException();
            }
            this.parent = parent;
            hierarchyChanged(); // as above
        }
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
                    getClass(),
                    interfaceSet,
                    controls,
                    ports,
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

     protected ComponentAddress findService(InterfaceDefinition service)
            throws ServiceUnavailableException {
        ServiceManager sm = getLookup().get(ServiceManager.class);
        if (sm == null) {
            throw new ServiceUnavailableException("No ServiceManager in Lookup");
        }
        return sm.findService(service);

    }


    public static interface ExtendedControl extends Control {

        public void addNotify(AbstractComponent component);

        public void removeNotify(AbstractComponent component);

        public void hierarchyChanged();
    }
}
