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

import com.tinkerforge.BrickletTemperature;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.NumberProperty;

/**
 *
 * @author Neil C Smith
 */
public class Temperature extends AbstractTFComponent<BrickletTemperature> {

    private final static Logger LOG = Logger.getLogger(Temperature.class.getName());
    private BrickletTemperature device;
    private double temperature;
    private ControlPort.Output tOut;
    private TemperatureListener tListener;

    public Temperature() {
        super(BrickletTemperature.class);
        NumberProperty tProperty = NumberProperty.builder()
                .binding(new TemperatureBinding())
                .build();
        registerControl("temperature", tProperty);
        tOut = new DefaultControlOutputPort();
        registerPort("temperature", tOut);
    }

    @Override
    protected void initDevice(BrickletTemperature device) {
        this.device = device;
        tListener = new TemperatureListener();
        device.addTemperatureListener(tListener);
        try {
            device.setTemperatureCallbackPeriod(getCallbackPeriod());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void disposeDevice(BrickletTemperature device) {
        device.removeTemperatureListener(tListener);
        tListener = null;
        try {
            device.setTemperatureCallbackPeriod(0);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        this.device = null;
    }

    private class TemperatureListener implements BrickletTemperature.TemperatureListener {

        @Override
        public void temperature(final short temp) {
            final long time = getTime();
            invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (tListener == TemperatureListener.this) {
                        double t = temp / 100.0;
                        temperature = t;
                        tOut.send(time, t);
                    }
                }
            });
        }
    }

    private class TemperatureBinding implements NumberProperty.ReadBinding {

        @Override
        public double getBoundValue() {
            return temperature;
        }
    }
}
