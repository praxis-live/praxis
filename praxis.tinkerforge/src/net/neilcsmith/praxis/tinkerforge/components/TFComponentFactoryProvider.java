/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2013 Neil C Smith.
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

            // Root
            addRoot("root:tinkerforge", data(TFRoot.class));

            // Components
            addComponent("tinkerforge:ambient-light", data(AmbientLight.class));
            addComponent("tinkerforge:analog-in", data(AnalogIn.class));
            addComponent("tinkerforge:analog-out", data(AnalogOut.class));
            addComponent("tinkerforge:barometer", data(Barometer.class));
            addComponent("tinkerforge:distance-ir", data(DistanceIR.class));
            addComponent("tinkerforge:dual-relay", data(DualRelay.class));
            addComponent("tinkerforge:io16", data(IO16.class));
            addComponent("tinkerforge:joystick", data(Joystick.class));
            addComponent("tinkerforge:lcd20x4", data(LCD20x4.class));
            addComponent("tinkerforge:linear-poti", data(LinearPoti.class));
            addComponent("tinkerforge:rotary-poti", data(RotaryPoti.class));
            addComponent("tinkerforge:servo", data(Servo.class));
            addComponent("tinkerforge:temperature", data(Temperature.class));
            addComponent("tinkerforge:temperature-ir", data(TemperatureIR.class));

            // Test components
            addComponent("tinkerforge:lcd16x2", data(LCD16x2.class).test());

        }
    }
}
