/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
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

import java.util.LinkedList;
import java.util.Queue;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.InterfaceDefinition;
import net.neilcsmith.praxis.core.interfaces.ScriptService;
import net.neilcsmith.praxis.core.syntax.InvalidSyntaxException;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.BasicControl;
import net.neilcsmith.praxis.impl.RootState;

/**
 * Praxis System Extension to provide a default script service.
 * 
 * @author Neil C Smith
 */

/* @TODO Replace parser reference with control stack. Add second hidden control
 * to handle all internal responses. Can call self for continuations / sleeping. */
public class OldScriptServiceImpl extends AbstractRoot {

    public final static String EVAL_CONTROL_ID = "eval";
    
    private TempScriptParser activeParser;
    private Queue<Call> evalQueue;
    private Call activeCall;
    private int matchID;
    private ControlAddress serviceAddress;

    public OldScriptServiceImpl() {
        super(RootState.ACTIVE_RUNNING);
        evalQueue = new LinkedList<Call>();
        EvalControl control = new EvalControl();
        registerControl(EVAL_CONTROL_ID, control);
    }
//
//    @Override
//    public InterfaceDefinition[] getInterfaces() {
//        return new InterfaceDefinition[] {ScriptService.INSTANCE};
//    }



    private class EvalControl extends BasicControl {

        private ControlInfo info;

        private EvalControl() {
            super(OldScriptServiceImpl.this);
            ArgumentInfo input = ArgumentInfo.create(PString.class, null);
            info = ControlInfo.createFunctionInfo(
                    new ArgumentInfo[]{input},
                    new ArgumentInfo[0],
                    null);
        }

        @Override
        protected Call processInvoke(Call call, boolean quiet) throws Exception {
            if (activeParser != null) {
                evalQueue.add(call);
            } else {
                activeCall = call;
                startParser();
            }

            return null;
        }

        private void startParser() {
            CallArguments args = activeCall.getArgs();
            if (args.getCount() == 1 && args.getArg(0) instanceof PString) {
                String script = ((PString) args.getArg(0)).toString();
                try {
                    if (serviceAddress == null) {
                        serviceAddress = ControlAddress.create(OldScriptServiceImpl.this.getAddress(), "eval");
                    }
                    activeParser = new TempScriptParser(script);
                    Call response = activeParser.getNextCall(serviceAddress, getTime());
                    if (response == null) {
                        route(createError(activeCall, "Empty Script"));
                        reset();
                    } else {
                        matchID = response.getMatchID();
                        route(response);
                    }

                } catch (Exception ex) {
                    route(Call.createErrorCall(activeCall, PReference.wrap(ex)));
                    reset();
                }

            } else {
                route(createError(activeCall, "Invalid arguments"));
            }
            
        }

        @Override
        protected void processError(Call call) {
            if (call.getMatchID() == matchID && activeParser != null) {
                route(Call.createErrorCall(activeCall, call.getArgs()));
                reset();
            }
        }

        @Override
        protected void processReturn(Call call) {
            if (call.getMatchID() == matchID && activeParser != null) {
                try {
                    Call nextCall = activeParser.getNextCall(serviceAddress, getTime());
                    if (nextCall != null) {
                        matchID = nextCall.getMatchID();
                        route(nextCall);
                    } else {
                        if (activeCall.getType() == Call.Type.INVOKE) {
                            route(Call.createReturnCall(activeCall, call.getArgs()));
                        }
                        reset();
                    }
                } catch (InvalidSyntaxException ex) {
                    Call errCall = createError(activeCall, "Syntax Exception");
                    route(errCall);
                }
            }
        }

        private void reset() {
            activeParser = null;
            activeCall = evalQueue.poll();
            if (activeCall != null) {
                startParser();
            }

        }

        public ControlInfo getInfo() {
            return info;
        }
    }

}
