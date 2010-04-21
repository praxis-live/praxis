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
package net.neilcsmith.praxis.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.Container;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.ParentVetoException;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.PortConnectionListener;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.info.ComponentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.info.PortInfo;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractComponent implements Component {

    private Map<String, Control> controlMap;
    private Map<String, Port> portMap;
    private PortConnectionListener portListener;
    private Container parent;
    private Root root;
    private ComponentAddress address;
    ComponentInfo info;
//    boolean componentInfoValid;
    boolean controlInfoValid;
    boolean portInfoValid;

    /**
     *
     */
    public AbstractComponent() {
        controlMap = new LinkedHashMap<String, Control>();
        portMap = new LinkedHashMap<String, Port>();
        portListener = new PortConnectionListener() {

            public void connectionsChanged(Port source) {
                portInfoValid = false;
            }
        };
    }

    public Control getControl(String id) {
        return controlMap.get(id);
    }


    public String getControlID(Control control) {
        for (Map.Entry<String, Control> entry : controlMap.entrySet()) {
            if (entry.getValue() == control) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String[] getControlIDs() {
        Set<String> keyset = controlMap.keySet();
        return keyset.toArray(new String[keyset.size()]);
    }

    /**
     *
     * @param id
     * @param control
     */
    protected void registerControl(String id, Control control) {
        if (id == null || control == null) {
            throw new NullPointerException();
        }
        if (controlMap.containsKey(id)) {
            throw new IllegalArgumentException();
        }
        controlMap.put(id, control);
        controlInfoValid = false;
    }

    /**
     *
     * @param id
     * @return
     */
    protected Control unregisterControl(String id) {
        Control control = controlMap.remove(id);
        if (control != null) {
            controlInfoValid = false;
        }
        return control;
    }

    public Port getPort(String id) {
        return portMap.get(id);
    }


    public String getPortID(Port port) {
        for (Map.Entry<String, Port> entry : portMap.entrySet()) {
            if (entry.getValue() == port) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     *
     * @param port
     * @param byType
     * @param byDirection
     * @return
     */
    public int getPortIndex(Port port, boolean byType, boolean byDirection) {
        int val = -1;
        Class<? extends Port> type = port.getTypeClass();
        Port.Direction dir = port.getDirection();
        for (Map.Entry<String, Port> entry : portMap.entrySet()) {
            Port entryPort = entry.getValue();
            if ((!byType || entryPort.getTypeClass().equals(type)) &&
                    (!byDirection || entryPort.getDirection().equals(dir))) {
                val++;
            }
            if (entryPort == port) {
                return val;
            }
        }
        return -1;
    }

    public String[] getPortIDs() {
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
        port.addConnectionListener(portListener);
        portInfoValid = false;
    }

    /**
     *
     * @param id
     * @return
     */
    protected Port unregisterPort(String id) {
        Port port = portMap.remove(id);
        if (port != null) {
            port.removeConnectionListener(portListener);
            port.disconnectAll();
            portInfoValid = false;
        }
        return port;
    }

    public Container getParent() {
        return parent;
    }
    
    public Root getRoot() {
        return root;
    }

    public ComponentAddress getAddress() {
        if (address == null) {
            if (parent != null) {
                ComponentAddress parentAddress = parent.getAddress();
                String thisID = parent.getChildID(this);
//                try {
                    address = ComponentAddress.create(parentAddress, thisID);
//                } catch (ArgumentFormatException ex) {
//                    address = null;
//                }
            }
        }
        return address;
    }

    public void parentNotify(Container parent) throws ParentVetoException {
        if (parent == null) {
            if (this.parent != null) {
                this.parent = null;
                hierarchyChanged(); // defer to hierarchy changed for all uncaching
            }
        } else {
            if (this.parent != null) {
                throw new ParentVetoException();
            }
            this.parent = parent;
            hierarchyChanged(); // as above
        }
    }

    public void hierarchyChanged() {
        root = parent == null ? null : parent.getRoot(); 
        address = null;
    }

    public ComponentInfo getInfo() {
        if (info == null || !portInfoValid || !controlInfoValid) {
            Map<String, ControlInfo> controls = buildControlInfoMap();
            Map<String, PortInfo> ports = buildPortInfoMap();
            info = ComponentInfo.create(getClass(), controls, ports, null);
            portInfoValid = true;
            controlInfoValid = true;
        }
        return info;
    }

    Map<String, ControlInfo> buildControlInfoMap() {
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

    Map<String, PortInfo> buildPortInfoMap() {
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
}
