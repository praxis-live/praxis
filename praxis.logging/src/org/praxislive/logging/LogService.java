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
 *
 */

package org.praxislive.logging;

import java.util.stream.Stream;
import org.praxislive.core.Value;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.Protocol;
import org.praxislive.core.services.Service;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PString;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class LogService implements Service {
    
    public final static String LOG = "log";
    public final static ControlInfo LOG_INFO =
            ControlInfo.createFunctionInfo(
            new ArgumentInfo[]{
                ArgumentInfo.create(PString.class, ArgumentInfo.Presence.Variable,
                        PMap.of(PString.KEY_ALLOWED_VALUES, PArray.of(
                                LogLevel.ERROR.asPString(),
                                LogLevel.WARNING.asPString(),
                                LogLevel.INFO.asPString(),
                                LogLevel.DEBUG.asPString()
                        ))),
                ArgumentInfo.create(Value.class, ArgumentInfo.Presence.Variable, PMap.EMPTY)
            },
            new ArgumentInfo[0],
            PMap.EMPTY);

    @Override
    public Stream<String> controls() {
        return Stream.of(LOG);
    }

    @Override
    public ControlInfo getControlInfo(String control) {
        if (LOG.equals(control)) {
            return LOG_INFO;
        }
        throw new IllegalArgumentException();
    }
    
    public static class Provider implements Protocol.TypeProvider {

        @Override
        public Stream<Type> types() {
            return Stream.of(new Protocol.Type<>(LogService.class));
        }
        
    }
    
}
