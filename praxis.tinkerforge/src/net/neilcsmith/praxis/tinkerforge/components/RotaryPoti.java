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

import com.tinkerforge.BrickletRotaryPoti;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;

/**
 *
 * @author Neil C Smith
 */
public class RotaryPoti extends AbstractTFComponent<BrickletRotaryPoti> {

    private BrickletRotaryPoti device;
    private int value;
    private ControlPort.Output out;
    private BooleanProperty normalize;
    private Listener active;

    public RotaryPoti() {
        super(BrickletRotaryPoti.class);
        registerControl("value", ArgumentProperty.createReadOnly(PNumber.info(), new ValueBinding()));
        normalize = BooleanProperty.create(false);
        registerControl("normalize", normalize);
        out = new DefaultControlOutputPort();
        registerPort("value", out);
    }

    @Override
    protected void initDevice(BrickletRotaryPoti device) {
        this.device = device;
        Listener l = new Listener();
        active = l;
        device.addPositionListener(l);
        try {
            device.setPositionCallbackPeriod(getCallbackPeriod());
        } catch (Exception ex) {
            Logger.getLogger(RotaryPoti.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void disposeDevice(BrickletRotaryPoti device) {
        active = null;
        try {
            device.setPositionCallbackPeriod(0);
        } catch (Exception ex) {
            Logger.getLogger(RotaryPoti.class.getName()).log(Level.FINE, null, ex);
        }
        this.device = null;
    }

    private double normalize(int val) {
        return (val + 150) / 300.0;
    }

    private class Listener implements BrickletRotaryPoti.PositionListener {

        @Override
        public void position(final short pos) {
            final long time = getTime();
            invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (active == Listener.this) {
                        value = pos;
                        if (normalize.getValue()) {
                            out.send(time, normalize(value));
                        } else {
                            out.send(time, value);
                        }
                    }
                }
            });
        }
        
    }

    private class ValueBinding implements ArgumentProperty.ReadBinding {

        @Override
        public Argument getBoundValue() {
            if (normalize.getValue()) {
                return PNumber.valueOf(normalize(value));
            } else {
                return PNumber.valueOf(value);
            }

        }
    }
}
