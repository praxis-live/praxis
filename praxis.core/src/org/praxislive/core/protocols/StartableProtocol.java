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
package org.praxislive.core.protocols;

import java.util.stream.Stream;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ComponentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.Info;
import org.praxislive.core.Protocol;
import org.praxislive.core.types.PBoolean;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class StartableProtocol implements Protocol {

    public final static StartableProtocol INSTANCE = new StartableProtocol();

    public final static String START = "start";
    public final static String STOP = "stop";
    public final static String IS_RUNNING = "is-running";
    public final static ControlInfo START_INFO = ControlInfo.createFunctionInfo(
                new ArgumentInfo[0],
                new ArgumentInfo[0],
                null);;
    public final static ControlInfo STOP_INFO = ControlInfo.createFunctionInfo(
                new ArgumentInfo[0],
                new ArgumentInfo[0],
                null);;
    public final static ControlInfo IS_RUNNING_INFO =
            ControlInfo.createReadOnlyPropertyInfo(
                new ArgumentInfo[]{PBoolean.info()},
                null);;

    public static final ComponentInfo API_INFO = Info.component(cmp -> cmp
            .protocol(StartableProtocol.class)
            .control(START, START_INFO)
            .control(STOP, STOP_INFO)
            .control(IS_RUNNING, IS_RUNNING_INFO)
    );
 

    @Override
    public Stream<String> controls() {
        return Stream.of(START, STOP, IS_RUNNING);
    }

    @Override
    public ControlInfo getControlInfo(String control) {
        if (START.equals(control)) {
            return START_INFO;
        }
        if (STOP.equals(control)) {
            return STOP_INFO;
        }
        if (IS_RUNNING.equals(control)) {
            return IS_RUNNING_INFO;
        }
        throw new IllegalArgumentException();
    }
}


