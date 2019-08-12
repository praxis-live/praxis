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
public final class PBoolean extends Value {
    
    public static final PBoolean TRUE = new PBoolean(true);
    public static final PBoolean FALSE = new PBoolean(false);
    
    private boolean value;
    
    private PBoolean(boolean value) {
        this.value = value;
    }
    
    public boolean value() {
        return value;
    }

    @Override
    public String toString() {
        return value ? "true" : "false";
    }

    @Override
    public int hashCode() {
        return value ? 1231 : 1237;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PBoolean) {
            return value == ((PBoolean) obj).value;
        }
        return false;
    }
    
    public static PBoolean of(boolean value) {
        return value ? TRUE : FALSE;
    }
    
    @Deprecated
    public static PBoolean valueOf(boolean value) {
        return value ? TRUE : FALSE;
    }
    
    public static PBoolean parse(String str) throws ValueFormatException {
        if (str.equals("true")) {
            return TRUE;
        } else if (str.equals("false")) {
            return FALSE;
        } else {
            return PNumber.parse(str).value() > 0.5 ? TRUE : FALSE;
        }
    }
    
    @Deprecated
    public static PBoolean valueOf(String str) throws ValueFormatException {
        return parse(str);
    }
       
    @Deprecated
    public static PBoolean coerce(Value arg) throws ValueFormatException {
        if (arg instanceof PBoolean) {
            return (PBoolean) arg;
        } else if (arg instanceof PNumber) {
            return ((PNumber) arg).value() > 0.5 ? TRUE : FALSE;
        } else {
            return parse(arg.toString());
        }
    }
    
    public static Optional<PBoolean> from(Value arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ValueFormatException ex) {
            return Optional.empty();
        }
    }
    
    public static ArgumentInfo info() {
        return ArgumentInfo.of(PBoolean.class, null);
    }

}
