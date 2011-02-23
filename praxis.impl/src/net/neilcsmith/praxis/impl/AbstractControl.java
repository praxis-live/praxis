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
package net.neilcsmith.praxis.impl;

import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.InterfaceDefinition;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.interfaces.ServiceManager;
import net.neilcsmith.praxis.core.interfaces.ServiceUnavailableException;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractControl implements AbstractComponent.ExtendedControl {

    private AbstractComponent host;
    private ControlAddress address;

    public void addNotify(AbstractComponent component) {
        this.host = component;
        hierarchyChanged();
    }

    public void removeNotify(AbstractComponent component) {
        if (this.host == component) {
            this.host = null;
        }
        hierarchyChanged();
    }

    public void hierarchyChanged() {
        address = null;
    }

    public AbstractComponent getComponent() {
        return host;
    }

    public ControlAddress getAddress() {
        if (address == null) {
            if (host == null) {
                return null;
            } else {
                address = host.getAddress(this);

            }
        }
        return address;
    }

    protected Lookup getLookup() {
        if (host == null) {
            return EmptyLookup.getInstance();
        } else {
            return host.getLookup();
        }
    }

    protected ComponentAddress findService(InterfaceDefinition service)
            throws ServiceUnavailableException {
        ServiceManager sm = getLookup().get(ServiceManager.class);
        if (sm == null) {
            throw new ServiceUnavailableException("No ServiceManager in Lookup");
        }
        return sm.findService(service);

    }
}
