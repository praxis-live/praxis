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

import net.neilcsmith.praxis.core.PortListener;
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

    private ControlPort.Output[] connections;
    private PortListenerSupport pls;
    private final PortInfo info;

    public AbstractControlInputPort() {
        connections = new ControlPort.Output[0];
        pls = new PortListenerSupport(this);
        info = PortInfo.create(ControlPort.class, PortInfo.Direction.IN, null);
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
        pls.fireListeners();
    }

    @Override
    protected void removeControlOutputPort(ControlPort.Output port) {
        List<ControlPort.Output> cons = Arrays.asList(connections);
        int idx = cons.indexOf(port);
        if (idx > -1) {
            cons = new ArrayList<ControlPort.Output>(cons);
            cons.remove(idx);
            connections = cons.toArray(new ControlPort.Output[cons.size()]);
            pls.fireListeners();
        }
    }
    
    
    public void disconnectAll() {
        if (connections.length == 0) {
            return;
        }
        for (ControlPort.Output port : connections) {
            port.disconnect(this);
        }
        pls.fireListeners();
    }

    public void addListener(PortListener listener) {
        pls.addListener(listener);
    }

    public void removeListener(PortListener listener) {
        pls.removeListener(listener);
    }

    

    public Port[] getConnections() {
        return Arrays.copyOf(connections, connections.length);
    }


    public PortInfo getInfo() {
            return info;
    }




    
}
