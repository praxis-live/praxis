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
package org.praxislive.base;

import java.util.List;
import org.praxislive.core.Call;
import org.praxislive.core.Control;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Value;

/**
 * A basic property control base class.
 */
public abstract class AbstractProperty implements Control {

    private long latest;

    @Override
    public void call(Call call, PacketRouter router) throws Exception {
        if (call.isRequest()) {
            List<Value> args = call.args();
            int argCount = args.size();
            long time = call.time();
            if (argCount > 0) {
                if (isLatest(time)) {
                    set(time, args.get(0));
                    setLatest(time);
                }
                if (call.isReplyRequired()) {
                    router.route(call.reply(args));
                }
            } else {
                // ignore quiet hint?
                router.route(call.reply(get()));
            }
        } else {
//            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the value.
     * 
     * @param time clock time of set call
     * @param arg new property value
     * @throws Exception if value setting fails
     */
    protected abstract void set(long time, Value arg) throws Exception;

    /**
     * Get the value.
     * 
     * @return value
     */
    protected abstract Value get();

    private void setLatest(long time) {
        latest = time == 0 ? -1 : time;
    }

    private boolean isLatest(long time) {
        return latest == 0 || (time - latest) >= 0;
    }

}
