/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2016 Neil C Smith.
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
package net.neilcsmith.praxis.hub;

import java.util.EnumSet;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentType;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.interfaces.ComponentFactoryService;
import net.neilcsmith.praxis.core.interfaces.RootFactoryService;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.impl.AbstractAsyncControl;
import net.neilcsmith.praxis.impl.AbstractRoot;

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
        registerInterface(ComponentFactoryService.class);
        registerInterface(RootFactoryService.class);
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
