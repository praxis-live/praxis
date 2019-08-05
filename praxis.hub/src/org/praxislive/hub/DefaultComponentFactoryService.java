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
package org.praxislive.hub;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.praxislive.base.AbstractAsyncControl;
import org.praxislive.base.AbstractRoot;
import org.praxislive.core.Call;
import org.praxislive.core.Component;
import org.praxislive.core.services.ComponentFactory;
import org.praxislive.core.ComponentType;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.Root;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.RootHub;
import org.praxislive.core.services.ComponentFactoryService;
import org.praxislive.core.services.RootFactoryService;
import org.praxislive.core.services.Service;
import org.praxislive.core.services.Services;
import org.praxislive.core.types.PError;
import org.praxislive.core.types.PReference;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class DefaultComponentFactoryService extends AbstractRoot
        implements RootHub.ServiceProvider {

    private final ComponentRegistry registry;
    private final NewInstanceControl newInstance;
    private final NewRootInstanceControl newRoot;

    public DefaultComponentFactoryService() {
        registry = ComponentRegistry.getInstance();
        newInstance = new NewInstanceControl();
        newRoot = new NewRootInstanceControl();
    }

    @Override
    public List<Class<? extends Service>> services() {
        return Stream.of(ComponentFactoryService.class,
                RootFactoryService.class)
                .collect(Collectors.toList());
    }

    @Override
    protected void processCall(Call call, PacketRouter router) {
        switch (call.getToAddress().getID()) {
            case ComponentFactoryService.NEW_INSTANCE: {
                try {
                    newInstance.call(call, router);
                } catch (Exception ex) {
                    router.route(Call.createErrorCall(call, PError.create(ex)));
                }
            }
            break;
            case RootFactoryService.NEW_ROOT_INSTANCE: {
                try {
                    newRoot.call(call, router);
                } catch (Exception ex) {
                    router.route(Call.createErrorCall(call, PError.create(ex)));
                }
            }
            break;
            default:
                if (call.getType() == Call.Type.INVOKE ||
                        call.getType() == Call.Type.INVOKE_QUIET) {
                    router.route(Call.createErrorCall(call));
                }

        }

    }

    private class NewInstanceControl extends AbstractAsyncControl {

        @Override
        protected Call processInvoke(Call call) throws Exception {
            ComponentType type = ComponentType.coerce(call.getArgs().get(0));
            ComponentFactory factory = registry.getComponentFactory(type);
            if (factory.getFactoryService() != ComponentFactoryService.class) {
                ControlAddress altFactory = getLookup().find(Services.class)
                        .flatMap(srvs -> srvs.locate(factory.getFactoryService()))
                        .map(cmp -> ControlAddress.create(cmp, ComponentFactoryService.NEW_INSTANCE))
                        .orElseThrow(() -> new IllegalStateException("Alternative factory service not found"));
                        
                return Call.createCall(altFactory,
                        call.getToAddress(),
                        call.getTimecode(),
                        call.getArgs());
            } else {
                Component component = factory.createComponent(type);
                return Call.createReturnCall(call, PReference.wrap(component));
            }
        }

        @Override
        protected Call processResponse(Call call) throws Exception {
            return Call.createReturnCall(getActiveCall(), call.getArgs());
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

    }

}
