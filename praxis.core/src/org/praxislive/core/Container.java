/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Neil C Smith.
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

package org.praxislive.core;

/**
 * Extension to the Component interface for components that can contain other
 * components as children.
 *
 * @author Neil C Smith
 */
public interface Container extends Component, Lookup.Provider {
    
//    /**
//     * Add child to this Container.
//     *
//     * Containers may throw InvalidChildException if the child component is not
//     * of a required type, or if the requested ID is invalid or already in use.
//     *
//     * @param id
//     * @param child
//     * @throws org.praxislive.core.VetoException
//     */
//    public void addChild(String id, Component child) throws VetoException;
//    
//    /**
//     * Remove child with given ID.
//     *
//     * @param id
//     * @return Component, or null if no component of that ID.
//     * @throws org.praxislive.core.VetoException
//     */
//    public Component removeChild(String id) throws VetoException;
    
    /**
     * Get child component with specific ID.
     * @param id
     * @return Component, or null if no component with that ID exists.
     */
    public Component getChild(String id);
    
    
    /**
     * Get the IDs of all child components of this container.
     * @return String array of IDs.
     */
    public String[] getChildIDs();

    public ComponentAddress getAddress(Component child);


}
