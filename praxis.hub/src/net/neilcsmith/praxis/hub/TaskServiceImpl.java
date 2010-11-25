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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.VetoException;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.interfaces.TaskService;
import net.neilcsmith.praxis.core.interfaces.TaskService.Task;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.impl.AbstractRoot;

/**
 *
 * @author Neil C Smith
 */
public class TaskServiceImpl extends AbstractRoot {

    private final static Logger LOG = Logger.getLogger(TaskServiceImpl.class.getName());

    private ExecutorService threadService;
    private Map<Future<Argument>, Call> futures;
    private List<Future> completed;

    public TaskServiceImpl() {
        super(EnumSet.noneOf(Caps.class));
        threadService = Executors.newCachedThreadPool(new ThreadFactory() {

            public Thread newThread(Runnable r) {
               Thread thr = new Thread(r);
               thr.setPriority(Thread.MIN_PRIORITY);
               return thr;
            }
        });
        Control submitter = new SubmitControl();
        registerControl(TaskService.SUBMIT, submitter);
        registerInterface(TaskService.INSTANCE);
        futures = new HashMap<Future<Argument>, Call>();
        completed = new ArrayList<Future>();
    }

//    @Override
//    public InterfaceDefinition[] getInterfaces() {
//        return new InterfaceDefinition[] {TaskService.INSTANCE};
//    }




    @Override
    public void addChild(String id, Component child) throws VetoException {
        throw new VetoException();
    }
    
    @Override
    protected void processingControlFrame() {
            for (Future<Argument> future : futures.keySet()) {
                if (future.isDone()) {
                    try {
                        Argument value = future.get();
                        Call call = futures.get(future);
                        call = Call.createReturnCall(call, value);
                        getPacketRouter().route(call);
                        completed.add(future);
                    } catch (Exception ex) {
                        LOG.log(Level.FINEST, null, ex);
                        Call call = futures.get(future);
                        call = Call.createErrorCall(call, PReference.wrap(ex));
                        getPacketRouter().route(call);
                        completed.add(future);
                    }
                }
            }
            while (! completed.isEmpty()) {
                futures.remove(completed.get(0));
                completed.remove(0);
            }
        }

    private class SubmitControl implements Control {

        public void call(Call call, PacketRouter router) throws Exception {
            switch (call.getType()) {
                case INVOKE :
                case INVOKE_QUIET :
                    submitTask(call);
                    break;
                default :
                    throw new IllegalArgumentException("Unexpected call\n" + call);
            }
        }

        private void submitTask(Call call) throws Exception {
            CallArguments args = call.getArgs();
            if (args.getCount() == 1) {
                Argument arg = args.getArg(0);
                if (arg instanceof PReference) {
                    Object ref = ((PReference) arg).getReference();
                    if (ref instanceof Task) {
                        final Task task = (Task) ref;
                        Future<Argument> future = threadService.submit(
                                new Callable<Argument>() {

                                    public Argument call() throws Exception {
                                        return task.execute();
                                    }
                                });
                        futures.put(future, call);
                    }
                }
            } else {
                throw new IllegalArgumentException();
            }
            
        }

        public ControlInfo getInfo() {
            return TaskService.SUBMIT_INFO;
        }

    }


//    private class SubmitControl extends BasicControl {
//
//        private ControlInfo info;
//
//        private SubmitControl() {
//            super(TaskServiceImpl.this);
//            ArgumentInfo input = ArgumentInfo.create(PReference.class, null);
//            ArgumentInfo output = ArgumentInfo.create(Argument.class, null);
//            info = ControlInfo.createFunctionInfo(
//                    new ArgumentInfo[]{input},
//                    new ArgumentInfo[]{output},
//                    null);
//        }
//
//        @Override
//        protected Call processInvoke(Call call, boolean quiet) throws Exception {
//            CallArguments args = call.getArgs();
//            if (args.getCount() == 1) {
//                Argument arg = args.getArg(0);
//                if (arg instanceof PReference) {
//                    Object ref = ((PReference) arg).getReference();
//                    if (ref instanceof Task) {
//                        final Task task = (Task) ref;
//                        Future<Argument> future = threadService.submit(
//                                new Callable<Argument>() {
//
//                                    public Argument call() throws Exception {
//                                        return task.execute();
//                                    }
//                                });
//                        futures.put(future, call);
//                        return null;
//                    }
//                }
//            }
//            throw new IllegalArgumentException();
//        }
//
//        public ControlInfo getInfo() {
//            return info;
//        }
//    }

}
