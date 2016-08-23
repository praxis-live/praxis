/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2016 Neil C Smith.
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

package net.neilcsmith.praxis.core.types;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.info.ArgumentInfo;

/**
 *
 * @author Neil C Smith
 */
public final class PResource extends Argument implements Comparable<PResource>{

    private final URI uri;
    
    private PResource(URI uri) {
        this.uri = uri;
    }

    public URI value() {
        return uri;
    }
    
    @Override
    public String toString() {
        return uri.toString();
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof PResource) {
            PResource o = (PResource) obj;
            return this.uri.equals(o.uri);
        }
        return false;
    }
    
    public static PResource valueOf(URI uri) {
        if (uri.isAbsolute()) {
            return new PResource(uri);
        }
        throw new IllegalArgumentException();
    }
    
    public static PResource valueOf(String str) throws ArgumentFormatException {
        try {
            URI uri = new URI(str);
            if (uri.isAbsolute()) {
                return new PResource(uri);
            }
            throw new ArgumentFormatException();
        } catch (URISyntaxException ex) {
            throw new ArgumentFormatException(ex);
        }
    }
    
    public static PResource coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof PResource) {
            return (PResource) arg;
        } else {
            return valueOf(arg.toString());
        }
    }
    
    public static Optional<PResource> from(Argument arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ArgumentFormatException ex) {
            return Optional.empty();
        }
    }
    
    public static ArgumentInfo info() {
        return ArgumentInfo.create(PResource.class, null);
    }

    public static ArgumentInfo info(boolean allowEmpty) {
        if (allowEmpty) {
            return ArgumentInfo.create(PResource.class,
//                    PMap.valueOf(PString.valueOf(KEY_ALLOW_EMPTY), PBoolean.TRUE));
                    PMap.create(ArgumentInfo.KEY_ALLOW_EMPTY, true));
        } else {
            return ArgumentInfo.create(PResource.class, null);
        }
    }

    public int compareTo(PResource o) {
        return uri.compareTo(o.uri);
    }
    
    public List<URI> resolve(Lookup lookup) {
        Resolver res = lookup.get(Resolver.class);
        if (res != null) {
            return res.resolve(this);
        } else {
            return Collections.singletonList(uri);
        }
    }
    
    public static interface Resolver {
        
        public List<URI> resolve(PResource resource);
        
    }

}
