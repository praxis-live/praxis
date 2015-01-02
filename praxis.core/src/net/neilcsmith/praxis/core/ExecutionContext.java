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

package net.neilcsmith.praxis.core;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class ExecutionContext {

    public static enum State {NEW, ACTIVE, IDLE, TERMINATED};

    public abstract void addStateListener(StateListener listener);

    public abstract void removeStateListener(StateListener listener);

    public abstract void addClockListener(ClockListener listener);

    public abstract void removeClockListener(ClockListener listener);

    public abstract State getState();

    public abstract long getTime();

    public long getPeriod() {
        return -1;
    }

    public boolean supportsPeriod() {
        return getPeriod() > 0;
    }

    public long getStartTime() {
        throw new UnsupportedOperationException();
    }
    
    public boolean supportsStartTime() {
        return false;
    }
    
    public static interface StateListener {

        public void stateChanged(ExecutionContext source);

    }

    public static interface ClockListener {

        public void tick(ExecutionContext source);

    }

}
