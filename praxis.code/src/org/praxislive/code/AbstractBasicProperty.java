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
package org.praxislive.code;

import java.util.List;
import org.praxislive.core.Value;
import org.praxislive.core.Call;
import org.praxislive.core.Control;
import org.praxislive.core.PacketRouter;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public abstract class AbstractBasicProperty implements Control {

    private boolean latestSet;
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

    protected abstract void set(long time, Value arg) throws Exception;

    protected abstract Value get();

    private void setLatest(long time) {
        latestSet = true;
        latest = time;
    }

    private boolean isLatest(long time) {
        if (latestSet) {
            return (time - latest) >= 0;
        } else {
            return true;
        }

    }

}
