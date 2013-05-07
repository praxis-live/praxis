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
package net.neilcsmith.praxis.tinkerforge.components;

import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentFactoryProvider;
import net.neilcsmith.praxis.impl.AbstractComponentFactory;

/**
 *
 * @author Neil C Smith
 */
public class TFComponentFactoryProvider implements ComponentFactoryProvider {

    private final static ComponentFactory factory = new Factory();

    @Override
    public ComponentFactory getFactory() {
        return factory;
    }

    private static class Factory extends AbstractComponentFactory {

        private Factory() {
            build();
        }

        private void build() {
//                addRoot("root:tinkerforge", TFRoot.class);
                addRoot("root:tinkerforge", data(TFRoot.class).test());
//                addComponent("tinkerforge:ambient-light", AmbientLight.class);
//                addComponent("tinkerforge:distance-ir", DistanceIR.class);
//                addComponent("tinkerforge:lcd20x4", LCD20x4.class);
//                addComponent("tinkerforge:rotary-poti", RotaryPoti.class);
                addComponent("tinkerforge:ambient-light", data(AmbientLight.class).test());
                addComponent("tinkerforge:distance-ir", data(DistanceIR.class).test());
                addComponent("tinkerforge:lcd20x4", data(LCD20x4.class).test());
                addComponent("tinkerforge:rotary-poti", data(RotaryPoti.class).test());
        }
        
    }
}
