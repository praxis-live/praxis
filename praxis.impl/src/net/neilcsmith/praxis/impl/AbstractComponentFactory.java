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
 */
package net.neilcsmith.praxis.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentInstantiationException;
import net.neilcsmith.praxis.core.ComponentType;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Root;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AbstractComponentFactory implements ComponentFactory {

    private Map<ComponentType, MetaData<? extends Component>> componentMap;
    private Map<ComponentType, MetaData<? extends Root>> rootMap;

    protected AbstractComponentFactory() {
        componentMap = new LinkedHashMap<ComponentType, MetaData<? extends Component>>();
        rootMap = new LinkedHashMap<ComponentType, MetaData<? extends Root>>(1);
    }

    public ComponentType[] getComponentTypes() {
        Set<ComponentType> keys = componentMap.keySet();
        return keys.toArray(new ComponentType[keys.size()]);
    }

    public ComponentType[] getRootComponentTypes() {
        Set<ComponentType> keys = rootMap.keySet();
        return keys.toArray(new ComponentType[keys.size()]);
    }

    public Component createComponent(ComponentType type) throws ComponentInstantiationException {
        MetaData<? extends Component> data = componentMap.get(type);
        if (data == null) {
            throw new IllegalArgumentException();
        }
        try {
            Class<? extends Component> cl = data.getComponentClass();
            return cl.newInstance();
        } catch (Exception ex) {
            throw new ComponentInstantiationException(ex);
        }
    }

    public Root createRootComponent(ComponentType type) throws ComponentInstantiationException {
        MetaData<? extends Root> data = rootMap.get(type);
        if (data == null) {
            throw new IllegalArgumentException();
        }
        try {
            Class<? extends Root> cl = data.getComponentClass();
            return (Root) cl.newInstance();
        } catch (Exception ex) {
            throw new ComponentInstantiationException(ex);
        }
    }

    public ComponentFactory.MetaData<? extends Component> getMetaData(ComponentType type) {
        return componentMap.get(type);
    }
    
    public ComponentFactory.MetaData<? extends Root> getRootMetaData(ComponentType type) {
        return rootMap.get(type);
    }

    protected void addComponent(String type, Class<? extends Component> cls) {
        addComponent(type, data(cls));
    }
    
    protected void addComponent(String type, Data<? extends Component> info) {
        componentMap.put(ComponentType.create(type), info.toMetaData());
    }

    protected void addRoot(String type, Class<? extends Root> cls) {
        addRoot(type, data(cls));
    }
    
    protected void addRoot(String type, Data<? extends Root> info) {
        rootMap.put(ComponentType.create(type), info.toMetaData());
    }
    
    public static <T> Data<T> data(Class<T> cls) {
        return new Data<T>(cls);
    }

    private static class MetaDataImpl<T> extends ComponentFactory.MetaData<T> {

        private final Class<T> cls;
        private final boolean test;
        private final boolean deprecated;
        private final ComponentType replacement;
        private final Lookup lookup;

        private MetaDataImpl(Class<T> cls,
                boolean test,
                boolean deprecated,
                ComponentType replacement,
                Lookup lookup) {
            this.cls = cls;
            this.test = test;
            this.deprecated = deprecated;
            this.replacement = replacement;
            this.lookup = lookup;
        }

        @Override
        public Class<T> getComponentClass() {
            return cls;
        }

        @Override
        public boolean isTest() {
            return test;
        }

        @Override
        public boolean isDeprecated() {
            return deprecated;
        }

        @Override
        public ComponentType getReplacement() {
            return replacement;
        }

        @Override
        public Lookup getLookup() {
            return lookup == null ? super.getLookup() : lookup;
        }
        
        
        
    }
    
    public static class Data<T> {

        private final Class<T> cls;
        private boolean test;
        private boolean deprecated;
        private ComponentType replacement;
        private List<Object> lookupList;
        
        private Data(Class<T> cls) {
            this.cls = cls;
        }
        
        public Data<T> test() {
            test = true;
            return this;
        }
        
        public Data<T> deprecated() {
            deprecated = true;
            return this;
        }
        
        public Data<T> replacement(String type) {
            replacement = ComponentType.create(type);
            deprecated = true;
            return this;
        }
        
        public Data<T> add(Object obj) {
            if (lookupList == null) {
                lookupList = new ArrayList<Object>();
            }
            lookupList.add(obj);
            return this;
        }
        
        private MetaData<T> toMetaData() {
            return new MetaDataImpl<T>(cls, test, deprecated, replacement,
                    lookupList == null ? null : InstanceLookup.create(lookupList.toArray()));
        }
    }
}
