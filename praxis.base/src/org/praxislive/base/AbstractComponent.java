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
package org.praxislive.base;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.praxislive.core.Call;
import org.praxislive.core.Component;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.Container;
import org.praxislive.core.Control;
import org.praxislive.core.Lookup;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Port;
import org.praxislive.core.VetoException;
import org.praxislive.core.protocols.ComponentProtocol;
import org.praxislive.core.services.Service;
import org.praxislive.core.services.ServiceUnavailableException;
import org.praxislive.core.services.Services;

/**
 *
 */
public abstract class AbstractComponent implements Component {

    private final Map<String, Control> controls;
    private final Map<String, Port> ports;

    private Container parent;

    protected AbstractComponent() {
        controls = new LinkedHashMap<>();
        controls.put(ComponentProtocol.INFO, new InfoControl());
        ports = new LinkedHashMap<>();
    }

    @Override
    public Container getParent() {
        return parent;
    }

    @Override
    public void parentNotify(Container parent) throws VetoException {
        if (parent == null) {
            if (this.parent != null) {
                this.parent = null;
                disconnectAll();
            }
        } else {
            if (this.parent != null) {
                throw new VetoException();
            }
            this.parent = parent;
        }
    }

    @Override
    public void hierarchyChanged() {
    }

    @Override
    public Control getControl(String id) {
        return controls.get(id);
    }

    @Override
    public Port getPort(String id) {
        return ports.get(id);
    }
    
    protected ComponentAddress getAddress() {
        if (parent != null) {
            return parent.getAddress(this);
        } else {
            return null;
        }
    }
    
    protected Lookup getLookup() {
        return parent == null ? Lookup.EMPTY : parent.getLookup();
    }
    
    protected ComponentAddress findService(Class<? extends Service> service)
            throws ServiceUnavailableException {
        return getLookup().find(Services.class)
                .flatMap(sm -> sm.locate(service))
                .orElseThrow(ServiceUnavailableException::new);
    }
    
    protected void disconnectAll() {
        ports.values().forEach(Port::disconnectAll);
    }
    
    protected final void registerControl(String id, Control control) {
        if (controls.putIfAbsent(Objects.requireNonNull(id),
                Objects.requireNonNull(control)) != null) {
            throw new IllegalArgumentException();
        }
    }
    
    protected final void unregisterControl(String id) {
        controls.remove(id);
    }
    
    protected final void registerPort(String id, Port port) {
        if (ports.putIfAbsent(Objects.requireNonNull(id),
                Objects.requireNonNull(port)) != null) {
            throw new IllegalArgumentException();
        }
    }
    
    protected final void unregisterPort(String id) {
        Port port = ports.remove(id);
        if (port != null) {
            port.disconnectAll();
        }
    }

    private class InfoControl implements Control {

        @Override
        public void call(Call call, PacketRouter router) throws Exception {
            router.route(Call.createReturnCall(call, getInfo()));
        }

    }

}
