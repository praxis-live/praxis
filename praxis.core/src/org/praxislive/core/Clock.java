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
package org.praxislive.core;

/**
 * High-precision nanosecond time source. This interface is only useful for
 * measuring elapsed time from the same Clock instance.
 * 
 * Time values might be calculated using System::nanoTime, but on no account should
 * the clock time be compared with it.
 * 
 * Implementations of Clock should normally be obtained from RootHub.
 * 
 * @author Neil C Smith - http://www.neilcsmith.net
 */
public interface Clock {
    
    /**
     * Clock time in nanoseconds. Only useful for measuring elapsed time relative
     * to other calls to this method made on the same Clock instance.
     * 
     * @return current clock time in nanoseconds
     */
    public long getTime();
    
}
