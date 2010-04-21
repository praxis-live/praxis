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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.*;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ComponentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith
 * @TODO Add Control address caching
 */
public abstract class AbstractRoot extends AbstractContainer implements Root, PacketRouter {

    private static final Logger logger = Logger.getLogger(AbstractRoot.class.getName());
    public static final int DEFAULT_FRAME_TIME = 100; // set in constructor?
    private static final ListenerSorter listenerSorter = new ListenerSorter();
    private AtomicReference<Root.State> state = new AtomicReference<Root.State>(Root.State.NEW);
    private Root.State cachedState = Root.State.NEW; // cache to pass to listeners because for thread safety
    private Root.State defaultRunState;
    private RootHub hub;
    private String ID;
    private ComponentAddress address;
    private PacketQueue orderedQueue = new PacketQueue();
    private BlockingQueue<Packet> blockingQueue = new LinkedBlockingQueue<Packet>();
    private RootStateListener[] stateListeners = new RootStateListener[0];
    private ControlFrameListener[] frameListeners = new ControlFrameListener[0];
    private long time;
    private DefaultTaskController defaultTaskController;
    private TaskController taskController;
    private Root.Controller controller;
    private Runnable interrupt;

    protected AbstractRoot() {
        this(Root.State.ACTIVE_IDLE);
    }

    protected AbstractRoot(Root.State defaultRunState) {
        if (defaultRunState != Root.State.ACTIVE_IDLE &&
                defaultRunState != Root.State.ACTIVE_RUNNING) {
            throw new IllegalArgumentException("Default run state must be ACTIVE_IDLE or ACTIVE_RUNNING");
        }
        this.defaultRunState = defaultRunState;
        createDefaultControls();
    }

    private void createDefaultControls() {
        registerControl("_add", new AddControl());
        registerControl("_remove", new RemoveControl());
        registerControl("_connect", new PortControl(true));
        registerControl("_disconnect", new PortControl(false));
        registerControl("start", new TransportControl(true));
        registerControl("stop", new TransportControl(false));
        registerControl("info", new InfoControl());
    }

    public Root.Controller initialize(String ID, RootHub hub) throws IllegalRootStateException {
        if (state.compareAndSet(Root.State.NEW, Root.State.INITIALIZING)) {
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
            // in place controller
            this.taskController = new TaskController();
            // task controller
            String managerID = "_ats_" + Integer.toHexString(hashCode());
//            try {
                ControlAddress taskAddress = ControlAddress.create(this.address, managerID);
                defaultTaskController = new DefaultTaskController(this, taskAddress);
                registerControl(taskAddress.getID(), defaultTaskController);
//            } catch (ArgumentFormatException ex) {
//
//            }
            initializing(); // hook for subclasses
            if (state.compareAndSet(Root.State.INITIALIZING, Root.State.INITIALIZED)) {
                controller = new Controller();
                fireRootStateListeners(Root.State.INITIALIZED);
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

    public PacketRouter getPacketRouter() {
        return this;
    }

    protected RootHub getRootHub() {
        return hub;
    }

    public long submitTask(Task task, TaskListener listener) throws ServiceUnavailableException {
        // in place controller

//            return taskController.submitTask(task, listener);

        // service controller
        if (defaultTaskController != null) {
            return defaultTaskController.submitTask(task, listener);
        } else {
            throw new ServiceUnavailableException();
        }
    }

    public void addRootStateListener(RootStateListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        List<RootStateListener> list = new ArrayList<RootStateListener>(Arrays.asList(stateListeners));
        list.add(listener);
        Collections.sort(list, listenerSorter);
        stateListeners = list.toArray(new RootStateListener[list.size()]);
    }

    public void removeRootStateListener(RootStateListener listener) {
        List<RootStateListener> list = new ArrayList<RootStateListener>(Arrays.asList(stateListeners));
        list.remove(listener);
        stateListeners = list.toArray(new RootStateListener[list.size()]);
    }

    protected void fireRootStateListeners(Root.State state) {
        cachedState = state;
        RootStateListener[] listeners = stateListeners; // cache in case of changes
        for (RootStateListener listener : listeners) {
            listener.rootStateChanged(this, state);
        }
    }

    public void addControlFrameListener(ControlFrameListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        List<ControlFrameListener> list = new ArrayList<ControlFrameListener>(Arrays.asList(frameListeners));
        list.add(listener);
        Collections.sort(list, listenerSorter);
        frameListeners = list.toArray(new ControlFrameListener[list.size()]);
    }

    public void removeControlFrameListener(ControlFrameListener listener) {
        List<ControlFrameListener> list = new ArrayList<ControlFrameListener>(Arrays.asList(frameListeners));
        list.remove(listener);
        frameListeners = list.toArray(new ControlFrameListener[list.size()]);
    }

    protected void fireControlFrameListeners() {
        ControlFrameListener[] listeners = frameListeners; // cache
        for (ControlFrameListener listener : listeners) {
            listener.nextControlFrame(this);
        }
    }

    public long getTime() {
        return time;
    }

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

    public State getState() {
        return state.get();
    }

    protected final void setRunning() throws IllegalRootStateException {
        if (state.compareAndSet(Root.State.ACTIVE_IDLE, Root.State.ACTIVE_RUNNING)) {
//            fireRootStateListeners();
            starting();
            return;
        }
        throw new IllegalRootStateException();
    }

    protected final void setIdle() throws IllegalRootStateException {
        if (state.compareAndSet(Root.State.ACTIVE_RUNNING, Root.State.ACTIVE_IDLE)) {
//            fireRootStateListeners();
            stopping();
            return;
        }
        throw new IllegalRootStateException();
    }

    protected void run() {
//        while (activeStates.contains(state.get())) {
        while (true) {
            Root.State currentState = state.get();
            if (currentState != Root.State.ACTIVE_IDLE && currentState != Root.State.ACTIVE_RUNNING) {
                break;
            }
            Packet packet = null;
            try {
                packet = blockingQueue.poll(DEFAULT_FRAME_TIME, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                continue; // check state again if interrupted
            }
            setTime(System.nanoTime());
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
        Root.State currentState = state.get();
        if (currentState == Root.State.ACTIVE_RUNNING || currentState == Root.State.ACTIVE_IDLE) {
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

    private void processControlFrame(Packet packet, Root.State currentState) {
        long t = getTime();
        if (currentState != cachedState) {
            cachedState = currentState;
            fireRootStateListeners(cachedState);
        }
        processingControlFrame();
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
        taskController.checkTasks(t);
        // fire listeners after all calls have been made
        if (currentState == Root.State.ACTIVE_RUNNING) {
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
                    route(Call.createErrorCall(call, PString.valueOf("Unknown control address")));
                }
            }
        } catch (Exception ex) {
            Call.Type type = call.getType();
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

    protected Port getPort(PortAddress address) {
        Component comp = getComponent(address.getComponentAddress());
        if (comp != null) {
            return comp.getPort(address.getID());
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

    @Override
    public Root getRoot() {
        return this;
    }

    public ServiceManager getServiceManager() {
        return hub.getServiceManager();
    }
    
    public Lookup getLookup() {
        return hub.getLookup();
    }

    @Override
    public ComponentAddress getAddress() {
        return address;
    }

    @Override
    public void parentNotify(Container parent) throws ParentVetoException {
        // should always throw exception, but keep in line with API and only throw
        // if parent isn't null
        if (parent != null) {
            throw new ParentVetoException();
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

    protected void connectPorts(PortAddress port1, PortAddress port2) throws Exception {
        Port pt1 = getPort(port1);
        Port pt2 = getPort(port2);
        if (pt1 == null) {
            throw new NullPointerException("Can't find port " + port1);
        }
        if (pt2 == null) {
            throw new NullPointerException("Can't find port " + port2);
        }
        pt1.connect(pt2);
    }

    protected void disconnectPorts(PortAddress port1, PortAddress port2) throws Exception {
        Port pt1 = getPort(port1);
        Port pt2 = getPort(port2);
        if (pt1 == null) {
            throw new NullPointerException("Can't find port 1");
        }
        if (pt2 == null) {
            throw new NullPointerException("Can't find port 2");
        }
        pt1.disconnect(pt2);

    }

    public class Controller implements Root.Controller {

        private Controller() {
        }

        public boolean submitPacket(Packet packet) {
            return blockingQueue.offer(packet);
        }

        public void shutdown() {
            Root.State s = state.get();
            while (true) {
                if (s == Root.State.TERMINATED) {
                    return;
                } else {
                    if (state.compareAndSet(s, Root.State.TERMINATING)) {
                        // System.out.println("State set to terminated");
                        return;
                    } else {
                        s = state.get();
                    }
                }
            }
        }

        public void run() throws IllegalRootStateException {
            if (state.compareAndSet(Root.State.INITIALIZED, defaultRunState)) {
                fireRootStateListeners(defaultRunState);
                activating(); // moved velow listeners so that subclasses may change state
                AbstractRoot.this.run();
                state.set(Root.State.TERMINATING); // in case run finished before shutdown called
                terminating();
                fireRootStateListeners(Root.State.TERMINATING);
                // disconnect all children?
                state.set(Root.State.TERMINATED);
            } else {
                throw new IllegalRootStateException();
            }

        }
    }

    private class AddControl extends BasicControl {

        private ControlInfo info;

        private AddControl() {
            super(AbstractRoot.this);
            ArgumentInfo arg0 = ArgumentInfo.create(ComponentAddress.class, null);
            ArgumentInfo arg1 = ArgumentInfo.create(PReference.class, null);
            ArgumentInfo[] in = new ArgumentInfo[]{arg0, arg1};
            ArgumentInfo[] out = new ArgumentInfo[0];
            info = ControlInfo.create(in, out, null);
        }

        @Override
        public Call processInvoke(Call call, boolean quiet) throws Exception {
            CallArguments args = call.getArgs();
            if (args.getCount() != 2) {
                return createError(call, "Two arguments required");
//                router.routeCall(createError(call, "Two arguments required"));
//                return;
            }
            ComponentAddress address = null;
            Component comp = null;
            Argument arg = args.getArg(0);
            if (arg instanceof ComponentAddress) {
                address = (ComponentAddress) arg;
            } else {
                try {
                    address = ComponentAddress.valueOf(arg.toString());
                } catch (ArgumentFormatException ex) {
//                    return createError(call, "First argument must be a valid component address");
//                    router.routeCall(createError(call, "First argument must be a valid component address"));
//                    return;
                }
            }
            arg = args.getArg(1);
            if (arg instanceof PReference) {
                Object ref = ((PReference) arg).getReference();
                if (ref instanceof Component) {
                    comp = (Component) ref;
                }
            }
            if (comp == null) {
                return createError(call, "Second argument must be a valid component reference");
//                router.routeCall(createError(call, "Second argument must be a valid component reference"));
            // return;
            }
            addComponent(address, comp);
            if (quiet) {
                return null;
//                return;
            }
            return Call.createReturnCall(call, CallArguments.EMPTY);
//            router.routeCall(Call.createReturnCall(call, CallArguments.EMPTY));
        }

        public ControlInfo getInfo() {
            return info;
        }
    }

    private class RemoveControl extends BasicControl {

        private ControlInfo info;

        private RemoveControl() {
            super(AbstractRoot.this);
            ArgumentInfo arg0 = ArgumentInfo.create(ComponentAddress.class, null);
            ArgumentInfo[] in = new ArgumentInfo[]{arg0};
            ArgumentInfo[] out = new ArgumentInfo[0];
            info = ControlInfo.create(in, out, null);
        }

        @Override
        public Call processInvoke(Call call, boolean quiet) throws Exception {
            CallArguments args = call.getArgs();
            if (args.getCount() != 1) {
                return createError(call, "One argument required");
            }
            Argument arg = args.getArg(0);
            ComponentAddress address;
            if (arg instanceof ComponentAddress) {
                address = (ComponentAddress) arg;
            } else {
                try {
                    address = ComponentAddress.valueOf(arg.toString());
                } catch (ArgumentFormatException ex) {
                    return createError(call, "Argument must be a valid component address");
                }
            }
            removeComponent(address);
            if (quiet) {
                return null;
            }
            return Call.createReturnCall(call, CallArguments.EMPTY);
        }

        public ControlInfo getInfo() {
            return info;
        }
    }

    private class PortControl extends BasicControl {

        private ControlInfo info;
        private boolean connect;

        private PortControl(boolean connect) {
            super(AbstractRoot.this);
            ArgumentInfo arg = ArgumentInfo.create(PortAddress.class, null);
            ArgumentInfo[] in = new ArgumentInfo[]{arg, arg};
            ArgumentInfo[] out = new ArgumentInfo[0];
            info = ControlInfo.create(in, out, null);
            this.connect = connect;
        }

        @Override
        public Call processInvoke(Call call, boolean quiet) throws Exception {
            CallArguments args = call.getArgs();
            if (args.getCount() != 2) {
                return createError(call, "Two arguments required");
            }
//            PortAddress port1, port2;
//            Argument arg = args.getArg(0);
//            if (arg instanceof PortAddress) {
//                port1 = (PortAddress) arg;
//            } else {
//                try {
//                    port1 = PortAddress.wrap(String.valueOf(arg));
//                } catch (ArgumentFormatException ex) {
//                    return createError(call, "First argument must be a valid port address");
//                }
//            }
//            arg = args.getArg(1);
//            if (arg instanceof PortAddress) {
//                port2 = (PortAddress) arg;
//            } else {
//                try {
//                    port2 = PortAddress.wrap(String.valueOf(arg));
//                } catch (ArgumentFormatException ex) {
//                    return createError(call, "Second argument must be a valid port address");
//                }
//            }
            PortAddress port1 = PortAddress.coerce(args.getArg(0));
            PortAddress port2 = PortAddress.coerce(args.getArg(1));
            if (connect) {
                connectPorts(port1, port2);
            } else {
                disconnectPorts(port1, port2);
            }
            if (quiet) {
                return null;
            }
            return Call.createReturnCall(call, CallArguments.EMPTY);
        }

        public ControlInfo getInfo() {
            return info;
        }
    }

    private class TransportControl extends BasicControl {

        private ControlInfo info;
        private boolean start;

        private TransportControl(boolean start) {
            super(AbstractRoot.this);
            ArgumentInfo[] emptyArgs = new ArgumentInfo[0];
            info = ControlInfo.create(emptyArgs, emptyArgs, null);
            this.start = start;
        }

        @Override
        public Call processInvoke(Call call, boolean quiet) throws Exception {
            if (start) {
                setRunning();
                if (!quiet) {
                    route(Call.createReturnCall(call, CallArguments.EMPTY));
                }
//                runDelegate();
            } else {
                setIdle();
                if (!quiet) {
                    route(Call.createReturnCall(call, CallArguments.EMPTY));
                }
//                terminateDelegate();
            }
            return null;
        }

        public ControlInfo getInfo() {
            return info;
        }
    }

    private class InfoControl extends BasicControl {

        private ControlInfo info;

        private InfoControl() {
            super(AbstractRoot.this);
            ArgumentInfo[] in = new ArgumentInfo[]{
                ArgumentInfo.create(ComponentAddress.class,
                        ArgumentInfo.Presence.Optional, null)
            };
            ArgumentInfo[] out = new ArgumentInfo[]{
                ArgumentInfo.create(ComponentInfo.class, null)
            };
            info = ControlInfo.create(in, out, null);
        }

        @Override
        public Call processInvoke(Call call, boolean quiet) throws Exception {
            CallArguments args = call.getArgs();
            if (args.getCount() == 0) {
                return Call.createReturnCall(call, AbstractRoot.this.getInfo());
            } else if (args.getCount() == 1) {
                Argument arg = args.getArg(0);
                ComponentAddress ad;
                ad = ComponentAddress.coerce(arg);
                Component comp = AbstractRoot.this.getComponent(ad);
                if (comp == null) {
                    return createError(call, "Unknown component");
                } else {
                    return Call.createReturnCall(call, comp.getInfo());
                }

            } else {
                return createError(call, "Invalid number of arguments");
            }
        }

        public ControlInfo getInfo() {
            return info;
        }
    }


    private static class ListenerSorter implements Comparator<OrderedListener> {

        public int compare(OrderedListener o1, OrderedListener o2) {
            int val = o2.getDepth() - o1.getDepth();
            return val == 0 ? o2.getPriority() - o1.getPriority() : val;

        }
    }
}
