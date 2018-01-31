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
package org.praxislive.impl;

import java.util.logging.Logger;
import org.praxislive.util.ArrayUtils;
import org.praxislive.core.Port;
import org.praxislive.core.PortConnectionException;
import org.praxislive.core.PortListener;
import org.praxislive.core.info.PortInfo;
import org.praxislive.core.types.PMap;

/**
 *
 * @author nsigma
 */
@Deprecated
public final class LinkPort<T> implements Port {
    
    public final static String ID = "link";

    private final static Logger LOG = Logger.getLogger(LinkPort.class.getName());
    private final Class<T> type;
    private final Handler<T> handler;
    private final T source;
    private final PortInfo info;
    private final PortListenerSupport pls;
    private LinkPort[] connections;
    private boolean inUpdate;

    public LinkPort(Class<T> type, Handler<T> handler, T source) {
        if (type == null || handler == null || source == null) {
            throw new NullPointerException();
        }
        this.type = type;
        this.handler = handler;
        this.source = source;
        this.info = PortInfo.create(LinkPort.class, PortInfo.Direction.BIDI, PMap.EMPTY);
        this.pls = new PortListenerSupport(this);
        connections = new LinkPort[0];
    }

    public void connect(Port port) throws PortConnectionException {
        if (port instanceof LinkPort) {
            LinkPort lport = (LinkPort) port;
            if (!valid(lport)) {
                throw new PortConnectionException("Trying to connect incompatible LinkPort");
            }
            if (connected(lport)) {
                throw new PortConnectionException("Ports already connected");
            }
            connections = ArrayUtils.add(connections, lport);
            lport.connections = ArrayUtils.add(lport.connections, this);
            inUpdate = true;
            lport.doUpdate(source);
            inUpdate = false;
            pls.fireListeners();
        } else {
            throw new PortConnectionException("Port is not a LinkPort");
        }
    }

    public void disconnect(Port port) {
        if (port instanceof LinkPort) {
            LinkPort lport = (LinkPort) port;
            connections = ArrayUtils.remove(connections, lport);
            lport.connections = ArrayUtils.remove(lport.connections, this);
            pls.fireListeners();
        }
    }

    public void disconnectAll() {
        if (connections.length == 0) {
            return;
        }
        for (LinkPort port : connections) {
            port.connections = ArrayUtils.remove(port.connections, this);
        }
        connections = new LinkPort[0];
        pls.fireListeners();
    }

    private boolean connected(LinkPort port) {
        for (LinkPort p : connections) {
            if (p == port) {
                return true;
            }
        }
        return false;
    }

    private boolean valid(LinkPort port) {
        return port.type.equals(type);
    }

    public Port[] getConnections() {
        return connections.clone();
    }

    public PortInfo getInfo() {
        return info;
    }

    public void addListener(PortListener listener) {
        pls.addListener(listener);
    }

    public void removeListener(PortListener listener) {
        pls.removeListener(listener);
    }

    public void fireUpdate() {
        if (inUpdate) {
            LOG.warning("fireUpdate() called during update cycle");
            return;
        }
        inUpdate = true;
        for (LinkPort port : connections) {
            port.doUpdate(source);
        }
        inUpdate = false;
    }

    private void doUpdate(T source) {
        if (inUpdate) {
            LOG.finest("doUpdate() called during update cycle.");
            return;
        }
        inUpdate = true;
        handler.update(source);
        for (LinkPort port : connections) {
            port.doUpdate(source);
        }
        inUpdate = false;
    }

    public static interface Handler<T> {

        public void update(T source);
    }
}
