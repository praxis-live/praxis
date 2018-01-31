/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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

import java.util.regex.Pattern;
import org.praxislive.core.info.ArgumentInfo;

/**
 * @TODO Enforce String with regex
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ComponentType extends Argument {
    
    private final static String TYPE_REGEX =
            "([\\p{javaLetter}][_\\-\\p{javaLetterOrDigit}]*\\:)+" + 
            "([\\p{javaLetter}][_\\-\\p{javaLetterOrDigit}]*)";
    private final static Pattern TYPE_PATTERN = Pattern.compile(TYPE_REGEX);

    private String typeString;

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

    public static ComponentType create(String str) {
        try {
            return valueOf(str);
        } catch (ArgumentFormatException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    private static boolean isValidTypeString(String str) {
        return TYPE_PATTERN.matcher(str).matches();
    }

    public static ComponentType valueOf(String str) throws ArgumentFormatException {
        if (isValidTypeString(str)) {
            return new ComponentType(str);
        }
        throw new ArgumentFormatException("Invalid String representation of Type");
    }

    public static ArgumentInfo info() {
        return ArgumentInfo.create(ComponentType.class, null);
    }


    public static ComponentType coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof ComponentType) {
            return (ComponentType) arg;
        } else {
            return valueOf(arg.toString());
        }
    }

}
