/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2018 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.video;

import org.praxislive.core.PortListener;
import org.praxislive.core.Port;
import org.praxislive.core.PortConnectionException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.util.PortListenerSupport;
import org.praxislive.video.pipes.VideoPipe;
import org.praxislive.video.pipes.impl.MultiInOut;
import org.praxislive.video.render.Surface;

/**
 *
 * @author Neil C Smith
 */
public class DefaultVideoOutputPort extends VideoPort.Output {

    private final static Logger LOG = Logger.getLogger(DefaultVideoOutputPort.class.getName());
    private final static int MAX_CONNECTIONS = 8;
    
    private final VideoPipe source;
    private final List<VideoPort.Input> connections;
    private final PortListenerSupport pls;
    
    private VideoPipe portSource;
    private Splitter splitter;
    
    public DefaultVideoOutputPort(VideoPipe source) {
        if (source == null) {
            throw new NullPointerException();
        }
        this.source = source;
        this.portSource = source;
        connections = new ArrayList<Input>(MAX_CONNECTIONS);
        pls = new PortListenerSupport(this);
    }

    public void connect(Port port) throws PortConnectionException {
        if (port instanceof VideoPort.Input) {
            VideoPort.Input input = (VideoPort.Input) port;
            if (connections.contains(input)) {
                throw new PortConnectionException();
            }
            if (connections.size() == 1) {
                switchToMultichannel();
            }
            try {
                makeConnection(input, portSource);
                connections.add(input);
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
        if (port instanceof VideoPort.Input) {
            VideoPort.Input input = (VideoPort.Input) port;
            if (connections.contains(input)) {
                breakConnection(input, portSource);
                connections.remove(input);
                if (connections.size() == 1) {
                    switchToSingleChannel();
                }
                pls.fireListeners();
            }
        }
    }

    public void disconnectAll() {
        for (VideoPort.Input port : getConnections()) {
            disconnect(port);
        }
    }

    public VideoPort.Input[] getConnections() {
        return connections.toArray(new VideoPort.Input[connections.size()]);
    }

    public void addListener(PortListener listener) {
        pls.addListener(listener);
    }

    public void removeListener(PortListener listener) {
        pls.removeListener(listener);
    }

    private void switchToMultichannel() {
        if (portSource == splitter) {
            return;
        }
        LOG.fine("VideoOutput switching to multichannel");
        VideoPipe[] sinks = removeSinks(source);
        try {
            if (splitter == null) {
                splitter = new Splitter(MAX_CONNECTIONS);
            }
            splitter.addSource(source);
            for (VideoPipe sink : sinks) {
                sink.addSource(splitter);
            }
            portSource = splitter;
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error converting port to multi channel", ex);
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
        LOG.fine("VideoOutput switching to single channel");
        VideoPipe[] sinks = removeSinks(splitter);
        try {
            splitter.removeSource(source);
            for (VideoPipe sink : sinks) {
                sink.addSource(source);
            }
            portSource = source;
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Error converting port to single channel", ex);
            removeSinks(source);
            removeSinks(splitter);
            portSource = source;
            connections.clear();
        }

    }

    private VideoPipe[] removeSinks(VideoPipe source) {
        VideoPipe[] sinks = new VideoPipe[source.getSinkCount()];
        for (int i = 0; i < sinks.length; i++) {
            sinks[i] = source.getSink(i);
        }
        for (VideoPipe sink : sinks) {
            sink.removeSource(source);
        }
        return sinks;
    }

    private static class Splitter extends MultiInOut {

        public Splitter(int maxOutputs) {
            super(1, maxOutputs);
        }

        @Override
        protected void process(Surface[] inputs, Surface output, int index, boolean rendering) {
            if (!rendering) {
                return;
            }
            if (inputs.length == 1) {
                Surface input = inputs[0];
                assert input != output;
                output.copy(input);
            } else {
                output.clear();
            }
        }

    }

}
