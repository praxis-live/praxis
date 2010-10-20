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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.neilcsmith.praxis.core.Lookup;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class MapInstanceLookup implements Lookup {

    private Map<Class<?>, Object> instances;
    private Lookup parent;

    private MapInstanceLookup(Map<Class<?>, Object> instances, Lookup parent) {
        this.instances = instances;
        this.parent = parent;
    }

    public <T> T get(Class<T> type) {
        T obj = findInstance(type);
        if (obj != null) {
            return obj;
        } else {
            return parent.get(type); 
        }  
    }

    public <T> Result<T> getAll(Class<T> type) {
        T obj = findInstance(type);
        if (obj != null) {
            return new ProxyResult(obj, parent.getAll(type));
        } else {
            return parent.getAll(type);
        }
    }
    
    private <T> T findInstance(Class<T> type) {
        Object obj = instances.get(type);
        if (obj != null) {
            try {
                return type.cast(obj);
            } catch (ClassCastException ex) {
                //@TODO log and remove instance?
            }        
        }
        return null;
    }

    public static MapInstanceLookup create(Map<Class<?>, Object> instances, Lookup parent) {
        if (instances == null || parent == null) {
            throw new NullPointerException();
        }
        return new MapInstanceLookup(new HashMap<Class<?>, Object>(instances), parent);
    }
    
    public static <T> MapInstanceLookup create(Class<T> type, T instance, Lookup parent) {
        if (! type.isInstance(instance)) {
            throw new IllegalArgumentException();
        }
        return new MapInstanceLookup(Collections.<Class<?>,Object>singletonMap(type, instance) , parent);
    }

    private static class ProxyResult<T> implements Result<T> {

        private T instance;
        private Result<T> parent;

        private ProxyResult(T instance, Result<T> parent) {
            this.instance = instance;
            this.parent = parent;
        }

        public Iterator<T> iterator() {
            return new ProxyIterator<T>(instance, parent.iterator());
        }

    }

    private static class ProxyIterator<T> implements Iterator<T> {

        private T instance;
        private Iterator<T> parent;

        private ProxyIterator(T instance, Iterator<T> parent) {
            this.instance = instance;
            this.parent = parent;
        }

        public boolean hasNext() {
            if (instance != null) {
                return true;
            } else {
                return parent.hasNext();
            }
        }

        public T next() {
            if (instance != null) {
                T obj = instance;
                instance = null;
                return obj;
            } else {
                return parent.next();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }



    }

}
