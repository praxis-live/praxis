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
package net.neilcsmith.praxis.components;

import net.neilcsmith.praxis.components.container.ContainerInput;
import net.neilcsmith.praxis.components.container.ContainerOutput;
import net.neilcsmith.praxis.components.container.ContainerProperty;
import net.neilcsmith.praxis.components.container.UserContainer;
import net.neilcsmith.praxis.components.routing.Send;
import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactoryProvider;
import net.neilcsmith.praxis.impl.AbstractComponentFactory;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class FactoryProvider implements ComponentFactoryProvider {

    private final static ComponentFactory factory = new Factory();

    public ComponentFactory getFactory() {
        return factory;
    }

    private static class Factory extends AbstractComponentFactory {

        private Factory() {
            build();
        }

        private void build() {
            addComponent("core:start-trigger", StartTrigger.class);

            //ROUTING
            addComponent("core:routing:send", Send.class);

            // CONTAINER
            addComponent("core:container", data(UserContainer.class));
            addComponent("core:container:property", data(ContainerProperty.class).deprecated());
            addComponent("core:container:input", data(ContainerInput.class));
            addComponent("core:container:output", data(ContainerOutput.class));
            
        }
    }
}
