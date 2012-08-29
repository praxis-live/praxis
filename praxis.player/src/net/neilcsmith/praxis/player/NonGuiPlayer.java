/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
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
package net.neilcsmith.praxis.player;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.IllegalRootStateException;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.RootHub;
import net.neilcsmith.praxis.core.interfaces.ServiceUnavailableException;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.interfaces.ScriptService;
import net.neilcsmith.praxis.core.interfaces.SystemManagerService;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractControl;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.SimpleControl;
import org.openide.LifecycleManager;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class NonGuiPlayer extends AbstractRoot {

    private final static Logger LOG = Logger.getLogger(NonGuiPlayer.class.getName());
    private String script;
    private ScriptControl scriptControl;
    private Root.Controller controller;

    public NonGuiPlayer(String script) {
        if (script == null) {
            throw new NullPointerException();
        }
        this.script = script;
        scriptControl = new ScriptControl();
        registerControl("_script-control", scriptControl);
        registerInterface(SystemManagerService.INSTANCE);
        registerControl(SystemManagerService.SYSTEM_EXIT, new ExitControl());

    }

    @Override
    protected void run() {
        
        scriptControl.runScript(script);
        super.run();
    }

    @Override
    public Root.Controller initialize(String ID, RootHub hub) throws IllegalRootStateException {
        controller = super.initialize(ID, hub);
        return controller;
    }

    private class ScriptControl extends AbstractControl {

        ControlAddress evalControl;
        ControlAddress clearControl;
        Call activeCall;

        ScriptControl() {
            
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
            if (activeCall != null && call.getMatchID() == activeCall.getMatchID()) {
                activeCall = null;
            }
        }

        private void processError(Call call) throws Exception {
            if (activeCall != null && call.getMatchID() == activeCall.getMatchID()) {
                activeCall = null;
                CallArguments args = call.getArgs();
                if (args.getSize() > 0) {
                    Argument err = args.get(0);
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
                System.exit(1);
            }

        }

        private void runScript(String script) {
            try {
                ComponentAddress ss = findService(ScriptService.INSTANCE);
                evalControl = ControlAddress.create(ss, ScriptService.EVAL);
            } catch (ServiceUnavailableException ex) {
                LOG.log(Level.SEVERE, "", ex);
            }
            activeCall = Call.createCall(evalControl, getAddress(), System.nanoTime(), PString.valueOf(script));
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
            LifecycleManager.getDefault().exit();
            return CallArguments.EMPTY;
        }
        
    }
}
