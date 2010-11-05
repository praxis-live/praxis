/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 - Neil C Smith. All rights reserved.
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
package net.neilcsmith.praxis.core.interfaces;

import net.neilcsmith.praxis.core.ComponentType;
import net.neilcsmith.praxis.core.InterfaceDefinition;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ComponentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ContainerInterface extends InterfaceDefinition {

    public final static ContainerInterface INSTANCE = new ContainerInterface();
    public final static String ADD_CHILD = "add-child";
    public final static String REMOVE_CHILD = "remove-child";
    public final static String CHILDREN = "children";
    public final static String CONNECT = "connect";
    public final static String DISCONNECT = "disconnect";
    public final static String CONNECTIONS = "connections";
    private final static ArgumentInfo STRING = PString.info();
    public final static ControlInfo ADD_CHILD_INFO =
            ControlInfo.createFunctionInfo(
            new ArgumentInfo[]{STRING, ComponentType.info()},
            new ArgumentInfo[0],
            PMap.EMPTY);
    public final static ControlInfo REMOVE_CHILD_INFO =
            ControlInfo.createFunctionInfo(
            new ArgumentInfo[]{STRING},
            new ArgumentInfo[0],
            PMap.EMPTY);
    public final static ControlInfo CHILDREN_INFO =
            ControlInfo.createReadOnlyPropertyInfo(
            new ArgumentInfo[]{PArray.info()},
            PMap.EMPTY);
    public final static ControlInfo CONNECT_INFO =
            ControlInfo.createFunctionInfo(
            new ArgumentInfo[]{STRING, STRING, STRING, STRING},
            new ArgumentInfo[0],
            PMap.EMPTY);
    public final static ControlInfo DISCONNECT_INFO =
            ControlInfo.createFunctionInfo(
            new ArgumentInfo[]{STRING, STRING, STRING, STRING},
            new ArgumentInfo[0],
            PMap.EMPTY);
    public final static ControlInfo CONNECTIONS_INFO =
            ControlInfo.createReadOnlyPropertyInfo(
            new ArgumentInfo[]{PArray.info()},
            PMap.EMPTY);



    @Override
    public String[] getControls() {
        return new String[]{ADD_CHILD, REMOVE_CHILD, CHILDREN,
        CONNECT, DISCONNECT, CONNECTIONS};
    }

    @Override
    public ControlInfo getControlInfo(String control) {
        if (ADD_CHILD.equals(control)) {
            return ADD_CHILD_INFO;
        }
        if (REMOVE_CHILD.equals(control)) {
            return REMOVE_CHILD_INFO;
        }
        if (CHILDREN.equals(control)) {
            return CHILDREN_INFO;
        }
        if (CONNECT.equals(control)) {
            return CONNECT_INFO;
        }
        if (DISCONNECT.equals(control)) {
            return DISCONNECT_INFO;
        }
        if (CONNECTIONS.equals(control)) {
            return CONNECTIONS_INFO;
        }
        throw new IllegalArgumentException();
    }
}


