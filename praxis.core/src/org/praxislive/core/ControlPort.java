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
package org.praxislive.core;

import org.praxislive.core.types.PString;

/**
 *
 * @author Neil C Smith
 */
public abstract class ControlPort implements Port {

    public final static String VALUE = "value";
    public final static String TRIGGER = "trigger";
    public final static String READY = "ready";
    public final static String ERROR = "error";

    public final static PString BANG = PString.of("");

    public static abstract class Input extends ControlPort {

        public void connect(Port port) throws PortConnectionException {
//            port.connect(this);
            if (port instanceof Output) {
                port.connect(this);
            } else {
                throw new PortConnectionException("Trying to connect 2 input ports together");
            }
        }

        public void disconnect(Port port) {
            if (port instanceof Output) {
                port.disconnect(this);
            }
            
        }

        protected abstract void addControlOutputPort(Output port)
                throws PortConnectionException;

        protected abstract void removeControlOutputPort(Output port);

        public abstract void receive(long time, double value);

        public abstract void receive(long time, Value value);
    }

    public static abstract class Output extends ControlPort {

        protected final void makeConnection(Input port) throws PortConnectionException {
            port.addControlOutputPort(this);
        }

        protected final void breakConnection(Input port) {
            port.removeControlOutputPort(this);
        }

        public abstract void send(long time, double value);

        public abstract void send(long time, Value value);

        public final void send(long time) {
            send(time, BANG);
        }
    }
}
