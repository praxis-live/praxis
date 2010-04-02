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
package net.neilcsmith.praxis.core.info;

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PString;

/**
 * Info object for an Argument, usually used to define the valid input and output
 * arguments of a Control. As well as giving the type of the argument, an ArgumentInfo
 * can have an optional set of properties. This might be used for defining the "minimum"
 * and "maximum" values of a PNumber argument, for example.
 *
 * @author Neil C Smith
 */
public final class ArgumentInfo extends Argument {

    /**
     *
     */
    public final static PString TYPE_KEY = PString.valueOf("type");
    /**
     *
     */
    public final static PString PROPERTIES_KEY = PString.valueOf("properties");
    private PMap data;

    private ArgumentInfo(PMap data) {
        this.data = data;
    }

//    public PString getType() {
//        return (PString) data.get(TYPE_KEY);
//    }
    
    /**
     *
     * @return String name of Argument subclass
     */
    public String getType() {
        return data.get(TYPE_KEY).toString();
    }

    /**
     *
     * @return PMap properties
     */
    public PMap getProperties() {
        return (PMap) data.get(PROPERTIES_KEY);
    }
    
    /**
     *
     * @return
     */
    public PMap getData() {
        return data;
    }

    @Override
    public String toString() {
        return data.toString();
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArgumentInfo) {
            ArgumentInfo o = (ArgumentInfo) obj;
            return data.equals(o.data);
        }
        return false;
    }


    /**
     * Create an ArgumentInfo from the Argument class and optional PMap of
     * additional properties.
     *
     * @param argClass
     * @param properties
     * @return ArgumentInfo
     */
    public static ArgumentInfo create(Class<? extends Argument> argClass,
            PMap properties) {
        if (argClass == null) {
            throw new NullPointerException();
        }
        PString type = PString.valueOf(argClass.getName());
        if (properties == null) {
            properties = PMap.EMPTY;
        }
        PMap data = PMap.valueOf(TYPE_KEY, type,
                PROPERTIES_KEY, properties);
        return new ArgumentInfo(data);

    }
    
    /**
     * Coerce the given Argument into an ArgumentInfo object.
     * 
     * @param arg Argument to be coerced.
     * @return ArgumentInfo
     * @throws ArgumentFormatException if Argument cannot be coerced.
     */
    public static ArgumentInfo coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof ArgumentInfo) {
            return (ArgumentInfo) arg;
        }
        throw new ArgumentFormatException();
    }
}
