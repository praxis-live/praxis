/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2019 Neil C Smith.
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

package org.praxislive.core.types;

import java.util.Optional;
import org.praxislive.core.Value;
import org.praxislive.core.ValueFormatException;
import org.praxislive.core.ArgumentInfo;

/**
 *
 * @author Neil C Smith
 */
public class PReference extends Value {
    
    public final static String REFERENCE_TYPE = "reference-type";
    
    private final Object ref;
    private final Class refClass;
    private final int refHash;
    
    private PReference(Object ref) {
        this.ref = ref;
        refHash = System.identityHashCode(ref);
        refClass = ref.getClass();
    }
    
    @Deprecated
    public Object getReference() {
        return ref;
    }
    
    public <T> Optional<T> as(Class<T> cls) {
        if (cls.isInstance(ref)) {
            return Optional.of(cls.cast(ref));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return refClass.getName() + "@" + Integer.toHexString(refHash);
    }

    @Override
    public int hashCode() {
        return refHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PReference) {
            return ((PReference) obj).hashCode() == refHash;
        }
        return false;
    }

    @Override
    public boolean equivalent(Value arg) {
        return equals(arg);
    }

    @Override
    public boolean isEmpty() {
        return false; // can PReference ever be empty???
    }

    public static PReference of(Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return new PReference(obj);
    }
    
    @Deprecated
    public static PReference wrap(Object obj) {
        return of(obj);
    }

    @Deprecated
    public static PReference coerce(Value arg) throws ValueFormatException {
        if (arg instanceof PReference) {
            return (PReference) arg;
        }
        throw new ValueFormatException();
    }
    
    public static Optional<PReference> from(Value arg) {
        if (arg instanceof PReference) {
            return Optional.of((PReference) arg);
        } else {
            return Optional.empty();
        }
    }
    
    public static ArgumentInfo info() {
        return ArgumentInfo.of(PReference.class, null);
    }

    public static ArgumentInfo info(Class<?> clas) {
//        PMap properties = PMap.valueOf(REFERENCE_TYPE, PString.valueOf(clas.getName()));
        PMap properties = PMap.of(REFERENCE_TYPE, clas.getName());
        return ArgumentInfo.of(PReference.class, properties);
    }
    
    
}
