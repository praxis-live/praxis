/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Neil C Smith.
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

package net.neilcsmith.praxis.nb.lookup;

import java.util.Collection;
import java.util.Iterator;
import net.neilcsmith.praxis.core.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
@ServiceProvider(service=Lookup.class)
public class LookupBridge implements Lookup {

    public LookupBridge() {}

    @Override
    public <T> T get(Class<T> type) {
        return org.openide.util.Lookup.getDefault().lookup(type);
    }

    @Override
    public <T> Result<T> getAll(Class<T> type) {
        return new ResultWrapper<T>(org.openide.util.Lookup.getDefault().lookupAll(type));
    }

    private class ResultWrapper<T> implements Lookup.Result<T> {

        private Collection<? extends T> all;

        private ResultWrapper(Collection<? extends T> all) {
            this.all = all;
        }

        @Override
        public Iterator<T> iterator() {
            return (Iterator<T>) all.iterator();
        }

    }

}
