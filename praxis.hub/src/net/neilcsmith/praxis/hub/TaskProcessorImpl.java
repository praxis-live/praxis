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

import java.util.ArrayList;
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
import net.neilcsmith.praxis.core.InvalidChildException;
import net.neilcsmith.praxis.core.interfaces.Task;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.InterfaceDefinition;
import net.neilcsmith.praxis.core.interfaces.TaskProcessor;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.impl.AbstractRoot;
import net.neilcsmith.praxis.impl.BasicControl;

/**
 *
 * @author Neil C Smith
 */
public class TaskProcessorImpl extends AbstractRoot {

    private final static Logger LOG = Logger.getLogger(TaskProcessorImpl.class.getName());

    private ExecutorService threadService;
    private Map<Future<Argument>, Call> futures;
    private List<Future> completed;

    public TaskProcessorImpl() {
        super(State.ACTIVE_RUNNING);
        threadService = Executors.newCachedThreadPool(new ThreadFactory() {

            public Thread newThread(Runnable r) {
               Thread thr = new Thread(r);
               thr.setPriority(Thread.MIN_PRIORITY);
               return thr;
            }
        });
        Control submitter = new SubmitControl();
        registerControl("submit", submitter);
        futures = new HashMap<Future<Argument>, Call>();
        completed = new ArrayList<Future>();
    }

    public InterfaceDefinition[] getInterfaces() {
        return new InterfaceDefinition[] {TaskProcessor.DEFINITION};
    }




    @Override
    public void addChild(String id, Component child) throws InvalidChildException {
        throw new InvalidChildException();
    }
    
    @Override
    protected void processingControlFrame() {
            for (Future<Argument> future : futures.keySet()) {
                if (future.isDone()) {
                    try {
                        Argument value = future.get();
                        Call call = futures.get(future);
                        call = Call.createReturnCall(call, value);
                        route(call);
                        completed.add(future);
                    } catch (Exception ex) {
                        LOG.log(Level.FINEST, null, ex);
                        Call call = futures.get(future);
                        call = Call.createErrorCall(call, PReference.wrap(ex));
                        route(call);
                        completed.add(future);
                    }
                }
            }
            while (! completed.isEmpty()) {
                futures.remove(completed.get(0));
                completed.remove(0);
            }
        }

    private class SubmitControl extends BasicControl {

        private ControlInfo info;

        private SubmitControl() {
            super(TaskProcessorImpl.this);
            ArgumentInfo input = ArgumentInfo.create(PReference.class, null);
            ArgumentInfo output = ArgumentInfo.create(Argument.class, null);
            info = ControlInfo.createFunctionInfo(
                    new ArgumentInfo[]{input},
                    new ArgumentInfo[]{output},
                    null);
        }

        @Override
        protected Call processInvoke(Call call, boolean quiet) throws Exception {
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
                        return null;
                    }
                }
            }
            return createError(call, "");
        }

        public ControlInfo getInfo() {
            return info;
        }
    }

}
