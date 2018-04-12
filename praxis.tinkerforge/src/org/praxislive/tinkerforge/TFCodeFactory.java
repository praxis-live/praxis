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
package org.praxislive.tinkerforge;

import org.praxislive.code.CodeContext;
import org.praxislive.code.CodeFactory;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class TFCodeFactory extends CodeFactory<TFCodeDelegate> {

   
    private final static TFBodyContext CBC = new TFBodyContext();

    private final boolean emptyDefault;

    public TFCodeFactory(String type) {
        super(CBC, type, TFBodyContext.TEMPLATE);
        emptyDefault = true;
    }

    public TFCodeFactory(String type, String sourceCode) {
        super(CBC, type, sourceCode);
        emptyDefault = false;
    }

    @Override
    public Task<TFCodeDelegate> task() {
        return new TaskImpl();
    }

    private class TaskImpl extends Task<TFCodeDelegate> {

        public TaskImpl() {
            super(TFCodeFactory.this);
        }

        @Override
        protected CodeContext<TFCodeDelegate> createCodeContext(TFCodeDelegate delegate) {
            return new TFCodeContext(new TFCodeConnector(this, delegate));
        }

    }

}
