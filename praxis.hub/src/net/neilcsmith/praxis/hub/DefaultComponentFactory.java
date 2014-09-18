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
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactoryProvider;
import net.neilcsmith.praxis.core.ComponentInstantiationException;
import net.neilcsmith.praxis.core.ComponentType;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Root;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class DefaultComponentFactory implements ComponentFactory {

    private final static Logger logger =
            Logger.getLogger(DefaultComponentFactory.class.getName());
    private Map<ComponentType, ComponentFactory> componentCache;
    private Map<ComponentType, ComponentFactory> rootCache;

    private DefaultComponentFactory(Map<ComponentType, ComponentFactory> componentCache,
            Map<ComponentType, ComponentFactory> rootCache) {
        this.componentCache = componentCache;
        this.rootCache = rootCache;
    }

    @Override
    public ComponentType[] getComponentTypes() {
        Set<ComponentType> keys = componentCache.keySet();
        return keys.toArray(new ComponentType[keys.size()]);
    }

    @Override
    public ComponentType[] getRootComponentTypes() {
        Set<ComponentType> keys = rootCache.keySet();
        return keys.toArray(new ComponentType[keys.size()]);
    }

    @Override
    public MetaData<? extends Component> getMetaData(ComponentType type) {
        ComponentFactory factory = componentCache.get(type);
        if (factory != null) {
            return factory.getMetaData(type);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public MetaData<? extends Root> getRootMetaData(ComponentType type) {
        ComponentFactory factory = rootCache.get(type);
        if (factory != null) {
            return factory.getRootMetaData(type);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Component createComponent(ComponentType type) throws ComponentInstantiationException {
        ComponentFactory factory = componentCache.get(type);
        if (factory != null) {
            return factory.createComponent(type);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Root createRootComponent(ComponentType type) throws ComponentInstantiationException {
        ComponentFactory factory = rootCache.get(type);
        if (factory != null) {
            return factory.createRootComponent(type);
        } else {
            throw new IllegalArgumentException();
        }
    }


    public static DefaultComponentFactory getInstance() {
        Map<ComponentType, ComponentFactory> componentCache =
                new LinkedHashMap<>();
        Map<ComponentType, ComponentFactory> rootCache =
                new LinkedHashMap<>();

        Lookup.Result<ComponentFactoryProvider> providers =
                Lookup.SYSTEM.getAll(ComponentFactoryProvider.class);
        for (ComponentFactoryProvider provider : providers) {
            ComponentFactory factory = provider.getFactory();
            logger.log(Level.INFO, "Adding components from : {0}", factory.getClass());
            for (ComponentType type : factory.getComponentTypes()) {
                componentCache.put(type, factory);
            }
            for (ComponentType type : factory.getRootComponentTypes()) {
                rootCache.put(type, factory);
            }
        }
        return new DefaultComponentFactory(componentCache, rootCache);
    }
}
