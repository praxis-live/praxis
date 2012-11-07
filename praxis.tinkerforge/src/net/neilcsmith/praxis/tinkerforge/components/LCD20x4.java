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

import com.tinkerforge.BrickletLCD20x4;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 *
 * @author Neil C Smith
 */
public class LCD20x4 extends AbstractTFComponent<BrickletLCD20x4> {
    
    private BrickletLCD20x4 device;
    private String[] lines;
    private boolean backlight;
    
    public LCD20x4() {
        super(BrickletLCD20x4.class);
        int len = 4;
        lines = new String[len];
        for (int i=0; i<len; i++) {
            lines[i] = "";
            StringProperty p = StringProperty.create(new LineBinding(i), "");
            String id = "line" + (i+1);
            registerControl(id, p);
            registerPort(id, p.createPort());
        }
        BooleanProperty bl = BooleanProperty.create(new BacklightBinding(), false);
        registerControl("backlight", bl);
        registerPort("backlight", bl.createPort());
    }

    @Override
    protected void initDevice(BrickletLCD20x4 device) {
        this.device = device;
        refreshBacklight();
        refreshDisplay();
    }

    @Override
    protected void disposeDevice(BrickletLCD20x4 device) {
        this.device = null;
    }

    @Override
    public void tick(ExecutionContext source) {
        
    }
    
    private void refreshDisplay() {
        if (device == null) {
            return;
        }
        device.clearDisplay();
        for (int i=0; i<lines.length; i++) {
            device.writeLine((short)i, (short)0, lines[i]);
        }
    }
    
    private void refreshBacklight() {
        if (device == null) {
            return;
        }
        if (backlight) {
            device.backlightOn();
        } else {
            device.backlightOff();
        }
    }
    
    private class LineBinding implements StringProperty.Binding {
        
        private int idx;
        
        private LineBinding(int idx) {
            this.idx = idx;
        }

        @Override
        public void setBoundValue(long time, String value) {
            lines[idx] = value;
            refreshDisplay();
        }

        @Override
        public String getBoundValue() {
            return lines[idx];
        }
        
    }
    
    private class BacklightBinding implements BooleanProperty.Binding {

        @Override
        public void setBoundValue(long time, boolean value) {
            if (backlight != value) {
                backlight = value;
                refreshBacklight();
            }
        }

        @Override
        public boolean getBoundValue() {
            return backlight;
        }
        
    }
    
    
    
    
}
