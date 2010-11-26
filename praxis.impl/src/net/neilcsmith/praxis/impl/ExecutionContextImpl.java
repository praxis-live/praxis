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
import java.util.Arrays;
import java.util.List;
import net.neilcsmith.praxis.core.ExecutionContext;

public class ExecutionContextImpl extends ExecutionContext {

    private StateListener[] stateListeners;
    private ClockListener[] clockListeners;
    private State state;
    private long time;

    public ExecutionContextImpl(long time) {
        this.stateListeners = new StateListener[0];
        this.clockListeners = new ClockListener[0];
        this.state = State.NEW;
        this.time = time;
    }

    @Override
    public void addStateListener(StateListener listener) {
        stateListeners = ListenerUtils.add(stateListeners, listener);

//        if (listener == null) {
//            throw new NullPointerException();
//        }
//        List<StateListener> list = Arrays.asList(stateListeners);
//        if (list.contains(listener)) {
//            return;
//        }
//        list = new ArrayList<StateListener>(list);
//        list.add(listener);
//        stateListeners = list.toArray(new StateListener[list.size()]);
    }

    @Override
    public void removeStateListener(StateListener listener) {
        stateListeners = ListenerUtils.add(stateListeners, listener);
//        List<StateListener> list = new ArrayList(Arrays.asList(stateListeners));
//        boolean changed = list.remove(listener);
//        if (changed) {
//            stateListeners = list.toArray(new StateListener[list.size()]);
//        }
    }

    @Override
    public void addClockListener(ClockListener listener) {
        clockListeners = ListenerUtils.add(clockListeners, listener);
//        if (listener == null) {
//            throw new NullPointerException();
//        }
//        List<ClockListener> list = Arrays.asList(clockListeners);
//        if (list.contains(listener)) {
//            return;
//        }
//        list = new ArrayList<ClockListener>(list);
//        list.add(listener);
//        clockListeners = list.toArray(new ClockListener[list.size()]);
    }

    @Override
    public void removeClockListener(ClockListener listener) {
        clockListeners = ListenerUtils.remove(clockListeners, listener);
//        List<ClockListener> list = new ArrayList(Arrays.asList(clockListeners));
//        boolean changed = list.remove(listener);
//        if (changed) {
//            clockListeners = list.toArray(new ClockListener[list.size()]);
//        }
    }

    public void setState(State state) {
        switch (state) {
            case ACTIVE:
            case IDLE:
                if (this.state == State.TERMINATED) {
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
                if (this.state != State.NEW) {
                    throw new IllegalStateException("Can't make Execution Context NEW again.");
                }
                break;
            default:
                throw new RuntimeException();

        }
    }

    @Override
    public State getState() {
        return state;
    }
    
    public void setTime(long time) {
        this.time = time;
        if (state == State.ACTIVE) {
            fireClockListeners();
        }
    }

    @Override
    public long getTime() {
        return time;
    }

    private void fireStateListeners() {
        for (StateListener l : stateListeners) {
            l.stateChanged(this);
        }
    }

    private void fireClockListeners() {
        for (ClockListener l : clockListeners) {
            l.tick(this);
        }
    }
}
