/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Neil C Smith.
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
    
    private FloatInputPort(Binding binding) {
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

    public static FloatInputPort create( Binding binding) {
        if (binding == null) {
            throw new NullPointerException();
        }
        return new FloatInputPort(binding);
    }
    
    public static interface Binding {
        public void receive(long time, double value);
    }
    
}
