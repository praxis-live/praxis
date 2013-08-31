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

import com.tinkerforge.BrickletJoystick;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.types.PBoolean;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 *
 * @author Neil C Smith
 */
public class Joystick extends AbstractTFComponent<BrickletJoystick> {

    private final static String SIGNAL = "Signal";
    private final static String VALUE = "Value";
    
    private BrickletJoystick device;
    private int x;
    private int y;
    private boolean pressed;
    private ControlPort.Output outX;
    private ControlPort.Output outY;
    private ControlPort.Output buttonOut;
    private BooleanProperty normalize;
    private PositionListener positionListener;
    private ButtonListener buttonListener;
    private StringProperty buttonMode;

    public Joystick() {
        super(BrickletJoystick.class);
        registerControl("x", ArgumentProperty.builder()
                .type(PNumber.class)
                .readOnly()
                .binding(new PositionBinding(false))
                .build());
        registerControl("y", ArgumentProperty.builder()
                .type(PNumber.class)
                .readOnly()
                .binding(new PositionBinding(true))
                .build());
        registerControl("button", ArgumentProperty.builder()
                .type(PBoolean.class)
                .readOnly()
                .binding(new ButtonBinding())
                .build());
        normalize = BooleanProperty.create(false);
        registerControl("normalize", normalize);
        buttonMode = StringProperty.builder()
                .allowedValues(VALUE, SIGNAL)
                .defaultValue(VALUE)
                .build();
        registerControl("button-mode", buttonMode);
        outX = new DefaultControlOutputPort();
        registerPort("x", outX);
        outY = new DefaultControlOutputPort();
        registerPort("y", outY);
        buttonOut = new DefaultControlOutputPort();
        registerPort("button", buttonOut);
    }

    @Override
    protected void initDevice(BrickletJoystick device) {
        this.device = device;
        positionListener = new PositionListener();
        buttonListener = new ButtonListener();
        device.addPositionListener(positionListener);
        device.addPressedListener(buttonListener);
        device.addReleasedListener(buttonListener);
        try {
            device.setPositionCallbackPeriod(50);
        } catch (Exception ex) {
            Logger.getLogger(Joystick.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    @Override
    protected void disposeDevice(BrickletJoystick device) {
        positionListener = null;
        buttonListener = null;
        try {
            device.removePositionListener(positionListener);
            device.removePressedListener(buttonListener);
            device.removeReleasedListener(buttonListener);
            device.setPositionCallbackPeriod(0);
        } catch (Exception ex) {
            Logger.getLogger(Joystick.class.getName()).log(Level.FINE, null, ex);
        }
        this.device = null;
    }

    @Override
    public void tick(ExecutionContext source) {
    }

    private double normalize(int val) {
        return (val + 100) / 200.0;
    }
    
    private class PositionBinding implements ArgumentProperty.ReadBinding {

        private final boolean vertical;
        
        private PositionBinding(boolean vertical) {
            this.vertical = vertical;
        }
        
        @Override
        public Argument getBoundValue() {
            int val = vertical ? y : x;
            if (normalize.getValue()) {
                return PNumber.valueOf(normalize(val));
            } else {
                return PNumber.valueOf(val);
            }
        }
    }
    
    private class ButtonBinding implements ArgumentProperty.ReadBinding {

        @Override
        public Argument getBoundValue() {
            return pressed ? PBoolean.TRUE : PBoolean.FALSE;
        }
        
    }
    
    private class PositionListener implements BrickletJoystick.PositionListener {

        @Override
        public void position(final short x, final short y) {
            final long time = getTime();
            invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (positionListener == PositionListener.this) {
                        Joystick.this.x = x;
                        Joystick.this.y = y;
                        if (normalize.getValue()) {
                            outX.send(time, normalize(x));
                            outY.send(time, normalize(y));
                        } else {
                            outX.send(time, x);
                            outY.send(time, y);
                        }
                    }
                }
            });
        }
        
    }
    
    private class ButtonListener implements BrickletJoystick.PressedListener,
            BrickletJoystick.ReleasedListener {

        @Override
        public void pressed() {
            update(true);
        }

        @Override
        public void released() {
            update(false);
        }
        
        private void update(final boolean pressed) {
            final long time = getTime();
            invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (buttonListener == ButtonListener.this) {
                        Joystick.this.pressed = pressed;
                        boolean signal = SIGNAL.equals(buttonMode.getValue());
                        if (signal) {
                            if (pressed) {
                                buttonOut.send(time);
                            }
                        } else {
                            buttonOut.send(time, pressed ? PBoolean.TRUE : PBoolean.FALSE);
                        }  
                    }
                }
            });
        }
        
    }
}
