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
package net.neilcsmith.praxis.core.info;

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.types.PMap;

/**
 * Info object for an Argument, usually used to define the valid input and output
 * arguments of a Control. As well as giving the type of the argument, an ArgumentInfo
 * can have an optional set of properties. This might be used for defining the "minimum"
 * and "maximum" values of a PNumber argument, for example.
 *
 * @author Neil C Smith
 */
public final class ArgumentInfo extends Argument {
    
    public final static String KEY_ALLOWED_VALUES = "allowed-values";
    public final static String KEY_SUGGESTED_VALUES = "suggested-values";
    public final static String KEY_ALLOW_EMPTY = "allow-empty";
    public final static String KEY_EMPTY_IS_DEFAULT = "empty-is-default";
    public final static String KEY_TEMPLATE = "template";

    public static enum Presence { Always, Optional, Variable }

    private Class<? extends Argument> type;
    private Presence presence;
    private PMap properties;

    private ArgumentInfo(Class<? extends Argument> type, Presence presence, PMap properties) {
        this.type = type;
        this.presence = presence;
        this.properties = properties;
    }

    
    /**
     *
     * @return String name of Argument subclass
     */
    public Class<? extends Argument> getType() {
        return type;
    }

    public Presence getPresence() {
        return presence;
    }

    /**
     *
     * @return PMap properties
     */
    public PMap getProperties() {
        return properties;
    }
    

    @Override
    public String toString() {
        return type + " " + presence + " {" + properties.toString() + "}";
    }

    @Override
    public boolean isEquivalent(Argument arg) {
        return equals(arg);
    }

    

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArgumentInfo) {
            ArgumentInfo o = (ArgumentInfo) obj;
            return type.equals(o.type) && presence == o.presence
                    && properties.equals(o.properties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 53 * hash + (this.presence != null ? this.presence.hashCode() : 0);
        hash = 53 * hash + (this.properties != null ? this.properties.hashCode() : 0);
        return hash;
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
        return create(argClass, Presence.Always, properties);

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
            Presence presence, PMap properties) {
        if (argClass == null || presence == null) {
            throw new NullPointerException();
        }
//        String type = argClass.getName();
        if (properties == null) {
            properties = PMap.EMPTY;
        }
        return new ArgumentInfo(argClass, presence, properties);

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
