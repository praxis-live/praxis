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
package org.praxislive.core.code;

import org.praxislive.code.CodeContext;
import org.praxislive.code.CodeFactory;

public class CoreCodeFactory extends CodeFactory<CoreCodeDelegate> {

    private final static CoreBodyContext CBC = new CoreBodyContext();

    private final boolean emptyDefault;
    
    public CoreCodeFactory(String type) {
        super(CBC, type, CoreBodyContext.TEMPLATE);
        emptyDefault = true;
    }
    
    public CoreCodeFactory(String type, String sourceTemplate) {
        super(CBC, type, sourceTemplate);
        emptyDefault = false;
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

        @Override
        protected CoreCodeDelegate createDefaultDelegate() throws Exception {
            if (emptyDefault) {
                return new CoreCodeDelegate() {
                };
            } else {
                return super.createDefaultDelegate();
            }
        }

    }

}
