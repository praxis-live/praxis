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

import org.praxislive.core.ExecutionContext;
import org.praxislive.util.ArrayUtils;

/**
 * Default implementation of {@link ExecutionContext} for use with
 * {@link AbstractRoot}
 */
public class DefaultExecutionContext implements ExecutionContext {

    private ExecutionContext.StateListener[] stateListeners;
    private ExecutionContext.ClockListener[] clockListeners;
    private ExecutionContext.State state;
    long time;
    private long startTime;

    /**
     * Create a DefaultExecutionContext
     *
     * @param time initial clock time / start time
     */
    public DefaultExecutionContext(long time) {
        this.stateListeners = new ExecutionContext.StateListener[0];
        this.clockListeners = new ExecutionContext.ClockListener[0];
        this.state = ExecutionContext.State.NEW;
        this.time = this.startTime = time;
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

    /**
     * Update the state. Will call all state listeners.
     *
     * @param time time of state change (if ACTIVE also the new start time)
     * @param state new state
     * @throws IllegalStateException if attempting to reset to NEW from another
     * state, or attempting to set to another state if current state is
     * TERMINATED
     */
    @SuppressWarnings(value = "fallthrough")
    protected void updateState(long time, ExecutionContext.State state) {
        // make sure time is set before state listeners called.
        this.time = time;
        switch (state) {
            case ACTIVE:
                if (this.state != ExecutionContext.State.ACTIVE) {
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

    /**
     * Update the clock time. Will call all clock listeners.
     *
     * @param time
     */
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
