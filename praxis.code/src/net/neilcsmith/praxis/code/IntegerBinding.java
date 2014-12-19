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
 *
 */
package net.neilcsmith.praxis.code;

import java.lang.reflect.Field;
import net.neilcsmith.praxis.code.userapi.Property;
import net.neilcsmith.praxis.code.userapi.Type;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.types.PNumber;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
abstract class IntegerBinding extends PropertyControl.Binding {

    private final int min;
    private final int max;
    final int def;
    private final boolean ranged;

    private IntegerBinding(int min, int max, int def) {
        this.min = min;
        this.max = max;
        this.def = def;
        ranged = (min != PNumber.MIN_VALUE
                || max != PNumber.MAX_VALUE);
    }

    @Override
    public void set(long time, Argument value) throws Exception {
        PNumber n = PNumber.coerce(value);
        double d = n.value();
        if (d < min || d > max) {
            throw new IllegalArgumentException();
        }
        set(n);
    }

    @Override
    public void set(long time, double value) throws Exception {
        if (value < min || value > max) {
            throw new IllegalArgumentException();
        }
        set(value);
    }

    abstract void set(PNumber value) throws Exception;

    abstract void set(double value) throws Exception;

    @Override
    public ArgumentInfo getArgumentInfo() {
        if (ranged) {
            return PNumber.info(min, max);
        } else {
            return PNumber.info();
        }
    }

    @Override
    public Argument getDefaultValue() {
        return PNumber.valueOf(def);
    }

    static boolean isBindableFieldType(Class<?> type) {
        return type == int.class;
//                || type == Double.class || type == Float.class;
    }

    static IntegerBinding create(CodeConnector<?> connector, Field field) {
        int min = PNumber.MIN_VALUE;
        int max = PNumber.MAX_VALUE;
        int def = 0;
        Type.Integer ann = field.getAnnotation(Type.Integer.class);
        if (ann != null) {
            min = ann.min();
            max = ann.max();
            def = ann.def();
        }
        Class<?> type = field.getType();
        if (type == double.class) { // || type == Double.class) {
            return new IntField(field, min, max, def);
        } else if (Property.class.isAssignableFrom(type)) {
            return new NoField(min, max, def);
        } else {
            return null;
        }

    }

    static class NoField extends IntegerBinding {

        private int value;
        private PNumber last = PNumber.ZERO;

        public NoField(int min, int max, int def) {
            super(min, max, def);
            value = def;
        }

        @Override
        public Argument get() {
            if (!last.isInteger() || last.value() != value) {
                last = PNumber.valueOf(value);
            }
            return last;
        }

        @Override
        public double get(double def) {
            return value;
        }

        @Override
        void set(PNumber value) throws Exception {
            this.value = value.toIntValue();
            last = value;
        }

        @Override
        void set(double value) throws Exception {
            this.value = (int) Math.round(value);
        }

    }

    static class IntField extends IntegerBinding {

        private final Field field;
        private CodeDelegate delegate;
        private PNumber last = PNumber.ZERO;

        public IntField(Field field, int min, int max, int def) {
            super(min, max, def);
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
        void set(PNumber value) throws Exception {
            set(value.toIntValue());
            last = value;
        }

        @Override
        void set(double value) throws Exception {
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
        public Argument get() {
            int value = get(0);
            if (!last.isInteger() || last.toIntValue() != value) {
                last = PNumber.valueOf(value);
            }
            return last;

        }
    }
    
}
