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
import net.neilcsmith.ripl.Sink;
import net.neilcsmith.ripl.Source;

/**
 *
 * @author Neil C Smith
 */
public class DefaultVideoInputPort extends VideoPort.Input {

    private final static Logger logger = Logger.getLogger(DefaultVideoInputPort.class.getName());
    private Sink sink;
    private VideoPort.Output connection;
    private Component component;
    PortInfo info;

    public DefaultVideoInputPort(Component host, Sink sink) {
        if (host == null || sink == null) {
            throw new NullPointerException();
        }
        this.component = host;
        this.sink = sink;
        info = PortInfo.create(this.getTypeClass(), this.getDirection(), null);
    }

    public void disconnectAll() {
        if (connection != null) {
            connection.disconnect(this);
        }
    }

    public Port[] getConnections() {
        return connection == null ? new Port[0] : new Port[]{connection};
    }

    public PortInfo getInfo() {
        return info;
    }



    @Override
    protected void addImageOutputPort(VideoPort.Output port, Source source) throws PortConnectionException {
        if (connection != null) {
            throw new PortConnectionException();
        }
        connection = port;
        try {
            sink.addSource(source);
        } catch (Exception ex) {
            connection = null;
            throw new PortConnectionException(); // wrap!
        }
    }

    @Override
    protected void removeImageOutputPort(VideoPort.Output port, Source source) {
        if (connection == port) {
            connection = null;
            sink.removeSource(source);
        }
    }
    

}
