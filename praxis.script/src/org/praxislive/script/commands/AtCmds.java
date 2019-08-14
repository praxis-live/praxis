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
package org.praxislive.script.commands;

import org.praxislive.script.impl.AbstractSingleCallFrame;
import org.praxislive.script.impl.VariableImpl;
import java.util.Map;
import org.praxislive.core.Value;
import org.praxislive.core.ValueFormatException;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ComponentType;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.protocols.ContainerProtocol;
import org.praxislive.core.services.RootManagerService;
import org.praxislive.core.services.ServiceUnavailableException;
import org.praxislive.core.services.Services;
import org.praxislive.core.types.PReference;
import org.praxislive.core.types.PString;
import org.praxislive.script.Command;
import org.praxislive.script.CommandInstaller;
import org.praxislive.script.Env;
import org.praxislive.script.ExecutionException;
import org.praxislive.script.Namespace;
import org.praxislive.script.StackFrame;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class AtCmds implements CommandInstaller {

    private final static AtCmds INSTANCE = new AtCmds();
    private final static At AT = new At();
    private final static NotAt NOT_AT = new NotAt();

    private AtCmds() {
    }

    public void install(Map<String, Command> commands) {
        commands.put("@", AT);
        commands.put("!@", NOT_AT);
    }

    public final static AtCmds getInstance() {
        return INSTANCE;
    }

    private static class At implements Command {

        public StackFrame createStackFrame(Namespace namespace, CallArguments args) throws ExecutionException {

            if (args.getSize() < 2) {
                throw new ExecutionException();
            }

            try {
                ComponentAddress ctxt = ComponentAddress.coerce(args.get(0));
                if (args.getSize() == 3) {
                    ComponentType type = ComponentType.coerce(args.get(1));
                    return new AtStackFrame(namespace, ctxt, type, args.get(2));
                } else {
                    Value arg = args.get(1);
                    if (! arg.toString().contains(" ")) {
                        try {
                            ComponentType type = ComponentType.coerce(arg);
                            return new AtStackFrame(namespace, ctxt, type, PString.EMPTY);
                        } catch (ValueFormatException ex) {
                            // fall through
                        }
                    }
                    return new AtStackFrame(namespace, ctxt, null, arg);
                }
            } catch (Exception ex) {
                throw new ExecutionException(ex);
            }

        }
    }

    private static class NotAt implements Command {

        public StackFrame createStackFrame(Namespace namespace, CallArguments args) throws ExecutionException {
            return new NotAtStackFrame(namespace, args);
        }

    }

    private static class AtStackFrame implements StackFrame {

        private State state;
        private Namespace namespace;
        private final ComponentAddress ctxt;
        private ComponentType type;
        private Value script;
        private int stage;
        private CallArguments result;
        private Call active;

        private AtStackFrame(Namespace namespace, ComponentAddress ctxt,
                ComponentType type, Value script) {
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

                    ControlAddress to;
                    CallArguments args;
                    int depth = ctxt.depth();
                    if (depth == 1) {
                        to = ControlAddress.of(
                                env.getLookup().find(Services.class)
                                .flatMap(sm -> sm.locate(RootManagerService.class))
                                .orElseThrow(ServiceUnavailableException::new),
                                RootManagerService.ADD_ROOT);
                        args = CallArguments.create(new Value[]{
                                    PString.of(ctxt.rootID()), type});
                    } else {
                        to = ControlAddress.of(ctxt.parent(),
                                ContainerProtocol.ADD_CHILD);
                        args = CallArguments.create(new Value[]{
                                    PString.of(ctxt.componentID(depth - 1)), type});
                    }
                    active = Call.createCall(to, env.getAddress(), env.getTime(), args);
                    env.getPacketRouter().route(active);

                } catch (Exception ex) {
                    state = State.Error;
                    result = CallArguments.create(PReference.of(ex));
                }
            }
            if (stage == 2) {
                stage++;
                try {
                    Namespace child = namespace.createChild();
                    child.addVariable(Env.CONTEXT, new VariableImpl(ctxt));
                    return ScriptCmds.INLINE_EVAL.createStackFrame(child, CallArguments.create(script));
                } catch (Exception ex) {
                    state = State.Error;
                    result = CallArguments.create(PReference.of(ex));
                }
            }

            return null;
        }

        public void postResponse(Call call) {
            if (active != null && call.matchID() == active.matchID()) {
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

    private static class NotAtStackFrame extends AbstractSingleCallFrame {

        private NotAtStackFrame(Namespace ns, CallArguments args) {
            super(ns, args);
        }

        @Override
        protected Call createCall(Env env, CallArguments args) throws Exception {
            ComponentAddress comp = ComponentAddress.coerce(args.get(0));
            if (comp.depth() == 1) {
                return createRootRemovalCall(env, comp.rootID());
            } else {
                return createChildRemovalCall(env, comp);
            }
        }

        private Call createRootRemovalCall(Env env, String id) throws Exception {
            ControlAddress to = ControlAddress.of(
                    env.getLookup().find(Services.class)
                            .flatMap(sm -> sm.locate(RootManagerService.class))
                            .orElseThrow(ServiceUnavailableException::new),
                    RootManagerService.REMOVE_ROOT);
            return Call.create(to, env.getAddress(), env.getTime(), PString.of(id));
        }

        private Call createChildRemovalCall(Env env, ComponentAddress comp) throws Exception {
            ControlAddress to = ControlAddress.of(comp.parent(),
                    ContainerProtocol.REMOVE_CHILD);
            return Call.create(to, env.getAddress(), env.getTime(),
                    PString.of(comp.componentID(comp.depth() - 1)));
        }
    }
}
