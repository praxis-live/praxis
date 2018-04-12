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
 *
 */
package org.praxislive.core.code;

import java.lang.reflect.Method;
import org.praxislive.code.CodeConnector;
import org.praxislive.code.CodeFactory;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class CoreCodeConnector extends CodeConnector<CoreCodeDelegate> {

    private final static String UPDATE = "update";

    private boolean foundUpdate;

    public CoreCodeConnector(CodeFactory.Task<CoreCodeDelegate> contextCreator,
            CoreCodeDelegate delegate) {
        super(contextCreator, delegate);
    }

    @Override
    protected boolean requiresClock() {
        return super.requiresClock() || foundUpdate;
    }

    @Override
    protected void analyseMethod(Method method) {

        if (UPDATE.equals(method.getName())
                && method.getParameterCount() == 0) {
            foundUpdate = true;
        }

        super.analyseMethod(method);
    }

}
