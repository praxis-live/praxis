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
 *
 */

package org.praxislive.impl;

import org.praxislive.core.Control;
import org.praxislive.core.Port;

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
    public abstract void registerControl(String id, AbstractComponent.ControlEx control) throws RegistrationException;

    /**
     * Unregister child control.
     * @param id
     * @param child
     * @param control
     */
    public abstract void unregisterControl(String id, AbstractComponent.ControlEx control);
    
    public abstract void refreshControlInfo(String id, AbstractComponent.ControlEx control);

     /**
     * Allows children to register a port on this container.
     * @param id
     * @param child
     * @param port
     * @throws org.praxislive.core.PortRegistrationException
     */
    public abstract void registerPort(String id, AbstractComponent.PortEx port) throws RegistrationException;


    /**
     * Unregister child port.
     * @param id
     * @param child
     * @param port
     */
    public abstract void unregisterPort(String id, AbstractComponent.PortEx port);
    
    public abstract void refreshPortInfo(String id, AbstractComponent.PortEx port);

}
