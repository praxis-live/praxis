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

import com.tinkerforge.BrickServo;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.IntProperty;
import net.neilcsmith.praxis.impl.NumberProperty;

/**
 *
 * @author Neil C Smith
 */
public class Servo extends AbstractTFComponent<BrickServo> {

    private final static Logger LOG = Logger.getLogger(Servo.class.getName());
    private final static int DEFAULT_PERIOD = 19500;
    private final static int DEFAULT_MIN_PW = 1000;
    private final static int DEFAULT_MAX_PW = 2000;
    private final static int DEFAULT_VOLTAGE = 5000;
    private final static int SERVO_COUNT = 7;
    private BrickServo device;
    private PositionBinding[] positions;
    private EnabledBinding[] enabled;
    private boolean configDirty;
    private IntProperty period;
    private IntProperty minPulseWidth;
    private IntProperty maxPulseWidth;
    private IntProperty outputVoltage;

    public Servo() {
        super(BrickServo.class);
        buildPositionControls();
        buildEnabledControls();
        buildConfigurationControls();



    }

    private void buildPositionControls() {
        int len = SERVO_COUNT;
        positions = new PositionBinding[len];
        for (int i = 0; i < len; i++) {
            PositionBinding pos = new PositionBinding(i, 0.5);
            positions[i] = pos;
            NumberProperty posProp = NumberProperty.builder()
                    .binding(pos)
                    .minimum(0)
                    .maximum(1)
                    .defaultValue(0.5)
                    .build();
            String id = "position-" + (i + 1);
            registerControl(id, posProp);
            registerPort(id, posProp.createPort());
        }
    }

    private void buildEnabledControls() {
        int len = SERVO_COUNT;
        enabled = new EnabledBinding[len];
        for (int i = 0; i < len; i++) {
            EnabledBinding en = new EnabledBinding(i);
            enabled[i] = en;
            BooleanProperty enProp = BooleanProperty.builder()
                    .binding(en)
                    .defaultValue(false)
                    .build();
            String id = "enabled-" + (i + 1);
            registerControl(id, enProp);
        }
    }

    private void buildConfigurationControls() {
        period = IntProperty.builder()
                .defaultValue(DEFAULT_PERIOD)
                .minimum(2000)
                .maximum(65535)
                .binding(new ConfigurationBinding(DEFAULT_PERIOD))
                .build();
        minPulseWidth = IntProperty.builder()
                .defaultValue(DEFAULT_MIN_PW)
                .minimum(1)
                .maximum(65535)
                .binding(new ConfigurationBinding(DEFAULT_MIN_PW))
                .build();
        maxPulseWidth = IntProperty.builder()
                .defaultValue(DEFAULT_MAX_PW)
                .minimum(1)
                .maximum(65535)
                .binding(new ConfigurationBinding(DEFAULT_MAX_PW))
                .build();
        outputVoltage = IntProperty.builder()
                .defaultValue(DEFAULT_VOLTAGE)
                .minimum(5000)
                .maximum(9000)
                .binding(new ConfigurationBinding(DEFAULT_VOLTAGE))
                .build();
        registerControl("period", period);
        registerControl("min-pulse-width", minPulseWidth);
        registerControl("max-pulse-width", maxPulseWidth);
        registerControl("output-voltage", outputVoltage);
    }

    @Override
    protected void initDevice(BrickServo device) {
        this.device = device;
        forceRefresh();
    }

    @Override
    protected void disposeDevice(BrickServo device) {
        try {
            for (short i = 0; i < SERVO_COUNT; i++) {
                device.disable(i);
            }
        } catch (TimeoutException ex) {
            Logger.getLogger(Servo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotConnectedException ex) {
            Logger.getLogger(Servo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void updateDevice(BrickServo device) {
        if (configDirty) {
            configureDevice();
        }
        for (EnabledBinding enable : enabled) {
            if (enable.dirty) {
                refresh(enable);
            }
        }
        for (PositionBinding position : positions) {
            if (position.dirty) {
                refresh(position);
            }
        }
    }

    private void configureDevice() {
        int per = period.getValue();
        int minPW = minPulseWidth.getValue();
        int maxPW = maxPulseWidth.getValue();
        int volts = outputVoltage.getValue();
        if (LOG.isLoggable(Level.CONFIG)) {
            LOG.log(Level.CONFIG,
                    "Reconfiguring Servo brick\n -- Period: {0}\n -- Min PW: {1}\n -- Max PW: {2}\n -- Voltage: {3}",
                    new Object[]{per, minPW, maxPW, volts});
        }
        try {
            device.setOutputVoltage(volts);
            for (short i = 0; i < SERVO_COUNT; i++) {
                device.setPeriod(i, per);
                device.setPulseWidth(i, minPW, maxPW);
            }
        } catch (TimeoutException ex) {
            Logger.getLogger(Servo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotConnectedException ex) {
            Logger.getLogger(Servo.class.getName()).log(Level.SEVERE, null, ex);
        }
        configDirty = false;
    }

    private void refresh(EnabledBinding enable) {
        try {
            if (enable.value) {
                device.enable((short) enable.idx);
            } else {
                device.disable((short) enable.idx);
            }
        } catch (Exception ex) {
            Logger.getLogger(Servo.class.getName()).log(Level.SEVERE, null, ex);
        }
        enable.dirty = false;
    }

    private void refresh(PositionBinding position) {
        try {
            short pos = convert(position.value);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Setting Servo:{0} to position:{1}",
                        new Object[]{position.idx, pos});
            }
            device.setPosition((short) position.idx, pos);
        } catch (Exception ex) {
            Logger.getLogger(Servo.class.getName()).log(Level.SEVERE, null, ex);
        }
        position.dirty = false;
    }

    private short convert(double val) {
        return (short) ((val * 18000) - 9000);
    }

    private void forceRefresh() {
        configDirty = true;
        for (PositionBinding position : positions) {
            position.dirty = true;
        }
        for (EnabledBinding enable : enabled) {
            enable.dirty = true;
        }
    }

    private class PositionBinding implements NumberProperty.Binding {

        private final int idx;
        private boolean dirty;
        private double value;

        private PositionBinding(int idx, double value) {
            this.idx = idx;
            this.value = value;
        }

        @Override
        public void setBoundValue(long time, double value) {
            if (this.value != value) {
                this.value = value;
                this.dirty = true;
            }
        }

        @Override
        public double getBoundValue() {
            return value;
        }
    }

    private class EnabledBinding implements BooleanProperty.Binding {

        private final int idx;
        private boolean dirty;
        private boolean value;

        private EnabledBinding(int idx) {
            this.idx = idx;
        }

        @Override
        public void setBoundValue(long time, boolean value) {
            if (this.value != value) {
                this.value = value;
                dirty = true;
            }
        }

        @Override
        public boolean getBoundValue() {
            return value;
        }
    }

    private class ConfigurationBinding implements IntProperty.Binding {

        private int value;

        private ConfigurationBinding(int initialValue) {
            this.value = initialValue;
        }

        @Override
        public void setBoundValue(long time, int value) {
            if (this.value != value) {
                this.value = value;
                configDirty = true;
            }
        }

        @Override
        public int getBoundValue() {
            return value;
        }
    }
}
