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

import net.neilcsmith.praxis.core.PortConnectionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.PortAddress;
import net.neilcsmith.praxis.core.PortConnectionException;
import net.neilcsmith.praxis.core.info.PortInfo;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractControlInputPort extends ControlPort.Input {

    private Component component;
    private ControlPort.Output[] connections;
    private List<PortConnectionListener> listeners;
    private final PortInfo emptyInfo;

    public AbstractControlInputPort(Component component) {
        if (component == null) {
            throw new NullPointerException();
        }
        this.component = component;
        connections = new ControlPort.Output[0];
        listeners = new ArrayList<PortConnectionListener>();
        emptyInfo = PortInfo.create(getTypeClass(), getDirection(), new PortAddress[0], null);
    }

    @Override
    protected void addControlOutputPort(ControlPort.Output port) throws PortConnectionException {
        List<ControlPort.Output> cons = Arrays.asList(connections);
        if (cons.contains(port)) {
            throw new PortConnectionException();
        }
        cons = new ArrayList<ControlPort.Output>(cons);
        cons.add(port);
        connections = cons.toArray(new ControlPort.Output[cons.size()]);
        fireConnectionListeners();
    }

    @Override
    protected void removeControlOutputPort(ControlPort.Output port) {
        List<ControlPort.Output> cons = Arrays.asList(connections);
        int idx = cons.indexOf(port);
        if (idx > -1) {
            cons = new ArrayList<ControlPort.Output>(cons);
            cons.remove(idx);
            connections = cons.toArray(new ControlPort.Output[cons.size()]);
            fireConnectionListeners();
        }
    }
    
    
    public void disconnectAll() {
        ControlPort.Output[] cons = connections;
        for (ControlPort.Output port : cons) {
            port.disconnect(this);
        }
    }

    public Port[] getConnections() {
        return Arrays.copyOf(connections, connections.length);
    }

    public PortAddress getAddress() {
//        try {
            ComponentAddress ad = component.getAddress();
            if (ad == null) {
                return null;
            } else {
                return PortAddress.create(component.getAddress(), component.getPortID(this));
            }
//        } catch (ArgumentFormatException ex) {
//            return null;
//        }
    }

    public PortInfo getInfo() {
        if (connections.length == 0) {
            return emptyInfo;
        } else {
            PortAddress[] ads = new PortAddress[connections.length];
            for (int i=0, k=ads.length; i < k; i++) {
                ads[i] = connections[i].getAddress();
            }
            return PortInfo.create(emptyInfo, ads);
        }
    }

    public Component getComponent() {
        return component;
    }

    

//    @Override
//    public int getIndex() {
//        return component.getPortIndex(this, true, true);
//    }
    
    

    public void addConnectionListener(PortConnectionListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        listeners.add(listener);
    }

    public void removeConnectionListener(PortConnectionListener listener) {
        listeners.remove(listener);
    }

    private void fireConnectionListeners() {
        for (PortConnectionListener listener : listeners) {
            listener.connectionsChanged(this);
        }
    }
    
}
