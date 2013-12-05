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

import com.tinkerforge.BrickletIO16;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.types.PBoolean;
import net.neilcsmith.praxis.impl.BooleanProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;

/**
 *
 * @author Neil C Smith
 */
public class IO16 extends AbstractTFComponent<BrickletIO16> {

    private final static Logger LOG = Logger.getLogger(IO16.class.getName());
    private BrickletIO16 device;
    private Pin[] pinsA;
    private Pin[] pinsB;
    private BooleanProperty invertInput;
    private BrickletIO16.InterruptListener listener;

    public IO16() {
        super(BrickletIO16.class);
        
        invertInput = BooleanProperty.create(true);
        registerControl("invert-input", invertInput);
        
        pinsA = new Pin[8];
        for (int i = 0; i < pinsA.length; i++) {
            ControlPort.Output pinInput = new DefaultControlOutputPort();
            Pin p = new Pin('a', (short) (1 << i), BrickletIO16.DIRECTION_IN, pinInput);
            p.value = true; // default to pull-up
            pinsA[i] = p;
            registerPort("input-a" + i, pinInput);
            registerControl("pull-up-a" + i, BooleanProperty.create(p, true));
        }
        
        pinsB = new Pin[8];
        for (int i = 0; i < pinsB.length; i++) {
            Pin p = new Pin('b', (short) (1 << i), BrickletIO16.DIRECTION_OUT, null);
            BooleanProperty pinOutput = BooleanProperty.create(p, false);
            pinsB[i] = p;
            String id = "output-b-" + i;
            registerControl(id, pinOutput);
            registerPort(id, pinOutput.createPort());
        }
        
    }

    @Override
    protected void initDevice(BrickletIO16 device) {
        this.device = device;
        listener = new Listener();
        device.addInterruptListener(listener);
        try {
            device.setPortInterrupt('a', (short) 0xFF);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        forceRefresh();
    }

    @Override
    protected void disposeDevice(BrickletIO16 device) {
        device.removeInterruptListener(listener);
        try {
            device.setPortInterrupt('a', (short) 0);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        listener = null;
        this.device = null;
    }

    @Override
    protected void updateDevice(BrickletIO16 device) {
        for (Pin p : pinsA) {
            if (p.dirty) {
                refreshConfiguration(p);
            }
        }
        for (Pin p : pinsB) {
            if (p.dirty) {
                refreshConfiguration(p);
            }
        }
    }

    private void refreshConfiguration(Pin p) {
        try {
            device.setPortConfiguration(p.PORT, p.MASK, p.DIRECTION, p.value);
        } catch (TimeoutException ex) {
            Logger.getLogger(IO16.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotConnectedException ex) {
            Logger.getLogger(IO16.class.getName()).log(Level.SEVERE, null, ex);
        }
        p.dirty = false;
    }

    private void forceRefresh() {
        for (Pin p : pinsA) {
            p.dirty = true;
        }
        for (Pin p : pinsB) {
            p.dirty = true;
        }
    }

    private void handleInterrupt(char port, int interruptMask, int valueMask, long time) {
        if (port == 'a') {
            for (Pin p : pinsA) {
                if (p.OUTPUT == null) {
                    continue;
                }
                if ((p.MASK & interruptMask) > 0) {
                    boolean value = (p.MASK & valueMask) > 0;
                    if (invertInput.getValue()) {
                        value = !value;
                    }
                    p.OUTPUT.send(time, value ? PBoolean.TRUE : PBoolean.FALSE);
                }
            }
        }
    }

    private class Pin implements BooleanProperty.Binding {

        private final char PORT;
        private final short MASK;
        private final char DIRECTION;
        private final ControlPort.Output OUTPUT;
        private boolean dirty;
        private boolean value; // true for pull-up (input) or high (output)

        private Pin(char port, short mask, char direction, ControlPort.Output output) {
            this.PORT = port;
            this.MASK = mask;
            this.DIRECTION = direction;
            this.OUTPUT = output;
        }

        @Override
        public void setBoundValue(long time, boolean value) {
            if (value != this.value) {
                this.value = value;
                dirty = true;
            }
        }

        @Override
        public boolean getBoundValue() {
            return value;
        }
    }

    private class Listener implements BrickletIO16.InterruptListener {

        @Override
        public void interrupt(final char port,
                final short interruptMask,
                final short valueMask) {
            final long time = getTime();
            invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (listener == Listener.this) {
                        handleInterrupt(port, interruptMask, valueMask, time);
                    }
                }
            });
        }
    }
}
