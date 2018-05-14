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
 * Name a method to call when a property field (see {@link P @P}) has an error
 * in response to an external port or control call. This should be set on the
 * field, not the method to be called.
 * <p>
 * When used with a resource loading property, this method will be called if the
 * designated resource fails to load for any reason. The value of the field will
 * not be changed.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OnError {

    /**
     * The name of a method to call.
     *
     * @return method name
     */
    String value();
}
