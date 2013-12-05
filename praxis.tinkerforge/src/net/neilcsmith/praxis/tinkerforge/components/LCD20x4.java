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

import com.tinkerforge.BrickletLCD20x4;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.types.PBoolean;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 *
 * @author Neil C Smith
 */
public class LCD20x4 extends AbstractTFComponent<BrickletLCD20x4> {
    
    private final static Logger LOG = Logger.getLogger(LCD20x4.class.getName());
    
    private final static String SIGNAL = "Signal";
    private final static String VALUE = "Value";

    private BrickletLCD20x4 device;
    private LineBinding[] lines;
    private BacklightBinding backlight;
    private ControlPort.Output[] buttonOuts;
    private StringProperty buttonMode;
    private ButtonListener buttonListener;

    public LCD20x4() {
        super(BrickletLCD20x4.class);
        int lineCount = 4;
        lines = new LineBinding[lineCount];
        for (int i = 0; i < lineCount; i++) {
            LineBinding line = new LineBinding(i);
            lines[i] = line;
            StringProperty p = StringProperty.create(line, line.value);
            String id = "line-" + (i + 1);
            registerControl(id, p);
            registerPort(id, p.createPort());
        }
        
        backlight = new BacklightBinding();
        BooleanProperty bl = BooleanProperty.create(backlight, false);
        registerControl("backlight", bl);
        registerPort("backlight", bl.createPort());
        
        int btnCount = 4;
        buttonOuts = new ControlPort.Output[btnCount];
        for (int i=0; i < btnCount; i++) {
            ControlPort.Output out = new DefaultControlOutputPort();
            buttonOuts[i] = out;
            String id = "button-" + (i + 1);
            registerPort(id, out);
        }
        
        buttonMode = StringProperty.builder()
                .allowedValues(VALUE, SIGNAL)
                .defaultValue(VALUE)
                .build();
        
        registerControl("button-mode", buttonMode);
    }

    @Override
    protected void initDevice(BrickletLCD20x4 device) {
        this.device = device;
        buttonListener = new ButtonListener();
        device.addButtonPressedListener(buttonListener);
        device.addButtonReleasedListener(buttonListener);
        forceRefresh();
    }

    @Override
    protected void disposeDevice(BrickletLCD20x4 device) {
        try {
            device.removeButtonPressedListener(buttonListener);
            device.removeButtonReleasedListener(buttonListener);
            device.backlightOff();
            device.clearDisplay();       
        } catch (Exception ex) {
            Logger.getLogger(LCD20x4.class.getName()).log(Level.FINE, null, ex);
        }
        buttonListener = null;
        this.device = null;
    }

    @Override
    protected void updateDevice(BrickletLCD20x4 device) {
        for (LineBinding line : lines) {
            if (line.dirty) {
                refreshLine(line);
            }
        }
        if (backlight.dirty) {
            refreshBacklight(backlight);
        }
    }
      
    private void forceRefresh() {
        for (LineBinding line : lines) {
            line.dirty = true;
        }
        backlight.dirty = true;
    }
 
    private void refreshLine(LineBinding line) {
        
        String txt = line.value;
        LOG.log(Level.FINE, "LCD updating line : {0}", txt);
        try {
            device.writeLine((short) line.idx, (short) 0, Util.lcdString(txt, 20));
        } catch (Exception ex) {
            Logger.getLogger(LCD20x4.class.getName()).log(Level.SEVERE, null, ex);
        }
        line.dirty = false;
    }
    
    private void refreshBacklight(BacklightBinding backlight) {
        LOG.fine("LCD updating backlight");
        try {
            if (backlight.value) {
                device.backlightOn();
            } else {
                device.backlightOff();
            }
        } catch (Exception ex) {
            Logger.getLogger(LCD20x4.class.getName()).log(Level.SEVERE, null, ex);
        }
        backlight.dirty = false;
    }

    private class LineBinding implements StringProperty.Binding {

        private final int idx;
        private boolean dirty;
        private String value;

        private LineBinding(int idx) {
            this.idx = idx;
            this.value = "";
        }

        @Override
        public void setBoundValue(long time, String value) {
//            if (!this.value.equals(value)) {
                this.value = value;
                dirty = true;
//            }
        }

        @Override
        public String getBoundValue() {
            return value;
        }
    }

    private class BacklightBinding implements BooleanProperty.Binding {
        
        private boolean value;
        private boolean dirty;

        @Override
        public void setBoundValue(long time, boolean val) {
            if (value != val) {
                value = val;
                dirty = true;
            }
        }

        @Override
        public boolean getBoundValue() {
            return value;
        }
    }
    
    private class ButtonListener implements BrickletLCD20x4.ButtonPressedListener, 
            BrickletLCD20x4.ButtonReleasedListener {

        @Override
        public void buttonPressed(short button) {
            update(true, button);
        }

        @Override
        public void buttonReleased(short button) {
            update(false, button);
        }
        
        private void update(final boolean pressed, final int button) {
            final long time = getTime();
            invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (buttonListener == ButtonListener.this) {
                        if (button < 0 || button >= buttonOuts.length) {
                            return;
                        }
                        boolean signal = SIGNAL.equals(buttonMode.getValue());
                        if (signal) {
                            if (pressed) {
                                buttonOuts[button].send(time);
                            }
                        } else {
                            buttonOuts[button].send(time, pressed ? PBoolean.TRUE : PBoolean.FALSE);
                        }  
                    }
                }
            });
        }
        
    }
}
