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
 */
package org.praxislive.hub.net;

import de.sciss.net.OSCClient;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCPacket;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.Control;
import org.praxislive.core.ExecutionContext;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.services.RootManagerService;
import org.praxislive.core.services.Service;
import org.praxislive.core.services.ServiceUnavailableException;
import org.praxislive.core.types.PMap;
import org.praxislive.impl.AbstractRoot;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class MasterClientRoot extends AbstractRoot {

    private final static Logger LOG = Logger.getLogger(MasterClientRoot.class.getName());
    private final static String HLO = "/HLO";
    private final static String BYE = "/BYE";

    private final PraxisPacketCodec codec;
    private final Dispatcher dispatcher;
    private final SlaveInfo slaveInfo;
    private final FileServer.Info fileServerInfo;

    private OSCClient client;
    private long lastPurgeTime;
    private Watchdog watchdog;
    
    MasterClientRoot(SlaveInfo slaveInfo, FileServer.Info fileServerInfo) {
        super(EnumSet.noneOf(Caps.class));
        this.slaveInfo = slaveInfo;
        this.fileServerInfo = fileServerInfo;
        codec = new PraxisPacketCodec();
        dispatcher = new Dispatcher(codec);
        registerControl(RootManagerService.ADD_ROOT, new RootControl(true));
        registerControl(RootManagerService.REMOVE_ROOT, new RootControl(false));
    }

    @Override
    protected void activating() {
        super.activating();
        getLookup().get(ExecutionContext.class).addClockListener(new ExecutionContext.ClockListener() {

            @Override
            public void tick(ExecutionContext source) {
                MasterClientRoot.this.tick(source);
            }
        });
        dispatcher.remoteSysPrefix = getAddress().toString() + "/_remote";
    }

    @Override
    protected void terminating() {
        super.terminating();
        if (client != null) {
            LOG.fine("Terminating - sending /BYE");
            try {
                client.send(new OSCMessage(BYE));
            } catch (IOException ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }
        clientDispose();
    }

    @Override
    protected void processCall(Call call) {
        if (call.getToAddress().getComponentAddress().getDepth() == 1 &&
                getAddress().getRootID().equals(call.getRootID())) {
            super.processCall(call);
        } else if (client != null) {
            dispatcher.handleCall(call);
        } else {
            connect();
            if (client != null) {
                dispatcher.handleCall(call);
            } else {
                getPacketRouter().route(Call.createErrorCall(call,
                        CallArguments.EMPTY));
            }
        }
    }

    private void tick(ExecutionContext source) {
        if ((source.getTime() - lastPurgeTime) > TimeUnit.SECONDS.toNanos(1)) {
//            LOG.fine("Triggering dispatcher purge");
            dispatcher.purge(10, TimeUnit.SECONDS);
            lastPurgeTime = source.getTime();
        }
        if (watchdog != null) {
            watchdog.tick();
        }
    }

    private void messageReceived(OSCMessage msg, SocketAddress sender, long timeTag) {
        dispatcher.handleMessage(msg, timeTag);
    }

    private void send(OSCPacket packet) {
        if (client != null) {
            try {
                client.send(packet);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "", ex);
                clientDispose();
            }
        }
    }

    private void connect() {
        LOG.fine("Connecting to slave");
        try {
            // connect to slave
            client = OSCClient.newUsing(codec, OSCClient.TCP);
            client.setBufferSize(65536);
            client.setTarget(slaveInfo.getAddress());
            watchdog = new Watchdog(client);
            watchdog.start();
//            client.connect();
//            LOG.fine("Connected - sending /HLO");

            // HLO request
            CountDownLatch hloLatch = new CountDownLatch(1);
            client.addOSCListener(new Receiver(hloLatch));
            client.start();
            client.send(new OSCMessage(HLO, new Object[]{buildHLOParams().toString()}));
            if (hloLatch.await(10, TimeUnit.SECONDS)) {
                LOG.fine("/HLO received OK");
            } else {
                LOG.severe("Unable to connect to slave");
                clientDispose();
            }

        } catch (IOException | InterruptedException ex) {
            LOG.log(Level.SEVERE, "Unable to connect to slave", ex);
            clientDispose();
        }
    }

    private PMap buildHLOParams() {
        PMap.Builder params = PMap.builder();
        if (!slaveInfo.isLocal() && slaveInfo.getUseLocalResources()) {
            params.put(Utils.KEY_MASTER_USER_DIRECTORY, Utils.getUserDirectory().toURI().toString());
        }
        List<Class<? extends Service>> remoteServices = slaveInfo.getRemoteServices();
        if (!remoteServices.isEmpty()) {
            PMap.Builder srvs = PMap.builder(remoteServices.size());
            for (Class<? extends Service> service : remoteServices) {
                try {
                    srvs.put(service.getName(), findService(service));
                } catch (ServiceUnavailableException ex) {
                    Logger.getLogger(MasterClientRoot.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            params.put(Utils.KEY_REMOTE_SERVICES, srvs.build());
        }
        if (!slaveInfo.isLocal() && slaveInfo.getUseRemoteResources() && fileServerInfo != null) {
            params.put(Utils.KEY_FILE_SERVER_PORT, fileServerInfo.getPort());
        }
        return params.build();
    }

    private void clientDispose() {
        if (client != null) {
            client.dispose();
            client = null;
        }
        if (watchdog != null) {
            watchdog.shutdown();
            watchdog = null;
        }
        dispatcher.purge(0, TimeUnit.NANOSECONDS);
    }

    private class Dispatcher extends OSCDispatcher {
        
        private String remoteSysPrefix;

        private Dispatcher(PraxisPacketCodec codec) {
            super(codec);
        }

        @Override
        void send(OSCPacket packet) {
            MasterClientRoot.this.send(packet);
        }

        @Override
        void send(Call call) {
            getPacketRouter().route(call);
        }

        @Override
        String getRemoteSysPrefix() {
            assert remoteSysPrefix != null;
            return remoteSysPrefix;
        }
        
        

    }

    private class Watchdog extends Thread {

        private final OSCClient client;
        private volatile long lastTickTime;
        private volatile boolean active;

        private Watchdog(OSCClient client) {
            this.client = client;
            lastTickTime = System.nanoTime();
            setDaemon(true);
        }

        @Override
        public void run() {
            while (active) {
                if ((System.nanoTime() - lastTickTime) > TimeUnit.SECONDS.toNanos(10)) {
                    client.dispose();
                    active = false;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    // not a problem
                }
            }
        }

        private void tick() {
            lastTickTime = System.nanoTime();
        }

        private void shutdown() {
            active = false;
            interrupt();
        }

    }

    private class Receiver implements OSCListener {

        private CountDownLatch hloLatch;

        private Receiver(CountDownLatch hloLatch) {
            this.hloLatch = hloLatch;
        }

        @Override
        public void messageReceived(final OSCMessage msg, final SocketAddress sender,
                final long timeTag) {
            if (hloLatch != null && HLO.equals(msg.getName())) {
                hloLatch.countDown();
                hloLatch = null;
            }
            invokeLater(new Runnable() {

                @Override
                public void run() {
                    MasterClientRoot.this.messageReceived(msg, sender, timeTag);
                }
            });
        }

    }

    private class RootControl implements Control {

        private final boolean add;

        private RootControl(boolean add) {
            this.add = add;
        }

        @Override
        public void call(Call call, PacketRouter router) throws Exception {
            if (call.getType() == Call.Type.INVOKE
                    || call.getType() == Call.Type.INVOKE_QUIET) {

                if (client != null) {
                    dispatch(call);
                } else {
                    connect();
                    if (client != null) {
                        dispatch(call);
                    } else {
                        router.route(Call.createErrorCall(call,
                                CallArguments.EMPTY));
                    }
                }
            } else {
                // 
            }
        }

        private void dispatch(Call call) {
            if (add) {
                dispatcher.handleAddRoot(call);
            } else {
                dispatcher.handleRemoveRoot(call);
            }
        }

        @Override
        public ControlInfo getInfo() {
            return add ? RootManagerService.ADD_ROOT_INFO : RootManagerService.REMOVE_ROOT_INFO;
        }

    }

}
