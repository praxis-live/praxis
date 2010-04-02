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
 * Interface to be informed when a Task has completed or thrown an exception.
 * @author Neil C Smith
 */
public interface TaskListener {
    
    /**
     * Task complete.
     *
     * @param time
     * @param id
     * @param arg
     */
    public void taskCompleted(long time, long id, Argument arg);
    
    /**
     * Task threw an Exception.
     * @param time
     * @param id
     * @param arg
     */
    public void taskError(long time, long id, Argument arg);

}
