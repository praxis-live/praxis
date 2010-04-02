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

package net.neilcsmith.praxis.core;

/**
 *
 * @author Neil C Smith
 */
public class ServiceID {
    
    public static final ServiceID AUXILLARY_THREAD_SERVICE =
            new ServiceID("AUXILLARY_THREAD_SERVICE");
    
    public static final ServiceID DEFAULT_SCRIPT_INTERPRETER =
            new ServiceID("DEFAULT_SCRIPT_INTERPRETER");
    
    private String name;
    
    public ServiceID(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Service Desciption : " + name.toString();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceID) {
            ServiceID sd = (ServiceID) obj;
            return sd.name.equals(this.name);
        }
        return false;
    }
    
    

}
