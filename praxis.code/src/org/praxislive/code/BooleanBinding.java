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
import org.praxislive.code.userapi.Type;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.types.PBoolean;
import org.praxislive.core.Value;

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
    public void set(Value value) throws Exception {
        setImpl(PBoolean.coerce(value));
    }

    @Override
    public void set(double value) throws Exception {
        set(value > 0.5 ? PBoolean.TRUE : PBoolean.FALSE);
    }

    abstract void setImpl(PBoolean value) throws Exception;

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
            def = PBoolean.of(ann.def());
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
        void setImpl(PBoolean value) throws Exception {
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
        void setImpl(PBoolean value) throws Exception {
            field.setBoolean(delegate, value.value());
        }

        @Override
        public Value get() {
            try {
                return PBoolean.of(field.getBoolean(delegate));
            } catch (Exception ex) {
                return PBoolean.FALSE;
            }
        }

    }

}
