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

import org.praxislive.core.Call;
import org.praxislive.core.Component;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.Container;
import org.praxislive.core.Control;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.Lookup;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.protocols.StartableProtocol;
import org.praxislive.core.types.PBoolean;
import org.praxislive.core.types.PError;
import org.praxislive.core.types.PString;

/**
 *
 */
public abstract class AbstractRootContainer extends AbstractRoot {

    private final ContainerImpl container;
    private ComponentAddress address;

    protected AbstractRootContainer() {
        container = new ContainerImpl();
        registerControl(StartableProtocol.START, (call, router) -> {
            setRunning();
            router.route(Call.createReturnCall(call));
        });
        registerControl(StartableProtocol.STOP, (call, router) -> {
            setIdle();
            router.route(Call.createReturnCall(call));
        });
        registerControl(StartableProtocol.IS_RUNNING, (call, router) -> {
            router.route(Call.createReturnCall(call,
                    PBoolean.valueOf(getState() == State.ACTIVE_RUNNING)));
        });
    }

    @Override
    protected void processCall(Call call, PacketRouter router) {
        Control control = findControl(call.getToAddress());
        try {
            if (control != null) {
                control.call(call, router);
            } else {
                Call.Type type = call.getType();
                if (type == Call.Type.INVOKE || type == Call.Type.INVOKE_QUIET) {
                    router.route(Call.createErrorCall(call, PString.valueOf("Unknown control address : " + call.getToAddress())));
                }
            }
        } catch (Exception ex) {
            Call.Type type = call.getType();
            if (type == Call.Type.INVOKE || type == Call.Type.INVOKE_QUIET) {
                router.route(Call.createErrorCall(call, PError.create(ex)));
            }
        }
    }

    protected final void registerControl(String id, Control control) {
        container.registerControl(id, control);
    }

    protected final void unregisterControl(String id) {
        container.unregisterControl(id);
    }
    
    protected abstract ComponentInfo getInfo();

    private Control findControl(ControlAddress address) {
        Component comp = findComponent(address.getComponentAddress());
        if (comp != null) {
            return comp.getControl(address.getID());
        } else {
            return null;
        }
    }

    private Component findComponent(ComponentAddress address) {
        Component comp = container;
        for (int i = 1; i < address.getDepth(); i++) {
            if (comp instanceof Container) {
                comp = ((Container) comp).getChild(address.getComponentID(i));
            } else {
                return null;
            }
        }
        return comp;
    }
    
    private ComponentAddress getAddress() {
        if (address == null) {
            address = ComponentAddress.create("/" + getID());
        }
        return address;
    }

    private class ContainerImpl extends AbstractContainer {

        @Override
        public ComponentInfo getInfo() {
            return AbstractRootContainer.this.getInfo();
        }

        @Override
        public Lookup getLookup() {
            return AbstractRootContainer.this.getLookup();
        }

        @Override
        protected ComponentAddress getAddress() {
            return AbstractRootContainer.this.getAddress();
        }

        
        
    }

}
