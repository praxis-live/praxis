/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Neil C Smith. All rights reserved.
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
 *
 */

package net.neilcsmith.praxis.core;

/**
 *
 * @author Neil C Smith
 */
public interface ContainerContext {

      /**
     * Allows children to register a control on this container.
     * @param id
     * @param child
     * @param control
     * @throws RegistrationException
     */
    public void registerControl(String id, Control control) throws RegistrationException;

    /**
     * Unregister child control.
     * @param id
     * @param child
     * @param control
     */
    public void unregisterControl(String id, Control control);

     /**
     * Allows children to register a port on this container.
     * @param id
     * @param child
     * @param port
     * @throws net.neilcsmith.praxis.core.PortRegistrationException
     */
    public void registerPort(String id, Port port) throws RegistrationException;


    /**
     * Unregister child port.
     * @param id
     * @param child
     * @param port
     */
    public void unregisterPort(String id, Port port);

}
