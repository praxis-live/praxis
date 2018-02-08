/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Neil C Smith.
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
package org.praxislive.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.Argument;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.ControlPort;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.interfaces.ServiceManager;
import org.praxislive.core.interfaces.ServiceUnavailableException;
import org.praxislive.core.interfaces.TaskService;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PReference;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
@Deprecated
public abstract class AbstractAsyncProperty<T> extends AbstractControl {

    private final static Logger LOG = Logger.getLogger(AbstractAsyncProperty.class.getName());
    private ControlInfo info;
    private Class<T> valueType;
    private Call activeCall;
    private Call taskCall;
    private CallArguments keys;
    private CallArguments portKeys;
    private T value;
    private boolean latestSet;
    private long latest;
    private ControlAddress taskSubmitAddress;

    protected AbstractAsyncProperty(ArgumentInfo keyType, Class<T> valueType, Argument defaultKey) {
        this(new ArgumentInfo[]{keyType}, valueType, new Argument[]{defaultKey}, null);
    }

    protected AbstractAsyncProperty(ArgumentInfo[] keyTypes,
            Class<T> valueType, Argument[] defaultKeys, PMap properties) {
        if (valueType == null) {
            throw new NullPointerException();
        }
        this.info = ControlInfo.createPropertyInfo(keyTypes, defaultKeys, properties);
        this.valueType = valueType;
        this.keys = CallArguments.create(defaultKeys);
    }

    public ControlPort.Input createPort() {
        return new InputPort();
    }

    public void call(Call call, PacketRouter router) throws Exception {
        switch (call.getType()) {
            case INVOKE:
            case INVOKE_QUIET:
                processInvoke(call, router);
                break;
            case RETURN:
                processReturn(call, router);
                break;
            case ERROR:
                processError(call, router);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void processInvoke(Call call, PacketRouter router) throws Exception {
        CallArguments args = call.getArgs();
        long time = call.getTimecode();
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
        if (taskCall == null || taskCall.getMatchID() != call.getMatchID()) {
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
        } else {
            LOG.warning("No keys able to be set");
        }
        valueChanged(call.getTimecode());
    }

    private void processError(Call call, PacketRouter router) throws Exception {
        if (taskCall == null || taskCall.getMatchID() != call.getMatchID()) {
            //LOG.warning("Unexpected Call received\n" + call.toString());
            return;
        }
        if (activeCall != null) {
            router.route(Call.createErrorCall(activeCall, call.getArgs()));
            activeCall = null;
        }
        taskError(call.getTimecode());
    }

    private void respond(Call call, CallArguments args, PacketRouter router) {

        if (call.getType() == Call.Type.INVOKE) {
            if (router == null) {
                router = getLookup().get(PacketRouter.class);
                if (router == null) {
                    LOG.warning("No PacketRouter found in Lookup");
                }
            }
            router.route(Call.createReturnCall(call, args));
        }
    }

    private void portInvoke(long time, Argument key) {
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
                LOG.log(Level.WARNING, "Invalid signal sent to port", ex);
            }

        }

    }

    private void castAndSetValue(Argument result) {
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

    private ControlAddress getTaskSubmitAddress() throws ServiceUnavailableException {
        if (taskSubmitAddress == null) {
            ServiceManager sm = getLookup().get(ServiceManager.class);
            if (sm == null) {
                throw new ServiceUnavailableException("Can't find Service Manager");
            }
            ComponentAddress service = sm.findService(TaskService.INSTANCE);
            taskSubmitAddress = ControlAddress.create(service, TaskService.SUBMIT);
        }
        return taskSubmitAddress;
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        taskSubmitAddress = null;
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

    protected T getValue() {
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
            router = getLookup().get(PacketRouter.class);
            if (router == null) {
                LOG.warning("Can't find a router");
                throw new ServiceUnavailableException();
            }
        }
        taskCall = Call.createCall(to, getAddress(), time, PReference.wrap(task));
        router.route(taskCall);
    }

    public ControlInfo getInfo() {
        return info;
    }

    protected abstract TaskService.Task createTask(CallArguments keys)
            throws Exception;

    protected void valueChanged(long time) {
    }

    protected void taskError(long time) {
    }

    private class InputPort extends AbstractControlInputPort {

        @Override
        public void receive(long time, double value) {
            receive(time, PNumber.valueOf(value));
        }

        @Override
        public void receive(long time, Argument value) {
            portInvoke(time, value);
        }
    }
}
