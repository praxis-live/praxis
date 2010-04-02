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
 * Extension to the Component interface for components that can contain other
 * components as children.
 *
 * @author Neil C Smith
 */
public interface Container extends Component {
    
    /**
     * Add child to this Container.
     *
     * Containers may throw InvalidChildException if the child component is not
     * of a required type, or if the requested ID is invalid or already in use.
     *
     * @param id
     * @param child
     * @throws net.neilcsmith.praxis.core.InvalidChildException
     */
    public void addChild(String id, Component child) throws InvalidChildException;
    
    /**
     * Remove child with given ID.
     *
     * @param id
     * @return Component, or null if no component of that ID.
     */
    public Component removeChild(String id); // throws InvalidChildException;
    
    /**
     * Get child component with specific ID.
     * @param id
     * @return Component, or null if no component with that ID exists.
     */
    public Component getChild(String id);
    
    /**
     * Get the ID for the given Component.
     * @param child
     * @return String ID, or null if component is not a child of this container.
     */
    public String getChildID(Component child);
    
    /**
     * Get the IDs of all child components of this container.
     * @return String array of IDs.
     */
    public String[] getChildIDs();
    


}
