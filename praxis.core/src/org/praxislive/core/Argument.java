/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2017 Neil C Smith.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PBoolean;
import org.praxislive.core.types.PBytes;
import org.praxislive.core.types.PError;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PReference;
import org.praxislive.core.types.PResource;
import org.praxislive.core.types.PString;

/**
 * Effectively deprecated - see Value.
 *
 * @author Neil C Smith
 */
public abstract class Argument {

//    public final static String KEY_ALLOW_EMPTY = "allow-empty";
    
    /**
     * Arguments must override the default method to return a string representation
     * that is immutable.
     *
     * @return String representation
     */
    @Override
    public abstract String toString();
    
    /**
     * Arguments must override the default hashcode method.
     *
     * @return int hashcode
     */
    @Override
    public abstract int hashCode();

    /**
     * Arguments must override the default equals method.
     * This method should only return <code>true</code> if the supplied Object is
     * of the same type as the implementing Argument.  Arguments of an unknown
     * type should be coerced before calling this method.  This method does not
     * have to guarantee that
     * <code>this.equals(that) == this.toString().equals(that.toString())</code>
     *
     * @param obj
     * @return boolean
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * Check whether this Argument is an empty value and has a zero length string
     * representation. Subclasses may wish to override this for efficiency if the
     * String representation is lazily created.
     *
     *
     * @return boolean true if empty
     */
    public boolean isEmpty() {
        return (toString().length() == 0);
    }

    @Deprecated
    public boolean isEquivalent(Argument arg) {
        return this == arg || this.toString().equals(arg.toString());
    }
    
    /**
     * Use this method to return an ArgumentInfo argument that can be used to refer
     * to ANY Argument subclass. Usually, you will want to get an ArgumentInfo object
     * directly from a specific Argument subclass.
     *
     * @return ArgumentInfo info
     */
    @Deprecated
    public static ArgumentInfo info() {
        return ArgumentInfo.create(Argument.class, null);
    }
    
    // @TODO - FIX THIS. Need to make this do automatic coercion to class if provided?
    @Deprecated
    public static final boolean equivalent(Class<? extends Argument> clazz,
            Argument arg1, Argument arg2) {
        return arg1.isEquivalent(arg2) || arg2.isEquivalent(arg1);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T extends Argument> Optional<Converter<T>> findConverter(Class<T> type) {
        return Optional.ofNullable((Converter<T>) converters.get(type));
    }
    
    @FunctionalInterface
    @Deprecated
    public static interface Converter<T extends Argument> {
        
        Optional<T> from(Argument arg);
        
    }
    
    private final static Map<Class<?>, Converter<?>> converters = 
            new HashMap<>();
    
    private static <T extends Argument> void registerConverter(Class<T> type, Converter<T> converter) {
        converters.put(type, converter);
    }
    
    static {
        registerConverter(PArray.class, PArray::from);
        registerConverter(PBoolean.class, PBoolean::from);
        registerConverter(PBytes.class, PBytes::from);
        registerConverter(PError.class, PError::from);
        registerConverter(PMap.class, PMap::from);
        registerConverter(PNumber.class, PNumber::from);
        registerConverter(PReference.class, PReference::from);
        registerConverter(PResource.class, PResource::from);
        registerConverter(PString.class, PString::from);
        
        registerConverter(ArgumentInfo.class, ArgumentInfo::from);
        registerConverter(ComponentInfo.class, ComponentInfo::from);
        registerConverter(ControlInfo.class, ControlInfo::from);
        registerConverter(PortInfo.class, PortInfo::from);
        
        registerConverter(ComponentAddress.class, ComponentAddress::from);
        registerConverter(ControlAddress.class, ControlAddress::from);
        registerConverter(PortAddress.class, PortAddress::from);
        
    }

}
