/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.code.userapi;

import org.praxislive.core.Value;
import org.praxislive.core.types.PBoolean;
import org.praxislive.core.types.PString;

/**
 * A field type providing a control output port. Use with @Out or @AuxOut.
 */
public abstract class Output {

    /**
     * Send a signal (empty String).
     */
    public abstract void send();

    /**
     * Send a double value.
     *
     * @param value
     */
    public abstract void send(double value);

    /**
     * Send a Value.
     *
     * @param value
     */
    public abstract void send(Value value);

    /**
     * Send a float.
     *
     * @param value
     */
    public void send(float value) {
        send((double) value);
    }

    /**
     * Send a String value.
     *
     * @param value
     */
    public void send(String value) {
        send(PString.of(value));
    }

    /**
     * Send a boolean value.
     *
     * @param value
     */
    public void send(boolean value) {
        send(PBoolean.of(value));
    }

    /**
     * Send an int value.
     *
     * @param value
     */
    public void send(int value) {
        send((double) value);
    }

}
