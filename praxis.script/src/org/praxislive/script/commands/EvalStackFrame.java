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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.Value;
import org.praxislive.core.ValueFormatException;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.types.PReference;
import org.praxislive.script.Command;
import org.praxislive.script.Env;
import org.praxislive.script.ExecutionException;
import org.praxislive.script.Namespace;
import org.praxislive.script.StackFrame;
import org.praxislive.script.ast.RootNode;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class EvalStackFrame implements StackFrame {

    private final static Logger log = Logger.getLogger(EvalStackFrame.class.getName());
    private Namespace namespace;
    private RootNode rootNode;
    private State state;
    private Call pending;
    private CallArguments result;
    private List<Value> argList;
    private boolean doProcess;

    public EvalStackFrame(Namespace namespace, RootNode rootNode) {
        this.namespace = namespace;
        this.rootNode = rootNode;
        this.state = State.Incomplete;
        this.argList = new LinkedList<Value>();
        rootNode.reset();
        rootNode.init(namespace);
        doProcess = true;
    }

    public State getState() {
        return state;
    }

    public StackFrame process(Env context) {
        if (state != State.Incomplete) {
            throw new IllegalStateException();
        }
        if (!doProcess) {
            return null;
        }
        try {
            if (rootNode.isDone()) {
                processResultFromNode();
                return null;
            } else {
                return processNextCommand(context);
            }
        } catch (Exception ex) {
            result = CallArguments.create(PReference.of(ex));
            state = State.Error;
            return null;
        } finally {
            doProcess = false;
        }

    }

    public void postResponse(Call call) {
        if (pending != null && pending.matchID() == call.matchID()) {
            pending = null;
            if (call.getType() == Call.Type.RETURN) {
                log.finest("EvalStackFrame - Received valid Return call : \n" + call);
                postResponse(call.getArgs());
            } else {
                log.finest("EvalStackFrame - Received valid Error call : \n" + call);
                this.state = State.Error;
                this.result = call.getArgs();
            }
            doProcess = true;
        } else {
            log.finest("EvalStackFrame - Received invalid call : \n" + call);
        }

    }

    public void postResponse(State state, CallArguments args) {
        if (this.state != State.Incomplete) {
            throw new IllegalStateException();
        }
        switch (state) {
            case Incomplete:
                throw new IllegalArgumentException();
            case OK:
                postResponse(args);
                break;
            default:
                this.state = state;
                this.result = args;
        }
        doProcess = true;
    }

    public CallArguments getResult() {
        if (state == State.Incomplete) {
            throw new IllegalStateException();
        }
        if (result == null) {
            return CallArguments.EMPTY;
        } else {
            return result;
        }
    }

    private void postResponse(CallArguments args) {
        try {
            argList.clear();
            argsToList(args, argList);
            rootNode.postResponse(argList);
        } catch (ExecutionException ex) {
            state = State.Error;//@TODO proper error reporting
        }
    }

    private void processResultFromNode() throws ExecutionException {
        argList.clear();
        rootNode.writeResult(argList);
        result = CallArguments.create(argList);
        state = State.OK;

    }

    private StackFrame processNextCommand(Env context)
            throws ExecutionException {

        argList.clear();
        rootNode.writeNextCommand(argList);
        if (argList.size() < 1) {
            throw new ExecutionException();
        }
        Value cmdArg = argList.get(0);
        if (cmdArg instanceof ControlAddress) {
            routeCall(context, argList);
            return null;
        }
//        if (cmdArg instanceof ComponentAddress) {
//            // default command
//            return tryDefault(namespace, argList);
//        }
        String cmdStr = cmdArg.toString();
        if (cmdStr.isEmpty()) {
            throw new ExecutionException();
        }
        Command cmd = namespace.getCommand(cmdStr);
        if (cmd != null) {
            argList.remove(0);
            return cmd.createStackFrame(namespace, CallArguments.create(argList));
        }
        if (cmdStr.charAt(0) == '/' && cmdStr.lastIndexOf('.') > -1) {
            routeCall(context, argList);
            return null;
        }

        throw new ExecutionException();
//        } else {
//            return tryDefault(namespace, argList);
//        }

    }

    private void routeCall(Env context, List<Value> argList)
            throws ExecutionException {
        try {
            ControlAddress ad = ControlAddress.coerce(argList.get(0));
            argList.remove(0);
            CallArguments args = CallArguments.create(argList);
            Call call = Call.createCall(ad, context.getAddress(), context.getTime(), args);
            log.finest("Sending Call" + call);
            pending = call;
            context.getPacketRouter().route(call);
        } catch (ValueFormatException ex) {
            throw new ExecutionException(ex);
        }
    }

//    private StackFrame tryDefault(Namespace namespace, List<Value> argList)
//            throws ExecutionException {
//        CallArguments args = CallArguments.create(argList);
//        return DefaultCommand.getInstance().createStackFrame(namespace, args);
//
//    }

    private void argsToList(CallArguments args, List<Value> list) {
        for (int i = 0, count = args.getSize(); i < count; i++) {
            list.add(args.get(i));
        }
    }
}
