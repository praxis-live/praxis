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
package org.praxislive.code.services.tools;

/**
 *
 * @author Neil C Smith
 */
public class CompilationException extends Exception {

    /**
     * Creates a new instance of <code>CompilationException</code> without
     * detail message.
     */
    public CompilationException() {
    }

    /**
     * Constructs an instance of <code>CompilationException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public CompilationException(String msg) {
        super(msg);
    }
    
    public CompilationException(Throwable cause) {
        super(cause);
    }
    
    public CompilationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
