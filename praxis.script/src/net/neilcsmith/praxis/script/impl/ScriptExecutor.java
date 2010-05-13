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
package net.neilcsmith.praxis.script.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.script.Command;
import net.neilcsmith.praxis.script.CommandInstaller;
import net.neilcsmith.praxis.script.Env;
import net.neilcsmith.praxis.script.Namespace;
import net.neilcsmith.praxis.script.StackFrame;
import net.neilcsmith.praxis.script.Variable;
import net.neilcsmith.praxis.script.commands.CoreCommandsInstaller;
import net.neilcsmith.praxis.script.commands.EvalCmds;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ScriptExecutor {

    private final static Logger log = Logger.getLogger(ScriptExecutor.class.getName());
    private List<StackFrame> stack;
    private Queue<Call> queue;
    private Env context;
    private Command evaluator;
    private Map<String, Command> commandMap;
    private Namespace rootNS;

    public ScriptExecutor(Env context, boolean inline) {
        this.context = context;
        stack = new LinkedList<StackFrame>();
        queue = new LinkedList<Call>();
        if (inline) {
            evaluator = EvalCmds.INLINE_EVAL;
        } else {
            evaluator = EvalCmds.EVAL;
        }
        rootNS = new NS();
        buildCommandMap();
    }

    private void buildCommandMap() {
        commandMap = new HashMap<String, Command>();
        CommandInstaller installer = new CoreCommandsInstaller();
        installer.install(commandMap);
    }

    public void queueEvalCall(Call call) {
        queue.offer(call);
        if (stack.isEmpty()) {
            checkAndStartEval();
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
//        StackFrame current = stack.get(0);
//        log.finest("Processing stack : " + current.getClass() +
//                "\n  Stack Size : " + stack.size());
//        // if incomplete do round of processing
//        if (current.getState() == StackFrame.State.Incomplete) {
//            log.finest("Calling process() on : " + current.getClass());
//            StackFrame child = current.process(context);
//            if (child != null) {
//                log.finest("Pushing to stack" + child.getClass());
//                stack.add(0, child);
//                processStack();
//            }
//        }
//        // now check state again and pop if necessary
//        StackFrame.State state = current.getState();
//        if (state != StackFrame.State.Incomplete) {
//            CallArguments args = current.getResult();
//            log.finest("Stack frame complete : " + current.getClass() +
//                    "\n  Result : " + args + "\n  Stack Size : " + stack.size());
//            stack.remove(0);
//            if (!stack.isEmpty()) {
//                log.finest("Posting result up stack");
//                stack.get(0).postResponse(state, args);
//                processStack();
//            } else {
//                Call call = queue.poll();
//                if (state == StackFrame.State.OK) {
//                    log.finest("Sending OK return call");
//                    call = Call.createReturnCall(call, args);
//                } else {
//                    log.finest("Sending Error return call");
//                    call = Call.createErrorCall(call, args);
//                }
//                context.getPacketRouter().route(call);
//                checkAndStartEval();
//            }
//        }
        while (!stack.isEmpty()) {
            StackFrame current = stack.get(0);
            log.finest("Processing stack : " + current.getClass()
                    + "\n  Stack Size : " + stack.size());

            // if incomplete do round of processing
            if (current.getState() == StackFrame.State.Incomplete) {
                StackFrame child = current.process(context);
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
                    context.getPacketRouter().route(call);
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
                context.getPacketRouter().route(
                        Call.createErrorCall(call, PReference.wrap(ex)));
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
