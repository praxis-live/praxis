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

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class AudioServerProvider {

    private final ObjectLookup lookup;

    protected AudioServerProvider(Object... exts) {
        if (exts == null || exts.length == 0) {
            lookup = ObjectLookup.EMPTY;
        } else {
            lookup = new ObjectLookup(exts);
        }
    }

    public <T> T find(Class<T> type) {
        return lookup.find(type);
    }

    public <T> Iterable<T> findAll(Class<T> type) {
        return lookup.findAll(type);
    }

    public abstract String getLibraryName();

    public String getLibraryDescription() {
        return "";
    }

    public abstract AudioServer createServer(
            AudioConfiguration config, AudioClient client) throws Exception;
}
