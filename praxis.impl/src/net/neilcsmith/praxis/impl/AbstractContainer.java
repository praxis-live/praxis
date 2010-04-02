/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
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
import net.neilcsmith.praxis.core.Container;
import net.neilcsmith.praxis.core.InvalidChildException;
import net.neilcsmith.praxis.core.ParentVetoException;
import net.neilcsmith.praxis.core.info.ComponentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.info.PortInfo;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractContainer extends AbstractComponent implements Container {

    private Map<String, Component> childMap;
    boolean childInfoValid;

    public AbstractContainer() {
        childMap = new LinkedHashMap<String, Component>();
    }

    public void addChild(String id, Component child) throws InvalidChildException {
        if (id == null || child == null) {
            throw new NullPointerException();
        }
        if (childMap.containsKey(id)) {
            throw new InvalidChildException("Child ID already in use");
        }
        childMap.put(id, child);
        try {
            child.parentNotify(this);
        } catch (ParentVetoException ex) {
            childMap.remove(id);
            throw new InvalidChildException();
        }
        childInfoValid = false;
    }

    public Component removeChild(String id) {
        Component child = childMap.remove(id);
        if (child != null) {
            try {
                child.parentNotify(null);
            } catch (ParentVetoException ex) {
            // it is an error for children to throw exception on removal
            // should we throw an error?
            }
            childInfoValid = false;
        }
        return child;
    }

    public Component getChild(String id) {
        return childMap.get(id);
    }

    public String getChildID(Component child) {
        Set<Map.Entry<String, Component>> entries = childMap.entrySet();
        for (Map.Entry<String, Component> entry : entries) {
            if (entry.getValue() == child) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String[] getChildIDs() {
        Set<String> keyset = childMap.keySet();
        return keyset.toArray(new String[keyset.size()]);
    }

    @Override
    public ComponentInfo getInfo() {
        if (info == null || !childInfoValid || !portInfoValid || !controlInfoValid) {
            Map<PString, ControlInfo> controls = buildControlInfoMap();
            controlInfoValid = true;
            Map<PString, PortInfo> ports = buildPortInfoMap();
            portInfoValid = true;
            PString[] children = buildChildArray();
            childInfoValid = true;
            info = ComponentInfo.create(getClass(), controls, ports, children, null);
        }
        return info;
    }

    PString[] buildChildArray() {
        Set<String> ids = childMap.keySet();
        PString[] children = new PString[ids.size()];
        int i = 0;
        for (String id : ids) {
            children[i] = PString.valueOf(id);
            i++;
        }
        return children;
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        for (Map.Entry<String, Component> entry : childMap.entrySet()) {
            entry.getValue().hierarchyChanged();
        }

    }


}
