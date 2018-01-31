/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2013 Neil C Smith.
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

package org.praxislive.impl;

import org.praxislive.core.Argument;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.Control;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Port;
import org.praxislive.core.info.ControlInfo;

/**
 *
 * @author Neil C Smith
 */
public class TriggerControl implements Control {
    
    private ControlInfo info;
    private Binding binding;
    
    private TriggerControl(Binding binding) {
        this.info = ControlInfo.createActionInfo(null);
        this.binding = binding;
    }

    public ControlInfo getInfo() {
        return info;
    }
    
    public Port createPort() {
        return new InputPort();
    }

    @Override
    public void call(Call call, PacketRouter router) throws Exception {
        switch (call.getType()) {
            case INVOKE :
                binding.trigger(call.getTimecode());
                router.route(Call.createReturnCall(call, CallArguments.EMPTY));
                break;
            case INVOKE_QUIET :
                binding.trigger(call.getTimecode());
                break;
            default :
                throw new IllegalArgumentException();
        }
    }
    
    
    private class InputPort extends AbstractControlInputPort {
        
        private InputPort() {
        }

        @Override
        public void receive(long time, double value) {
            binding.trigger(time);
        }

        @Override
        public void receive(long time, Argument value) {
            binding.trigger(time);
        }
        
    }

    public static TriggerControl create( Binding binding) {
        if (binding == null) {
            throw new NullPointerException();
        }
        return new TriggerControl(binding);
    }
    
    public static interface Binding {
        
        public void trigger(long time);
        
    }
    
}
