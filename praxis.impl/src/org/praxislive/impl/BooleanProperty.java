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
package org.praxislive.impl;

import java.util.logging.Logger;
import org.praxislive.core.Argument;
import org.praxislive.core.Component;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.types.PBoolean;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PNumber;

/**
 *
 * @author Neil C Smith
 */
public class BooleanProperty extends AbstractSingleArgProperty {

    private static Logger logger = Logger.getLogger(BooleanProperty.class.getName());
    private Binding binding;


//    public FloatProperty(AbstractComponent component, float min, float max, float def) {
//        this(component, null, min, max, def);
//    }
    private BooleanProperty(Binding binding, ControlInfo info) {
        super(info);
        this.binding = binding;

    }

    @Override
    protected void set(long time, Argument value) throws Exception {
        if (value instanceof PBoolean) {
            binding.setBoundValue(time, ( (PBoolean) value).value());
        } else if (value instanceof PNumber) {
            set(time, ( (PNumber) value ).value());
        } else {
            try {
                binding.setBoundValue(time, PBoolean.coerce(value).value());
            }
            catch (Exception ex) {
                set(time, PNumber.coerce(value).value());
            }
        }
        
    }

    @Override
    protected void set(long time, double value) throws Exception {
        if (Math.round(value) > 0) {
            binding.setBoundValue(time, true);
        } else {
            binding.setBoundValue(time, false);
        }
    }

    @Override
    protected Argument get() {
        return PBoolean.valueOf(binding.getBoundValue());
    }
    

    public boolean getValue() {
        return binding.getBoundValue();
    }

    @Deprecated
    public static BooleanProperty create(Component component, boolean def) {
        return create(component, null, def);
    }

    @Deprecated
    public static BooleanProperty create(Component component, Binding binding,
            boolean def) {
        return create(component, binding, def, null);
    }
    
    @Deprecated
    public static BooleanProperty create(Component component, Binding binding,
            boolean def, PMap properties) {
        if (binding == null) {
            binding = new DefaultBinding(def);
        }
        ArgumentInfo[] arguments = new ArgumentInfo[]{PBoolean.info()};
        Argument[] defaults = new Argument[]{PBoolean.valueOf(def)};
        ControlInfo info = ControlInfo.createPropertyInfo(arguments, defaults, properties);
        return new BooleanProperty(binding, info);
    }
    
    public static BooleanProperty create(boolean def) {
        return create(null, def, null);
    }

    public static BooleanProperty create(Binding binding, boolean def) {
        return create(binding, def, null);
    }
    
    public static BooleanProperty create(Binding binding, boolean def, PMap properties) {
        if (binding == null) {
            binding = new DefaultBinding(def);
        }
        ArgumentInfo[] arguments = new ArgumentInfo[]{PBoolean.info()};
        Argument[] defaults = new Argument[]{PBoolean.valueOf(def)};
        ControlInfo info = ControlInfo.createPropertyInfo(arguments, defaults, properties);
        return new BooleanProperty(binding, info);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static interface Binding {

        public void setBoundValue(long time, boolean value);

        public boolean getBoundValue();
    }

    private static class DefaultBinding implements Binding {

        private boolean value;

        private DefaultBinding(boolean value) {
            this.value = value;
        }

        @Override
        public void setBoundValue(long time, boolean value) {
            this.value = value;
        }

        @Override
        public boolean getBoundValue() {
            return value;
        }
    }
    
    
    public static class Builder extends AbstractSingleArgProperty.Builder<Builder> {
        
        private boolean def;
        private Binding binding;
        
        private Builder() {
            super(PBoolean.class);
        }
        
        public Builder defaultValue(boolean def) {
            defaults(PBoolean.valueOf(def));
            this.def = def;
            return this;
        }
        
        public Builder binding(Binding binding) {
            this.binding = binding;
            return this;
        }
        
        public BooleanProperty build() {
            Binding bdg = binding == null ? new DefaultBinding(def) : binding;
            ControlInfo info = buildInfo();
            return new BooleanProperty(binding, info);
        }
        
    }
    
    
}
