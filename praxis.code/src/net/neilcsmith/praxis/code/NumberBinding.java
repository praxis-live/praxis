/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Neil C Smith.
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
 *
 */

package net.neilcsmith.praxis.code;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.types.PNumber;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class NumberBinding extends PropertyControl.Binding {
    
    private final double min;
    private final double max;
    private final double def;
    private final boolean ranged;
    
    private double value;
    private PNumber lastGet;
    
    NumberBinding(double def) {
        min = PNumber.MIN_VALUE;
        max = PNumber.MAX_VALUE;
        this.def = def;
        ranged = false;
    }
    
    NumberBinding(double min, double max, double def) {
        this.min = min;
        this.max = max;
        this.ranged = true;
        this.def = def;
    }

    @Override
    public void set(long time, Argument value) throws Exception {
        set(time, PNumber.coerce(value).value());
    }

    @Override
    public void set(long time, double value) throws Exception {
        if (value < min || value > max) {
            throw new IllegalArgumentException();
        }
        this.value = value;
    }

    @Override
    public double get(double def) {
        return value;
    }

    @Override
    public Argument get() {
        if (lastGet == null || lastGet.value() != value) {
            lastGet = PNumber.valueOf(value);
        }
        return lastGet;
    }

    @Override
    public ArgumentInfo getArgumentInfo() {
        if (ranged) {
            return PNumber.info(min, max);
        } else {
            return PNumber.info();
        }
    }

    @Override
    public Argument getDefaultValue() {
        return PNumber.valueOf(def);
    }
    
    
    static class Reflect extends NumberBinding {
        
        private final Field field;
        private final boolean isDouble;
        private CodeDelegate delegate;


        public Reflect(Field field, double def) {
            super(def);
            this.field = field;
            isDouble = checkDouble();
        }
        
        public Reflect(Field field, double min, double max, double def) {
            super(min, max, def);
            this.field = field;
            isDouble = checkDouble();
        }
        
        private boolean checkDouble() {
            Class<?> cls = field.getType();
            if (cls == double.class || cls == Double.class) {
                return true;
            } else if (cls == float.class || cls == Float.class) {
                return false;
            }
            throw new IllegalArgumentException("Field type is not supported");
        }

        @Override
        protected void attach(CodeDelegate delegate) {
            this.delegate = delegate;
        }

        @Override
        public void set(long time, double value) throws Exception {
            super.set(time, value);
            if (isDouble) {
                field.setDouble(delegate, value);
            } else {
                field.setFloat(delegate, (float) value);
            }
        }

        @Override
        public double get(double def) {
            try {
                return field.getDouble(delegate);
            } catch (Exception ex) {
                return def;
            }
        }

        @Override
        public Argument get() {
            return PNumber.valueOf(get(0));
        }

        
        
        
    }
    
}
