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

import com.tinkerforge.BrickletBarometer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.NumberProperty;

/**
 *
 * @author Neil C Smith
 */
public class Barometer extends AbstractTFComponent<BrickletBarometer> {
    
    private final static Logger LOG = Logger.getLogger(Barometer.class.getName());
    private final static double DEFAULT_REFERENCE_PRESSURE = 1013.25;
    
    private BrickletBarometer device;
    private double airPressure;
    private double altitude;
    private AirPressureListener airPressureListener;
    private AltitudeListener altitudeListener;
    private ReferencePressure referencePressure;
    private ControlPort.Output airPressureOut;
    private ControlPort.Output altitudeOut;
    
    public Barometer() {
        super(BrickletBarometer.class);
        referencePressure = new ReferencePressure();
        NumberProperty reference = NumberProperty.builder()
                .binding(referencePressure)
                .defaultValue(DEFAULT_REFERENCE_PRESSURE)
                .build();
        registerControl("reference-pressure", reference);
        
        NumberProperty pressure = NumberProperty.builder()
                .binding(new AirPressureBinding())
                .build();
        registerControl("pressure", pressure);
        airPressureOut = new DefaultControlOutputPort();
        registerPort("pressure", airPressureOut);
        
        NumberProperty alt = NumberProperty.builder()
                .binding(new AltitudeBinding())
                .build();
        registerControl("altitude", alt);
        altitudeOut = new DefaultControlOutputPort();
        registerPort("altitude", altitudeOut);
        
        
    }

    @Override
    protected void initDevice(BrickletBarometer device) {
        this.device = device;
        airPressureListener = new AirPressureListener();
        altitudeListener = new AltitudeListener();
        device.addAirPressureListener(airPressureListener);
        device.addAltitudeListener(altitudeListener);
        refreshAll();
        try {
            device.setAirPressureCallbackPeriod(getCallbackPeriod());
            device.setAltitudeCallbackPeriod(getCallbackPeriod());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void disposeDevice(BrickletBarometer device) {
        device.removeAirPressureListener(airPressureListener);
        device.removeAltitudeListener(altitudeListener);
        airPressureListener = null;
        altitudeListener = null;
        try {
            device.setAirPressureCallbackPeriod(0);
            device.setAltitudeCallbackPeriod(0);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        this.device = null;
    }

    @Override
    protected void updateDevice(BrickletBarometer device) {
        if (referencePressure.dirty) {
            int value = (int) (referencePressure.value * 1000.0);
            try {
                device.setReferenceAirPressure(value);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
            } finally {
                referencePressure.dirty = false;
            }
        }
    }
    
    private void refreshAll() {
        referencePressure.dirty = true;
    }
 
    private class AirPressureListener implements BrickletBarometer.AirPressureListener {

        @Override
        public void airPressure(final int pressure) {
            final long time = getTime();
            invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (airPressureListener == AirPressureListener.this) {
                        double p = pressure / 1000.0;
                        airPressure = p;
                        airPressureOut.send(time, p);
                    }
                }
            });
        }
        
    }
    
    private class AirPressureBinding implements NumberProperty.ReadBinding {

        @Override
        public double getBoundValue() {
            return airPressure;
        }
        
    }
    
    private class AltitudeListener implements BrickletBarometer.AltitudeListener {

        @Override
        public void altitude(final int alt) {
            final long time = getTime();
            invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (altitudeListener == AltitudeListener.this) {
                        double a = alt / 100.0;
                        altitude = a;
                        altitudeOut.send(time, a);
                    }
                }
            });
        }
        
    }
    
    private class AltitudeBinding implements NumberProperty.ReadBinding {

        @Override
        public double getBoundValue() {
            return altitude;
        }
        
    }
    
    private class ReferencePressure implements NumberProperty.Binding {
        
        private boolean dirty;
        private double value;
        
        private ReferencePressure() {
            this.value = DEFAULT_REFERENCE_PRESSURE;
        }

        @Override
        public void setBoundValue(long time, double value) {
            this.value = value;
            dirty = true;
        }

        @Override
        public double getBoundValue() {
            return value;
        }
        
    }
    
}
