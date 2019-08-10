/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2019 Neil C Smith.
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
package org.praxislive.core;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

/**
 * A general type-safe registry by which clients can access implementations of
 * various services (eg. implementations of a specific interface).
 *
 * It is inspired by the similarly named feature in the Apache NetBeans
 * platform, but is generally simpler and more lightweight.
 */
public interface Lookup {

    /**
     * An empty Lookup
     */
    public final static Lookup EMPTY = new Empty();

    /**
     * A system-wide Lookup, that by default delegates to {@link ServiceLoader}.
     *
     * The implementation can be overridden or extended by registering a Lookup
     * subclass to be found using ServiceLoader.
     */
    public final static Lookup SYSTEM = new SystemLookup();

    /**
     * Search for the first implementation of the given type. Returns an
     * {@link Optional} wrapping an instance of the given Class, or
     * {@link Optional#EMPTY} if none exists.
     *
     * @param <T> service type to lookup
     * @param type class of service to lookup
     * @return Optional wrapping first instance of type, or an empty Optional if
     * not found
     */
    public <T> Optional<T> find(Class<T> type);

    /**
     * Search for all implementations of the given type.
     *
     * @param <T> service type to lookup
     * @param type class of service to lookup
     * @return Stream of all implementations of the given type
     */
    public <T> Stream<T> findAll(Class<T> type);

    /**
     * Create a Lookup wrapping the given Objects. Searches will maintain the
     * order of the provided instances.
     *
     * @param instances collection of objects
     * @return lookup
     */
    public static Lookup of(Object... instances) {
        return ObjectLookup.create(instances);
    }

    /**
     * Create a Lookup wrapping the given Objects and results from the provided
     * parent Lookup. Searches will maintain the order of the provided
     * instances. Instances registered directly will take priority over the
     * parent.
     *
     * @param parent lookup to include results from
     * @param instances collection of objects
     * @return lookup
     */
    public static Lookup of(Lookup parent, Object... instances) {
        return ObjectLookup.create(parent, instances);
    }

    /**
     * Interface to be implemented by all types that provide access to a Lookup.
     */
    public interface Provider {

        /**
         * Get the Lookup from this Provider.
         * 
         * @return lookup
         */
        public Lookup getLookup();

    }

    static class Empty implements Lookup {

        @Override
        public <T> Optional<T> find(Class<T> type) {
            return Optional.empty();
        }

        @Override
        public <T> Stream<T> findAll(Class<T> type) {
            return Stream.empty();
        }

    }

    static class ObjectLookup implements Lookup {

        private final Object[] instances;
        private final Lookup parent;

        private ObjectLookup(Object[] instances, Lookup parent) {
            this.instances = instances;
            this.parent = parent;
        }

        @Override
        public <T> Optional<T> find(Class<T> type) {
            for (Object obj : instances) {
                if (type.isInstance(obj)) {
                    return Optional.of(type.cast(obj));
                }
            }
            return parent.find(type);
        }

        @Override
        public <T> Stream<T> findAll(Class<T> type) {
            return Stream.concat(Stream.of(instances)
                    .filter(type::isInstance)
                    .map(type::cast),
                    parent.findAll(type));
        }

        public static ObjectLookup create(Object... instances) {
            return create(null, instances);
        }

        public static ObjectLookup create(Lookup parent, Object... instances) {
            if (instances == null) {
                throw new NullPointerException();
            }
            if (parent == null) {
                parent = Lookup.EMPTY;
            }
            return new ObjectLookup(instances.clone(), parent);
        }

    }

}
