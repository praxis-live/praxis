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
package net.neilcsmith.praxis.script.commands;

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.ComponentType;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.interfaces.ContainerInterface;
import net.neilcsmith.praxis.core.interfaces.RootManagerService;
import net.neilcsmith.praxis.core.interfaces.ServiceManager;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.script.Command;
import net.neilcsmith.praxis.script.Env;
import net.neilcsmith.praxis.script.ExecutionException;
import net.neilcsmith.praxis.script.Namespace;
import net.neilcsmith.praxis.script.StackFrame;
import net.neilcsmith.praxis.script.Variable;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AtCommand implements Command {

    private final static AtCommand instance = new AtCommand();

    private AtCommand() {
    }

    public StackFrame createStackFrame(Namespace namespace, CallArguments args) throws ExecutionException {
        
        if (args.getCount() < 2) {
            throw new ExecutionException();
        }
        
        try {
            ComponentAddress ctxt = ComponentAddress.coerce(args.getArg(0));
            if (args.getCount() == 3) {
                ComponentType type = ComponentType.coerce(args.getArg(1));
                return new DefaultStackFrame(namespace, ctxt, type, args.getArg(2));
            } else {
                return new DefaultStackFrame(namespace, ctxt, null, args.getArg(1));
            }
        } catch (Exception ex) {
            throw new ExecutionException(ex);
        }

    }

    public static AtCommand getInstance() {
        return instance;
    }

    private class DefaultStackFrame implements StackFrame {

        private State state;
        private Namespace namespace;
        private final ComponentAddress ctxt;
        private ComponentType type;
        private Argument script;
        private int stage;
        private CallArguments result;
        
        private Call active;

        private DefaultStackFrame(Namespace namespace, ComponentAddress ctxt,
                ComponentType type, Argument script) {
            this.namespace = namespace;
            this.ctxt = ctxt;
            this.type = type;
            this.script = script;
            state = State.Incomplete;
            if (type == null) {
                stage = 2;
            } else {
                stage = 0;
            }
        }

        public State getState() {
            return state;
        }

        public StackFrame process(Env env) {
            if (stage == 0) {
                stage++;
                try {
//                    CallArguments ca = CallArguments.create(new Argument[]{ctxt, type});
//                    return namespace.getCommand("create").createStackFrame(namespace, ca);
                    
                    ControlAddress to;
                    CallArguments args;
                    int depth = ctxt.getDepth();
                    if (depth == 1) {
                        to = ControlAddress.create(
                                env.getLookup().get(ServiceManager.class).
                                findService(RootManagerService.INSTANCE),
                                RootManagerService.ADD_ROOT);
                        args = CallArguments.create(new Argument[] {
                        PString.valueOf(ctxt.getRootID()), type});
                    } else {
                        to = ControlAddress.create(ctxt.getParentAddress(),
                                ContainerInterface.ADD_CHILD);
                        args = CallArguments.create(new Argument[] {
                        PString.valueOf(ctxt.getComponentID(depth-1)), type});
                    }
                    active = Call.createCall(to, env.getAddress(), env.getTime(), args);
                    env.getPacketRouter().route(active);

                } catch (Exception ex) {
                    state = State.Error;
                    result = CallArguments.create(PReference.wrap(ex));
                }
            }
            if (stage == 2) {
                stage++;
                try {
                    Namespace child = namespace.createChild();
                    child.addVariable(Env.CONTEXT, new VariableImpl(ctxt));
                    return EvalCmds.INLINE_EVAL.createStackFrame(child, CallArguments.create(script));
                } catch (Exception ex) {
                    state = State.Error;
                    result = CallArguments.create(PReference.wrap(ex));
                }
            }

            return null;
        }

        public void postResponse(Call call) {
            if (active != null && call.getMatchID() == active.getMatchID()) {
                active = null;
                if (call.getType() == Call.Type.RETURN && stage == 1) {
                    stage++;
                } else {
                    result = call.getArgs();
                    this.state = State.Error;
                }
            }
        }

        public void postResponse(State state, CallArguments args) {
            if (state == State.OK) {
//                if (stage == 1) {
//                    stage++;
//                } else
                    if (stage == 3) {
                    this.state = State.OK;
                    result = args;
                }
            } else {
                this.state = state;
                result = args;
            }
        }

        public CallArguments getResult() {
            if (result == null) {
                throw new IllegalStateException();
            }
            return result;
        }
    }
}
