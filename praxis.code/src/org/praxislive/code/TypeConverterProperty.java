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
import java.lang.reflect.Method;
import org.praxislive.code.userapi.OnChange;
import org.praxislive.code.userapi.OnError;
import org.praxislive.code.userapi.P;
import org.praxislive.core.Value;
import org.praxislive.core.CallArguments;
import org.praxislive.core.Control;
import org.praxislive.core.Port;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.PortInfo;
import org.praxislive.core.services.TaskService;
import org.praxislive.core.types.PError;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PReference;
import org.praxislive.core.types.PString;
import org.praxislive.logging.LogLevel;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class TypeConverterProperty<T> extends AbstractAsyncProperty<T> {

    private final TypeConverter<T> converter;
    private final ControlInfo info;

    private Field field;
    private Method onChange;
    private Method onError;
    private CodeContext<?> context;

    private TypeConverterProperty(TypeConverter<T> converter, ControlInfo info) {
        super(PString.EMPTY, converter.getType(), null);
        this.converter = converter;
        this.info = info;

    }

    private void attach(CodeContext<?> context,
            Field field, Method onChange, Method onError) {
        super.attach(context);
        this.context = context;
        this.field = field;
        try {
            field.set(context.getDelegate(), getValue());
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            context.getLog().log(LogLevel.WARNING, ex);
        }
        this.onChange = onChange;
        this.onError = onError;
    }

    @Override
    protected TaskService.Task createTask(CallArguments keys) throws Exception {
        return new Task(converter, keys.get(0));
    }

    @Override
    protected void valueChanged(long time) {
        try {
            field.set(context.getDelegate(), getValue());
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            context.getLog().log(LogLevel.ERROR, ex);
        }
        if (onChange != null) {
            context.invoke(time, onChange);
        }
    }

    @Override
    protected void taskError(long time, PError error) {
        if (onError != null) {
            context.invoke(time, onError);
        }
    }

    private final static class Task implements TaskService.Task {

        private final TypeConverter converter;
        private final Value key;

        private Task(TypeConverter converter, Value key) {
            this.converter = converter;
            this.key = key;
        }

        @Override
        public Value execute() throws Exception {
            return PReference.of(converter.fromArgument(key));
        }

    }

    public static class Descriptor<T> extends ControlDescriptor {

        private final TypeConverter<T> converter;
        private final Field field;
        private final Method onChange, onError;
        private final ControlInfo info;

        private TypeConverterProperty<T> control;

        private Descriptor(
                String id,
                int index,
                Field field,
                TypeConverter<T> converter,
                Method onChange,
                Method onError
        ) {
            super(id, ControlDescriptor.Category.Property, index);
            this.converter = converter;
            this.field = field;
            this.onChange = onChange;
            this.onError = onError;
            info = ControlInfo.createPropertyInfo(
                    new ArgumentInfo[]{converter.getInfo()},
                    new Value[]{PString.EMPTY}, PMap.EMPTY);
        }

        @Override
        public ControlInfo getInfo() {
            return info;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void attach(CodeContext<?> context, Control previous) {
            if (previous instanceof TypeConverterProperty
                    && ((TypeConverterProperty) previous).converter.getType() == converter.getType()) {
                control = (TypeConverterProperty<T>) previous;
            } else {
                control = new TypeConverterProperty<>(converter, info);
            }
            control.attach(context, field, onChange, onError);
        }

        @Override
        public Control getControl() {
            return control;
        }

        public PortDescriptor createPortDescriptor() {
            return new PortDescImpl(getID(), getIndex(), this);
        }

        public static <T> Descriptor<T> create(CodeConnector<?> connector, P ann,
                Field field, TypeConverter<T> converter) {
            if (!field.getType().isAssignableFrom(converter.getType())) {
                return null;
            }
            field.setAccessible(true);
            String id = connector.findID(field);
            int index = ann.value();
            Method onChange = null;
            Method onError = null;
            OnChange onChangeAnn = field.getAnnotation(OnChange.class);
            if (onChangeAnn != null) {
                onChange = extractMethod(connector, onChangeAnn.value());
            }
            OnError onErrorAnn = field.getAnnotation(OnError.class);
            if (onErrorAnn != null) {
                onError = extractMethod(connector, onErrorAnn.value());
            }
            return new Descriptor<>(id, index, field, converter, onChange, onError);
        }

        private static Method extractMethod(CodeConnector<?> connector, String methodName) {
            try {
                Method m = connector.getDelegate().getClass().getDeclaredMethod(methodName);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException | SecurityException ex) {
                connector.getLog().log(LogLevel.WARNING, ex);
                return null;
            }
        }

    }

    private static class PortDescImpl extends PortDescriptor implements ControlInput.Link {

        private final Descriptor<?> dsc;

        private ControlInput port;

        private PortDescImpl(String id, int index, Descriptor<?> dsc) {
            super(id, PortDescriptor.Category.Property, index);
            this.dsc = dsc;
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
            receive(time, PNumber.of(value));
        }

        @Override
        public void receive(long time, Value value) {
            dsc.control.portInvoke(time, value);
        }

    }

}
