/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 Neil C Smith.
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
package net.neilcsmith.praxis.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.Container;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.PortConnectionException;
import net.neilcsmith.praxis.core.PortListener;
import net.neilcsmith.praxis.core.VetoException;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.interfaces.ComponentFactoryService;
import net.neilcsmith.praxis.core.interfaces.ContainerInterface;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractContainer extends AbstractComponent implements Container {

    private final static Logger LOG = Logger.getLogger(AbstractContainer.class.getName());
    private Map<String, Component> childMap;
    private Set<PArray> connections;
    boolean childInfoValid;

    protected AbstractContainer() {
        this(true, true);
    }

    protected AbstractContainer(boolean containerInterface, boolean componentInterface) {
        super(componentInterface);
        childMap = new LinkedHashMap<String, Component>();
        connections = new LinkedHashSet<PArray>();
        if (containerInterface) {
            buildContainerInterface();
        }
    }

    private void buildContainerInterface() {
        registerControl(ContainerInterface.ADD_CHILD, new AddChildControl());
        registerControl(ContainerInterface.REMOVE_CHILD, new RemoveChildControl());
        registerControl(ContainerInterface.CHILDREN, new ChildrenControl());
        registerControl(ContainerInterface.CONNECT, new ConnectionControl(true));
        registerControl(ContainerInterface.DISCONNECT, new ConnectionControl(false));
        registerControl(ContainerInterface.CONNECTIONS, new ConnectionListControl());
        registerInterface(ContainerInterface.INSTANCE);

    }

    public void addChild(String id, Component child) throws VetoException {
        if (id == null || child == null) {
            throw new NullPointerException();
        }
        if (childMap.containsKey(id)) {
            throw new VetoException("Child ID already in use");
        }
        childMap.put(id, child);
        try {
            child.parentNotify(this);
        } catch (VetoException ex) {
            childMap.remove(id);
            throw new VetoException();
        }
        childInfoValid = false;
    }

    public Component removeChild(String id) {
        Component child = childMap.remove(id);
        if (child != null) {
            try {
                child.parentNotify(null);
            } catch (VetoException ex) {
                // it is an error for children to throw exception on removal
                // should we throw an error?
            }
            childInfoValid = false;
        }
        return child;
    }

    public Component getChild(String id) {
        return childMap.get(id);
    }

    public String getChildID(Component child) {
        Set<Map.Entry<String, Component>> entries = childMap.entrySet();
        for (Map.Entry<String, Component> entry : entries) {
            if (entry.getValue() == child) {
                return entry.getKey();
            }
        }
        return null;
    }

    public ComponentAddress getAddress(Component child) {
        ComponentAddress containerAddress = getAddress();
        String childID = getChildID(child);
        if (containerAddress == null || childID == null) {
            return null;
        } else {
            return ComponentAddress.create(containerAddress, childID);
        }   
    }

    public String[] getChildIDs() {
        Set<String> keyset = childMap.keySet();
        return keyset.toArray(new String[keyset.size()]);
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        for (Map.Entry<String, Component> entry : childMap.entrySet()) {
            entry.getValue().hierarchyChanged();
        }

    }

    @Override
    public Lookup getLookup() {
        return super.getLookup();
    }

    private class AddChildControl extends AbstractAsyncControl {

        @Override
        protected Call processInvoke(Call call) throws Exception {
            CallArguments args = call.getArgs();
            if (args.getSize() < 2) {
                throw new IllegalArgumentException("Invalid arguments");
            }
            if (!ComponentAddress.isValidID(args.get(0).toString())) {
                throw new IllegalArgumentException("Invalid Component ID");
            }
            ControlAddress to = ControlAddress.create(
                    findService(ComponentFactoryService.INSTANCE),
                    ComponentFactoryService.NEW_INSTANCE);
            return Call.createCall(to, getAddress(), call.getTimecode(), args.get(1));
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            CallArguments args = call.getArgs();
            if (args.getSize() < 1) {
                throw new IllegalArgumentException("Invalid response");
            }
            Component c = (Component) ((PReference) args.get(0)).getReference();
            Call active = getActiveCall();
            addChild(active.getArgs().get(0).toString(), c);
            return Call.createReturnCall(active, CallArguments.EMPTY);
        }

        public ControlInfo getInfo() {
            return ContainerInterface.ADD_CHILD_INFO;
        }
    }

    private class RemoveChildControl extends SimpleControl {

        private RemoveChildControl() {
            super(ContainerInterface.REMOVE_CHILD_INFO);
        }

        @Override
        protected CallArguments process(long time, CallArguments args, boolean quiet) throws Exception {
            removeChild(args.get(0).toString());
            return CallArguments.EMPTY;
        }
    }

    private class ChildrenControl extends SimpleControl {

        private ChildrenControl() {
            super(ContainerInterface.CHILDREN_INFO);
        }

        @Override
        protected CallArguments process(long time, CallArguments args, boolean quiet) throws Exception {
            if (childMap.isEmpty()) {
                return CallArguments.create(PArray.EMPTY);
            }
            List<PString> children = new ArrayList<PString>(childMap.size());
            for (String child : childMap.keySet()) {
                children.add(PString.valueOf(child));
            }
            return CallArguments.create(PArray.valueOf(children));
        }
    }

    private class ConnectionControl extends SimpleControl {

        private final boolean connect;

        private ConnectionControl(boolean connect) {
            super(connect ? ContainerInterface.CONNECT_INFO
                    : ContainerInterface.DISCONNECT_INFO);
            this.connect = connect;
        }

        @Override
        protected CallArguments process(long time, CallArguments args, boolean quiet) throws Exception {

            if (args.getSize() < 4) {
                throw new IllegalArgumentException();
            }
            PString c1id = PString.coerce(args.get(0));
            PString p1id = PString.coerce(args.get(1));
            PString c2id = PString.coerce(args.get(2));
            PString p2id = PString.coerce(args.get(3));
            try {
                Component c1 = getChild(c1id.toString());
                final Port p1 = c1.getPort(p1id.toString());
                Component c2 = getChild(c2id.toString());
                final Port p2 = c2.getPort(p2id.toString());

                final PArray connection = PArray.valueOf(c1id, p1id, c2id, p2id);

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
                return CallArguments.EMPTY;
            } catch (Exception ex) {
                LOG.log(Level.FINE, "Can't connect ports.", ex);
                throw new PortConnectionException("Can't connect " + c1id + "!" + p1id
                        + " to " + c2id + "!" + p2id);
            }
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

        public void connectionsChanged(Port source) {
            if (Arrays.asList(p1.getConnections()).contains(p2)
                    && Arrays.asList(p2.getConnections()).contains(p1)) {
                return;
            } else {
                LOG.finest("Removing connection\n" + connection);
                connections.remove(connection);
                p1.removeListener(this);
                p2.removeListener(this);
            }
        }
    }

    private class ConnectionListControl extends SimpleControl {

        private ConnectionListControl() {
            super(ContainerInterface.CONNECTIONS_INFO);
        }

        @Override
        protected CallArguments process(long time, CallArguments args, boolean quiet) throws Exception {
            return CallArguments.create(PArray.valueOf(connections));
        }
    }
}
