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

/**
 * Exception thrown if a String is not a valid representation of the required
 * Argument subclass.
 *
 * @author Neil C Smith
 */
public class ArgumentFormatException extends Exception {

    /**
     * Creates a new instance of <code>ArgumentFormatException</code> without detail message.
     */
    public ArgumentFormatException() {
    }


    /**
     * Constructs an instance of <code>ArgumentFormatException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ArgumentFormatException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>ArgumentFormatException</code> with the specified cause.
     * @param cause the cause.
     */
    public ArgumentFormatException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an instance of <code>ArgumentFormatException</code> with the specified detail message
     * and cause.
     * @param msg the detail message.
     * @param cause the cause.
     */
    public ArgumentFormatException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
