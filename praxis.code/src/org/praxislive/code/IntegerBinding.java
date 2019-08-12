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
import java.util.stream.IntStream;
import org.praxislive.code.userapi.Property;
import org.praxislive.code.userapi.Type;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.Value;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PMap;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
abstract class IntegerBinding extends PropertyControl.Binding {

    private final ArgumentInfo info;
    private final int min;
    private final int max;
    final int def;

    private IntegerBinding(ArgumentInfo info, int min, int max, int def) {
        this.info = info;
        this.min = min;
        this.max = max;
        this.def = def;
    }

    @Override
    public void set(Value value) throws Exception {
        PNumber n = PNumber.coerce(value);
        double d = n.value();
        if (d < min || d > max) {
            throw new IllegalArgumentException();
        }
        setImpl(n);
    }

    @Override
    public void set(double value) throws Exception {
        if (value < min || value > max) {
            throw new IllegalArgumentException();
        }
        setImpl(value);
    }

    abstract void setImpl(PNumber value) throws Exception;

    abstract void setImpl(double value) throws Exception;

    @Override
    public ArgumentInfo getArgumentInfo() {
        return info;
    }

    @Override
    public Value getDefaultValue() {
        return PNumber.of(def);
    }

    static boolean isBindableFieldType(Class<?> type) {
        return type == int.class;
    }

    static IntegerBinding create(CodeConnector<?> connector, Field field) {
        int min = PNumber.MIN_VALUE;
        int max = PNumber.MAX_VALUE;
        int def = 0;
        int[] suggested = {};
        Type.Integer ann = field.getAnnotation(Type.Integer.class);
        if (ann != null) {
            min = ann.min();
            max = ann.max();
            def = ann.def();
            suggested = ann.suggested();
        }
        PMap.Builder props = PMap.builder();
        props.put(PNumber.KEY_IS_INTEGER, true);
        if (min != PNumber.MIN_VALUE) {
            props.put(PNumber.KEY_MINIMUM, min);
        }
        if (max != PNumber.MAX_VALUE) {
            props.put(PNumber.KEY_MAXIMUM, max);
        }
        if (suggested.length > 0) {
            PArray vals = IntStream.of(suggested)
                    .mapToObj(PNumber::of)
                    .collect(PArray.collector());
            props.put(ArgumentInfo.KEY_SUGGESTED_VALUES, vals);
        }
        ArgumentInfo info = ArgumentInfo.of(PNumber.class, props.build());
        Class<?> type = field.getType();
        if (type == int.class) { // || type == Double.class) {
            return new IntField(field, info, min, max, def);
        } else if (Property.class.isAssignableFrom(type)) {
            return new NoField(info, min, max, def);
        } else {
            return null;
        }

    }

    static class NoField extends IntegerBinding {

        private int value;
        private PNumber last = PNumber.ZERO;

        private NoField(ArgumentInfo info, int min, int max, int def) {
            super(info, min, max, def);
            value = def;
        }

        @Override
        public Value get() {
            if (!last.isInteger() || last.value() != value) {
                last = PNumber.of(value);
            }
            return last;
        }

        @Override
        public double get(double def) {
            return value;
        }

        @Override
        void setImpl(PNumber value) throws Exception {
            this.value = value.toIntValue();
            last = value;
        }

        @Override
        void setImpl(double value) throws Exception {
            this.value = (int) Math.round(value);
        }

    }

    static class IntField extends IntegerBinding {

        private final Field field;
        private CodeDelegate delegate;
        private PNumber last = PNumber.ZERO;

        private IntField(Field field, ArgumentInfo info, int min, int max, int def) {
            super(info, min, max, def);
            this.field = field;
        }

        @Override
        protected void attach(CodeContext<?> context) {
            this.delegate = context.getDelegate();
            try {
                set(def);
            } catch (Exception ex) {
                // ignore
            }
        }

        @Override
        void setImpl(PNumber value) throws Exception {
            setImpl(value.toIntValue());
            last = value;
        }

        @Override
        void setImpl(double value) throws Exception {
            set((int) Math.round(value));
        }
        
        private void set(int value) throws Exception {
            field.setInt(delegate, value);
        }

        @Override
        public double get(double def) {
            return get((int) Math.round(def));
        }
        
        private int get(int def) {
            try {
                return field.getInt(delegate);
            } catch (Exception ex) {
                return def;
            }
        }

        @Override
        public Value get() {
            int value = get(0);
            if (!last.isInteger() || last.toIntValue() != value) {
                last = PNumber.of(value);
            }
            return last;

        }
    }
    
}
