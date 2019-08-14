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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.Call;
import org.praxislive.core.Component;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.Container;
import org.praxislive.core.Control;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.Lookup;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Port;
import org.praxislive.core.PortConnectionException;
import org.praxislive.core.PortListener;
import org.praxislive.core.Value;
import org.praxislive.core.VetoException;
import org.praxislive.core.protocols.ContainerProtocol;
import org.praxislive.core.services.ComponentFactoryService;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PReference;
import org.praxislive.core.types.PString;

/**
 *
 */
public abstract class AbstractContainer extends AbstractComponent implements Container {

    private final static Logger LOG = Logger.getLogger(AbstractContainer.class.getName());

    private final Map<String, Component> childMap;
    private final Set<PArray> connections;

    protected AbstractContainer() {
        childMap = new LinkedHashMap<>();
        connections = new LinkedHashSet<>();
        registerControl(ContainerProtocol.ADD_CHILD, new AddChildControl());
        registerControl(ContainerProtocol.REMOVE_CHILD, new RemoveChildControl());
        registerControl(ContainerProtocol.CHILDREN, new ChildrenControl());
        registerControl(ContainerProtocol.CONNECT, new ConnectControl());
        registerControl(ContainerProtocol.DISCONNECT, new DisconnectControl());
        registerControl(ContainerProtocol.CONNECTIONS, new ConnectionsControl());
    }

    @Override
    public Component getChild(String id) {
        return childMap.get(id);
    }

    @Override
    public String[] getChildIDs() {
        return childMap.keySet().toArray(new String[0]);
    }

    @Override
    public ComponentAddress getAddress(Component child) {
        ComponentAddress containerAddress = getAddress();
        String childID = getChildID(child);
        if (containerAddress == null || childID == null) {
            return null;
        } else {
            return ComponentAddress.of(containerAddress, childID);
        }
    }

    @Override
    public void hierarchyChanged() {
        childMap.values().forEach(Component::hierarchyChanged);
    }

    @Override
    public Lookup getLookup() {
        return super.getLookup();
    }

    protected void addChild(String id, Component child) throws VetoException {
        if (childMap.putIfAbsent(Objects.requireNonNull(id),
                Objects.requireNonNull(child)) != null) {
            throw new VetoException("Child ID already in use");
        }
        try {
            notifyChild(child);
        } catch (VetoException ex) {
            childMap.remove(id);
            throw new VetoException();
        }
        child.hierarchyChanged();
    }
    
    protected void notifyChild(Component child) throws VetoException {
        child.parentNotify(this);
    }
    
    protected Component removeChild(String id) {
        Component child = childMap.remove(id);
        if (child != null) {
            try {
                child.parentNotify(null);
            } catch (VetoException ex) {
                // it is an error for children to throw exception on removal
                // should we throw an error?
                LOG.log(Level.SEVERE, "Child throwing Veto on removal", ex);
            }
            child.hierarchyChanged();
        }
        return child;
    }

    protected String getChildID(Component child) {
        for (Map.Entry<String, Component> entry : childMap.entrySet()) {
            if (entry.getValue() == child) {
                return entry.getKey();
            }
        }
        return null;
    }

    protected void connect(String component1, String port1, String component2, String port2)
            throws PortConnectionException {
        handleConnection(true,
                PString.of(component1),
                PString.of(port1),
                PString.of(component2),
                PString.of(port2));
    }

    protected void disconnect(String component1, String port1, String component2, String port2) {
        try {
            handleConnection(false,
                    PString.of(component1),
                    PString.of(port1),
                    PString.of(component2),
                    PString.of(port2));
        } catch (PortConnectionException ex) {
            Logger.getLogger(AbstractContainer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleConnection(boolean connect, PString c1id, PString p1id, PString c2id, PString p2id)
            throws PortConnectionException {
        try {
            Component c1 = getChild(c1id.toString());
            final Port p1 = c1.getPort(p1id.toString());
            Component c2 = getChild(c2id.toString());
            final Port p2 = c2.getPort(p2id.toString());

            final PArray connection = PArray.of(c1id, p1id, c2id, p2id);

            if (connect) {
                p1.connect(p2);
                connections.add(connection);
                PortListener listener = new ConnectionListener(p1, p2, connection);
                p1.addListener(listener);
                p2.addListener(listener);
            } else {
                p1.disconnect(p2);
                connections.remove(connection);
            }
        } catch (Exception ex) {
            LOG.log(Level.FINE, "Can't connect ports.", ex);
            throw new PortConnectionException("Can't connect " + c1id + "!" + p1id
                    + " to " + c2id + "!" + p2id);
        }
    }

    protected class AddChildControl extends AbstractAsyncControl {

        @Override
        protected Call processInvoke(Call call) throws Exception {
            List<Value> args = call.args();
            if (args.size() < 2) {
                throw new IllegalArgumentException("Invalid arguments");
            }
            if (!ComponentAddress.isValidID(args.get(0).toString())) {
                throw new IllegalArgumentException("Invalid Component ID");
            }
            ControlAddress to = ControlAddress.of(findService(ComponentFactoryService.class),
                    ComponentFactoryService.NEW_INSTANCE);
            return Call.create(to, call.to(), call.time(), args.get(1));
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            List<Value> args = call.args();
            if (args.size() < 1) {
                throw new IllegalArgumentException("Invalid response");
            }
            Component c = (Component) ((PReference) args.get(0)).getReference();
            Call active = getActiveCall();
            addChild(active.args().get(0).toString(), c);
            return active.reply();
        }
    }

    protected class RemoveChildControl implements Control {

        @Override
        public void call(Call call, PacketRouter router) throws Exception {
            removeChild(call.args().get(0).toString());
            router.route(call.reply());
        }

    }

    protected class ChildrenControl implements Control {

        @Override
        public void call(Call call, PacketRouter router) throws Exception {
            PArray response = childMap.keySet().stream()
                    .map(PString::of)
                    .collect(PArray.collector());
            router.route(call.reply(response));
        }

    }

    protected class ConnectControl implements Control {

        @Override
        public void call(Call call, PacketRouter router) throws Exception {
            handleConnection(true,
                    PString.coerce(call.getArgs().get(0)),
                    PString.coerce(call.getArgs().get(1)),
                    PString.coerce(call.getArgs().get(2)),
                    PString.coerce(call.getArgs().get(3)));
            router.route(call.reply());
        }

    }

    protected class DisconnectControl implements Control {

        @Override
        public void call(Call call, PacketRouter router) throws Exception {
            handleConnection(false,
                    PString.coerce(call.getArgs().get(0)),
                    PString.coerce(call.getArgs().get(1)),
                    PString.coerce(call.getArgs().get(2)),
                    PString.coerce(call.getArgs().get(3)));
            router.route(call.reply());
        }

    }

    protected class ConnectionsControl implements Control {

        @Override
        public void call(Call call, PacketRouter router) throws Exception {
            PArray response = PArray.of(connections);
            router.route(call.reply(response));
        }

    }

    private class ConnectionListener implements PortListener {

        Port p1;
        Port p2;
        PArray connection;

        private ConnectionListener(Port p1, Port p2, PArray connection) {
            this.p1 = p1;
            this.p2 = p2;
            this.connection = connection;
        }

        @Override
        public void connectionsChanged(Port source) {
            if (Arrays.asList(p1.getConnections()).contains(p2)
                    && Arrays.asList(p2.getConnections()).contains(p1)) {
            } else {
                LOG.log(Level.FINEST, "Removing connection\n{0}", connection);
                connections.remove(connection);
                p1.removeListener(this);
                p2.removeListener(this);
            }
        }
    }

}
