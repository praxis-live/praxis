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
package org.praxislive.data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.praxislive.base.AbstractRootContainer;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.Protocol;
import org.praxislive.core.protocols.ComponentProtocol;
import org.praxislive.core.protocols.ContainerProtocol;
import org.praxislive.core.protocols.StartableProtocol;
import org.praxislive.core.types.PMap;


/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class DataRoot extends AbstractRootContainer {
    
    private final static ComponentInfo INFO;

    static {
        Set<Class<? extends Protocol>> protocols = new LinkedHashSet<>(3);
        protocols.add(ComponentProtocol.class);
        protocols.add(ContainerProtocol.class);
        protocols.add(StartableProtocol.class);
        Map<String, ControlInfo> controls = new LinkedHashMap<>(10);
        controls.put(ComponentProtocol.INFO, ComponentProtocol.INFO_INFO);
        controls.put(ContainerProtocol.ADD_CHILD, ContainerProtocol.ADD_CHILD_INFO);
        controls.put(ContainerProtocol.REMOVE_CHILD, ContainerProtocol.REMOVE_CHILD_INFO);
        controls.put(ContainerProtocol.CHILDREN, ContainerProtocol.CHILDREN_INFO);
        controls.put(ContainerProtocol.CONNECT, ContainerProtocol.CONNECT_INFO);
        controls.put(ContainerProtocol.DISCONNECT, ContainerProtocol.DISCONNECT_INFO);
        controls.put(ContainerProtocol.CONNECTIONS, ContainerProtocol.CONNECTIONS_INFO);
        controls.put(StartableProtocol.START, StartableProtocol.START_INFO);
        controls.put(StartableProtocol.STOP, StartableProtocol.STOP_INFO);
        controls.put(StartableProtocol.IS_RUNNING, StartableProtocol.IS_RUNNING_INFO);
        INFO = ComponentInfo.create(controls, Collections.EMPTY_MAP, protocols, PMap.EMPTY);
    }

    @Override
    protected ComponentInfo getInfo() {
        return INFO;
    }
    
}
