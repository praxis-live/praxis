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
package net.neilcsmith.praxis.impl;

import java.util.HashMap;
import java.util.Map;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.interfaces.ServiceUnavailableException;
import net.neilcsmith.praxis.core.interfaces.TaskService;
import net.neilcsmith.praxis.core.types.PReference;

/**
 *
 * @author Neil C Smith
 */
@Deprecated
public class TaskControl extends AbstractControl {

    private AbstractRoot root;
    private ControlAddress address;
    private Map<Integer, Callback> callbacks;
    private ControlAddress atsAddress;

    public TaskControl() {
        callbacks = new HashMap<Integer, Callback>();
    }

    public int submitTask(long time, TaskService.Task task, Callback callback) throws ServiceUnavailableException {
        ControlAddress to = ControlAddress.create(findService(TaskService.INSTANCE),
                TaskService.SUBMIT);
        PacketRouter router = getLookup().get(PacketRouter.class);
        if (router == null) {
            throw new ServiceUnavailableException("No router found in TaskControl.getLookup()");
        }
        Call call = Call.createCall(to, getAddress(), time, PReference.wrap(task));
        router.route(call);
        if (callback != null) {
            callbacks.put(call.getMatchID(), callback);
        }
        return call.getMatchID();
    }

    private void processResponse(Call call, boolean error) {
        int id = call.getMatchID();
        Callback callback = callbacks.remove(id);
        if (callback != null) {
            if (error) {
                callback.taskError(call.getTimecode(), id, call.getArgs().get(0));
            } else {
                callback.taskCompleted(call.getTimecode(), id, call.getArgs().get(0));
            }
        }
    }

    public ControlInfo getInfo() {
        return null;
    }

    public void call(Call call, PacketRouter router) throws Exception {
        switch (call.getType()) {
            case RETURN:
                processResponse(call, false);
                break;
            case ERROR:
                processResponse(call, true);
                break;
            default:
                throw new UnsupportedOperationException("Unexpected invoke call to TaskControl");
        }
    }

    public static interface Callback {

        /**
         * Task complete.
         *
         * @param time
         * @param id
         * @param arg
         */
        public void taskCompleted(long time, long id, Argument arg);

        /**
         * Task threw an Exception.
         * @param time
         * @param id
         * @param arg
         */
        public void taskError(long time, long id, Argument arg);
    }
}
