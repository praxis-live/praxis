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
package net.neilcsmith.praxis.script;

import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Packet;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.ServiceManager;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.InterfaceDefinition;
import net.neilcsmith.praxis.core.interfaces.ScriptInterpreter;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.BasicControl;
import net.neilcsmith.praxis.script.impl.ScriptExecutor;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ScriptServiceImpl extends AbstractRoot {

    private static final Logger log = Logger.getLogger(ScriptServiceImpl.class.getName());

    private ScriptContext context;
    private ScriptExecutor defaultExecutor;

    public ScriptServiceImpl() {
        registerControl(ScriptInterpreter.EVAL, new EvalControl());
    }

    

    @Override
    protected void initializing() {
        super.initializing();
        String id = "__default__";
        registerControl(id, new ScriptControl());
        context = new ScriptContext(ControlAddress.create(getAddress(), id));
        defaultExecutor = new ScriptExecutor(context, false);
    }

    @Override
    public InterfaceDefinition[] getInterfaces() {
        return new InterfaceDefinition[]{ScriptInterpreter.getInstance()};
    }

    private class EvalControl extends BasicControl {

        private EvalControl() {
            super(ScriptServiceImpl.this);
        }

        @Override
        protected void processInvoke(Call call, PacketRouter router, boolean quiet) throws Exception {
            defaultExecutor.queueEvalCall(call);
        }

        public ControlInfo getInfo() {
            return null;
        }
    }

    private class ScriptControl extends BasicControl {

        private ScriptControl() {
            super(ScriptServiceImpl.this);
        }

        @Override
        protected void processReturn(Call call) throws Exception {
            defaultExecutor.processScriptCall(call);
        }

        @Override
        protected void processError(Call call) throws Exception {
            defaultExecutor.processScriptCall(call);
        }

        @Override
        protected Call processInvoke(Call call, boolean quiet) throws Exception {
            if (call.getToAddress().equals(call.getFromAddress())) {
                defaultExecutor.processScriptCall(call);
                return null;
            } else {
                return super.processInvoke(call, quiet);
            }
        }



        public ControlInfo getInfo() {
            return null;
        }


    }



    private class ScriptContext implements Env {

        private ControlAddress address;
        private Router router;

        private ScriptContext(ControlAddress address) {
            this.address = address;
            router = new Router();
        }

        public Lookup getLookup() {
            return ScriptServiceImpl.this.getLookup();
        }

        public ServiceManager getServiceManager() {
            return ScriptServiceImpl.this.getServiceManager();
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
            log.finest("Sending Call : ---\n " + packet.toString());
            getPacketRouter().route(packet);
        }

    }
}
