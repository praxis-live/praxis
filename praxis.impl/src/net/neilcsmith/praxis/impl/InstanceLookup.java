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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.neilcsmith.praxis.core.Lookup;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class InstanceLookup implements Lookup {

    private Object[] instances;
    private Lookup parent;

    private InstanceLookup(Object[] instances, Lookup parent) {
        this.instances = instances;
        this.parent = parent;
    }

    public <T> T get(Class<T> type) {
        for (Object obj : instances) {
            if (type.isInstance(obj)) {
                return type.cast(obj);
            }
        }
        return parent.get(type);
    }

    public <T> Result<T> getAll(Class<T> type) {
        List<T> list = new ArrayList<T>();
        for (Object obj : instances) {
            if (type.isInstance(obj)) {
                list.add(type.cast(obj));
            }
        }
        if (list.isEmpty()) {
            return parent.getAll(type);
        } else {
            return new ProxyResult<T>(list, parent.getAll(type));
        }
    }
    
    public static InstanceLookup create( Lookup parent, Object ... instances) {
        if (instances == null || parent == null) {
            throw new NullPointerException();
        }
        return new InstanceLookup(Arrays.copyOf(instances, instances.length), parent);
    }



    private static class ProxyResult<T> implements Result<T> {

        private List<T> instances;
        private Result<T> parent;

        private ProxyResult(List<T> instances, Result<T> parent) {
            this.instances = instances;
            this.parent = parent;
        }

        public Iterator<T> iterator() {
            return new ProxyIterator<T>(instances.iterator(), parent.iterator());
        }

    }

    private static class ProxyIterator<T> implements Iterator<T> {

        private Iterator<T> first;
        private Iterator<T> second;

        private ProxyIterator(Iterator<T> first, Iterator<T> second) {
            this.first = first;
            this.second = second;
        }

        public boolean hasNext() {
            if (first.hasNext()) {
                return true;
            } else {
                return second.hasNext();
            }
        }

        public T next() {
            if (first.hasNext()) {
                return first.next();
            } else {
                return second.next();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }



    }

}
