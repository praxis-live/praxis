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
class RefDescriptor {
    
    private final String id;
    private final Field refField;
    private CodeContext<?> context;
    
    RefImpl<?> ref;

    private RefDescriptor(String id,
            Field refField) {
        this.id = id;
        this.refField = refField;
    }
    
    void attach(CodeContext<?> context, RefDescriptor previous) {
        this.context = context;
        if (previous != null) {
            if (refField.getGenericType().equals(previous.refField.getGenericType())) {
                ref = previous.ref;
            } else if (previous.ref != null) {
                previous.ref.dispose();
            }
        }
        
        if (ref == null) {
            ref = new RefImpl<>();
        }

        try {
            refField.set(context.getDelegate(), ref);
        } catch (Exception ex) {
            context.getLog().log(LogLevel.ERROR, ex);
        }
    }
    
    String getID() {
        return id;
    }
    
    void reset(boolean full) {
        if (full) {
            dispose();
        } else if (ref != null) {
            ref.reset();
        }
    }
    
    void dispose() {
        if (ref != null) {
            ref.dispose();
        }
    }
    
    static RefDescriptor create(CodeConnector<?> connector, Field field) {
        if (Ref.class.equals(field.getType()) && 
                field.getGenericType() instanceof ParameterizedType) {
            field.setAccessible(true);
            return new RefDescriptor(field.getName(), field);
        } else {
            return null;
        }
    }
    
    private class RefImpl<T> extends Ref<T> {

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
        
        
        
    }
    
}
