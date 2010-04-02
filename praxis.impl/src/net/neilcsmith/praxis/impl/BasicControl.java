/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.praxis.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith
 */
public abstract class BasicControl extends AbstractControl {

    private static Logger logger = Logger.getLogger(BasicControl.class.getName());
    private long latest;

//    private AbstractComponent component;
    protected BasicControl(Component component) {
        super(component);
        latest = System.nanoTime();
    }

    //@TODO implements standard error message system.
    protected Call createError(Call input, String msg) {
        return Call.createErrorCall(input, PString.valueOf(msg));
    }

    public void call(Call call, PacketRouter router) throws Exception {
        Call out = null;
        switch (call.getType()) {
            case INVOKE:
                processInvoke(call, router, false);
                break;
            case INVOKE_QUIET:
                processInvoke(call, router, true);
                break;
            case ERROR:
                processError(call, router);
                break;
            case RETURN:
                processReturn(call, router);
                break;
            default:
                throw new Exception();
        }
    }

    protected void processInvoke(Call call, PacketRouter router, boolean quiet) throws Exception {
        call = processInvoke(call, quiet);
        if (call != null) {
            router.route(call);
        }
    }

    protected Call processInvoke(Call call, boolean quiet) throws Exception {
        return createError(call, "Unsupported Operation");
    }

    protected void processError(Call call, PacketRouter router) throws Exception {
        processError(call);
    }

    protected void processError(Call call) throws Exception {
        if (logger.isLoggable(Level.WARNING)) {
            logger.warning(String.valueOf(getAddress()) +
                    "\n" + call);
        }
    }

    protected void processReturn(Call call, PacketRouter router) throws Exception {
        processReturn(call);
    }

    protected void processReturn(Call call) throws Exception {
        if (logger.isLoggable(Level.INFO)) {
            logger.warning(String.valueOf(getAddress()) +
                    "\n" + call);
        }
    }

    protected void setLatest(long time) {
        latest = time;
    }

    protected boolean isLatest(long time) {
        return (time - latest) > 0;
    }
}
