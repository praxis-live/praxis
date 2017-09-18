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
package net.neilcsmith.praxis.core.types;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.PortAddress;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ComponentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.info.PortInfo;

/**
 * Eventual replacement for Argument
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class Value extends Argument {

    /**
     * Values must override the default method to return a string representation
     * that is immutable.
     *
     * @return String representation
     */
    @Override
    public abstract String toString();

    /**
     * Values must override the default hashcode method.
     *
     * @return int hashcode
     */
    @Override
    public abstract int hashCode();

    /**
     * Values must override the default equals method. This method should only
     * return <code>true</code> if the supplied Object is of the same type as
     * the implementing Value. Values of an unknown type should be coerced
     * before calling this method. This method does not have to guarantee that
     * <code>this.equals(that) == this.toString().equals(that.toString())</code>
     *
     * @param obj
     * @return boolean
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * Check whether this Value is an empty value and has a zero length string
     * representation. Subclasses may wish to override this for efficiency if
     * the String representation is lazily created.
     *
     * @return boolean true if empty
     */
    public boolean isEmpty() {
        return (toString().length() == 0);
    }

    public boolean isEquivalent(Value value) {
        return this == value || this.toString().equals(value.toString());
    }

    public static class Type {

        @FunctionalInterface
        public static interface Converter<T extends Value> {

            Optional<T> from(Value arg);

        }

        @SuppressWarnings("unchecked")
        public static <T extends Value> Optional<Converter<T>> findConverter(Class<T> type) {
            return Optional.ofNullable((Converter<T>) converters.get(type));
        }

        private final static Map<Class<?>, Converter<?>> converters
                = new HashMap<>();

        private static <T extends Value> void registerConverter(Class<T> type, Converter<T> converter) {
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

}
