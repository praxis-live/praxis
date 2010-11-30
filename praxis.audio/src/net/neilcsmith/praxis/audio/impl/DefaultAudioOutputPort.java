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
package net.neilcsmith.praxis.audio.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.audio.AudioPort;
import net.neilcsmith.praxis.core.*;
import net.neilcsmith.praxis.core.info.PortInfo;
import net.neilcsmith.praxis.impl.PortListenerSupport;
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
    private List<AudioPort.Input> connections;
    private boolean multiChannelCapable;
    private PortListenerSupport pls;

    public DefaultAudioOutputPort(Component host, Source source) {
        this(host, source, false);
    }

    public DefaultAudioOutputPort(Component host, Source source,
            boolean multiChannelCapable) {
        if (source == null) {
            throw new NullPointerException();
        }
        this.source = source;
        this.portSource = source;
        this.multiChannelCapable = multiChannelCapable;
        connections = new ArrayList<AudioPort.Input>();
        pls = new PortListenerSupport(this);
    }

    public void connect(Port port) throws PortConnectionException {
        if (port instanceof AudioPort.Input) {
            AudioPort.Input aport = (AudioPort.Input) port;
            if (connections.contains(aport)) {
                throw new PortConnectionException();
            }
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
            pls.fireListeners();
        } else {
            throw new PortConnectionException();
        }
    }

    public void disconnect(Port port) {
        if (port instanceof AudioPort.Input) {
            AudioPort.Input aport = (AudioPort.Input) port;
            if (connections.contains(aport)) {
                breakConnection(aport, source);
                connections.remove(aport);
                if (connections.size() == 1) {
                    switchToSingleChannel();
                }
                pls.fireListeners();
            }
        }
    }

    public void disconnectAll() {
        for (AudioPort.Input port : getConnections()) {
            disconnect(port);
        }
    }

    public AudioPort.Input[] getConnections() {
        return connections.toArray(new AudioPort.Input[connections.size()]);
    }

    public void addListener(PortListener listener) {
        pls.addListener(listener);
    }

    public void removeListener(PortListener listener) {
        pls.removeListener(listener);
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
            logger.log(Level.WARNING, "Error converting port to multi channel", ex);
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
            logger.log(Level.WARNING, "Error converting port to single channel", ex);
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
