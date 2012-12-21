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

import com.tinkerforge.BrickletAmbientLight;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;

/**
 *
 * @author Neil C Smith
 */
public class AmbientLight extends AbstractTFComponent<BrickletAmbientLight> {

    private BrickletAmbientLight device;
    private int value;
    private ControlPort.Output out;
    private volatile int illuminance;
    private volatile Listener active;

    public AmbientLight() {
        super(BrickletAmbientLight.class);
        registerControl("illuminance", ArgumentProperty.createReadOnly(PNumber.info(), new ValueBinding()));
        out = new DefaultControlOutputPort();
        registerPort("illuminance", out);
    }

    @Override
    protected void initDevice(BrickletAmbientLight device) {
        this.device = device;
        Listener l = new Listener();
        active = l;
        device.addListener(l);
        device.setIlluminanceCallbackPeriod(50);
    }

    @Override
    protected void disposeDevice(BrickletAmbientLight device) {
        active = null;
        device.setIlluminanceCallbackPeriod(0);
        this.device = null;
    }

    @Override
    public void tick(ExecutionContext source) {
        int lum = illuminance;
        if (lum != value) {
            value = lum;
            out.send(source.getTime(), value);
        }
    }

    private class Listener implements BrickletAmbientLight.IlluminanceListener {

        @Override
        public void illuminance(int lum) {
            if (active == this) {
                illuminance = lum;
            }
        }
    }

    private class ValueBinding implements ArgumentProperty.ReadBinding {

        @Override
        public Argument getBoundValue() {
            return PNumber.valueOf(value);
        }
    }
}
