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
package org.praxislive.code;

import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.core.Value;
import org.praxislive.core.ValueFormatException;
import org.praxislive.core.Lookup;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.types.PString;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class TypeConverter<T> {
    
    public abstract Value toArgument(T value);
    
    public abstract T fromArgument(Value value) throws ValueFormatException;
    
    public abstract Class<T> getType();
    
    public ArgumentInfo getInfo() {
        return ArgumentInfo.info();
    }
    
    public boolean isRealTimeSafe() {
        return false;
    }
    
    public Value getDefaultArgument() {
        return PString.EMPTY;
    }
    
    public T getDefaultValue() {
        return null;
    }
    
    public static interface Provider {
        
        public <T> TypeConverter<T> getTypeConverter(Class<T> type, Annotation ... annotations);
        
    }
    
    public final static <T> TypeConverter<T> find(Class<T> type, Annotation ... annotations) {
        Provider[] providers = Lookup.SYSTEM.findAll(Provider.class).toArray(Provider[]::new);
        try {
            for (Provider p : providers) {
                TypeConverter converter = p.getTypeConverter(type, annotations);
                if (converter != null) {
                    return converter;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(TypeConverter.class.getName())
                    .log(Level.SEVERE, "Exception looking for TypeConverter", ex);
        }
        return null;
    }
    
}
