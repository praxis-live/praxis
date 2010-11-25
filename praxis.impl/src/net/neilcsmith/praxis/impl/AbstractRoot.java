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

import net.neilcsmith.praxis.core.interfaces.ServiceManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.*;
import net.neilcsmith.praxis.core.interfaces.StartableInterface;
import net.neilcsmith.praxis.core.types.PBoolean;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith
 * @TODO Add Control address caching
 */
public abstract class AbstractRoot extends AbstractContainer implements Root, PacketRouter {

    public static enum Caps {

        Component, Container, Startable
    };
    private static final Logger LOG = Logger.getLogger(AbstractRoot.class.getName());
    public static final int DEFAULT_FRAME_TIME = 100; // set in constructor?
    private static final ListenerSorter listenerSorter = new ListenerSorter();
    private AtomicReference<RootState> state = new AtomicReference<RootState>(RootState.NEW);
    private RootState cachedState = RootState.NEW; // cache to pass to listeners because for thread safety
    private RootState defaultRunState;
    private RootHub hub;
    private String ID;
    private ComponentAddress address;
    private PacketQueue orderedQueue = new PacketQueue();
    private BlockingQueue<Packet> blockingQueue = new LinkedBlockingQueue<Packet>();
    private RootStateListener[] stateListeners = new RootStateListener[0];
    private ControlFrameListener[] frameListeners = new ControlFrameListener[0];
    private long time;
//    private DefaultTaskController defaultTaskController;
//    private TaskController taskController;
    private Root.Controller controller;
    private Runnable interrupt;
    private Lookup lookup;
    private ExecutionContextImpl context;

    protected AbstractRoot() {
        this(EnumSet.allOf(Caps.class));
    }

//    @Deprecated
//    protected AbstractRoot(RootState defaultRunState) {
//        if (defaultRunState != RootState.ACTIVE_IDLE
//                && defaultRunState != RootState.ACTIVE_RUNNING) {
//            throw new IllegalArgumentException("Default run state must be ACTIVE_IDLE or ACTIVE_RUNNING");
//        }
//        this.defaultRunState = defaultRunState;
//        createStartableControls();
//    }

    protected AbstractRoot(EnumSet<Caps> caps) {
        super(caps.contains(Caps.Container), caps.contains(Caps.Component));
        if (caps.contains(Caps.Startable)) {
            createStartableControls();
            defaultRunState = RootState.ACTIVE_IDLE;
        } else {
            defaultRunState = RootState.ACTIVE_RUNNING;
        }
    }

    private void createStartableControls() {
        registerControl(StartableInterface.START, new TransportControl(true));
        registerControl(StartableInterface.STOP, new TransportControl(false));
        registerControl(StartableInterface.IS_RUNNING,
                ArgumentProperty.createReadOnly(PBoolean.info(),
                new ArgumentProperty.ReadBinding() {

            public Argument getBoundValue() {
                if (state.get() == RootState.ACTIVE_RUNNING) {
                    return PBoolean.TRUE;
                } else {
                    return PBoolean.FALSE;
                }
            }
        }));
    }

    public Root.Controller initialize(String ID, RootHub hub) throws IllegalRootStateException {
        if (state.compareAndSet(RootState.NEW, RootState.INITIALIZING)) {
            if (ID == null || hub == null) {
                throw new NullPointerException();
            }
            try {
                this.address = ComponentAddress.valueOf("/" + ID);
            } catch (ArgumentFormatException ArgumentFormatException) {
                throw new IllegalArgumentException(ArgumentFormatException);
            }
            this.ID = ID;
            this.hub = hub;
            this.context = new ExecutionContextImpl(System.nanoTime());
            this.lookup = InstanceLookup.create(hub.getLookup(), this, context);
            // in place controller
//            this.taskController = new TaskController();
            // task controller
            String managerID = "_ats_" + Integer.toHexString(hashCode());
//            try {
            ControlAddress taskAddress = ControlAddress.create(this.address, managerID);
//            defaultTaskController = new DefaultTaskController(this, taskAddress);
//            registerControl(taskAddress.getID(), defaultTaskController);
//            } catch (ArgumentFormatException ex) {
//
//            }
            initializing(); // hook for subclasses
            if (state.compareAndSet(RootState.INITIALIZING, RootState.INITIALIZED)) {
                controller = new Controller();
//                fireRootStateListeners(RootState.INITIALIZED);
                return controller;
            }
        }
        throw new IllegalRootStateException();
    }

    public void route(Packet packet) {
        try {
            hub.dispatch(packet);
        } catch (InvalidAddressException ex) {
            if (packet instanceof Call) {
                controller.submitPacket(
                        Call.createErrorCall((Call) packet, PReference.wrap(ex)));
            } else {
                throw new UnsupportedOperationException();
            }


        }
    }

    // make protected?
    @Deprecated
    public PacketRouter getPacketRouter() {
        return this;
    }

    protected RootHub getRootHub() {
        return hub;
    }

    @Deprecated
    public void addRootStateListener(RootStateListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        List<RootStateListener> list = new ArrayList<RootStateListener>(Arrays.asList(stateListeners));
        list.add(listener);
        Collections.sort(list, listenerSorter);
        stateListeners = list.toArray(new RootStateListener[list.size()]);
    }

    @Deprecated
    public void removeRootStateListener(RootStateListener listener) {
        List<RootStateListener> list = new ArrayList<RootStateListener>(Arrays.asList(stateListeners));
        list.remove(listener);
        stateListeners = list.toArray(new RootStateListener[list.size()]);
    }

    @Deprecated
    protected void fireRootStateListeners(RootState state) {
        cachedState = state;
        RootStateListener[] listeners = stateListeners; // cache in case of changes
        for (RootStateListener listener : listeners) {
            listener.rootStateChanged(this, state);
        }
    }

    @Deprecated
    public void addControlFrameListener(ControlFrameListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        List<ControlFrameListener> list = new ArrayList<ControlFrameListener>(Arrays.asList(frameListeners));
        list.add(listener);
        Collections.sort(list, listenerSorter);
        frameListeners = list.toArray(new ControlFrameListener[list.size()]);
    }

    @Deprecated
    public void removeControlFrameListener(ControlFrameListener listener) {
        List<ControlFrameListener> list = new ArrayList<ControlFrameListener>(Arrays.asList(frameListeners));
        list.remove(listener);
        frameListeners = list.toArray(new ControlFrameListener[list.size()]);
    }

    @Deprecated
    protected void fireControlFrameListeners() {
        ControlFrameListener[] listeners = frameListeners; // cache
        for (ControlFrameListener listener : listeners) {
            listener.nextControlFrame(this);
        }
    }

    public long getTime() {
        return time;
    }

    @Deprecated
    protected void setTime(long time) {
        this.time = time;
    }

    // Empty hooks for subclasses to extend setup
    protected void initializing() {
    }

    protected void activating() {
    }

    protected void terminating() {
    }

    protected void starting() {
    }

    protected void stopping() {
    }

    protected void processingControlFrame() {
    }

    public RootState getState() {
        return state.get();
    }

    protected final void setRunning() throws IllegalRootStateException {
        if (state.compareAndSet(RootState.ACTIVE_IDLE, RootState.ACTIVE_RUNNING)) {
//            fireRootStateListeners();
            starting();
            return;
        }
        throw new IllegalRootStateException();
    }

    protected final void setIdle() throws IllegalRootStateException {
        if (state.compareAndSet(RootState.ACTIVE_RUNNING, RootState.ACTIVE_IDLE)) {
//            fireRootStateListeners();
            stopping();
            return;
        }
        throw new IllegalRootStateException();
    }

    protected void run() {
//        while (activeStates.contains(state.get())) {
        while (true) {
            RootState currentState = state.get();
            if (currentState != RootState.ACTIVE_IDLE && currentState != RootState.ACTIVE_RUNNING) {
                break;
            }
            Packet packet = null;
            try {
                packet = blockingQueue.poll(DEFAULT_FRAME_TIME, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                continue; // check state again if interrupted
            }
//            setTime(System.nanoTime());
            time = System.nanoTime();
            processControlFrame(packet, currentState);
            if (interrupt != null) {
                Runnable task = interrupt;
                interrupt = null;
                task.run();
            }
        }
    }

    protected void setInterrupt(Runnable task) {
        if (interrupt == null) {
            interrupt = task;
        }
    }

    protected final void processControlFrame() throws IllegalRootStateException {
        processControlFrame(null);
    }

    protected final void processControlFrame(Call call) throws IllegalRootStateException {
        RootState currentState = state.get();
        if (currentState == RootState.ACTIVE_RUNNING || currentState == RootState.ACTIVE_IDLE) {
            processControlFrame(null, currentState);
        } else {
            throw new IllegalRootStateException();
        }
        if (interrupt != null) {
            Runnable task = interrupt;
            interrupt = null;
            task.run();
        }
    }

    private void processControlFrame(Packet packet, RootState currentState) {
        long t = getTime();

        if (currentState != cachedState) {
            cachedState = currentState;
            fireRootStateListeners(cachedState);
            if (cachedState == RootState.ACTIVE_RUNNING) {
                context.setState(ExecutionContext.State.ACTIVE);
            } else {
                context.setState(ExecutionContext.State.IDLE);
            }
        }

        processingControlFrame();

        context.setTime(time);

        if (packet == null) {
            packet = blockingQueue.poll();
        }
        orderedQueue.setTime(t);
        // drain blocking queue to ordered queue
        while (packet != null) {
            orderedQueue.add(packet);
            packet = blockingQueue.poll();
        }
        // process ordered queue
        packet = orderedQueue.poll();
        while (packet != null) {
            processPacket(packet);
            if (interrupt != null) {
                return;
            }
            packet = orderedQueue.poll();
        }
        // check tasks
//        taskController.checkTasks(t);
        // fire listeners after all calls have been made
        if (currentState == RootState.ACTIVE_RUNNING) {
            fireControlFrameListeners();
        }

//        // invoke tasks
//        while (!invocationQueue.isEmpty()) {
//            invocationQueue.poll().run();
//        }
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
        Control control = getControl(call.getToAddress());
        try {
            if (control != null) {
                control.call(call, this);
            } else {
                Call.Type type = call.getType();
                if (type == Call.Type.INVOKE || type == Call.Type.INVOKE_QUIET) {
                    route(Call.createErrorCall(call, PString.valueOf("Unknown control address : " + call.getToAddress())));
                }
            }
        } catch (Exception ex) {
            Call.Type type = call.getType();
            LOG.log(Level.WARNING, "Exception thrown from call\n" + call, ex);
            if (type == Call.Type.INVOKE || type == Call.Type.INVOKE_QUIET) {
                route(Call.createErrorCall(call, PReference.wrap(ex)));
            }
        }


    }

    protected Control getControl(ControlAddress address) {
        Component comp = getComponent(address.getComponentAddress());
        if (comp != null) {
            return comp.getControl(address.getID());
        } else {
            return null;
        }
    }

//    protected Port getPort(PortAddress address) {
//        Component comp = getComponent(address.getComponentAddress());
//        if (comp != null) {
//            return comp.getPort(address.getID());
//        } else {
//            return null;
//        }
//    }

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

    @Override
    public Root getRoot() {
        return this;
    }

    @Deprecated
    public ServiceManager getServiceManager() {
//        return hub.getServiceManager();
        return hub.getLookup().get(ServiceManager.class);
    }

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

    protected void addComponent(ComponentAddress address, Component component)
            throws Exception {
        if (address == null || component == null) {
            throw new NullPointerException();
        }
        Component parent = findComponent(address, address.getDepth() - 1);
        if (parent instanceof Container) {
            ((Container) parent).addChild(
                    address.getComponentID(address.getDepth() - 1),
                    component);
        } else {
            throw new InvalidAddressException();
        }
    }

    protected void removeComponent(ComponentAddress address) throws Exception {
        if (address == null) {
            throw new NullPointerException();
        }
        Component parent = findComponent(address, address.getDepth() - 1);
        if (parent instanceof Container) {
            Component child = ((Container) parent).removeChild(
                    address.getComponentID(address.getDepth() - 1));
            if (child == null) {
                throw new InvalidAddressException();
            }
        } else {
            throw new InvalidAddressException();
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
                fireRootStateListeners(RootState.TERMINATING);
                context.setState(ExecutionContext.State.TERMINATED);
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
            super(start ? StartableInterface.START_INFO :
                StartableInterface.STOP_INFO);
            this.start = start;
        }

        @Override
        protected CallArguments process(CallArguments args, boolean quiet) throws Exception {
            if (start) {
                setRunning();
                return CallArguments.EMPTY;
            } else {
                setIdle();
                return CallArguments.EMPTY;
            }
        }

    }


    private static class ListenerSorter implements Comparator<OrderedListener> {

        public int compare(OrderedListener o1, OrderedListener o2) {
            int val = o2.getDepth() - o1.getDepth();
            return val == 0 ? o2.getPriority() - o1.getPriority() : val;

        }
    }

}
