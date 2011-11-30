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

import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith
 */
public class ArgumentProperty extends AbstractSingleArgProperty {

    private static Logger logger = Logger.getLogger(ArgumentProperty.class.getName());
    private ReadBinding reader;
    private Binding writer;


    private ArgumentProperty(ReadBinding reader, Binding writer, ControlInfo info) {
        super(info);
        this.reader = reader;
        this.writer = writer;
    }


    public Argument getValue() {
        return get();
    }

    @Override
    protected void set(long time, Argument value) throws Exception {
        if (writer == null) {
            throw new UnsupportedOperationException("Read Only Property");
        } else {
            writer.setBoundValue(time, value);
        }
    }

    @Override
    protected void set(long time, double value) throws Exception {
        set(time, PNumber.valueOf(value));
    }

    @Override
    protected Argument get() {
        return reader.getBoundValue();
    }

    
    
    
    public static ArgumentProperty create() {
        return create(Argument.info(), PString.EMPTY, null);
    }
    
    public static ArgumentProperty create(ArgumentInfo info) {
        return create(info, PString.EMPTY, null);
    }

    public static ArgumentProperty create( Binding binding, Argument def) {
        return create(Argument.info(), def, binding);
        
    }
    
    public static ArgumentProperty create(ArgumentInfo typeInfo, Argument def, Binding binding) {
        if (binding == null) {
            binding = new DefaultBinding(def);
        }
        ArgumentInfo[] arguments = new ArgumentInfo[]{typeInfo};
        Argument[] defaults = new Argument[]{def};
        ControlInfo info = ControlInfo.createPropertyInfo(arguments, defaults, null);
        return new ArgumentProperty(binding, binding, info);
    }

    public static ArgumentProperty createReadOnly(ArgumentInfo typeInfo, ReadBinding binding) {
        if (binding == null) {
            throw new NullPointerException();
        }
        ControlInfo info = ControlInfo.createReadOnlyPropertyInfo(new ArgumentInfo[]{typeInfo}, null);
        return new ArgumentProperty(binding, null, info);
    }
    
    
    public static interface ReadBinding {
        
        public Argument getBoundValue();
        
    }

    public static interface Binding extends ReadBinding {

        public void setBoundValue(long time, Argument value) throws Exception;
     
    }

    private static class DefaultBinding implements Binding {

        private Argument value;

        private DefaultBinding(Argument value) {
            this.value = value;
        }

        @Override
        public void setBoundValue(long time, Argument value) {
            this.value = value;
        }

        @Override
        public Argument getBoundValue() {
            return value;
        }
    }

}
