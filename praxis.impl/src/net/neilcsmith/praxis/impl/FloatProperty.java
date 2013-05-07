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
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PNumber;

/**
 *
 * @author Neil C Smith
 */
public class FloatProperty extends AbstractSingleArgProperty {

    private double min;
    private double max;
    private Binding binding;

    private FloatProperty(Binding binding, double min, double max,
            ControlInfo info) {
        super(info);
        this.binding = binding;
        this.min = min;
        this.max = max;
    }


    @Override
    protected void set(long time, Argument value) throws Exception {
        set(time, PNumber.coerce(value).value());
    }

    @Override
    protected void set(long time, double value) throws Exception {
        if (value < min || value > max) {
            throw new IllegalArgumentException();
        }
        binding.setBoundValue(time, value);
    }

    @Override
    protected Argument get() {
        return PNumber.valueOf(binding.getBoundValue());
    }


    public double getValue() {
        return binding.getBoundValue();
    }

    public static FloatProperty create( double def) {
        return create( null, def, null);
    }

    public static FloatProperty create( Binding binding, double def) {
        return create(binding, def, null);
    }

    public static FloatProperty create( Binding binding, double def, PMap properties) {
        if (def < PNumber.MIN_VALUE || def > PNumber.MAX_VALUE) {
            throw new IllegalArgumentException();
        }
        if (binding == null) {
            binding = new DefaultBinding(def);
        }
        ArgumentInfo[] arguments = new ArgumentInfo[]{PNumber.info()};
        Argument[] defaults = new Argument[]{PNumber.valueOf(def)};
        ControlInfo info = ControlInfo.createPropertyInfo(arguments, defaults, properties);
        return new FloatProperty(binding, PNumber.MIN_VALUE, PNumber.MAX_VALUE, info);
    }

    public static FloatProperty create( double min,
            double max, double def) {
        return create( null, min, max, def, null);

    }

    public static FloatProperty create( Binding binding,
            double min, double max, double def) {
        return create(binding, min, max, def, null);
    }

    public static FloatProperty create( Binding binding,
            double min, double max, double def, PMap properties) {
        if (min > max || def < min || def > max) {
            throw new IllegalArgumentException();
        }
        if (binding == null) {
            binding = new DefaultBinding(def);
        }
        ArgumentInfo[] arguments = new ArgumentInfo[]{PNumber.info(min, max)};
        Argument[] defaults = new Argument[]{PNumber.valueOf(def)};
        ControlInfo info = ControlInfo.createPropertyInfo(arguments, defaults, properties);
        return new FloatProperty(binding, min, max, info);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static interface Binding {

        public abstract void setBoundValue(long time, double value);

        public abstract double getBoundValue();
    }

    private static class DefaultBinding implements Binding {

        private double value;

        private DefaultBinding(double value) {
            this.value = value;
        }

        @Override
        public void setBoundValue(long time, double value) {
            this.value = value;
        }

        @Override
        public double getBoundValue() {
            return value;
        }
    }
    
    public final static class Builder extends AbstractSingleArgProperty.Builder<Builder> {
        
        private double minimum;
        private double maximum;
        private double def;
        private Binding binding;
        
        private Builder() {
            super(PNumber.class);
            minimum = PNumber.MIN_VALUE;
            maximum = PNumber.MAX_VALUE;
        }
        
        public Builder minimum(double min) {
            putArgumentProperty(PNumber.KEY_MINIMUM, PNumber.valueOf(min));
            minimum = min;
            return this;
        }
        
        public Builder maximum(double max) {
            putArgumentProperty(PNumber.KEY_MAXIMUM, PNumber.valueOf(max));
            maximum = max;
            return this;
        }
        
        public Builder defaultValue(double value) {
            defaults(PNumber.valueOf(value));
            def = value;
            return this;
        }
        
        public Builder binding(Binding binding) {
            this.binding = binding;
            return this;
        }
        
        public FloatProperty build() {
            ControlInfo info = buildInfo();
            Binding b = binding == null ? new DefaultBinding(def) : binding;
            return new FloatProperty(b, minimum, maximum, info);
        }
        
        
    }
}
