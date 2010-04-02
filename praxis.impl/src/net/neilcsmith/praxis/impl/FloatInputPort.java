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
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.types.PNumber;

/**
 *
 * @author Neil C Smith
 */
public class FloatInputPort extends AbstractControlInputPort {

    
    private Binding binding;
    
    private FloatInputPort(Component host, Binding binding) {
        super(host);
        this.binding = binding;
    }

//    @Override
//    public void receive(int value) {
//        binding.receive((float) value);
//    }

    @Override
    public void receive(long time, Argument value) {
        try {
            binding.receive(time, PNumber.coerce(value).value());
        } catch (Exception ex) {
            Logger.getLogger(FloatInputPort.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    @Override
    public void receive(long time, double value) {
        try {
            binding.receive(time, value);
        } catch(Exception ex) {
            Logger.getLogger(FloatInputPort.class.getName()).log(Level.WARNING, null, ex);
        }
        
    }

    public static FloatInputPort create(Component host, Binding binding) {
        if (host == null || binding == null) {
            throw new NullPointerException();
        }
        return new FloatInputPort(host, binding);
    }
    
    public static interface Binding {
        public void receive(long time, double value);
    }
    
}
