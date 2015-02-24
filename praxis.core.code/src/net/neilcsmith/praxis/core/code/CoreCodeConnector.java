/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Neil C Smith.
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
 *
 */
package net.neilcsmith.praxis.core.code;

import java.lang.reflect.Method;
import net.neilcsmith.praxis.code.CodeConnector;
import net.neilcsmith.praxis.code.CodeFactory;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class CoreCodeConnector extends CodeConnector<CoreCodeDelegate> {

//    public final static String SETUP = "setup";
    private final static String UPDATE = "update";

    private boolean foundUpdate;

    public CoreCodeConnector(CodeFactory.Task<CoreCodeDelegate> contextCreator,
            CoreCodeDelegate delegate) {
        super(contextCreator, delegate);
    }

    protected boolean hasUpdateMethod() {
        return foundUpdate;
    }
    
    @Override
    protected void analyseMethod(Method method) {

        if (UPDATE.equals(method.getName())
                && method.getParameterTypes().length == 0
                && method.getReturnType().equals(Void.TYPE)) {
            foundUpdate = true;
        }

        super.analyseMethod(method);
    }

}
