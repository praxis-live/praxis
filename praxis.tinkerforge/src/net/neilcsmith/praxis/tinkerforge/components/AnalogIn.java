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

import com.tinkerforge.BrickletAnalogIn;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.NumberProperty;

/**
 *
 * @author Neil C Smith
 */
public class AnalogIn extends AbstractTFComponent<BrickletAnalogIn> {

    private final static Logger LOG = Logger.getLogger(AnalogIn.class.getName());
    private BrickletAnalogIn device;
    private double voltage;
    private ControlPort.Output vOut;
    private VoltageListener vListener;

    public AnalogIn() {
        super(BrickletAnalogIn.class);
        NumberProperty vProperty = NumberProperty.builder()
                .minimum(0)
                .maximum(45)
                .binding(new VoltageBinding())
                .build();
        registerControl("voltage", vProperty);
        vOut = new DefaultControlOutputPort();
        registerPort("voltage", vOut);
    }

    @Override
    protected void initDevice(BrickletAnalogIn device) {
        this.device = device;
        vListener = new VoltageListener();
        device.addVoltageListener(vListener);
        try {
            device.setVoltageCallbackPeriod(getCallbackPeriod());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void disposeDevice(BrickletAnalogIn device) {
        device.removeVoltageListener(vListener);
        vListener = null;
        try {
            device.setVoltageCallbackPeriod(0);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        this.device = null;
    }

    private class VoltageListener implements BrickletAnalogIn.VoltageListener {

        @Override
        public void voltage(final int mv) {
            final long time = getTime();
            invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (vListener == VoltageListener.this) {
                        double v = mv / 1000.0;
                        voltage = v;
                        vOut.send(time, v);
                    }
                }
            });
        }
    }

    private class VoltageBinding implements NumberProperty.ReadBinding {

        @Override
        public double getBoundValue() {
            return voltage;
        }
    }
}
