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
package org.praxislive.code;

import org.praxislive.core.PortListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.praxislive.core.Value;
import org.praxislive.core.ControlPort;
import org.praxislive.core.Port;
import org.praxislive.core.PortConnectionException;
import org.praxislive.core.PortInfo;

/**
 *
 * @author Neil C Smith
 */
public class ControlInput extends ControlPort.Input {
    
    public final static PortInfo INFO =
            PortInfo.create(ControlPort.class, PortInfo.Direction.IN, null);
    
    private final PortListenerSupport pls;

    private ControlPort.Output[] connections;
    private Link link;

    public ControlInput(Link link) {
        if (link == null) {
            throw new NullPointerException();
        }
        this.link = link;
        connections = new ControlPort.Output[0];
        pls = new PortListenerSupport(this);
    }

    public void setLink(Link link) {
        if (link == null) {
            throw new NullPointerException();
        }
        this.link = link;
    }

    public Link getLink() {
        return link;
    }

    @Override
    protected void addControlOutputPort(ControlPort.Output port) throws PortConnectionException {
        List<ControlPort.Output> cons = Arrays.asList(connections);
        if (cons.contains(port)) {
            throw new PortConnectionException();
        }
        cons = new ArrayList<>(cons);
        cons.add(port);
        connections = cons.toArray(new ControlPort.Output[cons.size()]);
        pls.fireListeners();
    }

    @Override
    protected void removeControlOutputPort(ControlPort.Output port) {
        List<ControlPort.Output> cons = Arrays.asList(connections);
        int idx = cons.indexOf(port);
        if (idx > -1) {
            cons = new ArrayList<>(cons);
            cons.remove(idx);
            connections = cons.toArray(new ControlPort.Output[cons.size()]);
            pls.fireListeners();
        }
    }

    @Override
    public void disconnectAll() {
        if (connections.length == 0) {
            return;
        }
        for (ControlPort.Output port : connections) {
            port.disconnect(this);
        }
        pls.fireListeners();
    }

    @Override
    public void addListener(PortListener listener) {
        pls.addListener(listener);
    }

    @Override
    public void removeListener(PortListener listener) {
        pls.removeListener(listener);
    }

    @Override
    public Port[] getConnections() {
        return Arrays.copyOf(connections, connections.length);
    }

    @Override
    public void receive(long time, double value) {
        link.receive(time, value);
    }

    @Override
    public void receive(long time, Value value) {
        link.receive(time, value);
    }

    public static interface Link {

        public void receive(long time, double value);

        public void receive(long time, Value value);

    }

}
