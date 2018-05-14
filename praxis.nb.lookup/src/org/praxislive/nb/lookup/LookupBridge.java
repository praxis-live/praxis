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

package org.praxislive.nb.lookup;

import java.util.Optional;
import java.util.stream.Stream;
import org.praxislive.core.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
@ServiceProvider(service=Lookup.class)
public class LookupBridge implements Lookup {

    public LookupBridge() {}

    @Override
    public <T> Optional<T> find(Class<T> type) {
        return Optional.ofNullable(org.openide.util.Lookup.getDefault().lookup(type));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Stream<T> findAll(Class<T> type) {
        return (Stream<T>) org.openide.util.Lookup.getDefault().lookupAll(type).stream();
    }


}
