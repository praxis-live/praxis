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

import com.tinkerforge.BrickletTemperatureIR;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.NumberProperty;

/**
 *
 * @author Neil C Smith
 */
public class TemperatureIR extends AbstractTFComponent<BrickletTemperatureIR> {

    private final static Logger LOG = Logger.getLogger(Temperature.class.getName());
    private BrickletTemperatureIR device;
    private double objectTemperature;
    private double ambientTemperature;
    private ControlPort.Output objectOut;
    private ControlPort.Output ambientOut;
    private TemperatureListener temperatureListener;
//    private EmissivityBinding emissivityBinding;

    public TemperatureIR() {
        super(BrickletTemperatureIR.class);

//        emissivityBinding = new EmissivityBinding();
//        NumberProperty emissivity = NumberProperty.builder()
//                .binding(emissivityBinding)
//                .minimum(0)
//                .maximum(1)
//                .defaultValue(1)
//                .build();
//        registerControl("emissivity", emissivity);

        NumberProperty objectProperty = NumberProperty.builder()
                .binding(new TemperatureBinding(true))
                .build();
        registerControl("object", objectProperty);
        objectOut = new DefaultControlOutputPort();
        registerPort("object", objectOut);

        NumberProperty ambientProperty = NumberProperty.builder()
                .binding(new TemperatureBinding(false))
                .build();
        registerControl("ambient", ambientProperty);
        ambientOut = new DefaultControlOutputPort();
        registerPort("ambient", ambientOut);

    }

    @Override
    protected void initDevice(BrickletTemperatureIR device) {
        this.device = device;
        temperatureListener = new TemperatureListener();
        device.addObjectTemperatureListener(temperatureListener);
        device.addAmbientTemperatureListener(temperatureListener);
        refreshAll();
        try {
            device.setObjectTemperatureCallbackPeriod(getCallbackPeriod());
            device.setAmbientTemperatureCallbackPeriod(getCallbackPeriod());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void disposeDevice(BrickletTemperatureIR device) {
        device.removeObjectTemperatureListener(temperatureListener);
        device.removeAmbientTemperatureListener(temperatureListener);
        temperatureListener = null;
        try {
            device.setObjectTemperatureCallbackPeriod(0);
            device.setAmbientTemperatureCallbackPeriod(0);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        this.device = null;
    }

    @Override
    protected void updateDevice(BrickletTemperatureIR device) {
//        if (emissivityBinding.dirty) {
//            int emInt = (int) emissivityBinding.value * 65535;
//            try {
//                device.setEmissivity(emInt);
//            } catch (Exception ex) {
//                LOG.log(Level.SEVERE, null, ex);
//            } finally {
//                emissivityBinding.dirty = false;
//            }
//        }
    }

    private void refreshAll() {
//        emissivityBinding.dirty = true;
    }

    private class TemperatureListener
            implements BrickletTemperatureIR.ObjectTemperatureListener,
            BrickletTemperatureIR.AmbientTemperatureListener {

        @Override
        public void objectTemperature(final short temp) {
            final long time = getTime();
            invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (temperatureListener == TemperatureListener.this) {
                        double t = temp / 10.0;
                        objectTemperature = t;
                        objectOut.send(time, t);
                    }
                }
            });
        }

        @Override
        public void ambientTemperature(final short temp) {
            final long time = getTime();
            invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (temperatureListener == TemperatureListener.this) {
                        double t = temp / 10.0;
                        ambientTemperature = t;
                        ambientOut.send(time, t);
                    }
                }
            });


        }
    }

    private class TemperatureBinding implements NumberProperty.ReadBinding {

        private final boolean object;

        private TemperatureBinding(boolean object) {
            this.object = object;
        }

        @Override
        public double getBoundValue() {
            return object ? objectTemperature : ambientTemperature;
        }
    }

//    private class EmissivityBinding implements NumberProperty.Binding {
//
//        private boolean dirty = true;
//        private double value = 1;
//
//        @Override
//        public void setBoundValue(long time, double value) {
//            this.value = value;
//            dirty = true;
//        }
//
//        @Override
//        public double getBoundValue() {
//            return value;
//        }
//    }
}
