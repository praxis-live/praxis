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
package org.praxislive.impl.components;

import org.praxislive.core.Value;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.Control;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Port;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.ExecutionContext;
import org.praxislive.core.services.ServiceUnavailableException;
import org.praxislive.core.types.PError;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PString;
import org.praxislive.impl.AbstractComponent;
import org.praxislive.impl.ArgumentInputPort;
import org.praxislive.impl.ArgumentProperty;
import org.praxislive.impl.BooleanProperty;
import org.praxislive.logging.LogBuilder;
import org.praxislive.logging.LogLevel;
import org.praxislive.logging.LogService;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 *
 */
public class Send extends AbstractComponent {

    private final LogControl LOG;
    private final BooleanProperty logErrors;

    private ControlAddress destination;

    public Send() {
        registerControl("address", ArgumentProperty.create(
                ArgumentInfo.of(ControlAddress.class,
                        PMap.of(ArgumentInfo.KEY_ALLOW_EMPTY, true)),
                new AddressBinding(),
                PString.EMPTY));
        registerPort(PortEx.IN, ArgumentInputPort.create(new InputBinding()));
        logErrors = BooleanProperty.create(true);
        registerControl("log-errors", logErrors);
        LOG = new LogControl();
        registerControl("_log", LOG);
    }

    @Override
    public void hierarchyChanged() {
        LOG.logService = null;
    }

    private class InputBinding implements ArgumentInputPort.Binding {

        @Override
        public void receive(long time, Value arg) {
            if (destination != null) {
                PacketRouter router = getPacketRouter();
                if (router != null) {
                    Call call = Call.createQuiet(destination, ControlAddress.of(getAddress(), "_log"), time, arg);
                    router.route(call);
                }
            }
        }

    }

    private class AddressBinding implements ArgumentProperty.Binding {

        @Override
        public void setBoundValue(long time, Value value) throws Exception {
            if (value.isEmpty()) {
                destination = null;
            } else {
                destination = ControlAddress.coerce(value);
            }
        }

        @Override
        public Value getBoundValue() {
            return destination == null ? PString.EMPTY : destination;
        }

    }

    private class LogControl implements ControlEx {

        private final LogBuilder logBuilder = new LogBuilder(LogLevel.ERROR);
        private ControlAddress logService;
        private long lastSend;

        @Override
        public void hierarchyChanged() {
            lastSend = getLookup().find(ExecutionContext.class)
                    .map(ExecutionContext::getTime)
                    .orElse(0L);
        }
        
        @Override
        public void call(Call call, PacketRouter router) throws Exception {
            if (call.getType() == Call.Type.ERROR && logErrors.getValue()) {

                if (logService == null) {
                    initLog();
                }

                if (!logBuilder.isLoggable(LogLevel.WARNING)) {
                    return;
                }

                CallArguments arrArgs = call.getArgs();
                if (arrArgs.getSize() > 0) {
                    Value errArg = arrArgs.get(0);
                    if (errArg instanceof PError) {
                        logBuilder.log(LogLevel.WARNING, (PError) errArg);
                    } else {
                        logBuilder.log(LogLevel.WARNING, errArg.toString());
                    }
                } else {
                    logBuilder.log(LogLevel.WARNING, "Error returned from " + call.from());
                }

                long callTime = call.time();
                if ((callTime - lastSend) > 500_000_000) {
                    router.route(
                            Call.createQuietCall(logService,
                                    ControlAddress.of(getAddress(), "_log"),
                                    call.time(),
                                    logBuilder.toCallArguments()));

                    logBuilder.clear();
                    lastSend = callTime;
                }

            }
        }

        private void initLog() throws ServiceUnavailableException {
            LogLevel level = getLookup().find(LogLevel.class).orElse(LogLevel.ERROR);
            logBuilder.setLevel(level);
            logService = ControlAddress.of(findService(LogService.class),
                    LogService.LOG);
        }

        public ControlInfo getInfo() {
            return null;
        }

    }

}
