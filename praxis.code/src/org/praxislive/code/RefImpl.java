/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.code;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import org.praxislive.code.userapi.Ref;
import org.praxislive.logging.LogLevel;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
class RefImpl<T> extends Ref<T> {

    private CodeContext<?> context;

    private void attach(CodeContext<?> context) {
        this.context = context;
    }
    
    @Override
    protected void reset() {
        super.reset();
    }

    @Override
    protected void dispose() {
        super.dispose();
    }

    @Override
    protected void log(Exception ex) {
        context.getLog().log(LogLevel.ERROR, ex);
    }

    static class Descriptor extends ReferenceDescriptor {

        private final Field refField;
        private RefImpl<?> ref;

        private Descriptor(String id, Field refField) {
            super(id);
            this.refField = refField;
        }

        @Override
        public void attach(CodeContext<?> context, ReferenceDescriptor previous) {
            if (previous instanceof RefImpl.Descriptor) {
                RefImpl.Descriptor pd = (RefImpl.Descriptor) previous;
                if (refField.getGenericType().equals(pd.refField.getGenericType())) {
                    ref = pd.ref;
                    pd.ref = null;
                } else {
                    pd.dispose();
                }
            } else if (previous != null) {
                previous.dispose();
            }

            if (ref == null) {
                ref = new RefImpl<>();
            }

            ref.attach(context);
            
            try {
                refField.set(context.getDelegate(), ref);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }

        }

        @Override
        public void reset(boolean full) {
            if (full) {
                dispose();
            } else if (ref != null) {
                ref.reset();
            }
        }

        @Override
        public void dispose() {
            if (ref != null) {
                ref.dispose();
            }
        }

        static Descriptor create(CodeConnector<?> connector, Field field) {
            if (Ref.class.equals(field.getType())
                    && field.getGenericType() instanceof ParameterizedType) {
                field.setAccessible(true);
                return new Descriptor(field.getName(), field);
            } else {
                return null;
            }
        }

    }

}
