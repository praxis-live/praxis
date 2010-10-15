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
package net.neilcsmith.praxis.hub;

import java.util.LinkedList;
import java.util.Queue;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.PortAddress;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.impl.BasicControl;

/**
 *
 * @author Neil C Smith
 */
class ConnectionControl extends BasicControl {

    private DefaultHub hub;
    private ControlInfo info;
    private Queue<Call> callQueue;
    private int matchID;
    private ControlAddress address;

    ConnectionControl(DefaultHub hub, ControlAddress address) {
        super(hub);
        this.hub = hub;
        this.address = address;
        callQueue = new LinkedList<Call>();
    }

    @Override
    protected Call processInvoke(Call call, boolean quiet) throws Exception {
        if (callQueue.isEmpty()) {
            callQueue.add(call);
            processCall(call);
        } else {
            callQueue.add(call);
        }
        return null;
    }

    private void processCall(Call call) {
        boolean valid = false;
        CallArguments args = call.getArgs();
        if (args.getCount() == 2) {
            PortAddress pt1 = coerceArgument(args.getArg(0));
            PortAddress pt2 = coerceArgument(args.getArg(1));
            if (pt1 != null && pt2 != null) {
                args = CallArguments.create(new Argument[]{pt1, pt2});
                String root = pt1.getComponentAddress().getRootID();
                if (root.equals(pt2.getComponentAddress().getRootID())) {
                    try {
                        ControlAddress to = ControlAddress.valueOf("/" + root + "._connect");
                        Call outbound = Call.createCall(to, address, hub.getTime(), args);
                        matchID = outbound.getMatchID();
                        valid = true;
                        hub.route(outbound);
                    } catch (ArgumentFormatException ex) {
                    }
                }
            }
        }
        if (!valid) {
            hub.route(createError(call, "Invalid arguments"));
            callQueue.poll();
            if (!callQueue.isEmpty()) {
                processCall(callQueue.peek());
            }
        }
        
    }

    private PortAddress coerceArgument(Argument arg) {
        if (arg instanceof PortAddress) {
            return (PortAddress) arg;
        } else {
            try {
                return PortAddress.valueOf(arg.toString());
            } catch (ArgumentFormatException ex) {
                return null;
            }
        }
    }

    @Override
    protected void processError(Call call) {
        if (call.getMatchID() == matchID) {
            Call active = callQueue.poll();
            if (active != null) {
                active = Call.createErrorCall(active, call.getArgs());
                hub.route(active);
            }
            if (!callQueue.isEmpty()) {
                processCall(callQueue.peek());
            }
        }
    }

    @Override
    protected void processReturn(Call call) {
        if (call.getMatchID() == matchID) {
            Call active = callQueue.poll();
            if (active != null) {
                if (active.getType() == Call.Type.INVOKE) {
                    active = Call.createReturnCall(active, call.getArgs());
                    hub.route(active);
                }
            }
            if (!callQueue.isEmpty()) {
                processCall(callQueue.peek());
            }
        }
    }

    public ControlInfo getInfo() {
        return info;
    }
}
