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

package org.praxislive.core.interfaces;

import org.praxislive.core.InterfaceDefinition;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ComponentType;
import org.praxislive.core.PortAddress;
import org.praxislive.core.info.ArgumentInfo;
import org.praxislive.core.info.ControlInfo;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
@Deprecated
public class ConnectionManager extends InterfaceDefinition {

    public final static String CONNECT = "connect";
    public final static String DISCONNECT = "disconnect";

    private final static ConnectionManager instance = new ConnectionManager();

    private ControlInfo connectInfo;
    private ControlInfo disconnectInfo;

    private ConnectionManager() {
        ArgumentInfo addressInfo = PortAddress.info();
        ArgumentInfo[] inputs = new ArgumentInfo[] {addressInfo, addressInfo};
        ArgumentInfo[] outputs = new ArgumentInfo[0];
        connectInfo = ControlInfo.createFunctionInfo(inputs, outputs, null);
        disconnectInfo = ControlInfo.createFunctionInfo(inputs, outputs, null);

    }


    @Override
    public String[] getControls() {
        return new String[] {CONNECT, DISCONNECT};
    }

    @Override
    public ControlInfo getControlInfo(String control) {
        if (CONNECT.equals(control)) {
            return connectInfo;
        }
        if (DISCONNECT.equals(control)) {
            return disconnectInfo;
        }
        throw new IllegalArgumentException();
    }

    public static ConnectionManager getInstance() {
        return instance;
    }

}
