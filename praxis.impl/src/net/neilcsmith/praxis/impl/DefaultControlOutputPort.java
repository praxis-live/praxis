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
import net.neilcsmith.praxis.core.Argument;
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
public class DefaultControlOutputPort extends ControlPort.Output {

    private ControlPort.Input[] connections;
    private Component component;
//    private static PortSorter sorter = new PortSorter();
//    private PortConnectionListener[] listeners;
    private List<PortConnectionListener> listeners;
    private boolean sending;
    private final PortInfo emptyInfo;


    public DefaultControlOutputPort(Component component) {
        if (component == null) {
            throw new NullPointerException();
        }
        this.component = component;
        connections = new ControlPort.Input[0];
        listeners = new ArrayList<PortConnectionListener>();
        emptyInfo = PortInfo.create(getTypeClass(), getDirection(), new PortAddress[0], null);
    }

    public void connect(Port port) throws PortConnectionException {
        if (port instanceof ControlPort.Input) {
            ControlPort.Input cport = (ControlPort.Input) port;
            List<ControlPort.Input> cons = Arrays.asList(connections);
            if (cons.contains(cport)) {
                throw new PortConnectionException();
            }
            makeConnection(cport);
            cons = new ArrayList<ControlPort.Input>(cons);
            cons.add(cport);
//            Collections.sort(cons, sorter);
            connections = cons.toArray(new ControlPort.Input[cons.size()]);
            fireConnectionListeners();
        } else {
            throw new PortConnectionException();
        }
    }

    public void disconnect(Port port) {
        if (port instanceof ControlPort.Input) {
            ControlPort.Input cport = (ControlPort.Input) port;
            List<ControlPort.Input> cons = Arrays.asList(connections);
            int idx = cons.indexOf(cport);
            if (idx > -1) {
                breakConnection(cport);
                cons = new ArrayList<ControlPort.Input>(cons);
                cons.remove(idx);
                connections = cons.toArray(new ControlPort.Input[cons.size()]);
                fireConnectionListeners();
            }
        }
    }

    public void disconnectAll() {
        ControlPort.Input[] cons = connections;
        for (ControlPort.Input port : cons) {
            breakConnection(port);
        }
        connections = new ControlPort.Input[0];
        fireConnectionListeners();
    }

    public Port[] getConnections() {
        return Arrays.copyOf(connections, connections.length);
    }

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

    

    public void send(long time, double value) {
        if (sending) {
            return; // @TODO recursion strategy - allow up to maximum count?
        }
        sending = true;
        for (ControlPort.Input port : connections) {
            try {
                port.receive(time, value);
            } catch (Exception ex) {
                // @TODO log errors
            }
            
        }
        sending = false;
    }

//    public void send(int value) {
//        if (sending) {
//            return;
//        }
//        sending = true;
//        for (ControlPort.Input port : connections) {
//            port.receive(value);
//        }
//        sending = false;
//    }

    public void send(long time, Argument value) {
        if (sending) {
            return;
        }
        sending = true;
        for (ControlPort.Input port : connections) {
            try {
                port.receive(time, value);
            } catch (Exception ex) {
                // @TODO log errors
            }
        }
        sending = false;
    }

//    private static class PortSorter implements Comparator<ControlPort.Input> {
//
//        public int compare(ControlPort.Input o1, ControlPort.Input o2) {
//            return o1.getIndex() - o2.getIndex();
//        }
//    }
}
