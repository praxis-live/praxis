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

package net.neilcsmith.praxis.hub;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactoryProvider;
import net.neilcsmith.praxis.core.ComponentInstantiationException;
import net.neilcsmith.praxis.core.ComponentType;
import net.neilcsmith.praxis.core.ComponentTypeNotFoundException;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Root;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class LookupComponentFactory implements ComponentFactory {
    
    private final static Logger logger = 
            Logger.getLogger(LookupComponentFactory.class.getName());

    private Map<ComponentType, ComponentFactory> componentCache;
    private Map<ComponentType, ComponentFactory> rootCache;

    private LookupComponentFactory(Map<ComponentType, ComponentFactory> componentCache,
            Map<ComponentType, ComponentFactory> rootCache) {
        this.componentCache = componentCache;
        this.rootCache = rootCache;
    }

    public ComponentType[] getComponentTypes() {
        Set<ComponentType> keys = componentCache.keySet();
        return keys.toArray(new ComponentType[keys.size()]);
    }

    public ComponentType[] getRootComponentTypes() {
        Set<ComponentType> keys = rootCache.keySet();
        return keys.toArray(new ComponentType[keys.size()]);
    }

    public Component createComponent(ComponentType type) throws ComponentTypeNotFoundException, ComponentInstantiationException {
        ComponentFactory factory = componentCache.get(type);
        if (factory != null) {
            return factory.createComponent(type);
        } else {
            throw new ComponentTypeNotFoundException();
        }
    }

    public Root createRootComponent(ComponentType type) throws ComponentTypeNotFoundException, ComponentInstantiationException {
        ComponentFactory factory = rootCache.get(type);
        if (factory != null) {
            return factory.createRootComponent(type);
        } else {
            throw new ComponentTypeNotFoundException();
        }
    }

    public ComponentType getTypeForClass(Class<? extends Component> clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public static LookupComponentFactory getInstance(Lookup lookup) {
        Map<ComponentType, ComponentFactory> componentCache =
                new LinkedHashMap<ComponentType, ComponentFactory>();
        Map<ComponentType, ComponentFactory> rootCache =
                new LinkedHashMap<ComponentType, ComponentFactory>();

        Lookup.Result<ComponentFactoryProvider> providers =
                lookup.getAll(ComponentFactoryProvider.class);
        for (ComponentFactoryProvider provider : providers) {
            ComponentFactory factory = provider.getFactory();
            logger.info("Adding components from : " + factory.getClass());
            for (ComponentType type : factory.getComponentTypes()) {
                componentCache.put(type, factory);
            }
            for (ComponentType type : factory.getRootComponentTypes()) {
                rootCache.put(type, factory);
            }
        }
        return new LookupComponentFactory(componentCache, rootCache);
    }


}
