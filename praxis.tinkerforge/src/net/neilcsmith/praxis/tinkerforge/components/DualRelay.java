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

import com.tinkerforge.BrickletDualRelay;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.impl.BooleanProperty;

/**
 *
 * @author Neil C Smith
 */
public class DualRelay extends AbstractTFComponent<BrickletDualRelay> {
    
    private final static Logger LOG = Logger.getLogger(DualRelay.class.getName());
    
    private BrickletDualRelay device;
    private Relay r1;
    private Relay r2;
    
    public DualRelay() {
        super(BrickletDualRelay.class);
        r1 = new Relay();
        r2 = new Relay();
        BooleanProperty r1prop = BooleanProperty.builder()
                .binding(r1)
                .build();
        BooleanProperty r2prop = BooleanProperty.builder()
                .binding(r2)
                .build();
        registerControl("relay-1", r1prop);
        registerPort("relay-1", r1prop.createPort());
        registerControl("relay-2", r2prop);
        registerPort("relay-2", r2prop.createPort());
    }

    @Override
    protected void initDevice(BrickletDualRelay device) {
        this.device = device;
        forceRefresh();
    }

    @Override
    protected void disposeDevice(BrickletDualRelay device) {
        try {
            device.setState(false, false);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        this.device = null;
    }

    @Override
    protected void updateDevice(BrickletDualRelay device) {
        if (r1.dirty || r2.dirty) {
            try {
                device.setState(r1.value, r2.value);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            r1.dirty = false;
            r2.dirty = false;
        }
    }
    
    private void forceRefresh() {
        r1.dirty = true;
        r2.dirty = true;
    }
    
    private class Relay implements BooleanProperty.Binding {
        
        private boolean value;
        private boolean dirty;

        @Override
        public void setBoundValue(long time, boolean value) {
            this.value = value;
            dirty = true;
        }

        @Override
        public boolean getBoundValue() {
            return value;
        }
        
    }
    
}
