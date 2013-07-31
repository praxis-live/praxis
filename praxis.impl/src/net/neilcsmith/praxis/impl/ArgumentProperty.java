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
package net.neilcsmith.praxis.impl;

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PBoolean;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith
 */
public class ArgumentProperty extends AbstractSingleArgProperty {

    private final ReadBinding reader;
    private final Binding writer;


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
        return create(Argument.info(), null, PString.EMPTY);
    }
    
    public static ArgumentProperty create(ArgumentInfo info) {
        return create(info, null, PString.EMPTY);
    }

    public static ArgumentProperty create( Binding binding, Argument def) {
        return create(Argument.info(), binding, def);
        
    }
    
    public static ArgumentProperty create(ArgumentInfo typeInfo, Binding binding, Argument def) {
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
    
    public static Builder builder() {
        return new Builder();
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

    
    public static class Builder extends AbstractSingleArgProperty.Builder<Builder> {
        
        private Argument defaultValue;
        private Binding writeBinding;
        private ReadBinding readBinding;
        
        private Builder() {
            
        }

        public Builder type(Class<? extends Argument> typeClass) {
            return super.argumentType(typeClass);
        }
        
        public Builder defaultValue(Argument def) {
            defaults(def);
            defaultValue = def;
            return this;
        }
        
        public Builder binding(ReadBinding binding) {
            if (binding instanceof Binding) {
                return binding((Binding) binding);
            } else {
                if (binding != null) {
                    readOnly();
                }
                readBinding = binding;
                writeBinding = null;
                return this;
            }
        }
        
        public Builder binding(Binding binding) {
            readBinding = binding;
            writeBinding = binding;
            return this;
        }
        
        public Builder allowEmpty() {
            putArgumentProperty(ArgumentInfo.KEY_ALLOW_EMPTY, PBoolean.TRUE);
            return this;
        }
        
        public Builder emptyIsDefault() {
            putArgumentProperty(ArgumentInfo.KEY_EMPTY_IS_DEFAULT, PBoolean.TRUE);
            return this;
        }

        public Builder suggestedValues(Argument ... values) {
            putArgumentProperty(ArgumentInfo.KEY_SUGGESTED_VALUES, PArray.valueOf(values));
            return this;
        }
        
        public Builder template(Argument template) {
            putArgumentProperty(ArgumentInfo.KEY_TEMPLATE, template);
            return this;
        }
        
        
        public ArgumentProperty build() {
            Argument def = defaultValue == null ? PString.EMPTY : defaultValue;
            ReadBinding read = readBinding;
            Binding write = writeBinding;
            if (read == null) {
                write = new DefaultBinding(def);
                read = write; 
            }
            ControlInfo info = buildInfo();
            if (info.getType() == ControlInfo.Type.ReadOnlyProperty) {
                write = null;
            }
            return new ArgumentProperty(read, write, info);
        }
        
    }
    
    
}
