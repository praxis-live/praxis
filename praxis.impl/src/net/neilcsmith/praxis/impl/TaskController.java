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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.interfaces.Task;
import net.neilcsmith.praxis.core.interfaces.TaskListener;
import net.neilcsmith.praxis.core.types.PReference;

/**
 *
 * @author Neil C Smith
 */
public class TaskController {
    
    private static AtomicLong idSource = new AtomicLong();
    private static Logger logger = Logger.getLogger(TaskController.class.getName());
//    private AbstractRoot root;
//    private ControlAddress address;
    private List<Future<Result>> futures;
    private ExecutorService threadService;
    
    
    TaskController() {
//        this.root = root;
        final int priority = Thread.currentThread().getPriority() - 1;
        threadService = Executors.newCachedThreadPool(new ThreadFactory() {

            public Thread newThread(Runnable r) {
               Thread thr = new Thread(r);
               thr.setPriority(priority);
               return thr;
            }
        });
        futures = new ArrayList<Future<Result>>();
    }
    
    public long submitTask(Task task, TaskListener listener) {
        long id = idSource.incrementAndGet();
        TaskCallable call = new TaskCallable(id, task, listener);
        futures.add(threadService.submit(call));
        return id;
    }
    
    public void checkTasks(long time) {
        if (!futures.isEmpty()) {
            for (int i = futures.size() - 1; i >= 0; i--) {
                Future<Result> future = futures.get(i);
                if (future.isDone()) {
                    try {
                        Result res = future.get();
                        TaskListener listener = res.listener;
                        if (res.success) {
                            listener.taskCompleted(time, res.id, res.argument);
                            logger.fine("Task " + res.id + " completed successfully");
                        } else {
                            listener.taskError(time, res.id, res.argument);
                            logger.log(Level.WARNING, "Task " + res.id + " threw an error", (Throwable)((PReference) res.argument).getReference());
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TaskController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(TaskController.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        futures.remove(i);
                    }
                }
            }
        }
    }

    private static class TaskCallable implements Callable<Result> {
        
        private long id;
        private Task task;
        private TaskListener listener;
        
        private TaskCallable(long id, Task task, TaskListener listener) {
            this.id = id;
            this.task = task;
            this.listener = listener;
        }

        public Result call() throws Exception {
            try {
                Argument ret = task.execute();
                return new Result(id, listener, true, ret);
            } catch (Exception ex) {
                return new Result(id, listener, false, PReference.wrap(ex));
            }
        }
        
    }
    
    private static class Result {
        
        private long id;
        private TaskListener listener;
        private boolean success;
        private Argument argument;
        
        private Result(long id, TaskListener listener, boolean success, Argument argument) {
            this.id = id;
            this.listener = listener;
            this.success = success;
            this.argument = argument;
        }
        
    }
 
    
}
