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
 */

package org.praxislive.script.impl;

import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.types.PReference;
import org.praxislive.script.Env;
import org.praxislive.script.ExecutionException;
import org.praxislive.script.InlineCommand;
import org.praxislive.script.Namespace;
import org.praxislive.script.StackFrame;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class AbstractInlineCommand implements InlineCommand {



    public StackFrame createStackFrame(Namespace namespace, CallArguments args) throws ExecutionException {
        return new InlineStackFrame(namespace, args);
    }

    private class InlineStackFrame implements StackFrame {

        private State state;
        private Namespace namespace;
        private CallArguments args;
        private CallArguments result;

        private InlineStackFrame(Namespace namespace, CallArguments args) {
            this.namespace = namespace;
            this.args = args;
            state = State.Incomplete;
        }

        public State getState() {
            return state;
        }

        public StackFrame process(Env env) {
            if (state == State.Incomplete) {
                try {
                    result = AbstractInlineCommand.this.process(env, namespace, args);
                    state = State.OK;
                } catch (ExecutionException ex) {
                    result = CallArguments.create(PReference.of(ex));
                    state = State.Error;
                }
            }
            return null;
        }

        public void postResponse(Call call) {
            throw new IllegalStateException();
        }

        public void postResponse(State state, CallArguments args) {
            throw new IllegalStateException();
        }

        public CallArguments getResult() {
            if (result == null) {
                throw new IllegalStateException();
            }
            return result;
        }

    }

}
