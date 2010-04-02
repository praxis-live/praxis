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
import net.neilcsmith.ripl.core.Sink;
import net.neilcsmith.ripl.core.Source;

/**
 *
 * @author Neil C Smith
 */
public class DefaultVideoInputPort extends VideoPort.Input {

    private final static Logger logger = Logger.getLogger(DefaultVideoInputPort.class.getName());
    private Sink sink;
    private VideoPort.Output connection;
    private Component component;
    private List<PortConnectionListener> listeners;
    PortInfo info;

    public DefaultVideoInputPort(Component host, Sink sink) {
        if (host == null || sink == null) {
            throw new NullPointerException();
        }
        this.component = host;
        this.sink = sink;
        listeners = new ArrayList<PortConnectionListener>();
        info = PortInfo.create(this.getTypeClass(), this.getDirection(), new PortAddress[0], null);
    }

    public void disconnectAll() {
        if (connection != null) {
            connection.disconnect(this);
        }
    }

    public Port[] getConnections() {
        return connection == null ? new Port[0] : new Port[]{connection};
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
        if (connection == null) {
            info = PortInfo.create(info, new PortAddress[0]);
        } else {
            info = PortInfo.create(info, new PortAddress[]{connection.getAddress()});
        }
        return info;
    }

    public Component getComponent() {
        return component;
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
        fireConnectionListeners();
    }

    @Override
    protected void removeImageOutputPort(VideoPort.Output port, Source source) {
        if (connection == port) {
            connection = null;
            sink.removeSource(source);
        }
        fireConnectionListeners();
    }
    
    private void fireConnectionListeners() {
        for (PortConnectionListener listener : listeners) {
            listener.connectionsChanged(this);
        }
    }
}
