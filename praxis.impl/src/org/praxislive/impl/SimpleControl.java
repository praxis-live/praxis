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
package org.praxislive.impl;

import java.util.logging.Logger;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.Control;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.ControlInfo;

/**
 *
 * @author Neil C Smith
 */
public abstract class SimpleControl implements AbstractComponent.ControlEx {

    private final static Logger LOG = Logger.getLogger(SimpleControl.class.getName());
    
    private ControlInfo info;

    protected SimpleControl(ControlInfo info) {
        this.info = info;
    }

    public final void call(Call call, PacketRouter router) throws Exception {
        CallArguments out = null;
        switch (call.getType()) {
            case INVOKE:
                out = process(call.time(), call.getArgs(), false);
                if (out == null) {
                    throw new Exception("No response returned from INVOKE\n" + call);
                }
                break;
            case INVOKE_QUIET:
                out = process(call.time(), call.getArgs(), true);
                break;
            default:
                LOG.warning("Unexpected call - \n" + call);
        }
        if (out != null) {
            router.route(Call.createReturnCall(call, out));
        }
    }

    @Override
    public void addNotify(AbstractComponent component) {
    }

    @Override
    public void removeNotify(AbstractComponent component) {
    }

    @Override
    public void hierarchyChanged() {
    }

    public final ControlInfo getInfo() {
        return info;
    }

    protected abstract CallArguments process(long time, CallArguments args, boolean quiet) throws Exception;
}
