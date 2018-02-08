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
package org.praxislive.core;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 *
 * @author Neil C Smith
 */
public class ControlAddress extends Value {

    public static final String SEPARATOR = ".";
    private static final String SEP_REGEX = "\\.";
    private static final String ID_REGEX = "[_\\-\\p{javaLetter}][_\\-\\p{javaLetterOrDigit}]*";
    
    private final ComponentAddress component;
    private final String controlID;
    private final String addressString;

    private ControlAddress(ComponentAddress component, String id, String address) {
        this.component = component;
        this.controlID = id;
        this.addressString = address;
    }

    public ComponentAddress getComponentAddress() {
        return this.component;
    }

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
    private static Pattern splitPoint = Pattern.compile(SEP_REGEX);

    public static ControlAddress valueOf(String address) throws ArgumentFormatException {
        String[] parts = splitPoint.split(address);
        if (parts.length != 2) {
            throw new ArgumentFormatException();
        }
//        String id = parts[1];
//        if (!(isValidID(id))) {
//            throw new ArgumentFormatException();
//        }
        if (!(isValidID(parts[1]))) {
            throw new ArgumentFormatException();
        }
        String id = parts[1].intern();
        ComponentAddress comp = ComponentAddress.valueOf(parts[0]);
//        return new ControlAddress(comp, id, address);
        address = address.intern();
        return new ControlAddress(comp, id, address);
    }

    public static ControlAddress create(String address) {
        try {
            return valueOf(address);
        } catch (ArgumentFormatException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static ControlAddress create(ComponentAddress component, String id) {
        if (!(isValidID(id))) {
            throw new IllegalArgumentException();
        }
        id = id.intern();
        String address = component.toString() + SEPARATOR + id;
        address = address.intern();
        return new ControlAddress(component, id, address);

    }
    
    public static ControlAddress coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof ControlAddress) {
            return (ControlAddress) arg;
        } else {
            return valueOf(arg.toString());
        }
    }
    
    public static Optional<ControlAddress> from(Argument arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ArgumentFormatException ex) {
            return Optional.empty();
        }
    }
    
    private static Pattern idChecker = Pattern.compile(ID_REGEX);

    public static boolean isValidID(String id) {
        return idChecker.matcher(id).matches();

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
