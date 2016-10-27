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

import java.util.Optional;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.info.ArgumentInfo;

/**
 *
 * @author Neil C Smith
 */
public class PString extends Argument implements Comparable<PString> {

    public final static String KEY_ALLOWED_VALUES = ArgumentInfo.KEY_ALLOWED_VALUES;
    public final static String KEY_MIME_TYPE = "mime-type";
    
    public final static PString EMPTY = PString.valueOf("");
    
    private String value;
    
    private PString(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof PString) {
            return ((PString) obj).toString().equals(value);
        } else {
           return false; 
        }
        
    }

    public static PString coerce(Argument arg) {
        if (arg instanceof PString) {
            return (PString) arg;
        } else {
            return new PString(arg.toString());
        }
    }
    
    public static Optional<PString> from(Argument arg) {
        return Optional.of(coerce(arg));
    }
    
    public static PString valueOf(String str) {
        if (str == null) {
            throw new NullPointerException();
        }
        return new PString(str);
    }
    
    public static PString valueOf(Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return new PString(String.valueOf(obj));
    }

    public static ArgumentInfo info() {
        return ArgumentInfo.create(PString.class, null);
    }
    
    public static ArgumentInfo info(String[] allowed) {
        if (allowed == null) {
            return info();
        } else {
            PString[] arr = new PString[allowed.length];
            for (int i=0; i < arr.length; i++) {
                arr[i] = PString.valueOf(allowed[i]);
            }
            PMap props = PMap.create(KEY_ALLOWED_VALUES,
                    PArray.valueOf(arr));
            return ArgumentInfo.create(PString.class, props);
        }
    }

    public int compareTo(PString o) {
        return value.compareTo(o.value);
    }
    
}
