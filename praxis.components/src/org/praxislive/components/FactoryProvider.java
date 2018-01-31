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
package org.praxislive.components;

import org.praxislive.components.container.ContainerInput;
import org.praxislive.components.container.ContainerOutput;
import org.praxislive.components.container.UserContainer;
import org.praxislive.components.routing.Send;
import org.praxislive.core.ComponentFactory;
import org.praxislive.core.ComponentFactoryProvider;
import org.praxislive.impl.AbstractComponentFactory;

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
            addComponent("core:container:in", data(ContainerInput.class));
            addComponent("core:container:out", data(ContainerOutput.class));
            
        }
    }
}
