/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */

package net.neilcsmith.praxis.core.types;

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.info.ArgumentInfo;

/**
 *
 * @author Neil C Smith
 */
public class PString extends Argument {
    
    
    public final static PString EMPTY = PString.valueOf("");
    
    private String value;
    
    private PString(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof PString) {
            return ((PString) obj).toString().equals(value);
        } else {
           return false; 
        }
        
    }
    
    public static PString valueOf(String str) {
        if (str == null) {
            throw new NullPointerException();
        }
        return new PString(str);
    }
    
    public static PString valueOf(Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return new PString(String.valueOf(obj));
    }

    public static ArgumentInfo info() {
        return ArgumentInfo.create(PString.class, null);
    }
    
    public static ArgumentInfo info(String[] allowed) {
        if (allowed == null) {
            return info();
        } else {
            PString[] arr = new PString[allowed.length];
            for (int i=0; i < arr.length; i++) {
                arr[i] = PString.valueOf(allowed[i]);
            }
            PMap props = PMap.valueOf(PString.valueOf("allowed-values"),
                    PArray.valueOf(arr));
            return ArgumentInfo.create(PString.class, props);
        }
    }
    
}
