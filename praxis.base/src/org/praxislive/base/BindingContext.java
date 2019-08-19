/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2019 Neil C Smith.
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
package org.praxislive.base;

import org.praxislive.core.ControlAddress;

/**
 * An interface allowing for binding to a Control and (if a property) syncing to
 * it. Implementations of this interface may be placed in a Root or Container to
 * be available for the use of child components.
 */
public interface BindingContext {

    /**
     * Bind adaptor to the binding for the given ControlAddress.
     *
     * @param address control to bind to
     * @param adaptor to send / receive values
     */
    public void bind(ControlAddress address, Binding.Adaptor adaptor);

    /**
     * Unbind adaptor from its binding. If the adaptor is not bound to this
     * address, the method will do nothing.
     *
     * @param address bound control address
     * @param adaptor to remove
     */
    public void unbind(ControlAddress address, Binding.Adaptor adaptor);

}
