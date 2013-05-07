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
import net.neilcsmith.praxis.core.types.PBoolean;
import net.neilcsmith.praxis.core.types.PNumber;

/**
 *
 * @author Neil C Smith
 */
public class IntProperty extends AbstractSingleArgProperty {

    private int min;
    private int max;
    private Binding binding;

    private IntProperty(Binding binding, int min, int max, ControlInfo info) {
        super(info);
        this.binding = binding;
        this.min = min;
        this.max = max;
    }

    @Override
    protected void set(long time, Argument value) throws Exception {
        set(time, PNumber.coerce(value).toIntValue());
    }

    @Override
    protected void set(long time, double value) throws Exception {
        set(time, (int) Math.round(value));
    }
    
    private void set(long time, int value) {
        if (value < min || value > max) {
            throw new IllegalArgumentException();
        }
        binding.setBoundValue(time, value);
    }

    @Override
    protected Argument get() {
        return PNumber.valueOf(binding.getBoundValue());
    }
    
    

    public int getValue() {
        return binding.getBoundValue();
    }

    public static IntProperty create(int min,
            int max, int def) {
        return create(null, min, max, def);

    }

    public static IntProperty create( Binding binding,
            int min, int max, int def) {
        if (min > max || def < min || def > max) {
            throw new IllegalArgumentException();
        }
        if (binding == null) {
            binding = new DefaultBinding(def);
        }
        ArgumentInfo[] arguments = new ArgumentInfo[]{PNumber.integerInfo(min, max)};
        Argument[] defaults = new Argument[]{PNumber.valueOf(def)};
        ControlInfo info = ControlInfo.createPropertyInfo(arguments, defaults, null);
        return new IntProperty(binding, min, max, info);
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static interface Binding {

        public void setBoundValue(long time, int value);

        public int getBoundValue();
    }

    private static class DefaultBinding implements Binding {

        private int value;

        private DefaultBinding(int value) {
            this.value = value;
        }

        @Override
        public void setBoundValue(long time, int value) {
            this.value = value;
        }

        @Override
        public int getBoundValue() {
            return value;
        }
    }
    
    public final static class Builder extends AbstractSingleArgProperty.Builder<Builder> {
        
        private int minimum;
        private int maximum;
        private int def;
        private Binding binding;
        
        private Builder() {
            super(PNumber.class);
            putArgumentProperty(PNumber.KEY_IS_INTEGER, PBoolean.TRUE);
            minimum = PNumber.MIN_VALUE;
            maximum = PNumber.MAX_VALUE;
        }
        
        public Builder minimum(int min) {
            putArgumentProperty(PNumber.KEY_MINIMUM, PNumber.valueOf(min));
            minimum = min;
            return this;
        }
        
        public Builder maximum(int max) {
            putArgumentProperty(PNumber.KEY_MAXIMUM, PNumber.valueOf(max));
            maximum = max;
            return this;
        }
        
        public Builder defaultValue(int value) {
            defaults(PNumber.valueOf(value));
            def = value;
            return this;
        }
        
        public Builder binding(Binding binding) {
            this.binding = binding;
            return this;
        }
        
        public IntProperty build() {
            ControlInfo info = buildInfo();
            Binding b = binding == null ? new DefaultBinding(def) : binding;
            return new IntProperty(b, minimum, maximum, info);
        }
        
        
    }
}
