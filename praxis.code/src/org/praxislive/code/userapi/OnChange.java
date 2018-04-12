/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
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

package org.praxislive.code.userapi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Name a method to call when a property field (see {@link P @P}) changes in
 * response to an external port or control call. This should be set on the field,
 * not the method to be called.
 * <p>
 * When used with a resource loading property, this method will be called after the
 * new resource has been set on the field and is available to use.
 * <p>
 * The method will not be called via internal changes or animation on a Property
 * - to respond to these see {@link Property#values()} or
 * {@link Property#valuesAs(java.lang.Class)} 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OnChange {

    /**
     * The name of a method to call.
     *
     * @return method name
     */
    String value();
}
