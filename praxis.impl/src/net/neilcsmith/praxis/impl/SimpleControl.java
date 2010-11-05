/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 - Neil C Smith. All rights reserved.
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

import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.PacketRouter;

/**
 *
 * @author Neil C Smith
 */
public abstract class SimpleControl implements Control {

    private final static Logger LOG = Logger.getLogger(SimpleControl.class.getName());

    public void call(Call call, PacketRouter router) throws Exception {
        CallArguments out = null;
        switch (call.getType()) {
            case INVOKE:
                out = process(call.getArgs(), false);
                if (out == null) {
                    throw new Exception("No response returned from INVOKE\n" + call);
                }
                break;
            case INVOKE_QUIET:
                out = process(call.getArgs(), true);
                break;
            default:
                LOG.warning("Unexpected call - \n" + call);
        }
        if (out != null) {
            router.route(Call.createReturnCall(call, out));
        }
    }

    protected abstract CallArguments process(CallArguments args, boolean quiet) throws Exception;
}
