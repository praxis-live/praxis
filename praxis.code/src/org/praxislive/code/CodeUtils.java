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

import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class CodeUtils {
 
    
    private CodeUtils() {}
    
    
    public static String load(Class<?> cls, String location) {
        try (InputStream is = cls.getResourceAsStream(location);
                Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A");) {
            return s.hasNext() ? s.next() : ""; 
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static <T> T[] join(T[] a, T[] b) {
        T[] r = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }
    
    public static String[] defaultImports() {
        return DefaultCodeDelegate.IMPORTS.clone();
    }
    
}
