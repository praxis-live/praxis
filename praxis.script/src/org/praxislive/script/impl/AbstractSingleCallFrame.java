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
import org.praxislive.script.Namespace;
import org.praxislive.script.StackFrame;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class AbstractSingleCallFrame implements StackFrame {

    private Namespace namespace;
    private CallArguments args;
    private State state;
    private Call call;
    private CallArguments result;

    protected AbstractSingleCallFrame(Namespace namespace, CallArguments args) {
        if (namespace == null || args == null) {
            throw new NullPointerException();
        }
        this.namespace = namespace;
        this.args = args;
        state = State.Incomplete;
    }

    public final State getState() {
        return state;
    }

    public final Namespace getNamespace() {
        return namespace;
    }

    public final StackFrame process(Env env) {
        if (state == State.Incomplete && call == null) {
            try {
                call = createCall(env, args);
                if (call == null || call.getType() != Call.Type.INVOKE) {
                    throw new IllegalStateException("Invalid call");
                }
                env.getPacketRouter().route(call);
            } catch (Exception ex) {
                result = CallArguments.create(PReference.of(ex));
                state = State.Error;
            }
        }
        return null;
    }

    public final void postResponse(Call response) {
        if (call != null && response.matchID() == call.matchID()) {
            call = null;
            result = response.getArgs();
            if (response.getType() == Call.Type.RETURN) {
                result = processResult(result);
                state = State.OK;
            } else {
                state = State.Error;
            }
        }
    }

    public final void postResponse(State state, CallArguments args) {
        throw new IllegalStateException();
    }

    public final CallArguments getResult() {
        if (result == null) {
            throw new IllegalStateException();
        }
        return result;
    }

    protected abstract Call createCall(Env env, CallArguments args) throws Exception;

    protected CallArguments processResult(CallArguments result) {
        return result;
    }

}

