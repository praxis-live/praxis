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
package org.praxislive.code.services;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.code.CodeComponentFactoryService;
import org.praxislive.core.services.ComponentFactory;
import org.praxislive.core.services.ComponentFactoryProvider;
import org.praxislive.core.ComponentType;
import org.praxislive.core.Lookup;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class ComponentRegistry {

    private final static Logger logger
            = Logger.getLogger(ComponentRegistry.class.getName());
    private final Map<ComponentType, ComponentFactory> componentCache;

    private ComponentRegistry(Map<ComponentType, ComponentFactory> componentCache) {
        this.componentCache = componentCache;
    }

    ComponentType[] getComponentTypes() {
        Set<ComponentType> keys = componentCache.keySet();
        return keys.toArray(new ComponentType[keys.size()]);
    }

    ComponentFactory getComponentFactory(ComponentType type) {
        return componentCache.get(type);
    }

    static ComponentRegistry getInstance() {
        Map<ComponentType, ComponentFactory> componentCache
                = new LinkedHashMap<>();

        ComponentFactoryProvider[] providers =
                Lookup.SYSTEM.findAll(ComponentFactoryProvider.class)
                .toArray(ComponentFactoryProvider[]::new);
        for (ComponentFactoryProvider provider : providers) {
            ComponentFactory factory = provider.getFactory();
            if (factory.getFactoryService() == CodeComponentFactoryService.class) {
                logger.log(Level.INFO, "Adding components from : {0}", factory.getClass());
                for (ComponentType type : factory.getComponentTypes()) {
                    componentCache.put(type, factory);
                }
            }
        }
        return new ComponentRegistry(componentCache);
    }
}
