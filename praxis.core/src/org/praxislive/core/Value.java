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
package org.praxislive.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PBoolean;
import org.praxislive.core.types.PBytes;
import org.praxislive.core.types.PError;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PReference;
import org.praxislive.core.types.PResource;
import org.praxislive.core.types.PString;

/**
 * Abstract base class of all types used in messaging inside Praxis CORE.
 * 
 * All Value sub-types are guaranteed to be immutable.
 *
 * @author Neil C Smith (http://www.neilcsmith.net)
 */
public abstract class Value {

    /**
     * Values must override the default method to return a string representation
     * that is immutable.
     *
     * @return String representation
     */
    @Override
    public abstract String toString();

    /**
     * Values must override the default hashcode method.
     *
     * @return int hashcode
     */
    @Override
    public abstract int hashCode();

    /**
     * Values must override the default equals method. This method should only
     * return <code>true</code> if the supplied Object is of the same type as
     * the implementing Value. Values of an unknown type should be coerced
     * before calling this method. This method does not have to guarantee that
     * <code>this.equals(that) == this.toString().equals(that.toString())</code>
     *
     * @param obj
     * @return boolean
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * Check whether this Value is an empty value and has a zero length string
     * representation. Subclasses may wish to override this for efficiency if
     * the String representation is lazily created.
     *
     * @return boolean true if empty
     */
    public boolean isEmpty() {
        return (toString().length() == 0);
    }
    
    public boolean equivalent(Value value) {
        return this == value || this.toString().equals(value.toString());
    }
    
    @SuppressWarnings("unchecked")
    public Type<?> type() {
        Class<?> cls = getClass();
        Type<?> type;
        while ((type = Type.typesByClass.get(cls)) == null) {
            cls = cls.getSuperclass();
            if (cls == null) {
                throw new IllegalStateException();
            }
        }
        return type;
    } 
    
    /**
     * Use this method to return an ArgumentInfo argument that can be used to refer
     * to ANY Value subclass. Usually, you will want to get an ArgumentInfo object
     * directly from a specific Value subclass.
     *
     * @return ArgumentInfo info
     */
    public static ArgumentInfo info() {
        return ArgumentInfo.of(Value.class, null);
    }
    
    public static class Type<T extends Value> {
        
        private final Class<T> type;
        private final String name;
        private final Function<Value, Optional<T>> converter;
        
        Type(Class<T> type, Function<Value, Optional<T>> converter) {
            this(type, type.getSimpleName(), converter);
        }
        
        Type(Class<T> type, String name, Function<Value, Optional<T>> converter) {
            this.type = Objects.requireNonNull(type);
            this.name = Objects.requireNonNull(name);
            this.converter = Objects.requireNonNull(converter);
        }
        
        public Class<T> asClass() {
            return type;
        }
        
        public String name() {
            return name;
        }
        
        public Function<Value, Optional<T>> converter() {
            return converter;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int hashCode() {
            return type.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Type<?> other = (Type<?>) obj;
            if (!Objects.equals(this.type, other.type)) {
                return false;
            }
            return true;
        }
        
        @SuppressWarnings("unchecked")
        public static <T extends Value> Type<T> of(Class<T> cls) {
            Type<T> type = (Type<T>) typesByClass.get(cls);
            if (type == null) {
                throw new IllegalArgumentException("Unregistered Value type : " + cls.getName());
            }
            return type;
        }
        
        public static Optional<Type<? extends Value>> fromName(String name) {
            return Optional.ofNullable(typesByName.get(name));
        }

        private final static Map<Class<? extends Value>, Type<? extends Value>> typesByClass
                = new HashMap<>();
        private final static Map<String, Type<? extends Value>> typesByName
                = new HashMap<>();

        private static <T extends Value> void register(Type<T> type) {
            if (typesByClass.containsKey(type.asClass()) || typesByName.containsKey(type.name())) {
                throw new IllegalStateException("Already registered type");
            }
            typesByClass.put(type.asClass(), type);
            typesByName.put(type.name(), type);
        }

        static {
            
            register(new Type<>(Value.class, v -> Optional.of(v)));
            
            register(new Type<>(PArray.class, "Array", PArray::from));
            register(new Type<>(PBoolean.class, "Boolean", PBoolean::from));
            register(new Type<>(PBytes.class, "Bytes", PBytes::from));
            register(new Type<>(PError.class, "Error", PError::from));
            register(new Type<>(PMap.class, "Map", PMap::from));
            register(new Type<>(PNumber.class, "Number", PNumber::from));
            register(new Type<>(PReference.class, "Reference", PReference::from));
            register(new Type<>(PResource.class, "Resource", PResource::from));
            register(new Type<>(PString.class, "String", PString::from));
            
            register(new Type<>(ArgumentInfo.class, ArgumentInfo::from));
            register(new Type<>(ComponentInfo.class, ComponentInfo::from));
            register(new Type<>(ControlInfo.class, ControlInfo::from));
            register(new Type<>(PortInfo.class, PortInfo::from));
            
            register(new Type<>(ComponentAddress.class, ComponentAddress::from));
            register(new Type<>(ControlAddress.class, ControlAddress::from));
            register(new Type<>(PortAddress.class, PortAddress::from));
            
            register(new Type<>(ComponentType.class, ComponentType::from));

        }
    }

}
