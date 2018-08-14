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
import java.util.stream.Stream;

/**
 *
 * @author Neil C Smith
 */
public interface Port {
 
    public final static String IN = "in";
    public final static String OUT = "out";
    
    public void connect(Port port) throws PortConnectionException;
    
    public void disconnect(Port port);
    
    public void disconnectAll();
    
    public Port[] getConnections();
    
    public void addListener(PortListener listener);

    public void removeListener(PortListener listener);
    
    public static class Type<T extends Port> {
        
        private final Class<T> baseClass;
        private final String name;
        
        public Type(Class<T> baseClass) {
            this.baseClass = Objects.requireNonNull(baseClass);
            this.name = baseClass.getSimpleName();
        }
        
        public Class<T> asClass() {
            return baseClass;
        }
        
        public String name() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int hashCode() {
            return baseClass.hashCode();
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
            return Objects.equals(this.baseClass, other.baseClass);
        }
        
        @SuppressWarnings("unchecked")
        public static <T extends Port> Type<T> of(Class<T> baseClass) {
            Type<T> type = (Type<T>) typesByClass.get(baseClass);
            if (type == null) {
                throw new IllegalArgumentException("Unregistered Port type : "
                        + baseClass.getName());
            }
            return type;
        }
        
        public static Optional<Type<? extends Port>> fromName(String name) {
            return Optional.ofNullable(typesByName.get(name));
        }
        
        private final static Map<Class<? extends Port>, Type<? extends Port>> typesByClass
                = new HashMap<>();
        private final static Map<String, Type<? extends Port>> typesByName
                = new HashMap<>();
        
        private static <T extends Port> void register(Type<T> type) {
            if (typesByClass.containsKey(type.asClass()) || typesByName.containsKey(type.name())) {
                throw new IllegalStateException("Already registered type");
            }
            typesByClass.put(type.asClass(), type);
            typesByName.put(type.name(), type);
        }
        
        static {
            
            register(new Type<>(ControlPort.class));
            
            Lookup.SYSTEM.findAll(TypeProvider.class)
                    .flatMap(TypeProvider::types)
                    .forEachOrdered(Type::register);
            
        }
        
        
    }
    
    public static interface TypeProvider {
        
        Stream<Type<?>> types();
        
    }
    
}
