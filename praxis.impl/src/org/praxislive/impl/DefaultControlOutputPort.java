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

import org.praxislive.util.PortListenerSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.praxislive.core.Value;
import org.praxislive.core.Component;
import org.praxislive.core.ControlPort;
import org.praxislive.core.Port;
import org.praxislive.core.PortConnectionException;
import org.praxislive.core.PortListener;
import org.praxislive.core.PortInfo;

/**
 *
 * @author Neil C Smith
 */
public class DefaultControlOutputPort extends ControlPort.Output implements AbstractComponent.PortEx {

    private ControlPort.Input[] connections;
    private PortListenerSupport pls;
    private boolean sending;
    private final PortInfo info;

    public DefaultControlOutputPort() {
        this(null);
    }
       
    @Deprecated
    public DefaultControlOutputPort(Component component) {
        connections = new ControlPort.Input[0];
        pls = new PortListenerSupport(this);
        info = PortInfo.create(ControlPort.class, PortInfo.Direction.OUT, null);
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
            pls.fireListeners();
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
                pls.fireListeners();
            }
        }
    }

    public void disconnectAll() {
        if (connections.length == 0) {
            return;
        }
        for (ControlPort.Input port : connections) {
            breakConnection(port);
        }
        connections = new ControlPort.Input[0];
        pls.fireListeners();
    }

    public Port[] getConnections() {
        return Arrays.copyOf(connections, connections.length);
    }

    public void addListener(PortListener listener) {
        pls.addListener(listener);
    }

    public void removeListener(PortListener listener) {
        pls.removeListener(listener);
    }

    public PortInfo getInfo() {
        return info;
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

    public void send(long time, Value value) {
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
}
