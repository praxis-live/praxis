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

import org.praxislive.code.CodeContext;
import org.praxislive.code.CodeFactory;
import org.praxislive.core.ComponentType;

public class CoreCodeFactory extends CodeFactory<CoreCodeDelegate> {

    private final static CoreBodyContext CBC = new CoreBodyContext();

    public CoreCodeFactory(ComponentType type,
            Class<? extends CoreCodeDelegate> baseClass,
            String sourceTemplate) {
        super(CBC, type, baseClass, sourceTemplate);
    }

    @Override
    public Task<CoreCodeDelegate> task() {
        return new CoreContextCreator();
    }

    private class CoreContextCreator extends Task<CoreCodeDelegate> {

        private CoreContextCreator() {
            super(CoreCodeFactory.this);
        }

        @Override
        protected CodeContext<CoreCodeDelegate> createCodeContext(CoreCodeDelegate delegate) {
            return new CoreCodeContext(new CoreCodeConnector(this, delegate));
        }


    }

}
