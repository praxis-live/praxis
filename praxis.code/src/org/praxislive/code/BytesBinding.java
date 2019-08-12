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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.types.PBytes;
import org.praxislive.core.types.PMap;
import org.praxislive.core.Value;
import org.praxislive.logging.LogLevel;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
abstract class BytesBinding extends PropertyControl.Binding {

    @Override
    public void set(Value value) throws Exception {
        Optional<PBytes> bytes = PBytes.from(value);
        if (bytes.isPresent()) {
            setImpl(bytes.get());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void set(double value) throws Exception {
        throw new IllegalArgumentException();
    }

    abstract void setImpl(PBytes value) throws Exception;

    @Override
    public ArgumentInfo getArgumentInfo() {
        return ArgumentInfo.of(PBytes.class, PMap.EMPTY);
    }

    @Override
    public Value getDefaultValue() {
        return PBytes.EMPTY;
    }

    static boolean isBindableFieldType(Class<?> type) {
        return type == PBytes.class || type == List.class || Serializable.class.isAssignableFrom(type);
    }

    static BytesBinding create(CodeConnector<?> connector, Field field) {
       if (field.getType() == PBytes.class) {
           return new PBytesField(field);
       }else if (isSerializableList(field.getGenericType())) {
           return new ListField(field);
       } else if (isSerializableType(field.getGenericType())) {
           return new SerializableField(field);
       } else {
           return null;
       }
    }

    private static boolean isSerializableType(java.lang.reflect.Type type) {
        if (type instanceof Class) {
            return Serializable.class.isAssignableFrom((Class<?>) type);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parType = (ParameterizedType) type;
            if (isSerializableType(parType.getRawType())) {
                for (java.lang.reflect.Type actualType : parType.getActualTypeArguments()) {
                    if (!isSerializableType(actualType)) {
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    private static boolean isSerializableList(java.lang.reflect.Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parType = (ParameterizedType) type;
            if (List.class.equals(parType.getRawType())) {
                for (java.lang.reflect.Type actualType : parType.getActualTypeArguments()) {
                    if (!isSerializableType(actualType)) {
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    private static class PBytesField extends BytesBinding {
        
        private final Field field;
        private CodeDelegate delegate;
        
        private PBytesField(Field field) {
            this.field = field;
        }

        @Override
        protected void attach(CodeContext<?> context) {
            this.delegate = context.getDelegate();
            try {
                set(PBytes.EMPTY);
            } catch (Exception ex) {
                // ignore
            }
        }

        @Override
        void setImpl(PBytes value) throws Exception {
            field.set(delegate, value);
        }

        @Override
        public Value get() {
            try {
                return (PBytes) field.get(delegate);
            } catch (Exception ex) {
                return PBytes.EMPTY;
            }
        }
        
    }

    private static class SerializableField extends BytesBinding {

        private final Field field;
        private CodeDelegate delegate;

        private SerializableField(Field field) {
            this.field = field;
        }

        @Override
        protected void attach(CodeContext<?> context) {
            this.delegate = context.getDelegate();
            try {
                set(PBytes.EMPTY);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }

        @Override
        void setImpl(PBytes value) throws Exception {
            if (value.isEmpty()) {
                if (field.getType().isArray()) {
                    field.set(delegate, Array.newInstance(field.getType().getComponentType(), 0));
                } else {
                    field.set(delegate, field.getType().newInstance());
                }
            } else {
                field.set(delegate, new ObjectInputStream(value.asInputStream()).readObject());
            }
        }

        @Override
        public Value get() {
            try {
                PBytes.OutputStream bos = new PBytes.OutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(field.get(delegate));
                oos.close();
                return bos.toBytes();
            } catch (Exception ex) {
                return PBytes.EMPTY;
            }
        }

    }

    private static class ListField extends BytesBinding {

        private final Field field;
        private CodeDelegate delegate;

        private ListField(Field field) {
            this.field = field;
        }

        @Override
        protected void attach(CodeContext<?> context) {
            this.delegate = context.getDelegate();
            try {
                set(PBytes.EMPTY);
            } catch (Exception ex) {
                // ignore
            }
        }

        @Override
        void setImpl(PBytes value) throws Exception {
            List<?> list;
            if (value.isEmpty()) {
                list = new ArrayList<>();
            } else {
                list = (List<?>) new ObjectInputStream(value.asInputStream()).readObject();
            }
            field.set(delegate, list);
        }

        @Override
        public Value get() {
            try {
                List<?> list = (List<?>) field.get(delegate);
                if (list.isEmpty()) {
                    return PBytes.EMPTY;
                }
                if (!ArrayList.class.equals(list.getClass())) {
                    list = new ArrayList<>(list);
                }
                PBytes.OutputStream bos = new PBytes.OutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(list);
                oos.close();
                return bos.toBytes();
            } catch (Exception ex) {
                return PBytes.EMPTY;
            }
        }

    }

}
