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
package org.praxislive.tinkerforge;

import com.tinkerforge.Device;
import com.tinkerforge.IPConnection;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.util.ArrayUtils;

/**
 *
 * @author Neil C Smith
 */
public class TFContext {

    private final Map<String, Device> devices;
    private final Set<Device> locked;
    private final TFRoot root;
    private Listener[] listeners;

    TFContext(TFRoot root) {
        devices = new LinkedHashMap<>();
        locked = new HashSet<>();
        this.root = root;
        listeners = new Listener[0];
    }

    void addDevice(String uid, Class<? extends Device> type) throws Exception {
//        if (devices.containsKey(uid)) {
//            throw new IllegalStateException("Context already has device for UID: " + uid);
//        }
        Device dev = createDevice(type, uid, root.getIPConnection());
        devices.put(uid, dev);
        fireListeners();
    }

    void removeDevice(String uid) {
        Device d = devices.remove(uid);
        if (d != null) {
            locked.remove(d);
        }
        fireListeners();
    }

    void removeAll() {
        devices.clear();
        fireListeners();
        locked.clear();
    }

    private void fireListeners() {
        for (Listener listener : listeners) {
            listener.stateChanged(this);
        }
    }

//    public <T extends Device> T acquireDevice(Class<T> type) {
//        for (Map.Entry<String, Device> entry : devices.entrySet()) {
//            if (type.isInstance(entry.getValue()) &&
//                    !locked.contains(entry.getKey())) {
//                locked.add(entry.getKey());
//                return type.cast(entry.getValue());
//            }
//        }
//        return null;
//    }
//    
//    public <T extends Device> T acquireDevice(Class<T> type, String uid) {
//        Device dev = devices.get(uid);
//        if (type.isInstance(dev) && !locked.contains(uid)) {
//            locked.add(uid);
//            return type.cast(dev);
//        }
//        return null;
//    }
//    
//    public void releaseDevice(Device device) {
//        for (Map.Entry<String, Device> entry : devices.entrySet()) {
//            if (entry.getValue().equals(device)) {
//                boolean changed = locked.remove(entry.getKey());
//                assert changed;
//                try {
//                    entry.setValue(createDevice(device.getClass(), entry.getKey(), root.getIPConnection()));
//                } catch (Exception ex) {
//                    Logger.getLogger(TFContext.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
//    }
    private Device createDevice(Class<? extends Device> cls, String uid, IPConnection ipcon) throws Exception {
        Constructor<? extends Device> con = cls.getConstructor(String.class, IPConnection.class);
        return con.newInstance(uid, ipcon);
    }

    public Device findDevice(String uid) {
        return devices.get(uid);
    }

    public List<Device> findDevices(Class<? extends Device> type) {
        List<Device> list = new ArrayList<>();
        for (Device device : devices.values()) {
            if (type.isInstance(device)) {
                list.add(device);
            }
        }
        return list;
    }

    public void lockDevice(Device device) throws DeviceLockedException {
        if (!locked.add(device)) {
            throw new DeviceLockedException();
        }
    }

    public void releaseDevice(Device device) {
        boolean changed = locked.remove(device);
//        assert changed;
        if (changed) {
            for (Map.Entry<String, Device> entry : devices.entrySet()) {
                if (entry.getValue() == device) {
                    try {
                        entry.setValue(createDevice(device.getClass(), entry.getKey(), root.getIPConnection()));
                    } catch (Exception ex) {
                        Logger.getLogger(TFContext.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

    }

    public boolean isLocked(Device device) {
        return locked.contains(device);
    }

    public void addListener(Listener listener) {
        listeners = ArrayUtils.add(listeners, listener);
    }

    public void removeListener(Listener listener) {
        listeners = ArrayUtils.remove(listeners, listener);
    }

    // @TODO change to deviceAdded, deviceRemoved, deviceReset?
    public static interface Listener {

        void stateChanged(TFContext context);
    }

    public static class DeviceLockedException extends Exception {
    }

}
