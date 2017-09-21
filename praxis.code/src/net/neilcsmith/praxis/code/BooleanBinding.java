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
import net.neilcsmith.praxis.code.userapi.Type;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.types.PBoolean;
import net.neilcsmith.praxis.core.types.Value;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
abstract class BooleanBinding extends PropertyControl.Binding {

    final PBoolean def;

    private BooleanBinding(PBoolean def) {
        this.def = def;
    }

    @Override
    public void set(long time, Value value) throws Exception {
        set(PBoolean.coerce(value));
    }

    @Override
    public void set(long time, double value) throws Exception {
        set(value > 0.5 ? PBoolean.TRUE : PBoolean.FALSE);
    }

    abstract void set(PBoolean value) throws Exception;

    @Override
    public ArgumentInfo getArgumentInfo() {
        return PBoolean.info();
    }

    @Override
    public Value getDefaultValue() {
        return def;
    }

    static boolean isBindableFieldType(Class<?> type) {
        return type == boolean.class;
    }

    static BooleanBinding create(CodeConnector<?> connector, Field field) {
        PBoolean def = PBoolean.FALSE;
        Type.Boolean ann = field.getAnnotation(Type.Boolean.class);
        if (ann != null) {
            def = PBoolean.valueOf(ann.def());
        }
        if (field.getType() == boolean.class) {
            return new BooleanField(field, def);
        } else {
            return new NoField(def);
        }
    }

    private static class NoField extends BooleanBinding {

        private PBoolean value;

        private NoField(PBoolean def) {
            super(def);
            this.value = def;
        }

        @Override
        void set(PBoolean value) throws Exception {
            this.value = value;
        }

        @Override
        public Value get() {
            return value;
        }

    }

    private static class BooleanField extends BooleanBinding {

        private final Field field;
        private CodeDelegate delegate;

        private BooleanField(Field field, PBoolean def) {
            super(def);
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
        void set(PBoolean value) throws Exception {
            field.setBoolean(delegate, value.value());
        }

        @Override
        public Value get() {
            try {
                return PBoolean.valueOf(field.getBoolean(delegate));
            } catch (Exception ex) {
                return PBoolean.FALSE;
            }
        }

    }

}
