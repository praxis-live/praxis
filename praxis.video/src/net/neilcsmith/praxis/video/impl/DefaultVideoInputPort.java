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
public class DefaultVideoInputPort extends VideoPort.Input {

    private VideoPipe sink;
    private VideoPort.Output connection;
    private PortListenerSupport pls;

    @Deprecated
    public DefaultVideoInputPort(Component host, VideoPipe sink) {
        this(sink);
    }
    
    public DefaultVideoInputPort(VideoPipe sink) {
        if (sink == null) {
            throw new NullPointerException();
        }
        this.sink = sink;
        pls = new PortListenerSupport(this);
    }

    public void disconnectAll() {
        if (connection != null) {
            connection.disconnect(this);
//            pls.fireListeners();
        }
    }

    public Port[] getConnections() {
        return connection == null ? new Port[0] : new Port[]{connection};
    }


    @Override
    protected void addVideoOutputPort(VideoPort.Output port, VideoPipe source) throws PortConnectionException {
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
        pls.fireListeners();
    }

    @Override
    protected void removeVideoOutputPort(VideoPort.Output port, VideoPipe source) {
        if (connection == port) {
            connection = null;
            sink.removeSource(source);
            pls.fireListeners();
        }
    }

    public void addListener(PortListener listener) {
        pls.addListener(listener);
    }

    public void removeListener(PortListener listener) {
        pls.removeListener(listener);
    }
    

}
