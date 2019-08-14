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
import static org.praxislive.core.ComponentAddress.cache;

/**
 *
 * @author Neil C Smith
 */
public class ControlAddress extends Value {

    public static final String SEPARATOR = ".";
    private static final String SEP_REGEX = "\\.";
    private static final String ID_REGEX = "[_\\-\\p{javaLetter}][_\\-\\p{javaLetterOrDigit}]*";
    private static final Pattern ID_PATTERN = Pattern.compile(ID_REGEX);
    private static final Pattern SEP_PATTERN = Pattern.compile(SEP_REGEX);
    
    private final ComponentAddress component;
    private final String controlID;
    private final String addressString;

    private ControlAddress(ComponentAddress component, String id, String address) {
        this.component = component;
        this.controlID = id;
        this.addressString = address;
    }

    public ComponentAddress component() {
        return this.component;
    }
    
    @Deprecated
    public ComponentAddress getComponentAddress() {
        return this.component;
    }

    public String controlID() {
        return this.controlID;
    }
    
    @Deprecated
    public String getID() {
        return this.controlID;
    }

    @Override
    public String toString() {
        return this.addressString;
    }

    @Override
    public int hashCode() {
        return this.addressString.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ControlAddress) {
            return this.addressString.equals(obj.toString());
        } else {
            return false;
        }
    }

    public static ControlAddress parse(String address) throws ValueFormatException {
        String[] parts = SEP_PATTERN.split(address);
        if (parts.length != 2) {
            throw new ValueFormatException();
        }
        if (!(isValidID(parts[1]))) {
            throw new ValueFormatException();
        }
        String id = cache(parts[1]);
        ComponentAddress comp = ComponentAddress.parse(parts[0]);
        address = cache(address);
        return new ControlAddress(comp, id, address);
    }

    @Deprecated
    public static ControlAddress valueOf(String address) throws ValueFormatException {
        return parse(address);
    }

    public static ControlAddress of(String address) {
        try {
            return parse(address);
        } catch (ValueFormatException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    @Deprecated
    public static ControlAddress create(String address) {
        return ControlAddress.of(address);
    }

    public static ControlAddress of(ComponentAddress component, String id) {
        if (!(isValidID(id))) {
            throw new IllegalArgumentException();
        }
        id = cache(id);
        String address = component.toString() + SEPARATOR + id;
        address = cache(address);
        return new ControlAddress(component, id, address);

    }
    
    @Deprecated
    public static ControlAddress create(ComponentAddress component, String id) {
        return of(component, id);
    }
    
    @Deprecated
    public static ControlAddress coerce(Value arg) throws ValueFormatException {
        if (arg instanceof ControlAddress) {
            return (ControlAddress) arg;
        } else {
            return parse(arg.toString());
        }
    }
    
    public static Optional<ControlAddress> from(Value arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ValueFormatException ex) {
            return Optional.empty();
        }
    }
    
    public static boolean isValidID(String id) {
        return ID_PATTERN.matcher(id).matches();

//        int length = id.length();
//        if (length == 0) {
//            return false;
//        }
//        boolean valid = Character.isLetter(id.charAt(0));
//        
//        if (length > 1 && valid) {
//            for (int i=1; i<length; i++) {
//                if (! (Character.isLetterOrDigit(id.charAt(i)))) {
//                    valid = false;
//                    break;
//                }
//            }
//        }
//        
//        return valid;
    }
}
