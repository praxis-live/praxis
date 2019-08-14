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
package org.praxislive.core.components;

import org.praxislive.code.AbstractComponentFactory;
import org.praxislive.core.ComponentType;
import org.praxislive.core.code.CoreCodeDelegate;
import org.praxislive.core.code.CoreCodeFactory;
import org.praxislive.core.services.ComponentFactory;
import org.praxislive.core.services.ComponentFactoryProvider;

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
            add("core:custom", CoreCustom.class, CoreCustom.TEMPLATE_PATH);

            // built-in
            // CORE
            add("core:property", CoreProperty.class, CoreProperty.TEMPLATE_PATH);
            add("core:start-trigger", CoreStartTrigger.class, CoreStartTrigger.TEMPLATE_PATH);
            add("core:variable", CoreVariable.class, CoreVariable.TEMPLATE_PATH);

            // ARRAY
            add("core:array:random", CoreArrayRandom.class, CoreArrayRandom.TEMPLATE_PATH);
            add("core:array:iterator", CoreArrayIterator.class, CoreArrayIterator.TEMPLATE_PATH);

            // MATH
            add("core:math:add", CoreMathAdd.class, CoreMathAdd.TEMPLATE_PATH);
            add("core:math:multiply", CoreMathMultiply.class, CoreMathMultiply.TEMPLATE_PATH);
            add("core:math:random", CoreMathRandom.class, CoreMathRandom.TEMPLATE_PATH);
            add("core:math:scale", CoreMathScale.class, CoreMathScale.TEMPLATE_PATH);
            add("core:math:threshold", CoreMathThreshold.class, CoreMathThreshold.TEMPLATE_PATH);

            // ROUTING
            add("core:routing:every", CoreRoutingEvery.class, CoreRoutingEvery.TEMPLATE_PATH);
            add("core:routing:gate", CoreRoutingGate.class, CoreRoutingGate.TEMPLATE_PATH);
            add("core:routing:inhibitor", CoreRoutingInhibitor.class, CoreRoutingInhibitor.TEMPLATE_PATH);
            add("core:routing:join", CoreRoutingJoin.class, CoreRoutingJoin.TEMPLATE_PATH);
            add("core:routing:order", CoreRoutingOrder.class, CoreRoutingOrder.TEMPLATE_PATH);
            add("core:routing:send", CoreRoutingSend.class, CoreRoutingSend.TEMPLATE_PATH);

            // TIMING
            add("core:timing:animator", CoreTimingAnimator.class, CoreTimingAnimator.TEMPLATE_PATH);
            add("core:timing:delay", CoreTimingDelay.class, CoreTimingDelay.TEMPLATE_PATH);
            add("core:timing:timer", CoreTimingTimer.class, CoreTimingTimer.TEMPLATE_PATH);

        }

        private void add(String type, Class<? extends CoreCodeDelegate> cls, String path) {
            add(data(
                    new CoreCodeFactory(ComponentType.of(type), cls, source(path))
            ));
        }

    }
}
