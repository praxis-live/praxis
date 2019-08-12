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
package org.praxislive.tracker.impl;

import org.praxislive.core.services.ComponentFactory;
import org.praxislive.core.services.ComponentFactoryProvider;
import org.praxislive.code.AbstractComponentFactory;
import org.praxislive.core.ComponentType;
import org.praxislive.core.code.CoreCodeFactory;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Components implements ComponentFactoryProvider {

    private final static Factory instance = new Factory();

    @Override
    public ComponentFactory getFactory() {
        return instance;
    }

    private static class Factory extends AbstractComponentFactory {
        
        private Factory() {
            build();
        }

        private void build() {
        
            add(data(new CoreCodeFactory(
                    ComponentType.of("core:tracker"),
                    CoreTracker.class,
                    source(CoreTracker.TEMPLATE_PATH)
            )));
            
        }
        
    }
}
