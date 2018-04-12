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
 *
 */
package org.praxislive.tinkerforge;

import com.tinkerforge.Device;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.code.CodeComponent;
import org.praxislive.code.CodeContext;
import org.praxislive.core.ExecutionContext;
import org.praxislive.core.types.PBoolean;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class TFCodeContext extends CodeContext<TFCodeDelegate> {

    static final String AUTO = "<auto>";

    private final Class<? extends Device> deviceClass;
    private final Field deviceField;
    private final Listener listener;

    private ExecutionContext execCtxt;
    private TFContext tinkerforge;
    private Device device;
    private String uid = "";

    public TFCodeContext(TFCodeConnector connector) {
        super(connector);
        deviceClass = connector.extractDeviceType();
        deviceField = connector.extractDeviceField();
        listener = new Listener();
    }

    @Override
    protected void configure(CodeComponent<TFCodeDelegate> cmp,
            CodeContext<TFCodeDelegate> oldCtxt) {
        // at this point oldCtxt not disposed, so try and steal device to stop
        // dispose() being called?
        super.configure(cmp, oldCtxt);
        if (oldCtxt instanceof TFCodeContext) {
            TFCodeContext old = (TFCodeContext) oldCtxt;
            // tinkerforge not set yet as hierarchyChanged not called
            TFContext tf = getLookup().find(TFContext.class).orElse(null);
            this.uid = old.uid; // copy over UID
            if (this.deviceClass == old.deviceClass &&
                    tf != null &&
                    old.device != null) {
                tf.releaseDevice(old.device);
                old.device = null;
            }
        }
        
    }

    @Override
    protected void hierarchyChanged() {
        super.hierarchyChanged();
        //Exec Ctxt
        ExecutionContext ctxt = getLookup().find(ExecutionContext.class).orElse(null);
        if (execCtxt != ctxt) {
            if (execCtxt != null) {
                execCtxt.removeStateListener(listener);
                execCtxt.removeClockListener(listener);
            }
            execCtxt = ctxt;
            if (ctxt != null) {
                ctxt.addStateListener(listener);
                ctxt.addClockListener(listener);
                listener.stateChanged(ctxt);
            }
        }
        //TinkerForge Ctxt
        TFContext tf = getLookup().find(TFContext.class).orElse(null);
        if (tinkerforge != tf) {
            if (tinkerforge != null) {
                tinkerforge.removeListener(listener);
                releaseDevice();
            }
            tinkerforge = tf;
            if (tinkerforge != null) {
                tinkerforge.addListener(listener);
                refresh();
            }

        }
    }

    void setUID(String uid) {
        this.uid = uid;
        refresh();
    }

    String getUID() {
        return uid;
    }

    PBoolean isConnected() {
        return device == null ? PBoolean.FALSE : PBoolean.TRUE;
    }

    private void releaseDevice() {
        if (tinkerforge == null || device == null) {
            return;
        }
        try {
            getDelegate().dispose();
            deviceField.set(getDelegate(), null);
        } catch (Exception e) {
        }
        tinkerforge.releaseDevice(device);
        device = null;
    }

    private void refresh() {
        if (tinkerforge == null) {
            return;
        }
        Device d = findDevice();
        if (device != d) {
            if (device != null) {
                releaseDevice();
                device = null;
            }
            if (d != null) {
                try {
                    tinkerforge.lockDevice(d);
                    deviceField.set(getDelegate(), d);
                    listener.setupRequired();
                    device = d;
                } catch (Exception ex) {
                    Logger.getLogger(TFCodeContext.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }
    
    private Device findDevice() {
        if (AUTO.equals(uid)) {
            List<Device> devices = tinkerforge.findDevices(deviceClass);
            for (Device dev : devices) {
                if (dev == device || !tinkerforge.isLocked(dev)) {
                    return dev;
                }
            }
        } else {
            Device dev = tinkerforge.findDevice(uid.toString());
            if (deviceClass.isInstance(dev)) {
                return dev;
            } 
        }
        return null;
    }

    private class Listener implements ExecutionContext.StateListener,
            ExecutionContext.ClockListener,
            TFContext.Listener {

        private boolean doSetup;

        @Override
        public void stateChanged(ExecutionContext source) {
            setupRequired();
        }

        @Override
        public void tick(ExecutionContext source) {
            if (device == null) {
                return;
            }
            update(source.getTime());
            if (doSetup) {
                try {
                    getDelegate().setup();
                } catch (Exception e) {
                }
                doSetup = false;
            }
            try {
                getDelegate().update();
            } catch (Exception e) {
            }
        }

        @Override
        public void stateChanged(TFContext context) {
            refresh();
        }

        private void setupRequired() {
            doSetup = true;
        }

    }

}
