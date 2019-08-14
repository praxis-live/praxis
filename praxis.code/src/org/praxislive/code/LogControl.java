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

import org.praxislive.core.Call;
import org.praxislive.core.Control;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.types.PError;
import org.praxislive.logging.LogLevel;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class LogControl implements Control {
    
    static final String ID = "_log";

    private CodeContext<?> context;

    @Override
    public void call(Call call, PacketRouter router) throws Exception {
        if (call.isError() && !call.args().isEmpty()) {
            PError err = PError.from(call.args().get(0))
                    .orElse(PError.of(call.args().get(0).toString()));
            context.getLog().log(LogLevel.ERROR, err);
        } else if (call.isReplyRequired()) {
            router.route(call.error(PError.of("Unexpected call")));
        }
    }

    public static class Descriptor extends ControlDescriptor {

        private final LogControl control;

        public Descriptor(int index) {
            super(ID, Category.Internal, index);
            control = new LogControl();
        }

        @Override
        public ControlInfo getInfo() {
            return null;
        }

        @Override
        public void attach(CodeContext<?> context, Control previous) {
            control.context = context;
        }

        @Override
        public Control getControl() {
            return control;
        }

    }

}
