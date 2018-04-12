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
package org.praxislive.impl;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.types.PError;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class AbstractAsyncControl extends AbstractControl {

    private final static Logger LOG = Logger.getLogger(AbstractAsyncControl.class.getName());
    private Queue<Call> callQueue;
    private Call pending;

    protected AbstractAsyncControl() {
        callQueue = new LinkedList<Call>();

    }

    public void call(Call call, PacketRouter router) throws Exception {
        switch (call.getType()) {
            case INVOKE:
            case INVOKE_QUIET:
                processInvoke(call, router);
                break;
            default:
                processResponse(call, router);
        }
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
        if (pending == null || pending.getMatchID() != call.getMatchID()) {
            LOG.warning("Unexpected call received by processResponse(call, router)");
            return;
        }
        pending = null;
        try {
            Call ret;
            if (call.getType() == Call.Type.ERROR) {
                ret = processError(call);
            } else {
                ret = processResponse(call);
            }
            switch (ret.getType()) {
                case INVOKE:
                    pending = ret;
                    router.route(ret);
                    return;
                case RETURN:
                case ERROR:
                    Call active = callQueue.peek();
                    if (active.getMatchID() != ret.getMatchID()) {
                        throw new IllegalStateException();
                    }
                    callQueue.poll();
                    router.route(ret);
            }
        } catch (Exception ex) {
            Call active = callQueue.poll();
            if (active != null) {
                router.route(Call.createErrorCall(active, CallArguments.EMPTY));
            }
        }
        doInvokeLoop(router);
    }

    private void doInvokeLoop(PacketRouter router) {

        while (!callQueue.isEmpty()) {
            Call call = callQueue.peek();
            try {
                Call ret = processInvoke(call);
                switch (ret.getType()) {
                    case INVOKE:
                        pending = ret;
                        router.route(ret);
                        return;
                    case RETURN:
                    case ERROR:
                        if (ret.getMatchID() != call.getMatchID()) {
                            LOG.warning("processInvoke(call) returned wrong response call");
                            throw new IllegalStateException();
                        }
                        callQueue.poll();
                        router.route(ret);
                        break;
                    default:
                        LOG.warning("processInvoke(call) returned illegal INVOKE_QUIET call");
                        throw new IllegalStateException();
                }
            } catch (Exception ex) {
                LOG.log(Level.FINE, "Exception thrown processing call", ex);
                callQueue.poll();
                router.route(Call.createErrorCall(call, PError.create(ex)));
            }
        }

    }

    protected Call getActiveCall() {
        return callQueue.peek();
    }


//    @Override
//    protected ControlAddress getAddress() {
//        return host.getAddress(this);
//    }

    protected abstract Call processInvoke(Call call) throws Exception;

    protected abstract Call processResponse(Call call) throws Exception;

    protected Call processError(Call call) throws Exception {
        return Call.createErrorCall(getActiveCall(), call.getArgs());
    }

}
