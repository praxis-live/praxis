/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2013 Neil C Smith.
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

import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.util.ArrayUtils;

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
        stateListeners = ArrayUtils.add(stateListeners, listener);
    }

    @Override
    public void removeStateListener(StateListener listener) {
        stateListeners = ArrayUtils.add(stateListeners, listener);
    }

    @Override
    public void addClockListener(ClockListener listener) {
        clockListeners = ArrayUtils.add(clockListeners, listener);
    }

    @Override
    public void removeClockListener(ClockListener listener) {
        clockListeners = ArrayUtils.remove(clockListeners, listener);
    }

    @SuppressWarnings("fallthrough")
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
