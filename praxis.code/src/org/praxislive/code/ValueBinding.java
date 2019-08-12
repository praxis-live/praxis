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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.Value;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PString;
import org.praxislive.logging.LogLevel;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
abstract class ValueBinding extends PropertyControl.Binding {

    final Value.Type<Value> type;
    final Value defaultValue;

    private ValueBinding(Value.Type<Value> type, Value defaultValue) {
        this.type = type;
        this.defaultValue = defaultValue;
    }

    @Override
    public void set(double value) throws Exception {
        set(PNumber.of(value));
    }

    @Override
    public ArgumentInfo getArgumentInfo() {
        return ArgumentInfo.of(type.asClass());
    }

    @Override
    public Value getDefaultValue() {
        return defaultValue;
    }

    static boolean isBindableFieldType(Class<?> cls) {
        return Value.class.isAssignableFrom(cls) || Optional.class.isAssignableFrom(cls);
    }

    static ValueBinding create(CodeConnector<?> connector, Field field) {
        Class<?> cls = field.getType();

        // Value field 
        if (Value.class.isAssignableFrom(cls)) {
            Value.Type<Value> type = Value.Type.of((Class<Value>) cls);
            Optional<Value> def = type.converter().apply(PString.EMPTY);
            if (!def.isPresent()) {
                connector.getLog().log(LogLevel.WARNING,
                        "Type " + cls.getSimpleName()
                        + " does not support default value, consider Optional<"
                        + cls.getSimpleName() + ">");
            }
            return new ValueField(type, def.orElse(PString.EMPTY), field);

            // Optional field    
        } else if (Optional.class.isAssignableFrom(cls)) {

            try {
                ParameterizedType parType = (ParameterizedType) field.getGenericType();
                Value.Type<Value> type = Value.Type.of(
                        ((Class<Value>) parType.getActualTypeArguments()[0])
                );
                Value def = type.converter().apply(PString.EMPTY).orElse(PString.EMPTY);
                return new OptionalField(type, def, field);
            } catch (Exception ex) {
                // fall through to null - can we log here? - might be other Optional?
            }
        }

        return null;

    }

    private static class ValueField extends ValueBinding {

        private final Field field;
        private CodeDelegate delegate;

        public ValueField(Value.Type<Value> type, Value defaultValue, Field field) {
            super(type, defaultValue);
            this.field = field;
        }

        @Override
        protected void attach(CodeContext<?> context) {
            delegate = context.getDelegate();
            try {
                field.set(delegate,
                        type.converter().apply(PString.EMPTY).orElse(null));
            } catch (Exception ex) {
                // ignore
            }
        }

        @Override
        public void set(Value value) throws Exception {
            if (value.isEmpty()) {
                field.set(delegate, type.converter().apply(value)
                        .orElse(null));
            } else {
                field.set(delegate, type.converter().apply(value)
                        .orElseThrow(IllegalArgumentException::new));
            }
        }

        @Override
        public Value get() {
            try {
                Value v = (Value) field.get(delegate);
                return v == null ? PString.EMPTY : v;
            } catch (Exception ex) {
                return PString.EMPTY;
            }
        }

    }

    private static class OptionalField extends ValueBinding {

        private final Field field;
        private CodeDelegate delegate;

        public OptionalField(Value.Type<Value> type, Value defaultValue, Field field) {
            super(type, defaultValue);
            this.field = field;
        }

        @Override
        protected void attach(CodeContext<?> context) {
            delegate = context.getDelegate();
            try {
                field.set(delegate,
                        Optional.empty());
            } catch (Exception ex) {
                // ignore
            }
        }

        @Override
        public void set(Value value) throws Exception {
            field.set(delegate, type.converter().apply(value));
        }

        @Override
        public Value get() {
            try {
                Optional<Value> opt = (Optional<Value>) field.get(delegate);
                return opt.orElse(defaultValue);
            } catch (Exception ex) {
                return defaultValue;
            }
        }

        @Override
        public ArgumentInfo getArgumentInfo() {
            return defaultValue.isEmpty() ?
                    ArgumentInfo.of(type.asClass(),
                            PMap.of(ArgumentInfo.KEY_EMPTY_IS_DEFAULT, true)) :
                    ArgumentInfo.of(type.asClass());
        }

    }

}
