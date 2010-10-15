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

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.ServiceUnavailableException;
import net.neilcsmith.praxis.core.interfaces.Task;
import net.neilcsmith.praxis.core.interfaces.TaskListener;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @param T 
 * @author Neil C Smith
 * @TODO should be able to do generics here to cast using Class object
 * @TODO add setURI method and move setResource to Binding inner interface
 * @TODO make honour time code
 */
public abstract class ResourceLoader<T> extends BasicControl implements TaskListener {

    private static Logger logger = Logger.getLogger(ResourceLoader.class.getName());
    private Component host;
    private long taskID;
    private Call activeCall;
//    private boolean portInvoked;
    private boolean taskActive;
    private ControlInfo info;
//    private DefaultControlOutputPort completePort;
//    private DefaultControlOutputPort errorPort;
    private InputPort inputPort;
    private Argument identifier;
    private Argument loadingIdentifier;
    private T resource;
    private Class<T> resourceType;

    protected ResourceLoader(Component host, Class<T> resourceType) {
        super(host);
        this.host = host;
        this.resourceType = resourceType; // @TODO null check?
        info = buildInfo();
//        completePort = new DefaultControlOutputPort(host);
//        errorPort = new DefaultControlOutputPort(host);
        inputPort = new InputPort(host);
    }

    private ControlInfo buildInfo() {
        ArgumentInfo[] argInfo = new ArgumentInfo[]{Argument.info()};
        Argument[] def = new Argument[]{PString.EMPTY};
        return ControlInfo.createPropertyInfo(argInfo, def, null);
    }

    public ControlPort.Input getInputPort() {
        return inputPort;
    }
//
//    public ControlPort.Output getCompletePort() {
//        return completePort;
//    }
//
//    public ControlPort.Output getErrorPort() {
//        return errorPort;
//    }

    public ControlInfo getInfo() {
        return info;
    }

    public void refresh() throws ServiceUnavailableException {
        if (taskActive) {
            return; // already in process of being refreshed.
        }
        startTask(identifier);
    }

    public Argument getResourceIdentifier() {
        return identifier;
    }

    public void setResourceIdentifier(long time, Argument id) {
        portInvoke(id);
    }

    protected T getResource() {
        return resource;
    }

    protected abstract Task getLoadTask(Argument identifier);

    protected abstract void resourceLoaded();

    protected abstract void resourceError();

    private void castAndSetResource(Argument arg) throws Exception {
        if (arg == null) {
            nullify();
        } else if (resourceType.isInstance(arg)) {
            // directly assignable
//            setResource(resourceType.cast(arg));
            resource = resourceType.cast(arg);
        } else if (arg instanceof PReference) {
            // wrapped object
            Object ref = ((PReference) arg).getReference();
//            setResource(resourceType.cast(ref));
            resource = resourceType.cast(ref);
        } else {
            throw new IllegalArgumentException();
        }

//        } catch (Exception ex) {
//            logger.log(Level.SEVERE, "", ex);
//            nullify();
//        }


//        if (arg instanceof PReference) {
//            try {
//                Object ref = ((PReference) arg).getReference();
//                T resource = resourceType.cast(ref);
//                setResource(resource);
//            } catch (Exception exception) {
//                logger.log(Level.SEVERE, "", exception);
//                nullify();
//            }
//        } else {
//            nullify();
//        }
    }
//
//    protected abstract void setResource(T resource);

    @Override
    protected final void processInvoke(Call call, PacketRouter router, boolean quiet) throws Exception {
        CallArguments args = call.getArgs();
        int argCount = args.getCount();
        if (argCount == 1) {
            if (activeCall != null) {
                router.route(Call.createReturnCall(activeCall, activeCall.getArgs()));
                activeCall = null;
            }
            Argument arg = args.getArg(0);
            if (arg.isEmpty()) {
                nullify();
                if (!quiet) {
                    router.route(Call.createReturnCall(call, args));
                }
            } else {
                activeCall = call;
                startTask(arg);

            }
        } else if (argCount == 0) {
            if (identifier == null) {
                router.route(Call.createReturnCall(call, PString.EMPTY));
            } else {
                router.route(Call.createReturnCall(call, identifier));
            }
        } else {
            router.route(createError(call, "Invalid Arguments"));
        }

    }

    private void portInvoke(Argument id) {
        if (activeCall != null) {
            Root root = host.getRoot();
            if (root != null) {
                Call ret = Call.createReturnCall(activeCall, activeCall.getArgs());
                root.getPacketRouter().route(ret);
            }
        }
        if (id == null || id.isEmpty()) {
            nullify();
        } else {
            try {
                startTask(id);
            } catch (ServiceUnavailableException ex) {
                logger.log(Level.WARNING, null, ex);
                resourceError();
            }
        }
    }

    private void nullify() {
        identifier = null;
        taskActive = false;
        resource = null;
        resourceLoaded();
//        portInvoked = false;
//        setResource(null);
//        completePort.send();
    }

    private void startTask(Argument id) throws ServiceUnavailableException {
        loadingIdentifier = id;
        taskActive = true;
        Root root = host.getRoot();
        if (root instanceof AbstractRoot) {
            AbstractRoot ar = (AbstractRoot) root;
            try {
                taskID = ar.submitTask(getLoadTask(id), this);
            } catch (ServiceUnavailableException ex) {
                loadingIdentifier = null;
                taskActive = false;
                throw ex;
            }
        } else {
            loadingIdentifier = null;
            taskActive = false;
            throw new ServiceUnavailableException();
            
        }


    }

    public void taskCompleted(long time, long id, Argument arg) {

        if (taskActive && id == taskID) {

            boolean ok = true;
            taskActive = false;
            try {
                castAndSetResource(arg);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error setting resource returned from task", ex);
                ok = false;
            }
            if (ok) {
                identifier = loadingIdentifier;
                resourceLoaded();
            } else {
                resourceError();
            }

            if (activeCall != null) {
                if (activeCall.getType() == Call.Type.INVOKE) {
                    Root root = host.getRoot();
                    if (root != null) {
                        Call ret;
                        if (ok) {
                            ret = Call.createReturnCall(activeCall, activeCall.getArgs());
                        } else {
                            ret = Call.createErrorCall(activeCall, CallArguments.EMPTY);
                        }
                        root.getPacketRouter().route(ret);
                    }
                }
                activeCall = null;
            }

        }
    }

    public void taskError(long time, long id, Argument arg) {
        if (taskActive && id == taskID) {
            taskActive = false;
            resourceError();
            if (activeCall != null) {
                Root root = host.getRoot();
                if (root != null) {
                    Call ret = Call.createErrorCall(activeCall, arg);
                    root.getPacketRouter().route(ret);
                }
                activeCall = null;
            }
        }

    }

    private class InputPort extends AbstractControlInputPort {

        private InputPort(Component host) {
            super(host);
        }

        @Override
        public void receive(long time, double value) {
            logger.warning(this.getAddress() + " received invalid argument");
        }

        @Override
        public void receive(long time, Argument value) {
            portInvoke(value);

        }
    }
//    public static interface Listener {
//        
//        public void resourceReady(ResourceLoader source);
//        
//        public void resourceError(ResourceLoader source);
//    }
}
