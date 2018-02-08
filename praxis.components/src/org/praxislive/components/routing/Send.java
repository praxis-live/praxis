/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Neil C Smith.
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
package org.praxislive.components.routing;

import org.praxislive.core.Argument;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.Control;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Port;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.interfaces.ServiceUnavailableException;
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
                ArgumentInfo.create(ControlAddress.class,
                        PMap.create(ArgumentInfo.KEY_ALLOW_EMPTY, true)),
                new AddressBinding(),
                PString.EMPTY));
        registerPort(Port.IN, ArgumentInputPort.create(new InputBinding()));
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
        public void receive(long time, Argument arg) {
            if (destination != null) {
                PacketRouter router = getPacketRouter();
                if (router != null) {
                    Call call = Call.createQuietCall(destination, ControlAddress.create(getAddress(), "_log"), time, arg);
                    router.route(call);
                }
            }
        }

    }

    private class AddressBinding implements ArgumentProperty.Binding {

        @Override
        public void setBoundValue(long time, Argument value) throws Exception {
            if (value.isEmpty()) {
                destination = null;
            } else {
                destination = ControlAddress.coerce(value);
            }
        }

        @Override
        public Argument getBoundValue() {
            return destination == null ? PString.EMPTY : destination;
        }

    }

    private class LogControl implements Control {

        private final LogBuilder logBuilder = new LogBuilder(LogLevel.ERROR);
        private ControlAddress logService;
        private long lastSend = System.nanoTime();

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
                    Argument errArg = arrArgs.get(0);
                    if (errArg instanceof PError) {
                        logBuilder.log(LogLevel.WARNING, (PError) errArg);
                    } else {
                        logBuilder.log(LogLevel.WARNING, errArg.toString());
                    }
                } else {
                    logBuilder.log(LogLevel.WARNING, "Error returned from " + call.getFromAddress());
                }

                long callTime = call.getTimecode();
                if ((callTime - lastSend) > 500_000_000) {
                    router.route(
                            Call.createQuietCall(logService,
                                    ControlAddress.create(getAddress(), "_log"),
                                    call.getTimecode(),
                                    logBuilder.toCallArguments()));

                    logBuilder.clear();
                    lastSend = callTime;
                }

            }
        }

        private void initLog() throws ServiceUnavailableException {
            LogLevel level = getLookup().get(LogLevel.class);
            if (level != null) {
                logBuilder.setLevel(level);
            }
            logService = ControlAddress.create(findService(LogService.class),
                    LogService.LOG);
        }

        public ControlInfo getInfo() {
            return null;
        }

    }

}
