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

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Neil C Smith
 */
class SystemLookup implements Lookup {
    
    private final static Logger LOG = Logger.getLogger(SystemLookup.class.getName());
    
    private final Lookup lookup; 
    
    SystemLookup() {
        Lookup lkp = new ServiceLoaderLookup();
        lookup = lkp.find(Lookup.class).orElse(lkp);
    }

    @Override
    public <T> Optional<T> find(Class<T> type) {
        return lookup.find(type);
    }

    @Override
    public <T> Stream<T> findAll(Class<T> type) {
        return lookup.findAll(type);
    }

    private static class ServiceLoaderLookup implements Lookup {

        @Override
        public <T> Optional<T> find(Class<T> type) {
            ServiceLoader<T> loader = ServiceLoader.load(type);
            try {
                Iterator<T> itr = loader.iterator();
                if (itr.hasNext()) {
                    return Optional.of(itr.next());
                }
            } catch (ServiceConfigurationError ex) {
                LOG.log(Level.SEVERE, "Error in service configuration", ex);
            }
            return Optional.empty();
        }

        @Override
        public <T> Stream<T> findAll(Class<T> type) {
            ServiceLoader<T> loader = ServiceLoader.load(type);
            // @TODO make lazy?
            try {
                List<T> results = StreamSupport.stream(loader.spliterator(), false)
                        .collect(Collectors.toList());
                return results.stream();
            } catch (ServiceConfigurationError ex) {
                LOG.log(Level.SEVERE, "Error in service configuration", ex);
            }
            return Stream.empty();
        }
        
    }
}
