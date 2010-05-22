/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.praxis.core;

import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith
 */
public abstract class ControlPort implements Port {

    public final static String VALUE = "value";
    public final static String TRIGGER = "trigger";
    public final static String READY = "ready";
    public final static String ERROR = "error";

    public final static PString BANG = PString.valueOf("");

    public final Class<? extends Port> getTypeClass() {
        return ControlPort.class;
    }

    public static abstract class Input extends ControlPort {

        public void connect(Port port) throws PortConnectionException {
            port.connect(this);
        }

        public void disconnect(Port port) {
            port.disconnect(this);
        }

        public final Direction getDirection() {
            return Port.Direction.IN;
        }

        protected abstract void addControlOutputPort(Output port)
                throws PortConnectionException;

        protected abstract void removeControlOutputPort(Output port);

//        public abstract int getIndex();

        public abstract void receive(long time, double value);

//        public abstract void receive(int value);

        public abstract void receive(long time, Argument value);
    }

    public static abstract class Output extends ControlPort {

        public final Direction getDirection() {
            return Port.Direction.OUT;
        }

        protected final void makeConnection(Input port) throws PortConnectionException {
            port.addControlOutputPort(this);
        }

        protected final void breakConnection(Input port) {
            port.removeControlOutputPort(this);
        }

        public abstract void send(long time, double value);

//        public abstract void send(int value);

        public abstract void send(long time, Argument value);

        public final void send(long time) {
            send(time, BANG);
        }
    }
}
