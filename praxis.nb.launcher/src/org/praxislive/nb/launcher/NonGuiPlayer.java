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
package org.praxislive.nb.launcher;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.Value;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.services.ScriptService;
import org.praxislive.core.services.SystemManagerService;
import org.praxislive.core.types.PReference;
import org.praxislive.core.types.PString;
import org.praxislive.impl.AbstractControl;
import org.praxislive.impl.AbstractRoot;
import org.praxislive.impl.SimpleControl;
import org.openide.LifecycleManager;
import org.praxislive.core.services.Services;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class NonGuiPlayer extends AbstractRoot {

    private final static Logger LOG = Logger.getLogger(NonGuiPlayer.class.getName());
    private ScriptControl scriptControl;

    public NonGuiPlayer(List<String> scripts) {
        if (scripts == null) {
            throw new NullPointerException();
        }
        scriptControl = new ScriptControl(scripts);
        registerControl("_script-control", scriptControl);
        registerProtocol(SystemManagerService.class);
        registerControl(SystemManagerService.SYSTEM_EXIT, new ExitControl());

    }

    @Override
    protected void activating() {
        try {
            scriptControl.nextScript();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "ERROR: ", ex);
            exit();
        }
    }
    
    private void exit() {
        LifecycleManager.getDefault().exit();
    }

    private class ScriptControl extends AbstractControl {

        private final Queue<String> scriptQueue;
        
        private ControlAddress evalControl;
        private Call activeCall;

        ScriptControl(List<String> scripts) {
            scriptQueue = new LinkedList<>(scripts);
        }

        public void call(Call call, PacketRouter router) throws Exception {
            switch (call.getType()) {
                case RETURN:
                    processReturn(call);
                    break;
                case ERROR:
                    processError(call);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }

        private void processReturn(Call call) throws Exception {
            if (activeCall != null && call.matchID() == activeCall.matchID()) {
                activeCall = null;
                nextScript();
            }
        }

        private void processError(Call call) throws Exception {
            if (activeCall != null && call.matchID() == activeCall.matchID()) {
                activeCall = null;
                CallArguments args = call.getArgs();
                if (args.getSize() > 0) {
                    Value err = args.get(0);
                    if (err instanceof PReference) {
                        Object o = ((PReference) err).getReference();
                        if (o instanceof Throwable) {
                            LOG.log(Level.SEVERE, "ERROR: ", (Throwable) o);
                        } else {
                            LOG.log(Level.SEVERE, "ERROR: {0}", o.toString());
                        }
                    } else {
                        LOG.log(Level.SEVERE, "ERROR: {0}", err.toString());
                    }
                }
                exit();
            }

        }
        
        private void nextScript() {
            String script = scriptQueue.poll();
            if (script != null) {
                runScript(script);
            }
        }

        private void runScript(String script) {
            if (evalControl == null) {
                ComponentAddress ss = getLookup()
                    .find(Services.class)
                    .flatMap(s -> s.locate(ScriptService.class))
                    .orElseThrow(IllegalStateException::new);
                evalControl = ControlAddress.of(ss, ScriptService.EVAL);
            }
            activeCall = Call.create(evalControl,
                    getAddress(),
                    getExecutionContext().getTime(),
                    PString.of(script));
            getPacketRouter().route(activeCall);
        }

        public ControlInfo getInfo() {
            return null;
        }
    }

    private class ExitControl extends SimpleControl {

        private ExitControl() {
            super(SystemManagerService.SYSTEM_EXIT_INFO);
        }

        @Override
        protected CallArguments process(long time, CallArguments args, boolean quiet) throws Exception {
            exit();
            return CallArguments.EMPTY;
        }
    }
}
