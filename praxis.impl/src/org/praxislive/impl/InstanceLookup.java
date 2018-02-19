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
package org.praxislive.impl;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import org.praxislive.core.Lookup;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class InstanceLookup implements Lookup {

    private final Object[] instances;
    private final Lookup parent;

    private InstanceLookup(Object[] instances, Lookup parent) {
        this.instances = instances;
        this.parent = parent;
    }

    public <T> Optional<T> find(Class<T> type) {
        for (Object obj : instances) {
            if (type.isInstance(obj)) {
                return Optional.of(type.cast(obj));
            }
        }
        return parent.find(type);
    }

    public <T> Stream<T> findAll(Class<T> type) {
        return Stream.concat(Stream.of(instances)
                .filter(type::isInstance)
                .map(type::cast),
                parent.findAll(type));
    }

    public static InstanceLookup create(Object... instances) {
        return create(null, instances);
    }

    public static InstanceLookup create(Lookup parent, Object... instances) {
        if (instances == null) {
            throw new NullPointerException();
        }
        if (parent == null) {
            parent = Lookup.EMPTY;
        }
        return new InstanceLookup(Arrays.copyOf(instances, instances.length), parent);
    }

}
