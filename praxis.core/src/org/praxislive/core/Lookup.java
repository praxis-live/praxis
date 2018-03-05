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
package org.praxislive.core;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Neil C Smith
 */
public interface Lookup {

    public final static Lookup EMPTY = new Empty();
    public final static Lookup SYSTEM = new SystemLookup();

    @Deprecated
    public default <T> T get(Class<T> type) {
        return find(type).orElse(null);
    }

    @Deprecated
    public default <T> Result<T> getAll(Class<T> type) {
        Iterable<T> itr = findAll(type).collect(Collectors.toList());
        return new Result<T>() {
            @Override
            public Iterator<T> iterator() {
                return itr.iterator();
            }
        };
    }

    public <T> Optional<T> find(Class<T> type);

    public <T> Stream<T> findAll(Class<T> type);

    public interface Provider {

        public Lookup getLookup();

    }

    @Deprecated
    public interface Result<T> extends Iterable<T> {
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

}
