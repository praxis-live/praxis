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
package org.praxislive.hub;

import java.util.EnumSet;
import org.praxislive.core.Call;
import org.praxislive.core.Component;
import org.praxislive.core.services.ComponentFactory;
import org.praxislive.core.ComponentType;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.Root;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.services.ComponentFactoryService;
import org.praxislive.core.services.RootFactoryService;
import org.praxislive.core.types.PReference;
import org.praxislive.impl.AbstractAsyncControl;
import org.praxislive.impl.AbstractRoot;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class DefaultComponentFactoryService extends AbstractRoot {

    private final ComponentRegistry registry;

    public DefaultComponentFactoryService() {
        super(EnumSet.noneOf(Caps.class));
        registry = ComponentRegistry.getInstance();
        registerControl(ComponentFactoryService.NEW_INSTANCE, new NewInstanceControl());
        registerControl(RootFactoryService.NEW_ROOT_INSTANCE, new NewRootInstanceControl());
        registerProtocol(ComponentFactoryService.class);
        registerProtocol(RootFactoryService.class);
    }

    private class NewInstanceControl extends AbstractAsyncControl {

        @Override
        protected Call processInvoke(Call call) throws Exception {
            ComponentType type = ComponentType.coerce(call.getArgs().get(0));
            ComponentFactory factory = registry.getComponentFactory(type);
            if (factory.getFactoryService() != ComponentFactoryService.class) {
                ControlAddress altFactory = 
                        ControlAddress.create(
                                findService(factory.getFactoryService()),
                                        ComponentFactoryService.NEW_INSTANCE);
                return Call.createCall(altFactory, getAddress(), call.getTimecode(), call.getArgs());
            } else {
                Component component = factory.createComponent(type);
                return Call.createReturnCall(call, PReference.wrap(component));
            }
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            return Call.createReturnCall(getActiveCall(), call.getArgs());
        }

        @Override
        public ControlInfo getInfo() {
            return ComponentFactoryService.NEW_INSTANCE_INFO;
        }
    }

    private class NewRootInstanceControl extends AbstractAsyncControl {

        @Override
        protected Call processInvoke(Call call) throws Exception {
            ComponentType type = ComponentType.coerce(call.getArgs().get(0));
            ComponentFactory factory = registry.getRootComponentFactory(type);
            Root root = factory.createRootComponent(type);
            return Call.createReturnCall(call, PReference.wrap(root));
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ControlInfo getInfo() {
            return RootFactoryService.NEW_ROOT_INSTANCE_INFO;
        }
    }

}
