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

package net.neilcsmith.praxis.video;

/**
 *
 * @author Neil C Smith
 */
public class InvalidVideoResourceException extends Exception {

    /**
     * Creates a new instance of <code>InvalidVideoResourceException</code> without detail message.
     */
    public InvalidVideoResourceException() {
    }


    /**
     * Constructs an instance of <code>InvalidVideoResourceException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InvalidVideoResourceException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>InvalidVideoResourceException</code> with the specified cause.
     * @param cause the cause.
     */
    public InvalidVideoResourceException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an instance of <code>InvalidVideoResourceException</code> with the specified detail message
     * and cause.
     * @param msg the detail message.
     * @param cause the cause.
     */
    public InvalidVideoResourceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
