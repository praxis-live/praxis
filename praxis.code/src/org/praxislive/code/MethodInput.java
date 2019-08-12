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
 *
 */
package org.praxislive.code;

import java.lang.reflect.Method;
import java.util.Optional;
import org.praxislive.code.userapi.AuxIn;
import org.praxislive.code.userapi.In;
import org.praxislive.core.ValueFormatException;
import org.praxislive.core.Port;
import org.praxislive.core.PortInfo;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.Value;
import org.praxislive.logging.LogLevel;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
abstract class MethodInput {

    final Method method;
    CodeContext<?> context;

    private MethodInput(Method method) {
        this.method = method;
    }

    private void attach(CodeContext<?> context) {
        this.context = context;
    }

    abstract void receive(long time, double value);

    abstract void receive(long time, Value value);

    void invoke(long time, final Object value) {
        context.invoke(time, method, value);
    }

//    static boolean isSuppportedType(Class<?> type) {
//        return type == double.class ||
//                type == String.class;
//    }
    static Descriptor createDescriptor(CodeConnector<?> connector,
            In ann, Method method) {
        return createDescriptor(connector, PortDescriptor.Category.In, ann.value(), method);
    }

    static Descriptor createDescriptor(CodeConnector<?> connector,
            AuxIn ann, Method method) {
        return createDescriptor(connector, PortDescriptor.Category.AuxIn, ann.value(), method);
    }

    private static Descriptor createDescriptor(CodeConnector<?> connector,
            PortDescriptor.Category category, int index, Method method) {
        method.setAccessible(true);
        Class<?>[] types = method.getParameterTypes();
        MethodInput input = null;
        if (types.length == 1) {
            Class<?> type = types[0];
            if (type == double.class) {
                input = new DoubleInput(method);
            } else if (type == int.class) {
                input = new IntInput(method);
            } else if (type == String.class) {
                input = new StringInput(method);
            } else if (Value.class.isAssignableFrom(type)) {
                input = new ValueInput((Class<Value>) type, method);
            }
        }
        if (input == null) {
            return null;
        }
        String id = connector.findID(method);
        return new Descriptor(id, category, index, input);
    }

    static class Descriptor extends PortDescriptor implements ControlInput.Link {

        private final MethodInput input;

        private ControlInput port;

        private Descriptor(String id, Category category, int index, MethodInput input) {
            super(id, category, index);
            this.input = input;
        }

        @Override
        public void attach(CodeContext<?> context, Port previous) {
            if (previous instanceof ControlInput) {
                port = (ControlInput) previous;
                port.setLink(this);
            } else {
                if (previous != null) {
                    previous.disconnectAll();
                }
                port = new ControlInput(this);
            }
            input.attach(context);
        }

        @Override
        public Port getPort() {
            assert port != null;
            return port;
        }

        @Override
        public PortInfo getInfo() {
            return ControlInput.INFO;
        }

        @Override
        public void receive(long time, double value) {
            input.receive(time, value);
        }

        @Override
        public void receive(long time, Value value) {
            input.receive(time, value);
        }

    }

    private static class DoubleInput extends MethodInput {

        private DoubleInput(Method method) {
            super(method);
        }

        @Override
        public void receive(long time, double value) {
            invoke(time, value);
        }

        @Override
        public void receive(long time, Value value) {
            try {
                invoke(time, PNumber.coerce(value).value());
            } catch (ValueFormatException ex) {
                invoke(time, 0.0);
            }
        }

    }

    private static class IntInput extends MethodInput {

        private IntInput(Method method) {
            super(method);
        }

        @Override
        public void receive(long time, double value) {
            invoke(time, (int) Math.round(value));
        }

        @Override
        public void receive(long time, Value value) {
            try {
                invoke(time, PNumber.coerce(value).toIntValue());
            } catch (ValueFormatException ex) {
                invoke(time, 0.0);
            }
        }

    }

    private static class StringInput extends MethodInput {

        private StringInput(Method method) {
            super(method);
        }

        @Override
        public void receive(long time, double value) {
            invoke(time, String.valueOf(value));
        }

        @Override
        public void receive(long time, Value value) {
            invoke(time, value.toString());
        }

    }

    private static class ValueInput extends MethodInput {

        private Value.Type<Value> type;
        
        private ValueInput(Class<Value> cls, Method method) {
            super(method);
            type = Value.Type.of(cls);
        }

        @Override
        void receive(long time, double value) {
            invoke(time, PNumber.of(value));
        }

        @Override
        void receive(long time, Value value) {
            Optional<Value> v = type.converter().apply(value);
            if (v.isPresent()) {
                invoke(time, v.get());
            } else {
                context.getLog().log(LogLevel.WARNING, 
                        "Invalid argument " + value  + 
                                " received by " + method.getName());
            }
        }

    }

}
