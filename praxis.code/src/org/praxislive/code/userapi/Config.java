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
package org.praxislive.code.userapi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
public abstract class Config {

    /**
     * Control automatic port creation for properties, triggers, etc.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Port {

        /**
         * Whether or not to create a port.
         *
         * @return false to suppress port creation
         */
        boolean value();
    }

    /**
     * Mark a feature as "preferred" - particularly important for presenting to
     * humans.
     * <p>
     * This will add a key to the info for this feature. It is up to an editor
     * whether to use or ignore this key (eg. the PraxisLIVE graph editor will
     * show properties marked this way on the graph itself).
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Preferred {

    }

}
