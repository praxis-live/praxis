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
package org.praxislive.code;

import org.praxislive.core.Value;
import org.praxislive.core.ValueFormatException;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.Control;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.Lookup;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.services.ServiceUnavailableException;
import org.praxislive.core.services.TaskService;
import org.praxislive.core.types.PError;
import org.praxislive.core.types.PReference;
import org.praxislive.logging.LogLevel;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class AbstractAsyncProperty<V> implements Control {
    
    private final Class<V> valueType;
    
    private CodeContext<?> context;
    private Call activeCall;
    private Call taskCall;
    private CallArguments keys;
    private CallArguments portKeys;
    private V value;
    private boolean latestSet;
    private long latest;

    
    protected AbstractAsyncProperty(Value initialKey, Class<V> valueType, V value) {
        this.valueType = valueType;
        this.keys = CallArguments.create(initialKey);
        this.value = value;
        
    }
    
    protected void attach(CodeContext<?> context) {
        if (context == null) {
            throw new NullPointerException();
        }
        this.context = context;
    }
    

//    public ControlPort.Input createPort() {
//        return new InputPort();
//    }

    @Override
    public void call(Call call, PacketRouter router) throws Exception {
        if (call.isRequest()) {
            processInvoke(call, router);
        } else if (call.isReply()) {
            processReturn(call, router);
        } else {
            processError(call, router);
        }
    }

    private void processInvoke(Call call, PacketRouter router) throws Exception {
        CallArguments args = call.getArgs();
        long time = call.time();
        if (args.getSize() > 0 && isLatest(time)) {
            TaskService.Task task = createTask(args);
            // no exception so valid args
            if (task == null) {
                nullify(time);
            } else {
                startTask(task, router, time);
            }
            // managed to start task ok
            setLatest(time);
            if (activeCall != null) {
                respond(activeCall, activeCall.getArgs(), router);
                activeCall = null;
            }
            if (task == null) {
                keys = args;
                respond(call, keys, router);
            } else {
                activeCall = call;
            }
        } else {
            respond(call, keys, router);
        }
    }

    private void processReturn(Call call, PacketRouter router) throws Exception {
        if (taskCall == null || taskCall.matchID() != call.matchID()) {
            //LOG.warning("Unexpected Call received\n" + call.toString());
            return;
        }
        taskCall = null;
        castAndSetValue(call.getArgs().get(0));
        if (activeCall != null) {
            keys = activeCall.getArgs();
            respond(activeCall, keys, router);
            activeCall = null;
        } else if (portKeys != null) {
            keys = portKeys;
            portKeys = null;
        }
        valueChanged(call.time());
    }

    private void processError(Call call, PacketRouter router) throws Exception {
        if (taskCall == null || taskCall.matchID() != call.matchID()) {
            //LOG.warning("Unexpected Call received\n" + call.toString());
            return;
        }
        if (activeCall != null) {
            router.route(Call.createErrorCall(activeCall, call.getArgs()));
            activeCall = null;
        }
        CallArguments args = call.getArgs();
        PError err = null;
        if (args.getSize() > 0) {
            try {
                err = PError.coerce(args.get(0));
            } catch (ValueFormatException ex) {
                err = PError.of(ex, args.get(0).toString());
            }
        } else {
            err = PError.of("");
        }
        taskError(latest, err);
    }

    private void respond(Call call, CallArguments args, PacketRouter router) {

        if (call.getType() == Call.Type.INVOKE) {
            if (router == null) {
                router = getLookup().find(PacketRouter.class)
                        .orElseThrow(IllegalStateException::new);
            }
            router.route(Call.createReturnCall(call, args));
        }
    }

    protected void portInvoke(long time, Value key) {
        if (isLatest(time)) {
            CallArguments pkeys = CallArguments.create(key);
            try {
                TaskService.Task task = createTask(pkeys);
                if (task == null) {
                    keys = pkeys;
                    nullify(time);
                } else {
                    startTask(task, null, time);
                    portKeys = pkeys;
                }
                setLatest(time);
                if (activeCall != null) {
                    respond(activeCall, activeCall.getArgs(), null);
                    activeCall = null;
                }
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex, "Invalid signal sent to port");
            }

        }

    }

    private void castAndSetValue(Value result) {
        if (valueType.isInstance(result)) {
            value = valueType.cast(result);
            return;
        }
        if (result instanceof PReference) {
            Object ref = ((PReference) result).getReference();
            if (valueType.isInstance(ref)) {
                value = valueType.cast(ref);
                return;
            }
        }
        value = null;
    }
    
    private Lookup getLookup() {
         return context.getLookup();
    }

    private ControlAddress getTaskSubmitAddress() throws ServiceUnavailableException {
        ComponentAddress service = context.locateService(TaskService.class)
                .orElseThrow(ServiceUnavailableException::new);
        return ControlAddress.of(service, TaskService.SUBMIT);
    }

    private void setLatest(long time) {
        latestSet = true;
        latest = time;
    }

    private boolean isLatest(long time) {
        if (latestSet) {
            return (time - latest) >= 0;
        } else {
            return true;
        }

    }

    protected CallArguments getKeys() {
        return keys;
    }

    protected V getValue() {
        return value;
    }

    private void nullify(long time) {
        taskCall = null;
        portKeys = null;
        value = null;
        valueChanged(time);
    }

    private void startTask(TaskService.Task task, PacketRouter router, long time)
            throws ServiceUnavailableException {
        ControlAddress to = getTaskSubmitAddress();
        if (router == null) {
            router = getLookup().find(PacketRouter.class)
                    .orElseThrow(() -> new IllegalStateException("No PacketRouter found"));
        }
        taskCall = Call.create(to, context.getAddress(this), time, PReference.of(task));
        router.route(taskCall);
    }

    protected abstract TaskService.Task createTask(CallArguments keys)
            throws Exception;

    protected void valueChanged(long time) {
    }

    protected void taskError(long time, PError error) {
    }

}
