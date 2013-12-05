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

import com.tinkerforge.BrickletAnalogOut;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.impl.NumberProperty;

/**
 *
 * @author Neil C Smith
 */
public class AnalogOut extends AbstractTFComponent<BrickletAnalogOut> {

    private final static Logger LOG = Logger.getLogger(AnalogOut.class.getName());
    private BrickletAnalogOut device;
    private VoltageBinding voltage;

    public AnalogOut() {
        super(BrickletAnalogOut.class);
        voltage = new VoltageBinding();
        NumberProperty volts = NumberProperty.builder()
                .minimum(0)
                .maximum(5)
                .defaultValue(0)
                .binding(voltage)
                .build();
        registerControl("voltage", volts);
        registerPort("voltage", volts.createPort());
    }

    @Override
    protected void initDevice(BrickletAnalogOut device) {
        this.device = device;
        try {
            device.setMode(BrickletAnalogOut.MODE_ANALOG_VALUE);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        forceRefresh();
    }

    @Override
    protected void disposeDevice(BrickletAnalogOut device) {
        try {
            device.setVoltage(0);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        this.device = null;
    }

    @Override
    protected void updateDevice(BrickletAnalogOut device) {
        if (voltage.dirty) {
            voltage.refresh(device);
        }
    }

    private void forceRefresh() {
        voltage.dirty = true;
    }

    private class VoltageBinding implements NumberProperty.Binding {

        private boolean dirty;
        private double voltage;

        @Override
        public void setBoundValue(long time, double value) {
            voltage = value;
            dirty = true;
        }

        @Override
        public double getBoundValue() {
            return voltage;
        }

        private void refresh(BrickletAnalogOut device) {
            int v = (int) Math.round(voltage * 1000);
            if (v < 0) {
                v = 0;
            } else if (v > 5000) {
                v = 5000;
            }
            try {
                device.setVoltage(v);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            dirty = false;
        }
    }
}
