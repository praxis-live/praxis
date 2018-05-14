/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
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

package org.praxislive.core;

import java.util.OptionalLong;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public interface ExecutionContext {

    public static enum State {NEW, ACTIVE, IDLE, TERMINATED};

    public void addStateListener(StateListener listener);

    public void removeStateListener(StateListener listener);

    public void addClockListener(ClockListener listener);

    public void removeClockListener(ClockListener listener);

    public long getTime();
    
    public long getStartTime();
    
    public State getState();

    public default OptionalLong getPeriod() {
        return OptionalLong.empty();
    }

    public static interface StateListener {

        public void stateChanged(ExecutionContext source);

    }

    public static interface ClockListener {

        public void tick(ExecutionContext source);

    }

}
