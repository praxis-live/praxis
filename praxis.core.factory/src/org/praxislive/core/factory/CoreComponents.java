/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Neil C Smith.
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
package org.praxislive.core.factory;

import org.praxislive.code.AbstractComponentFactory;
import org.praxislive.core.code.CoreCodeFactory;
import org.praxislive.core.ComponentFactory;
import org.praxislive.core.ComponentFactoryProvider;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class CoreComponents implements ComponentFactoryProvider {
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
            
            // custom
            add(data(new CoreCodeFactory("core:custom")));
            
            // built-in
            
            // CORE
            add("core:property", "resources/property.pxj");
            add("core:variable", "resources/variable.pxj");
            
            // ARRAY
            add("core:array:random", "resources/array_random.pxj");
            add("core:array:iterator", "resources/array_iterator.pxj");
            
            // MATH
            add("core:math:add", "resources/math_add.pxj");
            add("core:math:multiply", "resources/math_multiply.pxj");
            add("core:math:random", "resources/math_random.pxj");
            add("core:math:scale", "resources/math_scale.pxj");
            add("core:math:threshold", "resources/math_threshold.pxj");
            
            // ROUTING
            add("core:routing:every", "resources/routing_every.pxj");
            add("core:routing:gate", "resources/routing_gate.pxj");
            add("core:routing:inhibitor", "resources/routing_inhibitor.pxj");
            add("core:routing:join", "resources/routing_join.pxj");
            add("core:routing:order", "resources/routing_order.pxj");

            // TIMING
            add("core:timing:animator", "resources/timing_animator.pxj");
            add("core:timing:delay", "resources/timing_delay.pxj");
            add("core:timing:timer", "resources/timing_timer.pxj");
            
            
        }

        private void add(String type, String sourceFile) {
            add(data(type, sourceFile));
        }
        
        private Data data(String type, String sourceFile) {
            return data(new CoreCodeFactory(type, source(sourceFile)));
        }
        
    }
}