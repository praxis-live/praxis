/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 - Neil C Smith. All rights reserved.
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
package net.neilcsmith.praxis.components;

import net.neilcsmith.praxis.components.io.RandomFile;
import net.neilcsmith.praxis.components.math.Add;
import net.neilcsmith.praxis.components.math.Multiply;
import net.neilcsmith.praxis.components.math.RandomFloat;
import net.neilcsmith.praxis.components.math.Scale;
import net.neilcsmith.praxis.components.math.Threshold;
import net.neilcsmith.praxis.components.test.Log;
import net.neilcsmith.praxis.components.timing.SimpleDelay;
import net.neilcsmith.praxis.components.timing.Timer;
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
            addComponent("core:k-rate", ControlFrameTrigger.class);
            addComponent("core:i-rate", StartTrigger.class);
            addComponent("core:random-arg", RandomArg.class);
            addComponent("core:property", Property.class);
            addComponent("core:variable", Variable.class);

            addComponent("core:files:random", RandomFile.class);

            // MATH
            addComponent("core:math:random", RandomFloat.class);
            addComponent("core:math:threshold", Threshold.class);
            addComponent("core:math:multiply", Multiply.class);
            addComponent("core:math:add", Add.class);
            addComponent("core:math:scale", Scale.class);

            addComponent("core:test:log", Log.class);

            addComponent("core:timing:delay", SimpleDelay.class);
            addComponent("core:timing:timer", Timer.class);

        }
    }
}
