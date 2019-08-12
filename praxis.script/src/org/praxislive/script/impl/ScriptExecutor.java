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
package org.praxislive.script.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.Lookup;
import org.praxislive.core.types.PReference;
import org.praxislive.script.Command;
import org.praxislive.script.CommandInstaller;
import org.praxislive.script.Env;
import org.praxislive.script.Namespace;
import org.praxislive.script.StackFrame;
import org.praxislive.script.Variable;
import org.praxislive.script.commands.CoreCommandsInstaller;
import org.praxislive.script.commands.ScriptCmds;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ScriptExecutor {

    private final static Logger log = Logger.getLogger(ScriptExecutor.class.getName());
    private List<StackFrame> stack;
    private Queue<Call> queue;
    private Env env;
    private Command evaluator;
    private Map<String, Command> commandMap;
    private Namespace rootNS;

    public ScriptExecutor(Env env, boolean inline) {
        this.env = env;
        stack = new LinkedList<StackFrame>();
        queue = new LinkedList<Call>();
        if (inline) {
            evaluator = ScriptCmds.INLINE_EVAL;
        } else {
            evaluator = ScriptCmds.EVAL;
        }
        rootNS = new NS();
        buildCommandMap();
    }

    public ScriptExecutor(Env context, final ComponentAddress ctxt) {
        this(context, true);
        rootNS.addVariable(Env.CONTEXT, new ConstantImpl(ctxt));
    }

    private void buildCommandMap() {
        commandMap = new HashMap<String, Command>();
        CommandInstaller installer = new CoreCommandsInstaller();
        installer.install(commandMap);
        Lookup.SYSTEM.findAll(CommandInstaller.class).forEach(cmds -> cmds.install(commandMap));
    }

    public void queueEvalCall(Call call) {
        queue.offer(call);
        if (stack.isEmpty()) {
            checkAndStartEval();
        }
    }

    public void flushEvalQueue() {
        // flush stack
        stack.clear();
        while (!queue.isEmpty()) {
            Call call = queue.poll();
            env.getPacketRouter().route(Call.createErrorCall(call, CallArguments.EMPTY));
        }

    }

    public void processScriptCall(Call call) {
        log.finest("processScriptCall - received :\n" + call);
        if (!stack.isEmpty()) {
            stack.get(0).postResponse(call);
            processStack();
        }
        if (stack.isEmpty()) {
            checkAndStartEval();
        }
    }

    private void processStack() {
        while (!stack.isEmpty()) {
            StackFrame current = stack.get(0);
            log.finest("Processing stack : " + current.getClass()
                    + "\n  Stack Size : " + stack.size());

            // if incomplete do round of processing
            if (current.getState() == StackFrame.State.Incomplete) {
                StackFrame child = current.process(env);
                if (child != null) {
                    log.finest("Pushing to stack" + child.getClass());
                    stack.add(0, child);
                    continue;
                }
            }

            // now check state again and pop if necessary
            StackFrame.State state = current.getState();
            if (state == StackFrame.State.Incomplete) {
                return;
            } else {
                CallArguments args = current.getResult();
                log.finest("Stack frame complete : " + current.getClass()
                        + "\n  Result : " + args + "\n  Stack Size : " + stack.size());
                stack.remove(0);
                if (!stack.isEmpty()) {
                    log.finest("Posting result up stack");
                    stack.get(0).postResponse(state, args);
                    continue;
                } else {
                    Call call = queue.poll();
                    if (state == StackFrame.State.OK) {
                        log.finest("Sending OK return call");
                        call = Call.createReturnCall(call, args);
                    } else {
                        log.finest("Sending Error return call");
                        call = Call.createErrorCall(call, args);
                    }
                    env.getPacketRouter().route(call);
                }
            }
        }
    }

    private void checkAndStartEval() {
        while (!queue.isEmpty()) {
            Call call = queue.peek();
            CallArguments args = call.getArgs();
            try {
                stack.add(0, evaluator.createStackFrame(rootNS, args));
                processStack();
                break;
            } catch (Exception ex) {
                queue.poll();
                env.getPacketRouter().route(
                        Call.createErrorCall(call, PReference.of(ex)));
            }
        }
    }

    private class NS implements Namespace {

        private NS parent;
        private Map<String, Variable> variables;

        private NS() {
            this(null);
        }

        private NS(NS parent) {
            this.parent = parent;
            variables = new HashMap<String, Variable>();
        }

        public Variable getVariable(String id) {
            Variable var = variables.get(id);
            if (var == null && parent != null) {
                return parent.getVariable(id);
            } else {
                return var;
            }
        }

        public void addVariable(String id, Variable var) {
            if (variables.containsKey(id)) {
                throw new IllegalArgumentException();
            }
            variables.put(id, var);
        }

        public Command getCommand(String id) {
            return commandMap.get(id);
        }

        public void addCommand(String id, Command cmd) {
            throw new UnsupportedOperationException();
        }

        public Namespace createChild() {
            return new NS(this);
        }
    }
}
