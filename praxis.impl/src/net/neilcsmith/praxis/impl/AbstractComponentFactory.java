/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 - Neil C Smith. All rights reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */

package net.neilcsmith.praxis.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentInstantiationException;
import net.neilcsmith.praxis.core.ComponentType;
import net.neilcsmith.praxis.core.ComponentTypeNotFoundException;
import net.neilcsmith.praxis.core.Root;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AbstractComponentFactory implements ComponentFactory {
    
    private Map<ComponentType, Class<? extends Component>> componentMap;
    private Map<ComponentType, Class<? extends Root>> rootMap;

    protected AbstractComponentFactory() {
        componentMap = new LinkedHashMap<ComponentType, Class<? extends Component>>();
        rootMap = new LinkedHashMap<ComponentType, Class<? extends Root>>(1);
    }

    public ComponentType[] getComponentTypes() {
        Set<ComponentType> keys = componentMap.keySet();
        return keys.toArray(new ComponentType[keys.size()]);
    }

    public ComponentType[] getRootComponentTypes() {
        Set<ComponentType> keys = rootMap.keySet();
        return keys.toArray(new ComponentType[keys.size()]);
    }

    public Component createComponent(ComponentType type) throws
            ComponentTypeNotFoundException, ComponentInstantiationException {
        Class<? extends Component> cl = componentMap.get(type);
        if (cl == null) {
            throw new ComponentTypeNotFoundException();
        }
        try {
            return cl.newInstance();
        } catch (Exception ex) {
            throw new ComponentInstantiationException(ex);
        }
    }

    public Root createRootComponent(ComponentType type) throws
            ComponentTypeNotFoundException, ComponentInstantiationException {
        Class<? extends Root> cl = rootMap.get(type);
        if (cl == null) {
            throw new ComponentTypeNotFoundException();
        }
        try {
            return cl.newInstance();
        } catch (Exception ex) {
            throw new ComponentInstantiationException(ex);
        }
    }

    public ComponentType getTypeForClass(Class<? extends Component> clazz) {
        for (Map.Entry<ComponentType, Class<? extends Root>> entry : rootMap.entrySet()) {
            if (entry.getValue().equals(clazz)) {
                return entry.getKey();
            }
        }
        for (Map.Entry<ComponentType, Class<? extends Component>> entry : componentMap.entrySet()) {
            if (entry.getValue().equals(clazz)) {
                return entry.getKey();
            }
        }
        return null;
    }

    protected void addComponent(ComponentType type, Class<? extends Component> cl) {
        componentMap.put(type, cl);
    }

    protected void addComponent(String type, Class<? extends Component> cl) {
        addComponent(ComponentType.create(type), cl);
    }

    protected void addRoot(ComponentType type, Class<? extends Root> cl) {
        rootMap.put(type, cl);
    }
    
    protected void addRoot(String type, Class<? extends Root> cl) {
        addRoot(ComponentType.create(type), cl);
    }

}
