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
package org.praxislive.code;

import java.lang.reflect.Field;
import org.praxislive.code.userapi.AuxOut;
import org.praxislive.code.userapi.Out;
import org.praxislive.code.userapi.Output;
import org.praxislive.core.Value;
import org.praxislive.core.Port;
import org.praxislive.core.PortInfo;
import org.praxislive.logging.LogLevel;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class OutputImpl extends Output {
    
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
    public void send(Value value) {
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
                context.getLog().log(LogLevel.ERROR, ex);
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
