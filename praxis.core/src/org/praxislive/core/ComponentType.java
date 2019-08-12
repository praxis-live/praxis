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

package org.praxislive.core;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ComponentType extends Value {
    
    private final static String TYPE_REGEX =
            "([\\p{javaLetter}][_\\-\\p{javaLetterOrDigit}]*\\:)+" + 
            "([\\p{javaLetter}][_\\-\\p{javaLetterOrDigit}]*)";
    private final static Pattern TYPE_PATTERN = Pattern.compile(TYPE_REGEX);

    private final String typeString;

    private ComponentType(String str) {
        this.typeString = str;
    }

    @Override
    public String toString() {
        return typeString;
    }

    @Override
    public int hashCode() {
        return typeString.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ComponentType) {
            return typeString.equals(obj.toString());
        } else {
            return false;
        }
    }
    
    public static ComponentType of(String str) {
        try {
            return parse(str);
        } catch (ValueFormatException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    @Deprecated
    public static ComponentType create(String str) {
        return of(str);
    }
    
    private static boolean isValidTypeString(String str) {
        return TYPE_PATTERN.matcher(str).matches();
    }

    public static ComponentType parse(String str) throws ValueFormatException {
        if (isValidTypeString(str)) {
            return new ComponentType(str);
        }
        throw new ValueFormatException("Invalid String representation of Type");
    }

    @Deprecated
    public static ComponentType valueOf(String str) throws ValueFormatException {
        if (isValidTypeString(str)) {
            return new ComponentType(str);
        }
        throw new ValueFormatException("Invalid String representation of Type");
    }

    public static ArgumentInfo info() {
        return ArgumentInfo.of(ComponentType.class, null);
    }

    @Deprecated
    public static ComponentType coerce(Value arg) throws ValueFormatException {
        if (arg instanceof ComponentType) {
            return (ComponentType) arg;
        } else {
            return parse(arg.toString());
        }
    }
    
    public static Optional<ComponentType> from(Value arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ValueFormatException ex) {
            return Optional.empty();
        }
    }

}
