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

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.ListenerUtils;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Param implements ArgumentProperty.Binding {

    private Argument argValue;
    private Listener[] listeners;

    public Param() {
        this.argValue = PString.EMPTY;
        this.listeners = new Listener[0];
    }

    public final void setBoundValue(long time, Argument value) throws Exception {
        argValue = value;
        for (Listener listener : listeners) {
            listener.valueChanged(this);
        }
    }

    public final Argument getBoundValue() {
        return argValue;
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

    public Argument get() {
        return argValue;
    }

    public double getDouble() throws ArgumentFormatException {
        return PNumber.coerce(argValue).value();
    }

    public double getDouble(double def) {
        try {
            return getDouble();
        } catch (ArgumentFormatException ex) {
            return def;
        }
    }

    public int getInt() throws ArgumentFormatException {
        return PNumber.coerce(argValue).toIntValue();
    }

    public int getInt(int def) {
        try {
            return getInt();
        } catch (ArgumentFormatException ex) {
            return def;
        }
    }

    public static interface Listener {

        public void valueChanged(Param p);

    }

    public static interface Animator {

        public long getTime();

        public void connect(Param p);

        public void disconnect(Param p);

    }

}
