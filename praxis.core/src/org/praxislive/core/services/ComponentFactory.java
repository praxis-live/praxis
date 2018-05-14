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
package org.praxislive.core.services;

import java.util.Optional;
import org.praxislive.core.Component;
import org.praxislive.core.ComponentType;
import org.praxislive.core.Lookup;
import org.praxislive.core.Root;

/**
 *
 * @author Neil C Smith
 */
public interface ComponentFactory {

    public ComponentType[] getComponentTypes();

    public ComponentType[] getRootComponentTypes();

    public MetaData<? extends Component> getMetaData(ComponentType type);

    public MetaData<? extends Root> getRootMetaData(ComponentType type);
    
    public default Component createComponent(ComponentType type) throws ComponentInstantiationException {
        throw new ComponentInstantiationException();
    }

    public default Root createRootComponent(ComponentType type) throws ComponentInstantiationException {
        throw new ComponentInstantiationException();
    }
    
    public default Class<? extends ComponentFactoryService> getFactoryService() {
        return ComponentFactoryService.class;
    }
    
    public default Class<? extends RootFactoryService> getRootFactoryService() {
        return RootFactoryService.class;
    }
    
    public static abstract class MetaData<T> {

        public boolean isDeprecated() {
            return false;
        }

        public Optional<ComponentType> findReplacement() {
            return Optional.empty();
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }
}
