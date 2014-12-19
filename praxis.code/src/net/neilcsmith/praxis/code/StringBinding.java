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
import java.util.LinkedHashSet;
import java.util.Set;
import net.neilcsmith.praxis.code.userapi.Type;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
abstract class StringBinding extends PropertyControl.Binding {

    private final Set<PString> allowed;
    private final PString mime;
    final PString def;

    private StringBinding(String mime, String def) {
        allowed = null;
        this.mime = mime == null ? PString.EMPTY : PString.valueOf(mime);
        this.def = def == null ? PString.EMPTY : PString.valueOf(def);
    }

    private StringBinding(String[] allowedValues, String def) {
        if (allowedValues.length == 0) {
            throw new IllegalArgumentException();
        }
        boolean foundDef = false;
        allowed = new LinkedHashSet<>(allowedValues.length);
        for (String s : allowedValues) {
            allowed.add(PString.valueOf(s));
            if (s.equals(def)) {
                foundDef = true;
            }
        }
        this.def = foundDef ? PString.valueOf(def)
                : PString.valueOf(allowedValues[0]);
        mime = PString.EMPTY;
    }

    @Override
    public void set(long time, Argument value) throws Exception {
        PString pstr = PString.coerce(value);
        if (allowed == null || allowed.contains(pstr)) {
            set(pstr);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void set(long time, double value) throws Exception {
        set(time, PNumber.valueOf(value));
    }

    abstract void set(PString value) throws Exception;

    @Override
    public ArgumentInfo getArgumentInfo() {
        PMap keys = PMap.EMPTY;
        if (allowed != null) {
            keys = PMap.create(PString.KEY_ALLOWED_VALUES, PArray.valueOf(allowed));
        } else if (!mime.isEmpty()) {
            keys = PMap.create(PString.KEY_MIME_TYPE, mime);
        }
        return ArgumentInfo.create(PString.class, keys);
    }

    @Override
    public Argument getDefaultValue() {
        return PString.valueOf(def);
    }

    static boolean isBindableFieldType(Class<?> type) {
        return type == String.class;
    }

    static StringBinding create(CodeConnector<?> connector, Field field) {
        String[] allowed = null;
        String mime = "";
        String def = "";
        Type.String ann = field.getAnnotation(Type.String.class);
        if (ann != null) {
            allowed = ann.allowed();
            mime = ann.mime();
            def = ann.def();
        }
        if (field.getType() == String.class) {
            if (allowed != null && allowed.length > 0) {
                return new StringField(field, allowed, def);
            } else {
                return new StringField(field, mime, def);
            }
        } else {
            if (allowed != null && allowed.length > 0) {
                return new NoField(allowed, def);
            } else {
                return new NoField(mime, def);
            }
        }
    }

    private static class NoField extends StringBinding {

        private PString value;

        private NoField(String mime, String def) {
            super(mime, def);
            value = this.def;
        }
        
        private NoField(String[] allowed, String def) {
            super(allowed, def);
            value = this.def;
        }
               
        void set(PString value) throws Exception {
            this.value = value;
        }

        @Override
        public Argument get() {
            return value;
        }
        
    }
    
    private static class StringField extends StringBinding {
        
        private final Field field;
        private CodeDelegate delegate;
        
        private StringField(Field field, String mime, String def) {
            super(mime, def);
            this.field = field;
        }
        
        private StringField(Field field, String[] allowed, String def) {
            super(allowed, def);
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
        void set(PString value) throws Exception {
            field.set(delegate, value.toString());
        }

        @Override
        public Argument get() {
            try {
                return PString.valueOf(field.get(delegate));
            } catch (Exception ex) {
                return PString.EMPTY;
            }
        }
        
    }

}
