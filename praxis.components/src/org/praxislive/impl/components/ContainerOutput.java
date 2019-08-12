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
package org.praxislive.impl.components;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.impl.ContainerContext;
import org.praxislive.impl.RegistrationException;
import org.praxislive.impl.AbstractComponent;
import org.praxislive.impl.DefaultControlOutputPort;

/**
 *
 * @author Neil C Smith
 */
public class ContainerOutput extends AbstractComponent {

    private ContainerContext context;
    private String id;
    private DefaultControlOutputPort containerPort;

    public ContainerOutput() {
        containerPort = new DefaultControlOutputPort();
        LinkedInputPort input = new LinkedInputPort(containerPort);
        registerPort(PortEx.IN, input);
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        ContainerContext ctxt = getLookup().find(ContainerContext.class).orElse(null);
        if (context != ctxt) {
            if (context != null) {
                context.unregisterPort(id, containerPort);
            }
            if (ctxt != null) {
                id = getAddress().componentID();
                try {
                    ctxt.registerPort(id, containerPort);
                } catch (RegistrationException ex) {
                    Logger.getLogger(ContainerOutput.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            context = ctxt;
        }
    }
}
