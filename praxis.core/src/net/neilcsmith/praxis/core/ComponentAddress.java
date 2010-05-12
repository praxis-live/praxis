/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.praxis.core;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.types.PMap;

/**
 * Address of a Component
 *
 * A component is a slash separated path of IDs, starting with the ID of the Root
 * that the Component is in.
 *
 * eg. /rootID/parentID/componentID
 *
 * ComponentAddresses are always absolute.
 *
 * @author Neil C Smith
 */
public final class ComponentAddress extends Argument {
    
    private static final String ADDRESS_REGEX = "\\G/([_\\-\\p{javaLetter}][_\\-\\p{javaLetterOrDigit}]*)";
    private static final String ID_REGEX = "[_\\-\\p{javaLetter}][_\\-\\p{javaLetterOrDigit}]*";
    private final static Pattern idPattern = Pattern.compile(ID_REGEX);
    private final static Pattern addressPattern = Pattern.compile(ADDRESS_REGEX);
    
    private String[] address;
    private String addressString;

    private ComponentAddress(String[] address, String addressString) {
        this.address = address;
        this.addressString = addressString;
    }

    /**
     * Number of ID parts to this address
     *
     * @return int Depth (always >=1)
     */
    public int getDepth() {
        return address.length;
    }

    /**
     * Get ID at given depth in address.
     *
     * @param depth
     * @return String ID
     */
    public String getComponentID(int depth) {
        return address[depth];
    }
    
    /**
     * Equivalent to getComponentID(0), which will always refer to a Root component.
     *
     * @return String
     */
    public String getRootID() {
        return address[0];
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
        if (obj instanceof ComponentAddress) {
            ComponentAddress o = (ComponentAddress) obj;
            return addressString.equals(o.addressString);
        } else {
            return false;
        }
    }

    /**
     * Create an address from the supplied String
     *
     * @param addressString
     * @return ComponentAddress
     * @throws net.neilcsmith.praxis.core.ArgumentFormatException
     */
    public static ComponentAddress valueOf(String addressString) throws ArgumentFormatException {

        String[] address = parseAddress(addressString);
//        return new ComponentAddress(address, addressString);
        return new ComponentAddress(address, addressString.intern());

    }

    public static ComponentAddress create(String address) {
        try {
            return valueOf(address);
        } catch (ArgumentFormatException ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    /**
     * Create a ComponentAddress by adding the supplied ID to the end of the
     * supplied ComponentAddress path.
     *
     * @param address
     * @param id
     * @return ComponentAddress
     * @throws IllegalArgumentException
     */
    public static ComponentAddress create(ComponentAddress address, String id) {
        try {
            return valueOf(address.toString() + '/' + id);
        } catch (ArgumentFormatException ex) {
            throw new IllegalArgumentException(ex);
        }
        
    }
    
    /**
     *
     * @param arg
     * @return
     * @throws net.neilcsmith.praxis.core.ArgumentFormatException
     */
    public static ComponentAddress coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof ComponentAddress) {
            return (ComponentAddress) arg;
        } else {
            return valueOf(arg.toString());
        }
    }
    
    /**
     *
     * @param id
     * @return
     */
    public static boolean isValidID(String id) {
        return idPattern.matcher(id).matches();
    }


    
    private static String[] parseAddress(String addressString) throws ArgumentFormatException {
        Matcher match = addressPattern.matcher(addressString);
        ArrayList<String> addressList = new ArrayList<String>();
        int end = 0;
        while (match.find()) {
//            addressList.add(match.group(1));
            addressList.add(match.group(1).intern());
            end = match.end();
        }
        if (addressList.size() < 1 || end < addressString.length()) {
            throw new ArgumentFormatException();
        }
        return addressList.toArray(new String[addressList.size()]);
        
    }


    public static ArgumentInfo info() {
        return ArgumentInfo.create(ComponentAddress.class, PMap.EMPTY);
    }

    
//    private final static Pattern idChecker = Pattern.compile(ID_REGEX);
//    public static boolean isValidID(String id) {
//        return idChecker.matcher(id).matches();
//    }
    
    
// this is quicker by roughly factor of 3 but harder to maintain    
//    private static String[] parseAddress(String addressString) throws ArgumentFormatException {
//
//        if (addressString.length() < 2 || addressString.charAt(0) != SEPERATOR) {
//            //address has to be have at least a starting slash and one letter
//            throw new ArgumentFormatException();
//        }
//
//        StringBuilder stringBuilder = new StringBuilder();
//        ArrayList<String> addressList = new ArrayList<String>();
//
//        int length = addressString.length();
//
//        // parse loop
//        for (int i = 1; i < length; i++) {
//            char ch = addressString.charAt(i);
//            // split at slashes
//            // throw error if no id between slashes or slash at end
//            if (ch == SEPERATOR) {
//                if (stringBuilder.length() > 0 && i < (length - 1)) {
//                    addressList.add(stringBuilder.toString());
//                    stringBuilder.setLength(0);
//                } else {
//                    throw new ArgumentFormatException("Zero length id string found");
//                }
//            } else if (ch == '_') {
//                stringBuilder.append(ch);
//            } else {
//                if (stringBuilder.length() == 0) {
//                    if (Character.isLetter(ch)) {
//                        stringBuilder.append(ch);
//                    } else {
//                        throw new ArgumentFormatException("First character of id isn't valid");
//                    }
//                } else {
//                    if (Character.isLetterOrDigit(ch)) {
//                        stringBuilder.append(ch);
//                    } else {
//                        throw new ArgumentFormatException("Invalid character found in id");
//                    }
//                }
//            }
//        } // parse loop end
//
//        // add final bit of buffer
//        if (stringBuilder.length() > 0) {
//            addressList.add(stringBuilder.toString());
//        }
//
//        return addressList.toArray(new String[addressList.size()]);
//
//    }
}
