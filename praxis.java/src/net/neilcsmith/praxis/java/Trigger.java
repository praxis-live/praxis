/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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


package net.neilcsmith.praxis.java;

import net.neilcsmith.praxis.impl.ListenerUtils;
import net.neilcsmith.praxis.impl.TriggerControl;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Trigger implements TriggerControl.Binding {

    private boolean triggered;
    private Listener[] listeners;
    
    public Trigger() {
        this.triggered = false;
        this.listeners = new Listener[0];
    }

    public void trigger(long time) {
        triggered = true;
        for (Listener listener : listeners) {
            listener.triggered(this);
        }
    }

    public boolean get() {
        return triggered;
    }

    public boolean getAndReset() {
        boolean ret = triggered;
        triggered = false;
        return ret;
    }
    
    public void addListener(Listener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        listeners = ListenerUtils.add(listeners, listener);
    }
    
    public void removeListener(Listener listener) {
        listeners = ListenerUtils.remove(listeners, listener);
    }
    
    public static interface Listener {
        
        public void triggered(Trigger t);
        
    }

}
