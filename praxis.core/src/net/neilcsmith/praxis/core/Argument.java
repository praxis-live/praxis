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

import net.neilcsmith.praxis.core.info.ArgumentInfo;
import java.io.Serializable;

/**
 * The abstract base class of all arguments passed around in the Praxis environment,
 * either in Calls or through ControlPorts.
 * 
 * All Argument subclasses should be immutable (PReference being the only exception),
 * and have an immutable string representation. Arguments should provide static methods
 * for creating from a string representation, and for creating an ArgumentInfo object.
 * Argument should also provide a static coerce(Argument arg) method that casts the 
 * supplied argument to the relevant type, or attempts to create from its string
 * representation.
 *
 *
 * @author Neil C Smith
 */
public abstract class Argument implements Serializable {
    
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
     * <code>this.equals(that) == this.toString().eauals(that.toString()</code>
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


    public boolean isEquivalent(Argument arg) {
        return this.toString().equals(arg.toString());
    }
    
    /**
     * Use this method to return an ArgumentInfo argument that can be used to refer
     * to ANY Argument subclass. Usually, you will want to get an ArgumentInfo object
     * directly from a specific Argument subclass.
     *
     * @return ArgumentInfo info
     */
    public static ArgumentInfo info() {
        return ArgumentInfo.create(Argument.class, null);
    }
    
    // @TODO - FIX THIS. Need to make this do automatic coercion to class if provided?
    public static final boolean equivalent(Class<? extends Argument> clazz,
            Argument arg1, Argument arg2) {
        return arg1.isEquivalent(arg2) || arg2.isEquivalent(arg1);
    }


}
