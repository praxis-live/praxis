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
package net.neilcsmith.praxis.core;

import net.neilcsmith.praxis.core.info.ComponentInfo;

/**
 * Components are the main building blocks within Praxis. They may provide Controls
 * and Ports. Components expect to be used within a hierarchy, inside a Root
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
     * Component may throw a VetoException if it should not be added to or removed
     * from the Container provided. It should also throw this exception if the existing
     * parent has not been nulled prior to a new parent being notified.
     *
     * @param parent
     * @throws net.neilcsmith.praxis.core.VetoException
     */
    public void parentNotify(Container parent) throws VetoException;

    /**
     * Notify the component that a change has happened further up its component
     * hierarchy (eg. the parent of this Component's parent has changed).
     *
     */
    public void hierarchyChanged();

    /**
     * Get the Control with the given ID, or null if it does not exist.
     * @param id
     * @return Control or null
     */
    public Control getControl(String id);


    /**
     * Get an array of all the Control IDs from this component.
     * @return String[]
     */
    public String[] getControlIDs();

    /**
     * Get the Port with the given ID, or null if it does not exist.
     * @param id
     * @return Port or null
     */
    public Port getPort(String id);


    /**
     * Get an array of all the Port IDs from this component.
     * @return String[]
     */
    public String[] getPortIDs();

    /**
     * Get the ComponentInfo object for this component.
     * @return ComponentInfo
     */
    public ComponentInfo getInfo();

    @Deprecated
    public InterfaceDefinition[] getInterfaces();

}
