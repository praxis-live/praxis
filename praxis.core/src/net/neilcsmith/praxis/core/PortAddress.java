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
package net.neilcsmith.praxis.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.types.PMap;

/**
 *
 * @author Neil C Smith
 */
public class PortAddress extends Argument {

    public static final String SEPERATOR = "!";
    private static final String SEP_REGEX = "\\!";
    private static final String ID_REGEX = "[_\\-\\p{javaLetter}][_\\-\\p{javaLetterOrDigit}]*";
    private ComponentAddress component;
    private String portID;
    private String addressString;

    private PortAddress(ComponentAddress component, String id, String address) {
        this.component = component;
        this.portID = id;
        this.addressString = address;
    }

    public ComponentAddress getComponentAddress() {
        return this.component;
    }
    
    public String getID() {
        return this.portID;
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
        if (obj instanceof PortAddress) {
            return this.addressString.equals(obj.toString());
        } else {
            return false;
        }
    }
    
    private static Pattern splitPoint = Pattern.compile(SEP_REGEX);

    public static PortAddress valueOf(String address) throws ArgumentFormatException {
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
        address = address.intern();
        return new PortAddress(comp, id, address);
    }

    public static PortAddress create(String address) {
        try {
            return valueOf(address);
        } catch (ArgumentFormatException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static PortAddress create(ComponentAddress component, String id) {
        if (!(isValidID(id))) {
            throw new IllegalArgumentException();
        }
        id = id.intern();
        String address = component.toString() + SEPERATOR + id;
        address = address.intern();
        return new PortAddress(component, id, address);
    }
    
    public static PortAddress coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof PortAddress) {
            return (PortAddress) arg;
        } else {
            return valueOf(arg.toString());
        }
    }
    
    private static Pattern idChecker = Pattern.compile(ID_REGEX);

    public static boolean isValidID(String id) {
        return idChecker.matcher(id).matches();
    }

    public static ArgumentInfo info() {
        return ArgumentInfo.create(PortAddress.class, null);
    }
}
