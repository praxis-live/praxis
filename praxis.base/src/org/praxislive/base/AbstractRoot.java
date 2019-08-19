/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2019 Neil C Smith.
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
package org.praxislive.base;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.Call;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ExecutionContext;
import org.praxislive.core.Lookup;
import org.praxislive.core.Packet;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Root;
import org.praxislive.core.RootHub;
import org.praxislive.core.services.Service;
import org.praxislive.core.services.ServiceUnavailableException;
import org.praxislive.core.services.Services;

/**
 * A general purpose base implementation of {@link Root}. By default uses a
 * {@link ScheduledExecutorService} but allows for attaching custom
 * {@link Delegate} implementations to drive from a thread of choice.
 */
public abstract class AbstractRoot implements Root {

    /**
     * The possible states this implementation may transition through. All
     * transitions occur in order, except that an implementation may transition
     * back and forth between {@link State#ACTIVE_IDLE} and
     * {@link State#ACTIVE_RUNNING} - see {@link #setRunning()} and
     * {@link #setIdle()}
     * <p>
     * The default activation state is ACTIVE_IDLE. Use {@link #setRunning()}
     * inside {@link #activating()} if required.
     */
    protected static enum State {
        NEW, INITIALIZING, INITIALIZED, ACTIVE_IDLE, ACTIVE_RUNNING, TERMINATING, TERMINATED
    }

    private static final Logger LOG = Logger.getLogger(AbstractRoot.class.getName());

    private final AtomicReference<State> state;
    private final AtomicReference<Delegate> delegate;
    private final Queue<Object> queue;
    private final Queue<Object> pending;
    private final Lock lock;

    private volatile long time;

    private Lookup lookup;
    private ComponentAddress address;
    private RootHub hub;
    private DefaultExecutionContext context;
    private Controller controller;
    private PacketRouter router;
    private PacketQueue pendingPackets;
    private State cachedState;
    private boolean interrupted;

    /**
     * Default constructor.
     */
    protected AbstractRoot() {
        state = new AtomicReference<>(State.NEW);
        delegate = new AtomicReference<>();
        queue = new ConcurrentLinkedQueue<>();
        pending = new ArrayDeque<>();
        lock = new ReentrantLock();
        lookup = Lookup.EMPTY;
    }

    @Override
    public Controller initialize(String id, RootHub hub) {
        if (state.compareAndSet(State.NEW, State.INITIALIZING)) {
            if (id == null || hub == null) {
                throw new NullPointerException();
            }
            this.address = ComponentAddress.of("/" + id);
            this.hub = hub;
            this.time = hub.getClock().getTime();
            this.pendingPackets = new PacketQueue(time);
            this.context = createContext(time);
            this.router = createRouter();
            this.lookup = Lookup.of(hub.getLookup(), router, context);
            if (state.compareAndSet(State.INITIALIZING, State.INITIALIZED)) {
                controller = createController();
                return controller;
            }
        }
        throw new IllegalStateException();
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    /**
     * Find a Service address in the lookup.
     *
     * @param service
     * @return service address
     * @throws ServiceUnavailableException
     */
    protected ComponentAddress findService(Class<? extends Service> service)
            throws ServiceUnavailableException {
        return getLookup().find(Services.class)
                .flatMap(sm -> sm.locate(service))
                .orElseThrow(ServiceUnavailableException::new);
    }

    /**
     * Get the address of this Root. Only valid after initialization.
     *
     * @return address
     */
    protected final ComponentAddress getAddress() {
        return address;
    }

    /**
     * Get the {@link RootHub} this Root is installed in. Only valid after
     * initialization.
     *
     * @return RootHub
     */
    protected final RootHub getRootHub() {
        return hub;
    }

    /**
     * Get the {@link PacketRouter} for this Root. Only valid after
     * initialization.
     *
     * @return router
     */
    protected final PacketRouter getRouter() {
        return router;
    }

    /**
     * Get the {@link ExecutionContext} for this Root. Only valid after
     * initialization.
     *
     * @return execution context
     */
    protected final ExecutionContext getExecutionContext() {
        return context;
    }

    /**
     * Get the State of this Root.
     *
     * @return State
     */
    protected State getState() {
        return state.get();
    }

    /**
     * Method used to create the {@link Controller} during initialization.
     * Subclasses may override to customize the Controller.
     *
     * @return Controller
     */
    protected Controller createController() {
        return new Controller();
    }

    /**
     * Method used to create the {@link DefaultExecutionContext} during
     * initialization. Subclasses may override to customize the execution
     * context.
     *
     * @param initialTime the current clock time of the hub
     * @return execution context
     */
    protected DefaultExecutionContext createContext(long initialTime) {
        return new DefaultExecutionContext(initialTime);
    }

    /**
     * Method used to create the {@link PacketRouter} during initialization. The
     * default implementation directly calls
     * {@link RootHub#dispatch(org.praxislive.core.Packet)}. Subclasses may
     * override to provide an alternative that eg. logs all messages.
     *
     * @return packet router
     */
    protected PacketRouter createRouter() {
        return new Router();
    }

    /**
     * Hook called during activation of this Root, asynchronously after a call
     * to {@link Controller#start(java.util.concurrent.ThreadFactory)}. The
     * default implementation does nothing.
     */
    protected void activating() {
    }

    /**
     * Hook called during termination of this Root, asynchronously after a call
     * to {@link Controller#shutdown()} or other cause of termination. The
     * default implementation does nothing.
     */
    protected void terminating() {
    }

    /**
     * Hook called during a call to {@link #setRunning()} if the previous state
     * was {@link State#ACTIVE_IDLE}. The default implementation does nothing.
     */
    protected void starting() {
    }

    /**
     * Hook called during a call to {@link #setIdle()} if the previous state was
     * {@link State#ACTIVE_RUNNING}. The default implementation does nothing.
     */
    protected void stopping() {
    }

    /**
     * Hook called regularly every time the internal time changes, after all
     * {@link ExecutionContext.ClockListener} have been called. The default
     * implementation does nothing.
     */
    protected void update() {
    }

    /**
     * Method called to handle every received {@link Call}. The provided router
     * should be used for all ongoing or return calls.
     *
     * @param call
     * @param router
     */
    protected abstract void processCall(Call call, PacketRouter router);

    /**
     * Set the Root state to {@link State#ACTIVE_RUNNING}. The state will only
     * be changed, and {@link #starting()} called, if the existing state is
     * idle.
     *
     * @return true if the state has been set to running
     */
    protected final boolean setRunning() {
        if (state.compareAndSet(State.ACTIVE_IDLE, State.ACTIVE_RUNNING)) {
            starting();
            return true;
        }
        return false;
    }

    /**
     * Set the Root state to {@link State#ACTIVE_IDLE}. The state will only be
     * changed, and {@link #stopping()} called, if the existing state is
     * running.
     *
     * @return true if the state has been set to idle
     */
    protected final boolean setIdle() {
        if (state.compareAndSet(State.ACTIVE_RUNNING, State.ACTIVE_IDLE)) {
            stopping();
            return true;
        }
        return false;
    }

    /**
     * Attach a {@link Delegate} to this Root. Also calls {@link #interrupt()}.
     *
     * @param delegate
     * @throws IllegalStateException if a delegate is already attached
     */
    protected final void attachDelegate(Delegate delegate) {
        boolean ok = this.delegate.compareAndSet(null, delegate);
        if (!ok) {
            throw new IllegalStateException("Delegate already attached");
        }
        interrupt();
    }

    /**
     * Detach the provide delegate (if it is attached). Also calls
     * {@link #interrupt()}.
     *
     * @param delegate
     */
    protected final void detachDelegate(Delegate delegate) {
        this.delegate.compareAndSet(delegate, null);
        interrupt();
    }

    /**
     * Interrupt the current update cycle, leaving pending calls or tasks to a
     * subsequent update cycle.
     */
    protected final void interrupt() {
        interrupted = true;
    }

    /**
     * Submit a task to be run asynchronously on the main Root thread. The task
     * is added to the same queue as incoming packets.
     *
     * @param task
     * @return true if the task has been successfully submitted
     */
    protected final boolean invokeLater(Runnable task) {
        boolean ok = queue.add(task);
        if (ok) {
            controller.onQueueReceipt();
        }
        return ok;
    }

    private boolean update(long time, boolean poll) {

        interrupted = false;

        State currentState = state.get();
        if (currentState != State.ACTIVE_IDLE && currentState != State.ACTIVE_RUNNING) {
            return false;
        }

        if (currentState != cachedState) {
            cachedState = currentState;
            if (cachedState == State.ACTIVE_RUNNING) {
                context.updateState(time, ExecutionContext.State.ACTIVE);
            } else {
                context.updateState(time, ExecutionContext.State.IDLE);
            }
        }

        if (poll) {
            pollQueue();
        }

        this.time = time;
        context.updateClock(time);
        pendingPackets.setTime(time);

        update();

        Packet pkt = pendingPackets.poll();
        while (pkt != null) {
            processPacket(pkt);
            if (interrupted) {
                break;
            }
            pkt = pendingPackets.poll();
        }

        return true;
    }

    private void pollQueue() {

        if (interrupted) {
            return;
        }

        State currentState = state.get();
        if (currentState != State.ACTIVE_IDLE && currentState != State.ACTIVE_RUNNING) {
            return;
        }

        long now = context.time;

        for (Object obj = queue.poll(); obj != null; obj = queue.poll()) {
            pending.add(obj);
        }

        for (Object obj = pending.poll(); obj != null; obj = pending.poll()) {
            if (obj instanceof Packet) {
                Packet pkt = (Packet) obj;
                if ((pkt.time() - now) > 0) {
                    pendingPackets.add(pkt);
                } else {
                    processPacket(pkt);
                }
            } else if (obj instanceof Runnable) {
                try {
                    ((Runnable) obj).run();
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, "Runnable task error", t);
                }
            } else {
                LOG.log(Level.SEVERE, "Unknown Object in queue : {0}", obj);
            }

            if (interrupted) {
                break;
            }

        }

    }

    private void processPacket(Packet packet) {
        if (packet instanceof Call) {
            try {
                processCall((Call) packet, router);
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "Uncaught exception processing call", t);
            }
        } else {
            throw new UnsupportedOperationException();
            // have to check for interrupt in iterating CallPacket
            // error on all calls, or post back into queue?
        }
    }

    private class Router implements PacketRouter {

        @Override
        public void route(Packet packet) {
            hub.dispatch(packet);
        }

    }

    /**
     * Implementation of Root.Controller.
     */
    protected class Controller implements Root.Controller {

        private final AtomicBoolean updateQueued = new AtomicBoolean();

        private ScheduledExecutorService exec;
        private ScheduledFuture<?> updateTask;
        private ThreadFactory threadFactory;
        private boolean ownsScheduler;

        @Override
        public boolean submitPacket(Packet packet) {
            boolean ok = queue.offer(packet);
            if (ok) {
                onQueueReceipt();
            }
            return ok;
        }

        @Override
        public void start(ThreadFactory threadFactory) {
            if (state.compareAndSet(State.INITIALIZED, State.ACTIVE_IDLE)) {
                this.threadFactory = threadFactory;
                this.exec = hub.getLookup()
                        .find(ScheduledExecutorService.class)
                        .orElse(null);
                if (exec == null) {
                    exec = Executors.newScheduledThreadPool(1, threadFactory);
                    ownsScheduler = true;
                }
                this.exec.execute(this::doActivate);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public void shutdown() {
            state.updateAndGet(s -> s == State.TERMINATED
                    ? State.TERMINATED : State.TERMINATING);
        }

        /**
         * Called on receipt of a {@link Packet} (Call) or a Runnable task. The
         * default implementation will call {@link Delegate#onQueueReceipt()} if
         * a delegate is attached, or otherwise trigger an asynchronous poll of
         * the queue.
         */
        protected void onQueueReceipt() {
            Delegate del = delegate.get();
            if (del != null) {
                del.onQueueReceipt();
            } else {
                if (updateQueued.compareAndSet(false, true)) {
                    exec.execute(this::doPoll);
                }
            }
        }

        private void doActivate() {
            try {
                activating();
                updateTask = exec.scheduleAtFixedRate(this::doUpdate, 0, 10, TimeUnit.MILLISECONDS);
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "Uncaught error in activation", t);
                doTerminate();
            }
        }

        private void doUpdate() {
            Delegate del = delegate.get();
            if (del != null) {
                // do clock check
                if (Math.abs(hub.getClock().getTime() - time) > 10_000_000_000L) {
                    LOG.log(Level.SEVERE, "Delegate not updating time");
                    detachDelegate(del);
                }
            } else {
                if (lock.tryLock()) {
                    try {
                        if (!update(hub.getClock().getTime(), true)) {
                            updateTask.cancel(false);
                            doTerminate();
                        }
                    } catch (Throwable t) {
                        LOG.log(Level.SEVERE, "Uncaught error", t);
                    } finally {
                        lock.unlock();
                    }
                } else {
                    LOG.info("Lock already taken");
                }
            }
        }

        private void doPoll() {
            updateQueued.set(false);
            Delegate del = delegate.get();
            if (del != null) {
                // do nothing
            } else {
                if (lock.tryLock()) {
                    try {
                        pollQueue();
                    } catch (Throwable t) {
                        LOG.log(Level.SEVERE, "Uncaught error", t);
                    } finally {
                        lock.unlock();
                    }
                } else {
                    LOG.info("Lock already taken");
                }
            }
        }

        private void doTerminate() {
            State s = state.get();
            while (s != State.TERMINATED) {
                if (state.compareAndSet(s, State.TERMINATED)) {
                    try {
                        terminating();
                    } catch (Throwable t) {
                        LOG.log(Level.SEVERE, "Uncaught error in termination", t);
                    }
                    context.updateState(hub.getClock().getTime(), ExecutionContext.State.TERMINATED);
                    if (ownsScheduler) {
                        exec.shutdown();
                    }
                } else {
                    s = state.get();
                }
            }
        }

    }

    /**
     * An abstract delegate class that may be attached to this Root to drive it
     * from another source (eg. audio callback or UI event queue). Subclasses
     * should call {@link #doUpdate(long)} regularly to update the clock time
     * and process pending tasks.
     * <p>
     * The delegate should prefer calling
     * {@link #doTimedPoll(long, java.util.concurrent.TimeUnit)} to thread
     * sleeping or other timing mechanisms where possible so that incoming
     * packets and tasks are processed while waiting.
     * <p>
     * If the other source provides a mechanism for invoking tasks
     * asynchronously (eg. EventQueue.invokeLater(Runnable task)) it should
     * override {@link #onQueueReceipt()} to asynchronously trigger
     * {@link #doPollQueue()} to process tasks and packets between updates.
     * <p>
     * If the other source driving this delegate requires a new Thread to run
     * on, it should obtain it from {@link #getThreadFactory()}
     */
    protected abstract class Delegate {

        private final ReentrantLock pollLock;
        private final Condition pollCondition;

        protected Delegate() {
            this.pollLock = new ReentrantLock();
            this.pollCondition = pollLock.newCondition();
        }

        /**
         * Update the Root time and process tasks, inbound calls and clock
         * listeners.
         *
         * @param time new clock time (directly from or related to
         * {@link RootHub#getClock()}
         * @return false if the Root has been terminated or the delegate
         * detached
         */
        protected final boolean doUpdate(long time) {
            if (delegate.get() == this) {
                if (lock.tryLock()) {
                    try {
                        return update(time, true);
                    } catch (Throwable t) {
                        LOG.log(Level.SEVERE, "Uncaught error", t);
                    } finally {
                        lock.unlock();
                    }
                } else {
                    LOG.info("Lock already taken");
                }
                return true;
            } else {
                LOG.info("Delegate invalid");
                return false;
            }
        }

        /**
         * Poll the queue, running any available tasks and dispatching any
         * packets with a timecode before the current Root time.
         */
        protected final void doPollQueue() {
            if (delegate.get() == this) {
                if (lock.tryLock()) {
                    try {
                        pollQueue();
                    } catch (Throwable t) {
                        LOG.log(Level.SEVERE, "Uncaught error", t);
                    } finally {
                        lock.unlock();
                    }
                } else {
                    LOG.info("Lock already taken");
                }
            } else {
                LOG.info("Delegate invalid");
            }
        }

        /**
         * Wait up to the given time for a queue notification, running any
         * available tasks and dispatching any packets with a timecode before
         * the current Root time.
         *
         * @param time
         * @param unit
         * @throws InterruptedException
         */
        protected final void doTimedPoll(long time, TimeUnit unit) throws InterruptedException {
            if (pollLock.tryLock()) {
                try {
                    if (pollCondition.await(time, unit)) {
                        if (delegate.get() == this) {
                            try {
                                pollQueue();
                            } catch (Throwable t) {
                                LOG.log(Level.SEVERE, "Uncaught error", t);
                            }
                        }
                    }
                } finally {
                    pollLock.unlock();
                }
            }
        }

        /**
         * Get a thread factory for creating any new threads required by the
         * delegate.
         *
         * @return thread factory for all required threads
         */
        protected final ThreadFactory getThreadFactory() {
            return controller.threadFactory;
        }

        /**
         * Called when a new Packet
         * {@link Controller#submitPacket(org.praxislive.core.Packet)} or task
         * {@link #invokeLater(java.lang.Runnable)} is submitted. By default
         * signals the thread waiting on
         * {@link #doTimedPoll(long, java.util.concurrent.TimeUnit)}. Can be
         * overridden to perform other notification, but if this implementation
         * is not called then timed polling will not work.
         */
        protected void onQueueReceipt() {
            if (pollLock.tryLock()) {
                try {
                    pollCondition.signal();
                } finally {
                    pollLock.unlock();
                }
            }
        }

    }

}
