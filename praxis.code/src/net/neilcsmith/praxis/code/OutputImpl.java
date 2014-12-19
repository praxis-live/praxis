/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2014 Neil C Smith.
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
package net.neilcsmith.praxis.code;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.code.userapi.AuxOut;
import net.neilcsmith.praxis.code.userapi.Out;
import net.neilcsmith.praxis.code.userapi.Output;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.PortInfo;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class OutputImpl extends Output {

    private final static Logger LOG = Logger.getLogger(OutputImpl.class.getName());
    
    private final CodeContext<?> context;
    private final ControlOutput port;

    private OutputImpl(CodeContext<?> context, ControlOutput port) {
        this.context = context;
        this.port = port;
    }

    @Override
    public void send() {
        port.send(context.getTime());
    }

    @Override
    public void send(double value) {
        port.send(context.getTime(), value);
    }

    @Override
    public void send(Argument value) {
        port.send(context.getTime(), value);
    }
    
    static Descriptor createDescriptor(CodeConnector<?> connector,
            Out ann, Field field) {
        return createDescriptor(connector, PortDescriptor.Category.Out, ann.value(), field);
    }

    static Descriptor createDescriptor(CodeConnector<?> connector,
            AuxOut ann, Field field) {
        return createDescriptor(connector, PortDescriptor.Category.AuxOut, ann.value(), field);
    }
    
    private static Descriptor createDescriptor(CodeConnector<?> connector,
            PortDescriptor.Category category, int index, Field field) {
        if (!Output.class.isAssignableFrom(field.getType())) {
            return null;
        }
        String id = connector.findID(field);
        return new Descriptor(id, category, index, field);
    }
    
    static class Descriptor extends PortDescriptor {

        private ControlOutput port;
        private Field field;

        private Descriptor(String id, Category category, int index, Field field) {
            super(id, category, index);
            assert field != null;
            this.field = field;
        }

        @Override
        public void attach(CodeContext<?> context, Port previous) {
            if (previous instanceof ControlOutput) {
                port = (ControlOutput) previous;
            } else {
                if (previous != null) {
                    previous.disconnectAll();
                }
                port = new ControlOutput();
            }
            try {
                field.setAccessible(true);
                field.set(context.getDelegate(), new OutputImpl(context, port));
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
            } 
        }

        @Override
        public Port getPort() {
            return port;
        }

        @Override
        public PortInfo getInfo() {
            return ControlOutput.INFO;
        }

    }

}
