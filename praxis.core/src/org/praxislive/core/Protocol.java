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
import org.praxislive.core.protocols.ComponentProtocol;
import org.praxislive.core.protocols.ContainerProtocol;
import org.praxislive.core.protocols.StartableProtocol;
import org.praxislive.core.services.ComponentFactoryService;
import org.praxislive.core.services.RootFactoryService;
import org.praxislive.core.services.RootManagerService;
import org.praxislive.core.services.ScriptService;
import org.praxislive.core.services.SystemManagerService;
import org.praxislive.core.services.TaskService;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
public interface Protocol {
    
    public Stream<String> controls();
    
    public ControlInfo getControlInfo(String control);
    
    public static class Type<T extends Protocol> {
        
        private final Class<T> cls;
        private final String name;
        
        public Type(Class<T> cls) {
            this.cls = Objects.requireNonNull(cls);
            this.name = cls.getSimpleName();
        }
        
        public Class<T> asClass() {
            return cls;
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
            return cls.hashCode();
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
            return Objects.equals(this.cls, other.cls);
        }
        
        @SuppressWarnings("unchecked")
        public static <T extends Protocol> Type<T> of(Class<T> cls) {
            Type<T> type = (Type<T>) typesByClass.get(cls);
            if (type == null) {
                throw new IllegalArgumentException("Unregistered Protocol type : " + cls.getName());
            }
            return type;
        }
        
        public static Optional<Type<? extends Protocol>> fromName(String name) {
            return Optional.ofNullable(typesByName.get(name));
        }
        
        private final static Map<Class<? extends Protocol>, Type<? extends Protocol>> typesByClass
                = new HashMap<>();
        private final static Map<String, Type<? extends Protocol>> typesByName
                = new HashMap<>();

        private static <T extends Protocol> void register(Type<T> type) {
            if (typesByClass.containsKey(type.asClass()) || typesByName.containsKey(type.name())) {
                throw new IllegalStateException("Already registered type");
            }
            typesByClass.put(type.asClass(), type);
            typesByName.put(type.name(), type);
        }
        
        static {
            
            register(new Type<>(ComponentProtocol.class));
            register(new Type<>(ContainerProtocol.class));
            register(new Type<>(StartableProtocol.class));
            
            register(new Type<>(ComponentFactoryService.class));
            register(new Type<>(RootFactoryService.class));
            register(new Type<>(RootManagerService.class));
            register(new Type<>(ScriptService.class));
            register(new Type<>(SystemManagerService.class));
            register(new Type<>(TaskService.class));
            
            Lookup.SYSTEM.findAll(TypeProvider.class)
                    .flatMap(TypeProvider::types)
                    .forEachOrdered(Type::register);
            
            
            
        }
        
    }
    
    public static interface TypeProvider {
        
        Stream<Type> types();
        
    }
    
}
