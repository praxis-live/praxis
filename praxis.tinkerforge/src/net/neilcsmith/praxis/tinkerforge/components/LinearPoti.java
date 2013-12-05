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

import com.tinkerforge.BrickletLinearPoti;
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
public class LinearPoti extends AbstractTFComponent<BrickletLinearPoti> {

    private BrickletLinearPoti device;
    private int value;
    private ControlPort.Output out;
    private BooleanProperty normalize;
    private Listener listener;

    public LinearPoti() {
        super(BrickletLinearPoti.class);
        registerControl("value", ArgumentProperty.createReadOnly(PNumber.info(), new ValueBinding()));
        normalize = BooleanProperty.create(false);
        registerControl("normalize", normalize);
        out = new DefaultControlOutputPort();
        registerPort("value", out);
    }

    @Override
    protected void initDevice(BrickletLinearPoti device) {
        this.device = device;
        Listener l = new Listener();
        listener = l;
        device.addPositionListener(l);
        try {
            device.setPositionCallbackPeriod(getCallbackPeriod());
        } catch (Exception ex) {
            Logger.getLogger(RotaryPoti.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void disposeDevice(BrickletLinearPoti device) {
        listener = null;
        try {
            device.setPositionCallbackPeriod(0);
        } catch (Exception ex) {
            Logger.getLogger(RotaryPoti.class.getName()).log(Level.FINE, null, ex);
        }
        this.device = null;
    }

    private double normalize(int val) {
        return val / 100.0;
    }

    private class Listener implements BrickletLinearPoti.PositionListener {

        @Override
        public void position(final int pos) {
            final long time = getTime();
            invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (listener == Listener.this) {
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
