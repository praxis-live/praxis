/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 - Neil C Smith. All rights reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.praxis.impl;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.PacketRouter;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class AbstractAsyncControl extends AbstractControl {

    private final static Logger LOG = Logger.getLogger(AbstractAsyncControl.class.getName());
    private Queue<Call> callQueue;
    private Call pending;
    private AbstractComponent host;
    private ControlAddress address;

    protected AbstractAsyncControl(AbstractComponent host) {
//        super(host);
        this.host = host;
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
                            LOG.warning("processInvoke(call) returned wrong resposne call");
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
                LOG.log(Level.WARNING, "Exception thrown processing call", ex);
                callQueue.poll();
                router.route(Call.createErrorCall(call, CallArguments.EMPTY));
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
