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
 *
 */

package org.praxislive.logging;

import org.praxislive.core.Argument;
import org.praxislive.core.info.ArgumentInfo;
import org.praxislive.core.info.ControlInfo;
import org.praxislive.core.interfaces.Service;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PString;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class LogService extends Service {
    
    public final static String LOG = "log";
    public final static ControlInfo LOG_INFO =
            ControlInfo.createFunctionInfo(
            new ArgumentInfo[]{
                ArgumentInfo.create(PString.class, ArgumentInfo.Presence.Variable,
                        PMap.create(PString.KEY_ALLOWED_VALUES, PArray.valueOf(
                                LogLevel.ERROR.asPString(),
                                LogLevel.WARNING.asPString(),
                                LogLevel.INFO.asPString(),
                                LogLevel.DEBUG.asPString()
                        ))),
                ArgumentInfo.create(Argument.class, ArgumentInfo.Presence.Variable, PMap.EMPTY)
            },
            new ArgumentInfo[0],
            PMap.EMPTY);

    @Override
    public String[] getControls() {
        return new String[]{LOG};
    }

    @Override
    public ControlInfo getControlInfo(String control) {
        if (LOG.equals(control)) {
            return LOG_INFO;
        }
        throw new IllegalArgumentException();
    }
    
}
