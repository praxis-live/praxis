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

package org.praxislive.meta;

import java.util.List;
import java.util.Map;
import org.praxislive.core.Value;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public abstract class TypeRewriter {
    
    private final static TypeRewriter IDENTTY = new IdentityConverter();
    
    public abstract boolean rewriteProperties(Map<String, List<Value>> properties);
    
    public abstract String rewritePortID(String id);
    
    public abstract boolean isStable();
    
    public static TypeRewriter getIdentity() {
        return IDENTTY;
    }
    
    public static boolean isIdentity(TypeRewriter typeConverter) {
        return typeConverter instanceof IdentityConverter;
    }
     
    private static class IdentityConverter extends TypeRewriter {

        @Override
        public boolean rewriteProperties(Map<String, List<Value>> properties) {
            return false;
        }

        @Override
        public String rewritePortID(String id) {
            return id;
        }

        @Override
        public boolean isStable() {
            return true;
        }
        
    }
    
}
