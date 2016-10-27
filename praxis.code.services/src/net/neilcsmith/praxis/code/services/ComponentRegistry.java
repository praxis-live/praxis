/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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
 */
package net.neilcsmith.praxis.code.services;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.code.CodeComponentFactoryService;
import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactoryProvider;
import net.neilcsmith.praxis.core.ComponentType;
import net.neilcsmith.praxis.core.Lookup;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class ComponentRegistry {

    private final static Logger logger
            = Logger.getLogger(ComponentRegistry.class.getName());
    private final Map<ComponentType, ComponentFactory> componentCache;
//    private final Map<ComponentType, ComponentFactory> rootCache;

    private ComponentRegistry(Map<ComponentType, ComponentFactory> componentCache) {
        this.componentCache = componentCache;
    }

    ComponentType[] getComponentTypes() {
        Set<ComponentType> keys = componentCache.keySet();
        return keys.toArray(new ComponentType[keys.size()]);
    }

//    ComponentType[] getRootComponentTypes() {
//        Set<ComponentType> keys = rootCache.keySet();
//        return keys.toArray(new ComponentType[keys.size()]);
//    }
    ComponentFactory getComponentFactory(ComponentType type) {
        return componentCache.get(type);
    }

//    ComponentFactory getRootComponentFactory(ComponentType type) {
//        return rootCache.get(type);
//    }
    static ComponentRegistry getInstance() {
        Map<ComponentType, ComponentFactory> componentCache
                = new LinkedHashMap<>();

        Lookup.Result<ComponentFactoryProvider> providers
                = Lookup.SYSTEM.getAll(ComponentFactoryProvider.class);
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
