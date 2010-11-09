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
import net.neilcsmith.praxis.impl.PortListenerSupport;
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
    private boolean multiChannelCapable;
    private PortListenerSupport pls;

    public DefaultAudioInputPort(Component host, Sink sink) {
        this(host, sink, false);
    }

    public DefaultAudioInputPort(Component host, Sink sink,
            boolean multiChannelCapable) {
        if (sink == null) {
            throw new NullPointerException();
        }
        this.sink = sink;
        this.portSink = sink;
        this.multiChannelCapable = multiChannelCapable;
        connections = new ArrayList<AudioPort.Output>();
        pls = new PortListenerSupport(this);
    }

    public void disconnectAll() {
        for (AudioPort.Output connection : getConnections()) {
            disconnect(connection);
        }
    }

    public AudioPort.Output[] getConnections() {
        return connections.toArray(new AudioPort.Output[connections.size()]);
    }

    public void addListener(PortListener listener) {
        pls.addListener(listener);
    }

    public void removeListener(PortListener listener) {
        pls.removeListener(listener);
    }

    @Override
    protected void addAudioOutputPort(Output port, Source source) throws PortConnectionException {
        if (connections.contains(port)) {
            throw new PortConnectionException();
        }
        if (connections.size() == 1) {
            switchToMultichannel();
        }
        try {
            portSink.addSource(source);
            connections.add(port);
            pls.fireListeners();
        } catch (Exception ex) {
            if (connections.size() == 1) {
                switchToSingleChannel();
            }
            throw new PortConnectionException();
        }
    }

    @Override
    protected void removeAudioOutputPort(Output port, Source source) {
        if (connections.remove(port)) {
            portSink.removeSource(source);
            if (connections.size() == 1) {
                switchToSingleChannel();
            }
            pls.fireListeners();
        }
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
            logger.log(Level.WARNING, "Error converting port to multi channel", ex);
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
            logger.log(Level.WARNING, "Error converting port to single channel", ex);
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
