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
package net.neilcsmith.praxis.video;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.*;
import net.neilcsmith.praxis.core.info.PortInfo;
import net.neilcsmith.ripl.Source;

/**
 *
 * @author Neil C Smith
 */
public class DefaultVideoOutputPort extends VideoPort.Output {

    private final static Logger logger = Logger.getLogger(DefaultVideoOutputPort.class.getName());
    private Source source;
    private VideoPort.Input connection;
    private Component component;
    private PortInfo info;

    public DefaultVideoOutputPort(Component host, Source source) {
        if (host == null || source == null) {
            throw new NullPointerException();
        }
        this.component = host;
        this.source = source;
        info = PortInfo.create(getTypeClass(), getDirection(), null);
    }

    public void connect(Port port) throws PortConnectionException {
        if (connection != null) {
            throw new PortConnectionException();
        }
        if (port instanceof VideoPort.Input) {
            VideoPort.Input ip = (VideoPort.Input) port;
            makeConnection(ip, source);
            connection = ip;
        } else {
            throw new PortConnectionException();
        }
    }

    public void disconnect(Port port) {
        if (connection != null && connection == port) {
            breakConnection(connection, source);
            connection = null;
        }
    }
    


    public void disconnectAll() {
        if (connection != null) {
            disconnect(connection);
        }
    }

    public Port[] getConnections() {
        return connection == null ? new Port[0] : new Port[]{connection};
    }

    public PortInfo getInfo() {
        return info;
    }


   
}
