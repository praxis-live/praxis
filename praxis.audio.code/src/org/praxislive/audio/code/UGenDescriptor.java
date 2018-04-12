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
 *
 */
package org.praxislive.audio.code;

import java.lang.reflect.Field;
import org.praxislive.code.CodeContext;
import org.praxislive.logging.LogLevel;
import org.jaudiolibs.pipes.Pipe;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class UGenDescriptor {

    private final Field field;
    private final Field previousField;

    private Pipe ugen;
    
    private UGenDescriptor(Field field, Field previousField, Pipe ugen) {
        this.field = field;
        this.previousField = previousField;
        this.ugen = ugen;
    }
    
    void attach(AudioCodeContext<?> context, CodeContext<?> previous) {
        if (ugen == null) {
            try {
                ugen = (Pipe) previousField.get(previous.getDelegate());
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
                return;
            }
        }
        try {
            field.set(context.getDelegate(), ugen);
        } catch (Exception ex) {
            context.getLog().log(LogLevel.ERROR, ex);
        }
    }
    
    Pipe getUGen() {
        return ugen;
    }

    static UGenDescriptor create(AudioCodeConnector connector, Field field) {
        Class<?> fieldType = field.getType();
        if (!Pipe.class.isAssignableFrom(fieldType)) {
            return null;
        }
        field.setAccessible(true);
        Field oldField = null;
        Class<? extends AudioCodeDelegate> oldClass = connector.getPreviousClass();
        if (oldClass != null) {
            try {
                Field f = oldClass.getDeclaredField(field.getName());
                if (f.getType() == field.getType()) {
                    f.setAccessible(true);
                    oldField = f;
                }
            } catch (Exception ex) {
                // fall through
            }
        }
        Pipe ugen = null;
        if (oldField == null) {
            try {
                ugen = field.getType().asSubclass(Pipe.class).newInstance();
            } catch (Exception ex) {
                connector.getLog().log(LogLevel.ERROR, ex);
                return null;
            }
        }
        return new UGenDescriptor(field, oldField, ugen);
    }

}
