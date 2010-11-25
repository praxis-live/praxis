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
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PBoolean;
import net.neilcsmith.praxis.core.types.PNumber;

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
    private BooleanProperty(Component component, Binding binding,
            ControlInfo info) {
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


    public static BooleanProperty create(Component component, boolean def) {
        return create(component, null, def);
    }

    public static BooleanProperty create(Component component, Binding binding,
            boolean def) {
        if (binding == null) {
            binding = new DefaultBinding(def);
        }
        ArgumentInfo[] arguments = new ArgumentInfo[]{PBoolean.info()};
        Argument[] defaults = new Argument[]{PBoolean.valueOf(def)};
        ControlInfo info = ControlInfo.createPropertyInfo(arguments, defaults, null);
        return new BooleanProperty(component, binding, info);
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
}
