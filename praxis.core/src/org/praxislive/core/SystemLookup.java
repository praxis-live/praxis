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
package org.praxislive.core;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Neil C Smith
 */
class SystemLookup implements Lookup {
    
    private final static Logger LOG = Logger.getLogger(SystemLookup.class.getName());
    
    private final Lookup lookup; 
    
    SystemLookup() {
        Lookup lkp = new ServiceLoaderLookup();
        Lookup override = lkp.get(Lookup.class);
        if (override != null) {
            lkp = override;
            LOG.log(Level.FINE, "Overriding system lookup with {0}", override);
        }
        lookup = lkp;
    }

    public <T> T get(Class<T> type) {
        return lookup.get(type);
    }

    public <T> Result<T> getAll(Class<T> type) {
        return lookup.getAll(type);
    }

    private static class ServiceLoaderLookup implements Lookup {

        public <T> T get(Class<T> type) {
            Iterator<T> results = getAll(type).iterator();
            if (results.hasNext()) {
                return results.next();
            } else {
                return null;
            }
        }

        public <T> Result<T> getAll(Class<T> type) {
            ServiceLoader<T> serviceLoader = ServiceLoader.load(type);
            return new ServiceLoaderLookup.ServiceLoaderWrapper<T>(serviceLoader);
        }

        private class ServiceLoaderWrapper<T> implements Lookup.Result<T> {

            private ServiceLoader<T> serviceLoader;

            ServiceLoaderWrapper(ServiceLoader<T> serviceLoader) {
                this.serviceLoader = serviceLoader;
            }

            public Iterator<T> iterator() {
                return serviceLoader.iterator();
            }
        }
    }
}
