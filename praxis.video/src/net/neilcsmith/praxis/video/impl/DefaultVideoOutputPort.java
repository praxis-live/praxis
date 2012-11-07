/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Neil C Smith.
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
package net.neilcsmith.praxis.video.impl;

import net.neilcsmith.praxis.core.*;
import net.neilcsmith.praxis.impl.PortListenerSupport;
import net.neilcsmith.praxis.video.VideoPort;
import net.neilcsmith.praxis.video.pipes.VideoPipe;

/**
 *
 * @author Neil C Smith
 */
public class DefaultVideoOutputPort extends VideoPort.Output {

    private VideoPipe source;
    private VideoPort.Input connection;
    private PortListenerSupport pls;

    public DefaultVideoOutputPort(Component host, VideoPipe source) {
        if (source == null) {
            throw new NullPointerException();
        }
        this.source = source;
        pls = new PortListenerSupport(this);
    }

    public void connect(Port port) throws PortConnectionException {
        if (connection != null) {
            throw new PortConnectionException();
        }
        if (port instanceof VideoPort.Input) {
            VideoPort.Input ip = (VideoPort.Input) port;
            makeConnection(ip, source);
            connection = ip;
            pls.fireListeners();
        } else {
            throw new PortConnectionException();
        }
    }

    public void disconnect(Port port) {
        if (connection != null && connection == port) {
            breakConnection(connection, source);
            connection = null;
            pls.fireListeners();
        }
    }

    public void disconnectAll() {
        if (connection != null) {
            disconnect(connection);
            pls.fireListeners();
        }
    }

    public Port[] getConnections() {
        return connection == null ? new Port[0] : new Port[]{connection};
    }

    public void addListener(PortListener listener) {
        pls.addListener(listener);
    }

    public void removeListener(PortListener listener) {
        pls.removeListener(listener);
    }


   
}
