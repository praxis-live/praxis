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
package org.praxislive.terminal.impl;

import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.ExecutionContext;
import org.praxislive.core.services.ScriptService;
import org.praxislive.core.types.PString;
import org.praxislive.impl.AbstractControl;
import org.praxislive.terminal.Terminal;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
@Deprecated
public class DefaultTerminalContext extends AbstractControl implements Terminal.Context {

    private Call activeCall;
    private Terminal terminal;

    public DefaultTerminalContext(Terminal terminal) {
        if (terminal == null) {
            throw new NullPointerException();
        }
        this.terminal = terminal;
    }

    @Override
    public void eval(String script) throws Exception {
        ControlAddress to = ControlAddress.create(
                findService(ScriptService.class),
                ScriptService.EVAL);
        Call call = Call.createCall(to, getAddress(),
                getLookup().find(ExecutionContext.class).get().getTime(), PString.valueOf(script));
        route(call);
        activeCall = call;
    }

    @Override
    public void clear() throws Exception {
        ControlAddress to = ControlAddress.create(
                findService(ScriptService.class),
                ScriptService.CLEAR);
        Call call = Call.createQuietCall(to, getAddress(),
                getLookup().find(ExecutionContext.class).get().getTime(), CallArguments.EMPTY);
        route(call);
        activeCall = null;
    }

    @Override
    public void call(Call call, PacketRouter router) throws Exception {
        switch (call.getType()) {
            case RETURN:
                if (call.getMatchID() == activeCall.getMatchID()) {
                    terminal.processResponse(call.getArgs());
                    activeCall = null;
                }
                break;
            case ERROR:
                if (call.getMatchID() == activeCall.getMatchID()) {
                    terminal.processError(call.getArgs());
                    activeCall = null;
                }
                break;
            default:

        }
    }

    @Override
    public ControlInfo getInfo() {
        return null;
    }

    private void route(Call call) {
        PacketRouter router = getLookup().find(PacketRouter.class)
                .orElseThrow(IllegalStateException::new);
        router.route(call);
    }
}
