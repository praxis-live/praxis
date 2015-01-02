/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2014 Neil C Smith.
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

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.info.ArgumentInfo;

/**
 *
 * @author Neil C Smith
 */
public class PReference extends Argument {
    
    public final static String REFERENCE_TYPE = "reference-type";
    
    private Object ref;
    private Class refClass;
    private int refHash;
    
    private PReference(Object ref) {
        this.ref = ref;
        refHash = System.identityHashCode(ref);
        refClass = ref.getClass();
        
    }
    
    public Object getReference() {
        return ref;
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
    public boolean isEquivalent(Argument arg) {
        return equals(arg);
    }

    @Override
    public boolean isEmpty() {
        return false; // can PReference ever be empty???
    }


    
    public static PReference wrap(Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return new PReference(obj);
    }

    public static PReference coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof PReference) {
            return (PReference) arg;
        }
        throw new ArgumentFormatException();
    }
    
    public static ArgumentInfo info() {
        return ArgumentInfo.create(PReference.class, null);
    }

    public static ArgumentInfo info(Class<?> clas) {
//        PMap properties = PMap.valueOf(REFERENCE_TYPE, PString.valueOf(clas.getName()));
        PMap properties = PMap.create(REFERENCE_TYPE, clas.getName());
        return ArgumentInfo.create(PReference.class, properties);
    }
    
    
}
