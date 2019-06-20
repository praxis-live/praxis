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
package org.praxislive.core.protocols;

import java.util.stream.Stream;
import org.praxislive.core.ComponentType;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.Info;
import org.praxislive.core.Protocol;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PString;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ContainerProtocol implements Protocol {

    public final static ContainerProtocol INSTANCE = new ContainerProtocol();
    public final static String ADD_CHILD = "add-child";
    public final static String REMOVE_CHILD = "remove-child";
    public final static String CHILDREN = "children";
    public final static String CONNECT = "connect";
    public final static String DISCONNECT = "disconnect";
    public final static String CONNECTIONS = "connections";
    private final static ArgumentInfo STRING = PString.info();
    public final static ControlInfo ADD_CHILD_INFO
            = ControlInfo.createFunctionInfo(
                    new ArgumentInfo[]{STRING, ComponentType.info()},
                    new ArgumentInfo[0],
                    PMap.EMPTY);
    public final static ControlInfo REMOVE_CHILD_INFO
            = ControlInfo.createFunctionInfo(
                    new ArgumentInfo[]{STRING},
                    new ArgumentInfo[0],
                    PMap.EMPTY);
    public final static ControlInfo CHILDREN_INFO
            = ControlInfo.createReadOnlyPropertyInfo(
                    new ArgumentInfo[]{PArray.info()},
                    PMap.EMPTY);
    public final static ControlInfo CONNECT_INFO
            = ControlInfo.createFunctionInfo(
                    new ArgumentInfo[]{STRING, STRING, STRING, STRING},
                    new ArgumentInfo[0],
                    PMap.EMPTY);
    public final static ControlInfo DISCONNECT_INFO
            = ControlInfo.createFunctionInfo(
                    new ArgumentInfo[]{STRING, STRING, STRING, STRING},
                    new ArgumentInfo[0],
                    PMap.EMPTY);
    public final static ControlInfo CONNECTIONS_INFO
            = ControlInfo.createReadOnlyPropertyInfo(
                    new ArgumentInfo[]{PArray.info()},
                    PMap.EMPTY);

    public static final ComponentInfo API_INFO = Info.component(cmp -> cmp
            .protocol(ContainerProtocol.class)
            .control(ADD_CHILD, ADD_CHILD_INFO)
            .control(REMOVE_CHILD, REMOVE_CHILD_INFO)
            .control(CHILDREN, CHILDREN_INFO)
            .control(CONNECT, CONNECT_INFO)
            .control(DISCONNECT, DISCONNECT_INFO)
            .control(CONNECTIONS, CONNECTIONS_INFO)
    );

    @Override
    public Stream<String> controls() {
        return Stream.of(ADD_CHILD, REMOVE_CHILD, CHILDREN,
                CONNECT, DISCONNECT, CONNECTIONS);
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
