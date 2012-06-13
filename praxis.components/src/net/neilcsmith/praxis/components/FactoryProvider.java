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
package net.neilcsmith.praxis.components;

import net.neilcsmith.praxis.components.array.ArrayIterator;
import net.neilcsmith.praxis.components.array.ArrayRandom;
import net.neilcsmith.praxis.components.file.Resolver;
import net.neilcsmith.praxis.components.math.Add;
import net.neilcsmith.praxis.components.math.Multiply;
import net.neilcsmith.praxis.components.math.Normalize;
import net.neilcsmith.praxis.components.math.RandomFloat;
import net.neilcsmith.praxis.components.math.Scale;
import net.neilcsmith.praxis.components.math.Threshold;
import net.neilcsmith.praxis.components.routing.Gate;
import net.neilcsmith.praxis.components.routing.Inhibitor;
import net.neilcsmith.praxis.components.routing.Join;
import net.neilcsmith.praxis.components.routing.Send;
import net.neilcsmith.praxis.components.test.Log;
import net.neilcsmith.praxis.components.timing.Animator;
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
            addComponent("core:control-rate", ControlFrameTrigger.class);
            addComponent("core:start-trigger", StartTrigger.class);
            addComponent("core:property", Property.class);
            addComponent("core:variable", Variable.class);

            // ARRAY
            addComponent("core:array:random", ArrayRandom.class);
            addComponent("core:array:iterator", ArrayIterator.class);

            // FILE
            //addComponent("core:file:random", RandomFile.class);
            addComponent("core:file:resolver", Resolver.class);

            // MATH
            addComponent("core:math:random", RandomFloat.class);
            addComponent("core:math:threshold", Threshold.class);
            addComponent("core:math:multiply", Multiply.class);
            addComponent("core:math:add", Add.class);
            addComponent("core:math:scale", Scale.class);
            addComponent("core:test:math:normalize", Normalize.class);

            //ROUTING
            addComponent("core:routing:gate", Gate.class);
            addComponent("core:routing:join", Join.class);
            addComponent("core:routing:inhibitor", Inhibitor.class);
            addComponent("core:routing:send", Send.class);

            
            // TIMING
            addComponent("core:timing:animator", Animator.class);
            addComponent("core:timing:delay", SimpleDelay.class);
            addComponent("core:timing:timer", Timer.class);


            
            // IN TESTING
            addComponent("core:test:log", Log.class);
            addComponent("core:test:routing:inhibitor", Inhibitor.class);
            addComponent("core:test:routing:send", Send.class);
            
        }
    }
}
