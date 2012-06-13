/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
 *
 * Copying and distribution of this file, with or without modification,
 * are permitted in any medium without royalty provided the copyright
 * notice and this notice are preserved.  This file is offered as-is,
 * without any warranty.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 *
 */
package org.jaudiolibs.audioservers;

import java.util.ArrayList;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class ObjectLookup {
    
    final static ObjectLookup EMPTY = new ObjectLookup(new Object[0]);

    private Object[] objs;

    ObjectLookup(Object[] objs) {
        for (Object o : objs) {
            if (o == null) {
                throw new NullPointerException();
            }
        }
        this.objs = objs;
    }

    <T> T find(Class<T> type) {
        for (Object obj : objs) {
            if (type.isInstance(obj)) {
                return type.cast(obj);
            }
        }
        return null;
    }

    <T> Iterable<T> findAll(Class<T> type) {
        ArrayList<T> list = new ArrayList<T>();
        for (Object obj : objs) {
            if (type.isInstance(obj)) {
                list.add(type.cast(obj));
            }
        }
        return list;
    }
}
