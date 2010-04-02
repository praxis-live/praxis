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
package net.neilcsmith.praxis.audio;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.*;
import net.neilcsmith.praxis.core.info.PortInfo;
import net.neilcsmith.rapl.components.Splitter;
import net.neilcsmith.rapl.core.Sink;
import net.neilcsmith.rapl.core.Source;


/**
 *
 * @author Neil C Smith
 */
public class DefaultAudioOutputPort extends AudioPort.Output {

    private final static Logger logger = Logger.getLogger(DefaultAudioOutputPort.class.getName());
    private Source source;
    private Source portSource;
    private Splitter splitter;
    private AudioPort.Input connection;
    private List<AudioPort.Input> connections;
    private Component component;
    private boolean multiChannelCapable;
    private List<PortConnectionListener> listeners;
    private PortInfo info;

    public DefaultAudioOutputPort(Component host, Source source) {
        this(host, source, false);
    }
    
    public DefaultAudioOutputPort(Component host, Source source,
            boolean multiChannelCapable) {
        if (host == null || source == null) {
            throw new NullPointerException();
        }
        this.component = host;
        this.source = source;
        this.portSource = source;
        this.multiChannelCapable = multiChannelCapable;
        listeners = new ArrayList<PortConnectionListener>();
        connections = new ArrayList<AudioPort.Input>();
        info = PortInfo.create(getTypeClass(), getDirection(), new PortAddress[0], null);
    }

    public void connect(Port port) throws PortConnectionException {
//        if (connection != null) {
//            throw new PortConnectionException();
//        }
//        if (port instanceof AudioPort.Input) {
//            AudioPort.Input ip = (AudioPort.Input) port;
//            makeConnection(ip, source);
//        } else {
//            throw new PortConnectionException();
//        }
//        fireConnectionListeners();
        if (connections.contains(port)) {
            throw new PortConnectionException();
        }
        if (port instanceof AudioPort.Input) {
            if (connections.size() == 1) {
                switchToMultichannel();
            }
            AudioPort.Input ip = (AudioPort.Input) port;
            try {
                makeConnection(ip, portSource);
                connections.add(ip);
            } catch (PortConnectionException ex) {
                if (connections.size() == 1) {
                    switchToSingleChannel();
                }
                throw ex;
            }
        } else {
            throw new PortConnectionException();
        }
        fireConnectionListeners();
    }

    public void disconnect(Port port) {
        if (connections.contains(port)) {
            breakConnection(connection, source);
            connections.remove(port);
            if (connections.size() == 1) {
                switchToSingleChannel();
            }
        }
        fireConnectionListeners();
    }
    
    private void fireConnectionListeners() {
        for (PortConnectionListener listener : listeners) {
            listener.connectionsChanged(this);
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
            info = PortInfo.create(info, new PortAddress[] {connection.getAddress()});
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
    
    private void switchToMultichannel() {
        if (multiChannelCapable || portSource == splitter) {
            return;
        }    
        Sink[] sinks = removeSinks(portSource);      
        try {
            if (splitter == null) {
                splitter = new Splitter(16); // @TODO make channels configurable
            }         
            splitter.addSource(source);
            for (Sink sink : sinks) {
                sink.addSource(splitter);
            }
            portSource = splitter;
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error converting port "
                    + getAddress() + " to multi channel", ex);
            removeSinks(splitter);
            removeSinks(source);
            portSource = source;
            connections.clear();
        }
    }
    
    private void switchToSingleChannel() {
        if (portSource == source) {
            return;
        }
        Sink[] sinks = removeSinks(splitter);
        try {
            for (Sink sink : sinks) {
                sink.addSource(source);
            }
            portSource = source;
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error converting port "
                    + getAddress() + " to single channel", ex);
            removeSinks(source);
            removeSinks(splitter);
            portSource = source;
            connections.clear();
        }
        
    }
    
    private Sink[] removeSinks(Source source) {
        Sink[] sinks = source.getSinks();
        for (Sink sink : sinks) {
            sink.removeSource(source);
        }
        return sinks;
    }
}
