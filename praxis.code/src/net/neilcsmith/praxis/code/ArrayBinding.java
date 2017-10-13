/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Neil C Smith.
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
//import net.neilcsmith.praxis.code.userapi.Cycle;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.Value;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
abstract class ArrayBinding extends PropertyControl.Binding {
    
    @Override
    public void set(long time, Value value) throws Exception {
        set(PArray.coerce(value));
    }

    @Override
    public void set(long time, double value) throws Exception {
        set(PArray.valueOf(PNumber.valueOf(value)));
    }

    abstract void set(PArray value) throws Exception;

    @Override
    public ArgumentInfo getArgumentInfo() {
        return ArgumentInfo.create(PArray.class, PMap.EMPTY);
    }

    @Override
    public Value getDefaultValue() {
        return PArray.EMPTY;
    }

    static boolean isBindableFieldType(Class<?> type) {
        return type == PArray.class;// || type == Cycle.class;
    }

    static ArrayBinding create(CodeConnector<?> connector, Field field) {
//        if (field.getType() == Cycle.class) {
//            return new CycleField(field);
//        } else
        if (field.getType() == PArray.class) {
            return new PArrayField(field);
        } else {
            return null;
        }
       
    }
    
//    private static class CycleField extends ArrayBinding {
//
//        private final Field field;
//        private CodeDelegate delegate;
//        private Cycle cycle;
//        
//        private CycleField(Field field) {
//            this.field = field;
//        }
//
//        @Override
//        protected void attach(CodeContext<?> context, PropertyControl.Binding previous) {
//            this.delegate = context.getDelegate();
//            if (previous instanceof CycleField) {
//                cycle = ((CycleField) previous).cycle;
//            } else {
//                cycle = new Cycle(){};
//            }
//            try {
//                field.set(delegate, cycle);
//            } catch (Exception ex) {
//                context.getLog().log(LogLevel.ERROR, ex);
//            }
//        }
//
//        @Override
//        void set(PArray value) throws Exception {
//            cycle.values(value);
//        }
//
//        @Override
//        public Value get() {
//            return cycle.values();
//        }
//        
//    }
    
    private static class PArrayField extends ArrayBinding {
        
        private final Field field;
        private CodeDelegate delegate;
        
        private PArrayField(Field field) {
            this.field = field;
        }

        @Override
        protected void attach(CodeContext<?> context) {
            this.delegate = context.getDelegate();
            try {
                set(PArray.EMPTY);
            } catch (Exception ex) {
                // ignore
            }
        }

        @Override
        void set(PArray value) throws Exception {
            field.set(delegate, value);
        }

        @Override
        public Value get() {
            try {
                return (PArray) field.get(delegate);
            } catch (Exception ex) {
                return PArray.EMPTY;
            }
        }
        
    }
    
}
