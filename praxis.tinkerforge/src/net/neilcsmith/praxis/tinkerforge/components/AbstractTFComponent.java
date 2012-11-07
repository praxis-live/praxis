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
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.types.PBoolean;
import net.neilcsmith.praxis.impl.AbstractClockComponent;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.StringProperty;
import net.neilcsmith.praxis.tinkerforge.components.TFContext.DeviceLockedException;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractTFComponent<T extends Device> extends AbstractClockComponent {

    private T device;
    private Class<T> cls;
    private String uid;
    private TFContext context;
    private TFContext.Listener listener;

    protected AbstractTFComponent(Class<T> cls) {
        this.cls = cls;
        listener = new ContextListener();
        uid = "";
        initControls();
    }

    private void initControls() {
        registerControl("uid", StringProperty.create(new UIDBinding(), uid));
        registerControl("active", ArgumentProperty.createReadOnly(PBoolean.info(), new ActiveBinding()));
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        TFContext ctxt = getLookup().get(TFContext.class);
        if (ctxt != context) {
            if (context != null) {
                context.removeListener(listener);
                releaseDevice();
                context = null;
            }
            if (ctxt == null) {
                return;
            }
            context = ctxt;
            context.addListener(listener);
            refresh();
        }
    }

    private void releaseDevice() {
        if (context == null || device == null) {
            return;
        }
        disposeDevice(device);
        context.releaseDevice(device);
        device = null;
    }

    private void refresh() {
        if (context == null) {
            return;
        }
        Device d = context.findDevice(uid);
        if (device != d) {
            if (device != null) {
                disposeDevice(device);
                context.releaseDevice(device);
                device = null;
            }
            if (d != null && cls.isInstance(d)) {
                try {
                    context.lockDevice(d);
                    device = cls.cast(d);
                    initDevice(device);
                } catch (DeviceLockedException ex) {
                    Logger.getLogger(AbstractTFComponent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    protected abstract void initDevice(T device);

    protected abstract void disposeDevice(T device);

    protected T getDevice() {
        return device;
    }

    private class ContextListener implements TFContext.Listener {

        @Override
        public void stateChanged(TFContext context) {
            refresh();
        }
    }

    private class UIDBinding implements StringProperty.Binding {

        @Override
        public void setBoundValue(long time, String value) {
            uid = value;
            refresh();
        }

        @Override
        public String getBoundValue() {
            return uid;
        }
    }
    
    private class ActiveBinding implements ArgumentProperty.ReadBinding {

        @Override
        public Argument getBoundValue() {
            if (device != null) {
                return PBoolean.TRUE;
            } else {
                return PBoolean.FALSE;
            }
        }
        
    }
}
