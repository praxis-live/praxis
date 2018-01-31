/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2016 Neil C Smith.
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
package org.praxislive.core.interfaces;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.InterfaceDefinition;

/**
 *
 * @author Neil C Smith
 */
public abstract class Services {

    public Optional<ComponentAddress> locate(Class<? extends Service> service) {
        try {
            return Optional.of(findService(service));
        } catch (ServiceUnavailableException ex) {
            return Optional.empty();
        }
    }
    
    public List<ComponentAddress> locateAll(Class<? extends Service> service) {
        try {
            return Arrays.asList(findAllServices(service.newInstance()));
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    @Deprecated
    public ComponentAddress findService(Class<? extends InterfaceDefinition> info) throws ServiceUnavailableException {
        throw new ServiceUnavailableException();
    }

    @Deprecated
    public ComponentAddress[] findAllServices(InterfaceDefinition info) throws ServiceUnavailableException {
        throw new ServiceUnavailableException();
    }

}
