/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 Neil C Smith.
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.neilcsmith.praxis.util.ArrayUtils;

/**
 *
 * @author Neil C Smith
 */
class TFContext {

    private Map<String, Device> devices;
    private Set<Device> locked;
    private Listener[] listeners;
    
    TFContext() {
        devices = new LinkedHashMap<String, Device>();
        locked = new HashSet<Device>();
        listeners = new Listener[0];
    }
    
    void addDevice(String uid, Device device) {
        if (devices.containsKey(uid)) {
            throw new IllegalStateException("Context already has device for UID: " + uid);
        }
        devices.put(uid, device);
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
    
    public Device findDevice(String uid) {
        return devices.get(uid);
    }

    public void lockDevice(Device device) throws DeviceLockedException {
        if (!locked.add(device)) {
            throw new DeviceLockedException();
        }
    }

    public void releaseDevice(Device device) {
        locked.remove(device);
    }

    public void addListener(Listener listener) {
        listeners = ArrayUtils.add(listeners, listener);
    }

    public void removeListener(Listener listener) {
        listeners = ArrayUtils.remove(listeners, listener);
    }

    static interface Listener {

        void stateChanged(TFContext context);
    }

    static class DeviceLockedException extends Exception {
    }
}
