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
package net.neilcsmith.praxis.video;

import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.PortConnectionException;
import net.neilcsmith.ripl.core.Source;

/**
 *
 * @author Neil C Smith
 */
public abstract class VideoPort implements Port {

    public final Class<? extends Port> getTypeClass() {
        return VideoPort.class;
    }

    public static abstract class Input extends VideoPort {

        public void connect(Port port) throws PortConnectionException {
            if (port instanceof Output) {
                port.connect(this);
            } else {
                throw new PortConnectionException();
            }
            
        }

        public void disconnect(Port port) {
            if (port instanceof Output) {
                port.disconnect(this);
            }
            
        }

        public final Direction getDirection() {
            return Port.Direction.INPUT;
        }

        protected abstract void addImageOutputPort(Output port, Source source) throws PortConnectionException;

        protected abstract void removeImageOutputPort(Output port, Source source);
    }

    public static abstract class Output extends VideoPort {

        public final Direction getDirection() {
            return Port.Direction.OUTPUT;
        }

        protected void makeConnection(Input port, Source source) throws PortConnectionException {
            port.addImageOutputPort(this, source);
        }

        protected void breakConnection(Input port, Source source) {
            port.removeImageOutputPort(this, source);
        }
    }
}
