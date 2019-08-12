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
package org.praxislive.code.services;

import org.praxislive.core.Value;
import org.praxislive.core.ValueFormatException;
import org.praxislive.core.types.PBytes;
import org.praxislive.core.types.PMap;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class PMapClassLoader extends ClassLoader {
    
    private final PMap classes;
    
    PMapClassLoader(PMap classes, ClassLoader parent) {
        super(parent);
        this.classes = classes;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Value dataArg = classes.get(name);
        if (dataArg == null) {
            throw new ClassNotFoundException(name);
        }
        try {
            PBytes data = PBytes.coerce(dataArg);
            byte[] bytes = new byte[data.size()];
            data.read(bytes);
            return defineClass(name, bytes, 0, bytes.length);
        } catch (ValueFormatException ex) {
            throw new ClassNotFoundException(name, ex);
        }
    }
    
    
    
}
