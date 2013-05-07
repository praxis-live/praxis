/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2013 Neil C Smith.
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
package net.neilcsmith.praxis.tinkerforge.components;

import com.tinkerforge.Device;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.InstanceLookup;
import net.neilcsmith.praxis.impl.IntProperty;
import net.neilcsmith.praxis.impl.RootState;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 *
 * @author Neil C Smith
 */
public class TFRoot extends AbstractRoot {

    private final static String DEFAULT_HOST = "localhost";
    private final static int DEFAULT_PORT = 4223;
    private final static Logger LOG = Logger.getLogger(TFRoot.class.getName());
    private String host = DEFAULT_HOST;
    private int port = DEFAULT_PORT;
    private Lookup lookup;
    private volatile IPConnection ipcon;
    private TFContext context;
    private BlockingQueue<Runnable> queue;
    private ComponentEnumerator enumerator;
    private Status status;

    public TFRoot() {
        context = new TFContext();
        queue = new LinkedBlockingQueue<Runnable>();
        enumerator = new ComponentEnumerator();
        status = new Status();
        initControls();
    }

    private void initControls() {
        registerControl("host", StringProperty.create(new HostBinding(), host));
        registerControl("port", IntProperty.create(new PortBinding(), 1, 65535, port));
        registerControl("status", ArgumentProperty.createReadOnly(PString.info(), status));
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
            ipcon = new IPConnection();
            ipcon.connect(host, port);
            ipcon.addEnumerateListener(enumerator);
            ipcon.enumerate();
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Can't start connection.", ex);
        }
    }

    @Override
    protected void stopping() {
        status.clear();
        context.removeAll();
        if (ipcon != null) {
            try {
                ipcon.disconnect();
            } catch (NotConnectedException ex) {
                Logger.getLogger(TFRoot.class.getName()).log(Level.SEVERE, null, ex);
            }
            ipcon = null;
        }
    }

    @Override
    protected void processingControlFrame() {
        Runnable task = queue.poll();
        while (task != null) {
            task.run();
            task = queue.poll();
        }
    }

    private class HostBinding implements StringProperty.Binding {

        @Override
        public void setBoundValue(long time, String value) {
            if (getState() == RootState.ACTIVE_RUNNING) {
                throw new UnsupportedOperationException("Can't set IP address while running");
            }
            host = value;
        }

        @Override
        public String getBoundValue() {
            return host;
        }
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

    private class Status implements ArgumentProperty.ReadBinding {

//        private Set<String> statusSet = new LinkedHashSet<String>();
        private final Map<String, String> infoMap = new LinkedHashMap<String, String>();
        private PString cache;

        @Override
        public Argument getBoundValue() {
            if (cache == null) {
                StringBuilder builder = new StringBuilder();
                for (String line : infoMap.values()) {
                    builder.append(line);
                    builder.append("\n");
                }
                cache = PString.valueOf(builder);
            }
            return cache;
        }

        private void add(String uid, String info) {
            infoMap.put(uid, info);
            cache = null;
        }

        private void remove(String uid) {
            infoMap.remove(uid);
            cache = null;
        }

        private void clear() {
            infoMap.clear();
            cache = null;
        }
    }

    private class ComponentEnumerator implements IPConnection.EnumerateListener {

        @Override
        public void enumerate(final String uid,
                String connectedUid,
                char position,
                short[] hardwareVersion,
                short[] firmwareVersion,
                int deviceID,
                short enumerationType) {
            IPConnection ip = ipcon;
            if (ip == null) {
                LOG.fine("enumerate() called but IPConnection is null - ignoring!");
                return; // removed from under us?
            }
            if (enumerationType != IPConnection.ENUMERATION_TYPE_DISCONNECTED) {
                LOG.log(Level.FINE, "Component connected - UID:{0} Name:{1}", new Object[]{uid, deviceID});
                try {
                    final Device device = TFDeviceFactory.getDefault().createDevice(deviceID ,uid, ipcon);
                    final String name = TFDeviceFactory.getDefault().getDeviceName(deviceID);
                    queue.add(new Runnable() {
                        @Override
                        public void run() {
                            if (getState() == RootState.ACTIVE_RUNNING) {
                                context.addDevice(uid, device);
                                status.add(uid, name + " UID:" + uid);
                            }
                        }
                    });
                } catch (Exception ex) {
                    LOG.log(Level.FINE, "Unable to create device for UID: {0} Name: {1}", new Object[]{uid, deviceID});
                    LOG.log(Level.FINE, "", ex);
                }
            } else {
                LOG.log(Level.FINE, "Component disconnected - UID:{0} Name:{1}", new Object[]{uid, deviceID});
                queue.add(new Runnable() {
                    @Override
                    public void run() {
                        if (getState() == RootState.ACTIVE_RUNNING) {
                            context.removeDevice(uid);
                            status.remove(uid);
                        }
                    }
                });
            }
        }
    }
}
