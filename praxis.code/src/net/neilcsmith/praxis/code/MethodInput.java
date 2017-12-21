/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Neil C Smith.
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
 *
 */
package net.neilcsmith.praxis.code;

import java.lang.reflect.Method;
import net.neilcsmith.praxis.code.userapi.AuxIn;
import net.neilcsmith.praxis.code.userapi.In;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.PortInfo;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.core.types.Value;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
abstract class MethodInput {

    private final Method method;
    private CodeContext<?> context;

    private MethodInput(Method method) {
        this.method = method;
    }

    private void attach(CodeContext<?> context) {
        this.context = context;
    }

    abstract void receive(long time, double value);

    abstract void receive(long time, Argument value);

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
            } else if (type == Value.class || type == Argument.class) {
                input = new ValueInput(method);
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
        public void receive(long time, Argument value) {
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
        public void receive(long time, Argument value) {
            try {
                invoke(time, PNumber.coerce(value).value());
            } catch (ArgumentFormatException ex) {
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
        public void receive(long time, Argument value) {
            try {
                invoke(time, PNumber.coerce(value).toIntValue());
            } catch (ArgumentFormatException ex) {
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
        public void receive(long time, Argument value) {
            invoke(time, value.toString());
        }

    }

    private static class ValueInput extends MethodInput {

        private ValueInput(Method method) {
            super(method);
        }

        @Override
        void receive(long time, double value) {
            invoke(time, PNumber.valueOf(value));
        }

        @Override
        void receive(long time, Argument value) {
            Value v = value instanceof Value ? (Value) value : PString.valueOf(value);
            invoke(time, v);
        }

    }

}
