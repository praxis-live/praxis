/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2014 Neil C Smith.
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

import org.praxislive.core.IllegalRootStateException;
import org.praxislive.core.ValueFormatException;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.CallArguments;
import org.praxislive.core.Call;
import org.praxislive.core.RootHub;
import org.praxislive.core.VetoException;
import org.praxislive.core.Component;
import org.praxislive.core.Packet;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.Lookup;
import org.praxislive.core.Root;
import org.praxislive.core.ExecutionContext;
import org.praxislive.core.Container;
import org.praxislive.core.Value;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Control;
import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.protocols.StartableProtocol;
import org.praxislive.core.services.SystemManagerService;
import org.praxislive.core.types.PBoolean;
import org.praxislive.core.types.PReference;
import org.praxislive.core.types.PString;
import org.praxislive.util.ArrayUtils;

/**
 *
 * @author Neil C Smith
 * @TODO Add Control address caching
 */
public abstract class AbstractRoot extends AbstractContainer implements Root {

    public static enum Caps {

        Component, Container, Startable, ExitableOnStop
    };
    private static final Logger LOG = Logger.getLogger(AbstractRoot.class.getName());
    public static final int DEFAULT_FRAME_TIME = 10; // set in constructor?
    private AtomicReference<RootState> state = new AtomicReference<RootState>(RootState.NEW);
    private RootState cachedState = RootState.NEW; // cache to pass to listeners for thread safety
    private RootState defaultRunState;
    private RootHub hub;
    private String ID;
    private ComponentAddress address;
    private PacketQueue orderedQueue = new PacketQueue();
    private BlockingQueue<Object> blockingQueue = new LinkedBlockingQueue<Object>();
    private long time;
    private Root.Controller controller;
    //private Runnable interrupt;
    private Lookup lookup;
    private Context context;
    private Router router;
    private ExitOnStopControl exitOnStop;
    private Runnable delegate;
    private boolean interrupted;

    protected AbstractRoot() {
        this(EnumSet.allOf(Caps.class));
    }

    protected AbstractRoot(EnumSet<Caps> caps) {
        super(caps.contains(Caps.Container), caps.contains(Caps.Component));
        if (caps.contains(Caps.Startable)) {
            createStartableInterface();
            defaultRunState = RootState.ACTIVE_IDLE;
        } else {
            defaultRunState = RootState.ACTIVE_RUNNING;
        }
        if (caps.contains(Caps.ExitableOnStop)) {
            createExitOnStopControl();
        }
    }

    private void createStartableInterface() {
        registerControl(StartableProtocol.START, new TransportControl(true));
        registerControl(StartableProtocol.STOP, new TransportControl(false));
        registerControl(StartableProtocol.IS_RUNNING,
                ArgumentProperty.createReadOnly(PBoolean.info(),
                        new ArgumentProperty.ReadBinding() {
                    public Value getBoundValue() {
                        if (state.get() == RootState.ACTIVE_RUNNING) {
                            return PBoolean.TRUE;
                        } else {
                            return PBoolean.FALSE;
                        }
                    }
                }));
        registerProtocol(StartableProtocol.class);
    }

    private void createExitOnStopControl() {
        exitOnStop = new ExitOnStopControl();
        registerControl("exit-on-stop", BooleanProperty.create(exitOnStop, false));
        registerControl("_exit-log", exitOnStop);
    }

    public Root.Controller initialize(String ID, RootHub hub) throws IllegalRootStateException {
        if (state.compareAndSet(RootState.NEW, RootState.INITIALIZING)) {
            if (ID == null || hub == null) {
                throw new NullPointerException();
            }
            try {
                this.address = ComponentAddress.valueOf("/" + ID);
            } catch (ValueFormatException ex) {
                throw new IllegalArgumentException(ex);
            }
            this.ID = ID;
            this.hub = hub;
            this.context = createContext();
            this.router = new Router();
            this.lookup = InstanceLookup.create(hub.getLookup(), router, context);
            if (state.compareAndSet(RootState.INITIALIZING, RootState.INITIALIZED)) {
                controller = new Controller();
                return controller;
            }
        }
        throw new IllegalRootStateException();
    }

    protected void disconnect() {
        this.router = null;
        this.lookup = Lookup.EMPTY;
        for (String id : getChildIDs()) {
            removeChild(id);
        }
        hierarchyChanged();
        hub = null;
    }

    //@TODO make protected.
    @Override
    protected PacketRouter getPacketRouter() {
        return router;
    }

    @Deprecated
    protected RootHub getRootHub() {
        return hub;
    }

    public long getTime() {
        return time;
    }

//    @Deprecated
//    protected void setTime(long time) {
//        this.time = time;
//    }
    protected Context createContext() {
        return new Context();
    }

    protected void activating() {
    }

    protected void terminating() {
    }

    protected void starting() {
    }

    protected void stopping() {
    }

    @Deprecated
    protected void processingControlFrame() {
    }

    public RootState getState() {
        return state.get();
    }

    protected final void setRunning() throws IllegalRootStateException {
        if (state.compareAndSet(RootState.ACTIVE_IDLE, RootState.ACTIVE_RUNNING)) {
            starting();
            return;
        }
        throw new IllegalRootStateException();
    }

    protected final void setIdle() throws IllegalRootStateException {
        if (state.compareAndSet(RootState.ACTIVE_RUNNING, RootState.ACTIVE_IDLE)) {
            stopping();
            if (exitOnStop != null) {
                exitOnStop.idling();
            }
            return;
        }
        throw new IllegalRootStateException();
    }

    protected void run() {
        while (state.get() != RootState.TERMINATING) {
            try {
                if (delegate != null) {
                    try {
                        delegate.run();
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, "Delegate threw Exception", ex);
                    } finally {
                        delegate = null;
                    }
                }
                update(System.nanoTime(), true);
                if (!interrupted) {
                    poll(DEFAULT_FRAME_TIME, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException ex) {
                continue;
            } catch (Exception ex) {
                LOG.log(Level.FINEST, "", ex);
                break;
            }
        }
    }

    @SuppressWarnings("deprecation")
    protected final void update(long time, boolean poll) throws IllegalRootStateException {

        interrupted = false;

        RootState currentState = state.get();
        if (currentState != RootState.ACTIVE_IDLE && currentState != RootState.ACTIVE_RUNNING) {
            throw new IllegalRootStateException();
        }

        if (currentState != cachedState) {
            cachedState = currentState;
            if (cachedState == RootState.ACTIVE_RUNNING) {
                context.updateState(time, ExecutionContext.State.ACTIVE);
            } else {
                context.updateState(time, ExecutionContext.State.IDLE);
            }
        }

        if (poll) {
            try {
                poll(0, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                Logger.getLogger(AbstractRoot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        this.time = time;
        context.updateClock(time);
        orderedQueue.setTime(time);

        processingControlFrame();

        Packet pkt = orderedQueue.poll();
        while (pkt != null) {
            processPacket(pkt);
            if (interrupted) {
                break;
            }
            pkt = orderedQueue.poll();
        }

    }

    protected final void poll(long timeout, TimeUnit unit) throws InterruptedException {

        if (interrupted) {
            if (timeout > 0) {
                LockSupport.parkNanos(unit.toNanos(timeout));
            }
            return;
        }

        Object obj;
        if (timeout <= 0) {
            obj = blockingQueue.poll();
        } else {
            obj = blockingQueue.poll(timeout, unit);
        }

        long now = time;
        while (obj != null) {

            if (obj instanceof Packet) {
                Packet pkt = (Packet) obj;
                if ((pkt.getTimecode() - now) > 0) {
                    orderedQueue.add(pkt);
                } else {
                    processPacket(pkt);
                }
            } else if (obj instanceof Runnable) {
                processTask((Runnable) obj);
            } else {
                LOG.log(Level.SEVERE, "Unknown Object in queue : {0}", obj);
            }

            if (interrupted) {
                break;
            }

            obj = blockingQueue.poll();

        }

    }

    protected final void setDelegate(Runnable del) {
        if (delegate != null) {
            LOG.log(Level.SEVERE, "Trying to set delegate while delegate already set");
            throw new IllegalStateException("Delegate already set");
        }
        delegate = del;
        interrupt();
    }

    protected final void interrupt() {
        interrupted = true;
    }

    protected boolean invokeLater(Runnable task) {
        return blockingQueue.offer(task);
    }

    @Deprecated
    protected void setInterrupt(Runnable task) {
        if (delegate == null) {
            delegate = task;
            interrupt();
        } else {
            task.run();
            interrupt();
        }
    }

    @Deprecated
    protected void nextControlFrame(long time) throws IllegalRootStateException {
        update(time, true);
    }

    protected void processTask(Runnable task) {
        task.run();
    }

    protected void processPacket(Packet packet) {
        if (packet instanceof Call) {
            processCall((Call) packet);
        } else {
            throw new UnsupportedOperationException();
            // have to check for interrupt in iterating CallPacket
            // error on all calls, or post back into queue?
        }
    }

    protected void processCall(Call call) {
        org.praxislive.core.Control control = getControl(call.getToAddress());
        try {
            if (control != null) {
                control.call(call, router);
            } else {
                Call.Type type = call.getType();
                if (type == Call.Type.INVOKE || type == Call.Type.INVOKE_QUIET) {
                    router.route(Call.createErrorCall(call, PString.valueOf("Unknown control address : " + call.getToAddress())));
                }
            }
        } catch (Exception ex) {
            Call.Type type = call.getType();
            LOG.log(Level.FINE, "Exception thrown from call\n" + call, ex);
            if (type == Call.Type.INVOKE || type == Call.Type.INVOKE_QUIET) {
                router.route(Call.createErrorCall(call, PReference.wrap(ex)));
            }
        }
    }

    protected org.praxislive.core.Control getControl(ControlAddress address) {
        Component comp = getComponent(address.getComponentAddress());
        if (comp != null) {
            return comp.getControl(address.getID());
        } else {
            return null;
        }
    }

    protected Component getComponent(ComponentAddress address) {
        // add caching
        return findComponent(address, address.getDepth());
    }

    private Component findComponent(ComponentAddress address, int depth) {
        if (address.getComponentID(0).equals(this.ID)) {
            Component comp = this;
            for (int i = 1; i < depth; i++) {
                if (comp instanceof Container) {
                    comp = ((Container) comp).getChild(address.getComponentID(i));
                } else {
                    return null;
                }
            }
            return comp;
        }
        return null;
    }

//    @Deprecated
//    public ServiceManager getServiceManager() {
////        return hub.getServiceManager();
//        return hub.getLookup().get(ServiceManager.class);
//    }
    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public ComponentAddress getAddress() {
        return address;
    }

    @Override
    public void parentNotify(Container parent) throws VetoException {
        // should always throw exception, but keep in line with API and only throw
        // if parent isn't null
        if (parent != null) {
            throw new VetoException();
        }
    }

    public class Controller implements Root.Controller {

        private Controller() {
        }

        public boolean submitPacket(Packet packet) {
            return blockingQueue.offer(packet);
        }

        public void shutdown() {
            RootState s = state.get();
            while (true) {
                if (s == RootState.TERMINATED) {
                    return;
                } else {
                    if (state.compareAndSet(s, RootState.TERMINATING)) {
                        // System.out.println("State set to terminated");
                        return;
                    } else {
                        s = state.get();
                    }
                }
            }
        }

        public void run() throws IllegalRootStateException {
            if (state.compareAndSet(RootState.INITIALIZED, defaultRunState)) {
                activating();
                AbstractRoot.this.run();
                state.set(RootState.TERMINATING); // in case run finished before shutdown called
                terminating();
                context.updateState(System.nanoTime(), ExecutionContext.State.TERMINATED);
                disconnect();
                // disconnect all children?
                state.set(RootState.TERMINATED);
            } else {
                throw new IllegalRootStateException();
            }

        }
    }

    private class TransportControl extends SimpleControl {

        private boolean start;

        private TransportControl(boolean start) {
            super(start ? StartableProtocol.START_INFO
                    : StartableProtocol.STOP_INFO);
            this.start = start;
        }

        @Override
        protected CallArguments process(long time, CallArguments args, boolean quiet) throws Exception {
            if (start) {
                setRunning();
                return CallArguments.EMPTY;
            } else {
                setIdle();
                return CallArguments.EMPTY;
            }
        }
    }

    private class ExitOnStopControl extends SimpleControl implements BooleanProperty.Binding {

        private boolean exit;

        private ExitOnStopControl() {
            super(null);
        }

        @Override
        protected CallArguments process(long time, CallArguments args, boolean quiet) throws Exception {
            throw new UnsupportedOperationException("Not supported.");
        }

        public void setBoundValue(long time, boolean value) {
            exit = value;
        }

        public boolean getBoundValue() {
            return exit;
        }

        private void idling() {
            if (exit) {
                try {
                    ControlAddress to = ControlAddress.create(findService(SystemManagerService.class),
                            SystemManagerService.SYSTEM_EXIT);
                    getPacketRouter().route(Call.createCall(
                            to,
                            ControlAddress.create(getAddress(), "_exit-log"),
                            getTime(),
                            CallArguments.EMPTY));
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Can't access SystemManagerService - exiting manually", ex);
                    System.exit(0);
                }

            }
        }
    }

    private class Router implements PacketRouter {

        public void route(Packet packet) {
            hub.dispatch(packet);
        }
    }

    protected class Context extends ExecutionContext {

        private ExecutionContext.StateListener[] stateListeners;
        private ExecutionContext.ClockListener[] clockListeners;
        private ExecutionContext.State state;
        private long time;
        private long startTime;

        public Context() {
            this.stateListeners = new ExecutionContext.StateListener[0];
            this.clockListeners = new ExecutionContext.ClockListener[0];
            this.state = ExecutionContext.State.NEW;
            this.time = System.nanoTime();
            this.startTime = this.time;
        }

        @Override
        public void addStateListener(ExecutionContext.StateListener listener) {
            stateListeners = ArrayUtils.add(stateListeners, listener);
        }

        @Override
        public void removeStateListener(ExecutionContext.StateListener listener) {
            stateListeners = ArrayUtils.remove(stateListeners, listener);
        }

        @Override
        public void addClockListener(ExecutionContext.ClockListener listener) {
            clockListeners = ArrayUtils.add(clockListeners, listener);
        }

        @Override
        public void removeClockListener(ExecutionContext.ClockListener listener) {
            clockListeners = ArrayUtils.remove(clockListeners, listener);
        }

        @SuppressWarnings("fallthrough")
        protected void updateState(long time, ExecutionContext.State state) {
            // make sure time is set before state listeners called.
            this.time = time;
            switch (state) {
                case ACTIVE:
                    if (this.state != State.ACTIVE) {
                        startTime = time;
                    }
                case IDLE:
                    if (this.state == ExecutionContext.State.TERMINATED) {
                        throw new IllegalStateException("Execution Context terminated");
                    }
                // fall through
                case TERMINATED:
                    if (this.state != state) {
                        this.state = state;
                        fireStateListeners();
                    }
                    break;
                case NEW:
                    if (this.state != ExecutionContext.State.NEW) {
                        throw new IllegalStateException("Can't make Execution Context NEW again.");
                    }
                    break;
                default:
                    throw new RuntimeException();

            }
        }

        @Override
        public ExecutionContext.State getState() {
            return state;
        }

        protected void updateClock(long time) {
            this.time = time;
            if (state == ExecutionContext.State.ACTIVE) {
                fireClockListeners();
            }
        }

        @Override
        public long getTime() {
            return time;
        }

        @Override
        public boolean supportsStartTime() {
            return true;
        }

        @Override
        public long getStartTime() {
            return startTime;
        }

        private void fireStateListeners() {
            for (ExecutionContext.StateListener l : stateListeners) {
                l.stateChanged(this);
            }
        }

        private void fireClockListeners() {
            for (ExecutionContext.ClockListener l : clockListeners) {
                l.tick(this);
            }
        }
    }
}
