/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Neil C Smith.
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
 *
 */
package net.neilcsmith.praxis.osc.components;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.InstanceLookup;
import net.neilcsmith.praxis.impl.IntProperty;
import net.neilcsmith.praxis.impl.RootState;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 *
 * @author Neil C Smith
 */
public class OSCRoot extends AbstractRoot {

    private final static Logger LOG = Logger.getLogger(OSCRoot.class.getName());
    private final static int DEFAULT_PORT = 1234;
    private final static String DEFAULT_PROTOCOL = OSCServer.UDP;
    private final OSCContext context;
    private final BlockingQueue<OSCMessage> messages;
    private int port = DEFAULT_PORT;
    private Lookup lookup;
    private OSCServer server;
    private OSCMessage lastMessage;

    public OSCRoot() {
        context = new OSCContext();
        messages = new LinkedBlockingQueue<OSCMessage>();
        initControls();
    }

    private void initControls() {
        registerControl("port", IntProperty.builder()
                .minimum(1)
                .maximum(65535)
                .defaultValue(port)
                .binding(new PortBinding())
                .build());
        registerControl("last-message", StringProperty.builder()
                .binding(new LastMessageBinding())
                .build());
    }

    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = InstanceLookup.create(super.getLookup(), context);
        }
        return lookup;
    }

    @Override
    protected void starting() {
        try {
            lastMessage = null;
            server = OSCServer.newUsing(DEFAULT_PROTOCOL, port);
            server.addOSCListener(new OSCListenerImpl());
            server.start();
            setInterrupt(new OSCRunnable());
        } catch (IOException ex) {
            Logger.getLogger(OSCRoot.class.getName()).log(Level.SEVERE, null, ex);
            try {
                setIdle();
            } catch (IllegalRootStateException ex1) {
            }
        }
    }

    @Override
    protected void stopping() {
        terminateServer();
    }

    @Override
    protected void terminating() {
        terminateServer();
    }

    private void terminateServer() {
        if (server == null) {
            return;
        }
        try {
            server.stop();
        } catch (IOException ex) {
            // not bothered?
        }
        server.dispose();
        server = null;
        messages.clear();
    }

    private class PortBinding implements IntProperty.Binding {

        @Override
        public void setBoundValue(long time, int value) {
            if (getState() == RootState.ACTIVE_RUNNING) {
                throw new UnsupportedOperationException("Can't set port while running");
            }
            port = value;
        }

        @Override
        public int getBoundValue() {
            return port;
        }
    }

    private class LastMessageBinding implements StringProperty.ReadBinding {

        @Override
        public String getBoundValue() {
            if (lastMessage != null) {
                StringBuilder sb = new StringBuilder(lastMessage.getName());
                for (int i = 0; i < lastMessage.getArgCount(); i++) {
                    sb.append(" ");
                    sb.append(lastMessage.getArg(i));
                }
                return sb.toString();
            } else {
                return "";
            }
        }

    }

    private class OSCListenerImpl implements OSCListener {

        @Override
        public void messageReceived(OSCMessage oscm, SocketAddress sa, long l) {
            messages.add(oscm);
        }
    }

    private class OSCRunnable implements Runnable {

        @Override
        public void run() {
            while (getState() == RootState.ACTIVE_RUNNING) {
                long time = System.nanoTime();
                try {
                    nextControlFrame(time);
                } catch (IllegalRootStateException ex) {
                }
                OSCMessage msg = null;
                try {
                    msg = messages.poll(50, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                }
                while (msg != null) {
                    LOG.log(Level.FINEST, "Handling message to {0}", msg.getName());
                    lastMessage = msg;
                    context.dispatch(msg, time);
                    msg = messages.poll();
                }
                if (server != null && !server.isActive()) {
                    try {
                        setIdle();
                    } catch (IllegalRootStateException ex) {
                    }
                }
            }
        }
    }
}
