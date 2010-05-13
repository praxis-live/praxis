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

import java.net.URI;
import java.net.URISyntaxException;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.info.ArgumentInfo;

/**
 *
 * @author Neil C Smith
 */
public class PUri extends Argument implements Comparable<PUri>{
    
    
    private URI uri;
    
    private PUri(URI uri) {
        this.uri = uri;
    }

    public URI value() {
        return uri;
    }
    
    @Override
    public String toString() {
        return uri.toString();
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof PUri) {
            PUri o = (PUri) obj;
            return this.uri.equals(o.uri);
        }
        return false;
    }
    
    public static PUri valueOf(URI uri) {
        if (uri.isAbsolute()) {
            return new PUri(uri);
        }
        throw new IllegalArgumentException();
    }
    
    public static PUri valueOf(String str) throws ArgumentFormatException {
        try {
            URI uri = new URI(str);
            if (uri.isAbsolute()) {
                return new PUri(uri);
            }
            throw new ArgumentFormatException();
        } catch (URISyntaxException ex) {
            throw new ArgumentFormatException(ex);
        }
    }
    
    public static PUri coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof PUri) {
            return (PUri) arg;
        } else {
            return valueOf(arg.toString());
        }
    }
    
    public static ArgumentInfo info() {
        return ArgumentInfo.create(PUri.class, null);
    }

    public int compareTo(PUri o) {
        return uri.compareTo(o.uri);
    }

}
