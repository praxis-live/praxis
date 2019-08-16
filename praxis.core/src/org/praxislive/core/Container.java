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
package org.praxislive.core;

import java.util.stream.Stream;
import org.praxislive.core.protocols.ContainerProtocol;

/**
 * Extension to the Component interface for components that can contain other
 * components as children.
 *
 * @author Neil C Smith
 */
public interface Container extends Component, Lookup.Provider {

    /**
     * Get child component with specific ID.
     *
     * @param id
     * @return Component, or null if no component with that ID exists.
     */
    public Component getChild(String id);

    /**
     * Get the IDs of all child components of this container.
     *
     * @return String array of IDs.
     */
    @Deprecated
    public String[] getChildIDs();

    /**
     * Get a Stream of the child IDs that this container makes publicly visible.
     * Containers may have hidden children that can be returned from
     * {@link #getChild(java.lang.String)} but are not listed here. All IDs
     * returned should correspond to valid components, and (if provided) match
     * the implementation of {@link ContainerProtocol}.
     * <p>
     * A Stream is returned allowing flexibility in implementation.
     *
     * @return stream of public child IDs
     */
    public default Stream<String> children() {
        return Stream.of(getChildIDs());
    }

    /**
     * Get the address for the provided child component, or null if the
     * component is not a child of this container.
     *
     * @param child component
     * @return address, or null of component is not a child of this container.
     */
    public ComponentAddress getAddress(Component child);

}
