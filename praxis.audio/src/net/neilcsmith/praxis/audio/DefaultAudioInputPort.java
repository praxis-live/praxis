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

import net.neilcsmith.praxis.audio.AudioPort.Output;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.*;
import net.neilcsmith.praxis.core.info.PortInfo;
import net.neilcsmith.rapl.components.Mixer;
import net.neilcsmith.rapl.core.Sink;
import net.neilcsmith.rapl.core.Source;

/**
 *
 * @author Neil C Smith
 */
public class DefaultAudioInputPort extends AudioPort.Input {

    private final static Logger logger = Logger.getLogger(DefaultAudioInputPort.class.getName());
    private Sink sink;
    private Sink portSink;
    private Mixer mixer;
    private List<AudioPort.Output> connections;
    private Component component;
    private List<PortConnectionListener> listeners;
    private boolean multiChannelCapable;
    private PortInfo info;

    public DefaultAudioInputPort(Component host, Sink sink) {
        this(host, sink, false);
    }

    public DefaultAudioInputPort(Component host, Sink sink,
            boolean multiChannelCapable) {
        if (host == null || sink == null) {
            throw new NullPointerException();
        }
        this.component = host;
        this.sink = sink;
        this.portSink = sink;
        this.multiChannelCapable = multiChannelCapable;
        listeners = new ArrayList<PortConnectionListener>();
        connections = new ArrayList<AudioPort.Output>();
        info = PortInfo.create(this.getTypeClass(), this.getDirection(), new PortAddress[0], null);
    }

    public void disconnectAll() {
        for (AudioPort.Output connection : connections) {
            disconnect(connection);
        }
    }

    public Port[] getConnections() {
        return connections.toArray(new Port[connections.size()]);
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
//        if (connection == null) {
//            info = PortInfo.create(info, new PortAddress[0]);
//        } else {
//            info = PortInfo.create(info, new PortAddress[]{connection.getAddress()});
//        }
//        return info;
        if (connections.isEmpty()) {
            info = PortInfo.create(info, new PortAddress[0]);
        } else {
            int size = connections.size();
            PortAddress[] addresses = new PortAddress[size];
            for (int i = 0; i < size; i++) {
                addresses[i] = connections.get(i).getAddress();
            }
            info = PortInfo.create(info, addresses);
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
//
//    @Override
//    protected void addImageOutputPort(AudioPort.Output port, Source source) throws PortConnectionException {
//        if (connection != null) {
//            throw new PortConnectionException();
//        }
//        connection = port;
//        try {
//            sink.addSource(source);
//        } catch (Exception ex) {
//            connection = null;
//            throw new PortConnectionException(); // wrap!
//        }
//        fireConnectionListeners();
//    }

//    @Override
//    protected void removeImageOutputPort(AudioPort.Output port, Source source) {
//        if (connection == port) {
//            connection = null;
//            sink.removeSource(source);
//        }
//        fireConnectionListeners();
//    }
    private void fireConnectionListeners() {
        for (PortConnectionListener listener : listeners) {
            listener.connectionsChanged(this);
        }
    }

    @Override
    protected void addAudioOutputPort(Output port, Source source) throws PortConnectionException {
        if (connections.contains(source)) {
            throw new PortConnectionException();
        }
        if (connections.size() == 1) {
            switchToMultichannel();
        }
        try {
            portSink.addSource(source);
            connections.add(port);
        } catch (Exception ex) {
            if (connections.size() == 1) {
                switchToSingleChannel();
            }
        }
        fireConnectionListeners();
    }

    @Override
    protected void removeAudioOutputPort(Output port, net.neilcsmith.rapl.core.Source source) {
        if (connections.remove(port)) {
            portSink.removeSource(source);
            if (connections.size() == 1) {
                switchToSingleChannel();
            }
        }
        fireConnectionListeners();
    }

    private void switchToMultichannel() {
        if (multiChannelCapable || portSink == mixer) {
            return;
        }    
        Source[] sources = removeSources(portSink);      
        try {
            if (mixer == null) {
                mixer = new Mixer(16); // @TODO make channels configurable
            }         
            sink.addSource(mixer);
            for (Source source : sources) {
                mixer.addSource(source);
            }
            portSink = mixer;
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error converting port "
                    + getAddress() + " to multi channel", ex);
            removeSources(mixer);
            removeSources(sink);
            connections.clear();
        }
    }

    private void switchToSingleChannel() {
        if (portSink == sink) {
            return;
        }
        Source[] sources = removeSources(mixer);
        try {
            for (Source source : sources) {
                sink.addSource(source);
            }
            portSink = sink;
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error converting port "
                    + getAddress() + " to single channel", ex);
            removeSources(sink);
            removeSources(mixer);
            connections.clear();
        }
        
    }
    
    private Source[] removeSources(Sink sink) {
        Source[] sources = sink.getSources();
        for (Source source : sources) {
            sink.removeSource(source);
        }
        return sources;
    }
 }
