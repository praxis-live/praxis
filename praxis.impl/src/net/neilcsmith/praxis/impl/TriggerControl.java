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

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;

/**
 *
 * @author Neil C Smith
 * @TODO implement latest check
 */
public class TriggerControl extends BasicControl {
    
    private ControlInfo info;
    private Binding binding;
    private InputPort port;
    
    private TriggerControl(Component host, Binding binding) {
        super(host);
        ArgumentInfo[] empty = new ArgumentInfo[0];
        this.info = ControlInfo.create(empty, empty, null);
        this.binding = binding;
        this.port = new InputPort(host);
    }

    public ControlInfo getInfo() {
        return info;
    }
    
    public Port getPort() {
        return port;
    }

    @Override
    protected Call processInvoke(Call call, boolean quiet) throws Exception {
        binding.trigger(call.getTimecode());
        if (!quiet) {
            return Call.createReturnCall(call, CallArguments.EMPTY);
        } else {
            return null;
        }
    }
    
    
    
    private class InputPort extends AbstractControlInputPort {
        
        private InputPort(Component host) {
            super(host);
        }

        @Override
        public void receive(long time, double value) {
            binding.trigger(time);
        }

//        @Override
//        public void receive(int value) {
//            binding.trigger();
//        }

        @Override
        public void receive(long time, Argument value) {
            binding.trigger(time);
        }
        
    }

    public static TriggerControl create(Component host, Binding binding) {
        if (host == null || binding == null) {
            throw new NullPointerException();
        }
        return new TriggerControl(host, binding);
    }
    
    public static interface Binding {
        
        public void trigger(long time);
        
    }
    
}
