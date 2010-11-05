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
package net.neilcsmith.praxis.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.Container;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.InvalidChildException;
import net.neilcsmith.praxis.core.ParentVetoException;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.interfaces.ComponentFactoryService;
import net.neilcsmith.praxis.core.interfaces.ContainerInterface;
import net.neilcsmith.praxis.core.types.PReference;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractContainer extends AbstractComponent implements Container {

    private Map<String, Component> childMap;
    boolean childInfoValid;

    protected AbstractContainer() {
        this(true, true);
    }
    
    protected AbstractContainer(boolean containerInterface, boolean componentInterface) {
        super(componentInterface);
        childMap = new LinkedHashMap<String, Component>();
        if (containerInterface) {
            buildContainerInterface();
        }
    }

    private void buildContainerInterface() {
        registerControl(ContainerInterface.ADD_CHILD, new AddChildControl());
        registerControl(ContainerInterface.REMOVE_CHILD, new RemoveChildControl());
        registerControl(ContainerInterface.CONNECT, new ConnectionControl(true));
        registerControl(ContainerInterface.DISCONNECT, new ConnectionControl(false));
        registerInterface(ContainerInterface.INSTANCE);

    }

    public void addChild(String id, Component child) throws InvalidChildException {
        if (id == null || child == null) {
            throw new NullPointerException();
        }
        if (childMap.containsKey(id)) {
            throw new InvalidChildException("Child ID already in use");
        }
        childMap.put(id, child);
        try {
            child.parentNotify(this);
        } catch (ParentVetoException ex) {
            childMap.remove(id);
            throw new InvalidChildException();
        }
        childInfoValid = false;
    }

    public Component removeChild(String id) {
        Component child = childMap.remove(id);
        if (child != null) {
            try {
                child.parentNotify(null);
            } catch (ParentVetoException ex) {
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
        return ComponentAddress.create(getAddress(), getChildID(child));
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

    private class AddChildControl extends AbstractAsyncControl {
        
        @Override
        protected Call processInvoke(Call call) throws Exception {
            CallArguments args = call.getArgs();
            if (args.getCount() < 2) {
                throw new IllegalArgumentException("Invalid arguments");
            }
            if (!ComponentAddress.isValidID(args.getArg(0).toString())) {
                throw new IllegalArgumentException("Invalid Component ID");
            }
            ControlAddress to = ControlAddress.create(
                    findService(ComponentFactoryService.INSTANCE),
                    ComponentFactoryService.NEW_INSTANCE);
            return Call.createCall(to, getAddress(), call.getTimecode(), args.getArg(1));
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            CallArguments args = call.getArgs();
            if (args.getCount() < 1) {
                throw new IllegalArgumentException("Invalid response");
            }
            Component c = (Component) ((PReference) args.getArg(0)).getReference();
            Call active = getActiveCall();
            addChild(active.getArgs().getArg(0).toString(), c);
            return Call.createReturnCall(active, CallArguments.EMPTY);
        }

        public ControlInfo getInfo() {
            return ContainerInterface.ADD_CHILD_INFO;
        }
        
    }

    private class RemoveChildControl extends SimpleControl {


        @Override
        protected CallArguments process(CallArguments args, boolean quiet) throws Exception {
            removeChild(args.getArg(0).toString());
            return CallArguments.EMPTY;
        }

        public ControlInfo getInfo() {
            return ContainerInterface.REMOVE_CHILD_INFO;
        }
    }

    private class ConnectionControl extends SimpleControl {

        private final boolean connect;

        private ConnectionControl(boolean connect) {
            this.connect = connect;
        }

        @Override
        protected CallArguments process(CallArguments args, boolean quiet) throws Exception {
            if (args.getCount() < 4) {
                throw new IllegalArgumentException();
            }
            Component c1 = getChild(args.getArg(0).toString());
            Port p1 = c1.getPort(args.getArg(1).toString());
            Component c2 = getChild(args.getArg(2).toString());
            Port p2 = c2.getPort(args.getArg(3).toString());
            if (connect) {
                p1.connect(p2);
            } else {
                p1.disconnect(p2);
            }
            return CallArguments.EMPTY;
        }

        public ControlInfo getInfo() {
            return connect ? ContainerInterface.CONNECT_INFO :
                ContainerInterface.DISCONNECT_INFO;
        }

    }




}
