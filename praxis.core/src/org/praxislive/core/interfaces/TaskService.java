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
package org.praxislive.core.interfaces;

import org.praxislive.core.Value;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PReference;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class TaskService extends Service {

    @Deprecated public final static TaskService INSTANCE = new TaskService();
    public final static String SUBMIT = "submit";
    public final static ControlInfo SUBMIT_INFO =
            ControlInfo.createFunctionInfo(
            new ArgumentInfo[]{PReference.info(Task.class)},
            new ArgumentInfo[]{Value.info()},
            PMap.EMPTY);

    @Override
    public String[] getControls() {
        return new String[]{SUBMIT};
    }

    @Override
    public ControlInfo getControlInfo(String control) {
        if (SUBMIT.equals(control)) {
            return SUBMIT_INFO;
        }
        throw new IllegalArgumentException();
    }

    public static interface Task {

        /**
         * Called to execute task.
         * @return Value (use PReference to wrap arbitrary Objects)
         * @throws java.lang.Exception
         */
        public Value execute() throws Exception;
    }
}
