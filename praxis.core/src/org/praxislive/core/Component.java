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
package org.praxislive.core;

/**
 * Components are the main building blocks (actors) within Praxis CORE. They may 
 * provide Controls (asynchronous message endpoints) and Ports (synchronous message
 * points). 
 * 
 * Components expect to be used within a hierarchy, inside a Root
 * component, and potentially inside other Container components.
 *
 * @author Neil C Smith
 */
public interface Component {

    /**
     * Return the Container that is the immediate parent of this Component, or
     * null if this Component is not currently contained within a Component
     * hierarchy.
     * @return Container
     */
    public Container getParent();

    /**
     * Notify the Component that it has been added to the supplied Container, or
     * removed from its parent if the supplied argument is null. The
     * Component may throw a VetoException if it should not be added to the Container
     * provided. It should also throw this exception if a parent is already set.
     *
     * @param parent
     * @throws org.praxislive.core.VetoException
     */
    public void parentNotify(Container parent) throws VetoException;

    /**
     * Notify the component that a change has happened in its component
     * hierarchy. For example its direct parent or an ancestor has changed.
     * 
     * This method will be called after parentNotify() if the result of an
     * immediate parent change.
     *
     */
    public void hierarchyChanged();
    
    /**
     * Get a Control that can handle a Call to the given ID, or null if it does 
     * not exist. Component implementations are free to return a different Control
     * for each ID, a single control to handle any message, or somewhere in between.
     * 
     * A null return from this method shall be handled by the Root component by
     * responding with an error message to the sender where required.
     * 
     * @param id
     * @return Control or null
     */
    
    public Control getControl(String id);


    /**
     * Get the Port with the given ID, or null if it does not exist.
     * @param id
     * @return Port or null
     */
    public Port getPort(String id);



    /**
     * Get the ComponentInfo object for this component.
     * @return ComponentInfo
     */
    public ComponentInfo getInfo();

    
}
