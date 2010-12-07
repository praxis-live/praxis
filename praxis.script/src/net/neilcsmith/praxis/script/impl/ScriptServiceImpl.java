/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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
 */
package net.neilcsmith.praxis.script.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Packet;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.interfaces.ScriptService;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.script.Env;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ScriptServiceImpl extends AbstractRoot {

    private static final Logger LOG = Logger.getLogger(ScriptServiceImpl.class.getName());

//    private ScriptContext context;
//    private ScriptExecutor defaultExecutor;
    private Map<ControlAddress, ScriptContext> contexts;
    private int exID;

    public ScriptServiceImpl() {
        registerControl(ScriptService.EVAL, new EvalControl());
        registerControl(ScriptService.CLEAR, new ClearControl());
        registerInterface(ScriptService.INSTANCE);
        contexts = new HashMap<ControlAddress, ScriptContext>();
    }

    

//    @Override
//    protected void initializing() {
//        super.initializing();
//        String id = "__default__";
//        registerControl(id, new ScriptControl());
//        context = new ScriptContext(ControlAddress.create(getAddress(), id));
//        defaultExecutor = new ScriptExecutor(context, false);
//    }

    private ScriptExecutor getExecutor(ControlAddress from) {
        ScriptContext ctxt = contexts.get(from);
        if (ctxt != null) {
            return ctxt.executor;
        }
        exID++;
        String id = "_exec_" + exID;  
        EnvImpl env = new EnvImpl(ControlAddress.create(getAddress(), id));
        ScriptExecutor ex = new ScriptExecutor(env, true);
        registerControl(id, new ScriptControl(ex));
        contexts.put(from, new ScriptContext(id, ex));
        return ex;
    }

    private void clearContext(ControlAddress from) {
        ScriptContext ctxt = contexts.remove(from);
        if (ctxt == null) {
            return;
        }
        ctxt.executor.flushEvalQueue();
        unregisterControl(ctxt.id);
    }


    private class EvalControl implements Control {

        public void call(Call call, PacketRouter router) throws Exception {
            switch (call.getType()) {
                case INVOKE :
                case INVOKE_QUIET :
//                    defaultExecutor.queueEvalCall(call);
                    getExecutor(call.getFromAddress()).queueEvalCall(call);
                    break;
                default:
                    LOG.warning("Eval control received unexpected call.\n" + call);
            }
        }

        public ControlInfo getInfo() {
            return ScriptService.INSTANCE.getControlInfo(ScriptService.EVAL);
        }

    }

    private class ClearControl implements Control {

        public void call(Call call, PacketRouter router) throws Exception {
            switch (call.getType()) {
                case INVOKE :
                    clearContext(call.getFromAddress());
                    router.route(Call.createReturnCall(call, CallArguments.EMPTY));
                    break;
                case INVOKE_QUIET :
                    clearContext(call.getFromAddress());
                    break;
                default :
                    LOG.warning("Clear control received unexpected call.\n" + call);
            }
        }

        public ControlInfo getInfo() {
            return ScriptService.INSTANCE.getControlInfo(ScriptService.CLEAR);
        }

    }


    private class ScriptControl implements Control {

        private ScriptExecutor executor;

        private ScriptControl(ScriptExecutor executor) {
            this.executor = executor;
        }

        public void call(Call call, PacketRouter router) throws Exception {
            switch (call.getType()) {
                case INVOKE :
                case INVOKE_QUIET :
                    if (call.getToAddress().equals(call.getFromAddress())) {
                        executor.processScriptCall(call);
                    } else {
                        throw new UnsupportedOperationException();
                    }
                    break;
                default :
                    executor.processScriptCall(call);
            }
        }

        public ControlInfo getInfo() {
            return null;
        }

    }

    private class ScriptContext {

        private String id;
        private ScriptExecutor executor;

        private ScriptContext(String id, ScriptExecutor executor) {
            this.id = id;
            this.executor = executor;
        }


    }



    private class EnvImpl implements Env {

        private ControlAddress address;
        private Router router;

        private EnvImpl(ControlAddress address) {
            this.address = address;
            router = new Router();
        }

        public Lookup getLookup() {
            return ScriptServiceImpl.this.getLookup();
        }

        public long getTime() {
            return ScriptServiceImpl.this.getTime();
        }

        public PacketRouter getPacketRouter() {
            return router;
        }

        public ControlAddress getAddress() {
            return address;
        }
    }

    private class Router implements PacketRouter {

        public void route(Packet packet) {
            LOG.finest("Sending Call : ---\n " + packet.toString());
            getPacketRouter().route(packet);
        }

    }
}
