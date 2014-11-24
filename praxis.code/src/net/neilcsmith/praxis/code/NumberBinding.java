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
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.code.userapi.Property;
import net.neilcsmith.praxis.code.userapi.Type;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.types.PNumber;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
abstract class NumberBinding extends PropertyControl.Binding {

    private final double min;
    private final double max;
    final double def;
    private final boolean ranged;

    private NumberBinding(double min, double max, double def) {
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
        return type == double.class || type == float.class;
//                || type == Double.class || type == Float.class;
    }

    static NumberBinding create(CodeConnector<?> connector, Field field) {
        double min = PNumber.MIN_VALUE;
        double max = PNumber.MAX_VALUE;
        double def = 0;
        Type.Number ann = field.getAnnotation(Type.Number.class);
        if (ann != null) {
            min = ann.min();
            max = ann.max();
            def = ann.def();
        }
        Class<?> type = field.getType();
        if (type == double.class) { // || type == Double.class) {
            return new DoubleField(field, min, max, def);
        } else if (Property.class.isAssignableFrom(type)) {
            return new NoField(min, max, def);
        } else {
            return null;
        }

    }

    static class NoField extends NumberBinding {

        private double value;
        private PNumber last = PNumber.ZERO;

        public NoField(double min, double max, double def) {
            super(min, max, def);
            value = def;
        }

        @Override
        public Argument get() {
            if (last.value() != value) {
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
            set(value.value());
            last = value;
        }

        @Override
        void set(double value) throws Exception {
            this.value = value;
        }

    }

    static class DoubleField extends NumberBinding {

        private final Field field;
        private CodeDelegate delegate;
        private PNumber last;

        public DoubleField(Field field, double min, double max, double def) {
            super(min, max, def);
            this.field = field;
        }

        @Override
        protected void attach(CodeDelegate delegate) {
            this.delegate = delegate;
            try {
                set(def);
            } catch (Exception ex) {
                // ignore
            }
        }

        @Override
        void set(PNumber value) throws Exception {
            set(value.value());
            last = value;
        }

        @Override
        void set(double value) throws Exception {
            field.setDouble(delegate, value);
        }

        @Override
        public double get(double def) {
            try {
                return field.getDouble(delegate);
            } catch (Exception ex) {
                return def;
            }
        }

        @Override
        public Argument get() {
            double value = get(0);
            if (last.value() != value) {
                last = PNumber.valueOf(value);
            }
            return last;

        }
    }

}
