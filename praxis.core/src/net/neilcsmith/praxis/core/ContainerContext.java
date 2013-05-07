/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Neil C Smith.
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
 *
 */

package net.neilcsmith.praxis.core;

/**
 *
 * @author Neil C Smith
 */
public abstract class ContainerContext {

      /**
     * Allows children to register a control on this container.
     * @param id
     * @param child
     * @param control
     * @throws RegistrationException
     */
    public abstract void registerControl(String id, Control control) throws RegistrationException;

    /**
     * Unregister child control.
     * @param id
     * @param child
     * @param control
     */
    public abstract void unregisterControl(String id, Control control);

     /**
     * Allows children to register a port on this container.
     * @param id
     * @param child
     * @param port
     * @throws net.neilcsmith.praxis.core.PortRegistrationException
     */
    public abstract void registerPort(String id, Port port) throws RegistrationException;


    /**
     * Unregister child port.
     * @param id
     * @param child
     * @param port
     */
    public abstract void unregisterPort(String id, Port port);

}
