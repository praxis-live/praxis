/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2018 Neil C Smith.
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
import java.util.stream.Stream;

/**
 *
 * @author Neil C Smith
 */
public interface Lookup {

    public final static Lookup EMPTY = new Empty();
    public final static Lookup SYSTEM = new SystemLookup();

    public <T> Optional<T> find(Class<T> type);

    public <T> Stream<T> findAll(Class<T> type);

    public interface Provider {

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

}
