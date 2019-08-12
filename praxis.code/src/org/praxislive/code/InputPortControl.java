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
package org.praxislive.code;

import java.util.List;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.Call;
import org.praxislive.core.Control;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Value;
import org.praxislive.core.types.PError;
import org.praxislive.core.types.PMap;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
class InputPortControl implements Control {
    
    private final ControlInput.Link link;
    
    private InputPortControl(ControlInput.Link link) {
        this.link = link;
    }
    
    @Override
    public void call(Call call, PacketRouter router) throws Exception {
        List<Value> args = call.args();
        if (args.size() != 1) {
            router.route(call.error(PError.of(IllegalArgumentException.class,
                    "Input port requires single argument")));
        }
        link.receive(call.time(), args.get(0));
        router.route(call.reply());
    }
    
    static class Descriptor extends ControlDescriptor {
        
        private final InputPortControl control;
        private final ControlInfo info;
        
        private Descriptor(String id, Category category, int index, ControlInput.Link link) {
            super(id, category, index);
            control = new InputPortControl(link);
            info = ControlInfo.createFunctionInfo(
                    new ArgumentInfo[]{
                        ArgumentInfo.of(Value.class)
                    },
                    new ArgumentInfo[0],
                    PMap.of("input-port", id)
            );
        }
        
        @Override
        public ControlInfo getInfo() {
            return info;
        }
        
        @Override
        public void attach(CodeContext<?> context, Control previous) {
        }
        
        @Override
        public Control getControl() {
            return control;
        }
        
        static Descriptor createInput(String id, int index, ControlInput.Link link) {
            return new Descriptor(id, Category.In, index, link);
        }
        
        static Descriptor createAuxInput(String id, int index, ControlInput.Link link) {
            return new Descriptor(id, Category.AuxIn, index, link);
        }
        
    }
    
}
