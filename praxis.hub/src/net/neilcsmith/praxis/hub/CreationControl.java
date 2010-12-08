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
package net.neilcsmith.praxis.hub;

import java.util.LinkedList;
import java.util.Queue;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.ComponentFactory;
import net.neilcsmith.praxis.core.ComponentType;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.impl.BasicControl;

/**
 *
 * @author Neil C Smith
 */
class CreationControl extends BasicControl {

    private DefaultHub hub;
    private ControlInfo info;
    private Queue<Call> callQueue;
    private int matchID;
    private ControlAddress address;
    private ComponentFactory factory;

    CreationControl(DefaultHub hub, ControlAddress address, ComponentFactory factory) {
        super(hub);
        this.hub = hub;
        this.address = address;
        this.factory = factory;
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
        try {
            CallArguments args = call.getArgs();
            if (args.getSize() == 2) {
                ComponentAddress ad = coerceArgument(args.get(0));
                if (ad != null) {
                    if (ad.getDepth() == 1) {
                        createRoot(ad.getRootID(), args.get(1).toString());
                        Call ret = Call.createReturnCall(call, CallArguments.EMPTY);
                        hub.route(ret);
                        callQueue.remove(call);
                        if (!callQueue.isEmpty()) {
                            processCall(callQueue.peek());
                        }
                        return;
                    } else {
                        Call out = createComponent(ad, args.get(1).toString());
                        matchID = out.getMatchID();
                        hub.route(out);
                        return;
                    }
                }
            }
        } catch (Exception exception) {
        }
        hub.route(createError(call, "Invalid arguments"));
        callQueue.remove(call);
        if (!callQueue.isEmpty()) {
            processCall(callQueue.peek());
        }
    }

    private void createRoot(String rootID, String typeID) throws Exception {
//        Root root = DepComponentFactory.getFactory().createRootComponent(typeID);
        Root root = factory.createRootComponent(ComponentType.valueOf(typeID));
        hub.installRoot(rootID, typeID, root);

    }

    private Call createComponent(ComponentAddress ad, String typeID) throws Exception {
//        Component comp = DepComponentFactory.getFactory().createComponent(typeID);
        Component comp = factory.createComponent(ComponentType.valueOf(typeID));
        ControlAddress to = ControlAddress.valueOf("/" + ad.getRootID() + "._add");
        CallArguments args = CallArguments.create(
                new Argument[]{ad, PReference.wrap(comp)});
        return Call.createCall(to, address, hub.getTime(), args);

    }

    private ComponentAddress coerceArgument(Argument arg) {
        if (arg instanceof ComponentAddress) {
            return (ComponentAddress) arg;
        } else {
            try {
                return ComponentAddress.valueOf(arg.toString());
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
