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
import java.util.Iterator;
import java.util.List;
import net.neilcsmith.praxis.core.Lookup;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class EmptyLookup implements Lookup {

    private final static EmptyResult EMPTY_RESULT = new EmptyResult<Object>();
    private final static EmptyLookup INSTANCE = new EmptyLookup();

    private EmptyLookup() {}

    public <T> T get(Class<T> type) {
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> Result<T> getAll(Class<T> type) {
        return (Result<T>) EMPTY_RESULT;
    }

    public static EmptyLookup getInstance() {
        return INSTANCE;
    }

    private static class EmptyResult<T> implements Lookup.Result<T> {

        public Iterator<T> iterator() {
            List<T> list = Collections.emptyList();
            return list.iterator();
        }

    }


}
