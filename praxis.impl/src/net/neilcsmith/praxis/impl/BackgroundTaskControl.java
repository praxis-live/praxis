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

package net.neilcsmith.praxis.impl;

import java.util.HashMap;
import java.util.Map;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.interfaces.ServiceUnavailableException;
import net.neilcsmith.praxis.core.interfaces.Task;
import net.neilcsmith.praxis.core.interfaces.TaskListener;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.interfaces.TaskService;
import net.neilcsmith.praxis.core.types.PReference;

/**
 *
 * @author Neil C Smith
 */
class BackgroundTaskControl extends AbstractControl {
    
    private AbstractRoot root;
    private ControlAddress address;
    private Map<Long, TaskListener> listenerMap;
    private ControlAddress atsAddress;
    
    
    BackgroundTaskControl() {
        listenerMap = new HashMap<Long, TaskListener>();
    }

    public long submitTask(Task task, TaskListener listener) throws ServiceUnavailableException {
        PReference taskRef = PReference.wrap(task);
        if (atsAddress == null) {
            ComponentAddress ats = root.getServiceManager().findService(
                    TaskService.INSTANCE);
            atsAddress = ControlAddress.create(ats, TaskService.SUBMIT);
        }
        Call call = Call.createCall(
                atsAddress, address, root.getTime(), taskRef);
        long id = call.getMatchID();
        listenerMap.put(id, listener);
        root.route(call);
        return id;
    }


    protected Call processInvoke(Call call, boolean quiet) throws Exception {
//        return createError(call, "This control does not accept inbound calls");
        return null;
    }


    protected void processError(Call call, PacketRouter router) {
        processResponse(call, true);
    }


    protected void processReturn(Call call, PacketRouter router) {
        processResponse(call, false);
    }
    
    private void processResponse(Call call, boolean error) {
        long id = call.getMatchID();
        TaskListener listener = listenerMap.remove(id);
        if (listener != null) {
            if (error) {
//                listener.taskError(id, call.getArgs());
                listener.taskError(call.getTimecode(), id, call.getArgs().getArg(0));
                
            } else {
                listener.taskCompleted(call.getTimecode(), id, call.getArgs().getArg(0));
            }
        }
    }
    
    public ControlInfo getInfo() {
        return null;
    }

    public void call(Call call, PacketRouter router) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
 
    
}
