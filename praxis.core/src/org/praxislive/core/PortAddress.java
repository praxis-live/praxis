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
public class PortAddress extends Value {

    public static final String SEPERATOR = "!";
    private static final String SEP_REGEX = "\\!";
    private static final String ID_REGEX = "[_\\-\\p{javaLetter}][_\\-\\p{javaLetterOrDigit}]*";
    private static final Pattern SEP_PATTERN = Pattern.compile(SEP_REGEX);
    private static final Pattern ID_PATTERN = Pattern.compile(ID_REGEX);

    private final ComponentAddress component;
    private final String portID;
    private final String addressString;

    private PortAddress(ComponentAddress component, String id, String address) {
        this.component = component;
        this.portID = id;
        this.addressString = address;
    }

    public ComponentAddress component() {
        return this.component;
    }

    @Deprecated
    public ComponentAddress getComponentAddress() {
        return this.component;
    }

    public String portID() {
        return this.portID;
    }

    @Deprecated
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

    public static PortAddress parse(String address) throws ValueFormatException {
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
        return new PortAddress(comp, id, address);
    }

    @Deprecated
    public static PortAddress valueOf(String address) throws ValueFormatException {
        return parse(address);
    }

    public static PortAddress of(String address) {
        try {
            return parse(address);
        } catch (ValueFormatException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Deprecated
    public static PortAddress create(String address) {
        return PortAddress.of(address);
    }

    public static PortAddress of(ComponentAddress component, String id) {
        if (!(isValidID(id))) {
            throw new IllegalArgumentException();
        }
        id = cache(id);
        String address = component.toString() + SEPERATOR + id;
        address = cache(address);
        return new PortAddress(component, id, address);
    }

    @Deprecated
    public static PortAddress create(ComponentAddress component, String id) {
        return of(component, id);
    }

    @Deprecated
    public static PortAddress coerce(Value arg) throws ValueFormatException {
        if (arg instanceof PortAddress) {
            return (PortAddress) arg;
        } else {
            return parse(arg.toString());
        }
    }

    public static Optional<PortAddress> from(Value arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ValueFormatException ex) {
            return Optional.empty();
        }
    }

    public static boolean isValidID(String id) {
        return ID_PATTERN.matcher(id).matches();
    }

    public static ArgumentInfo info() {
        return ArgumentInfo.of(PortAddress.class, null);
    }
}
