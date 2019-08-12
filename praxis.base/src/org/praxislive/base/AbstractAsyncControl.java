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
package org.praxislive.base;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.Call;
import org.praxislive.core.Control;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.types.PError;

/**
 *
 */
public abstract class AbstractAsyncControl implements Control {

    private final static Logger LOG = Logger.getLogger(AbstractAsyncControl.class.getName());
    private final Queue<Call> callQueue;
    private Call pending;

    protected AbstractAsyncControl() {
        callQueue = new LinkedList<>();
    }

    @Override
    public void call(Call call, PacketRouter router) throws Exception {
        if (call.isRequest()) {
            processInvoke(call, router);
        } else {
            processResponse(call, router);
        }
    }

    protected Call getActiveCall() {
        return callQueue.peek();
    }

    protected abstract Call processInvoke(Call call) throws Exception;

    protected abstract Call processResponse(Call call) throws Exception;

    protected Call processError(Call call) throws Exception {
        return getActiveCall().error(call.args());
    }

    private void processInvoke(Call call, PacketRouter router) {
        if (callQueue.isEmpty()) {
            callQueue.add(call);
            doInvokeLoop(router);
        } else {
            callQueue.add(call);
        }

    }

    private void processResponse(Call call, PacketRouter router) {
        if (pending == null || pending.matchID() != call.matchID()) {
            LOG.warning("Unexpected call received by processResponse(call, router)");
            return;
        }
        pending = null;
        try {
            Call ret;
            if (call.isError()) {
                ret = processError(call);
            } else {
                ret = processResponse(call);
            }
            if (ret.isRequest()) {
                pending = ret;
                router.route(ret);
                return;
            } else {
                Call active = callQueue.peek();
                if (active.matchID() != ret.matchID()) {
                    throw new IllegalStateException();
                }
                callQueue.poll();
                router.route(ret);
            }
        } catch (Exception ex) {
            Call active = callQueue.poll();
            if (active != null) {
                router.route(active.error(PError.of(ex)));
            }
        }
        doInvokeLoop(router);
    }

    private void doInvokeLoop(PacketRouter router) {

        while (!callQueue.isEmpty()) {
            Call call = callQueue.peek();
            try {
                Call ret = processInvoke(call);
                if (ret.isRequest()) {
                    if (!ret.isReplyRequired()) {
                        throw new IllegalStateException("processInvoke(call) returned illegal quiet call");
                    }
                    pending = ret;
                    router.route(ret);
                    return;
                } else {
                    if (ret.matchID() != call.matchID()) {
                        throw new IllegalStateException("processInvoke(call) returned non-matching response call");
                    }
                    callQueue.poll();
                    router.route(ret);
                }
            } catch (Exception ex) {
                LOG.log(Level.FINE, "Exception thrown processing call", ex);
                callQueue.poll();
                router.route(call.error(PError.of(ex)));
            }
        }

    }

}
