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

package org.praxislive.core;

import org.praxislive.core.info.PortInfo;

/**
 *
 * @author Neil C Smith
 */
public interface Port {
 
    public final static String IN = "in";
    public final static String OUT = "out";
    
    public void connect(Port port) throws PortConnectionException;
    
    public void disconnect(Port port);
    
    public void disconnectAll();
    
    public Port[] getConnections();
    
    public PortInfo getInfo();

    public void addListener(PortListener listener);

    public void removeListener(PortListener listener);
    
}
