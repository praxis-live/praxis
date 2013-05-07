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
 */
package net.neilcsmith.praxis.components.container;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ContainerContext;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.RegistrationException;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractExecutionContextComponent;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;

/**
 *
 * @author Neil C Smith
 */
public class ContainerProperty extends AbstractExecutionContextComponent {

    private final static Logger LOG = Logger.getLogger(ContainerProperty.class.getName());
    private ControlPort.Output output;
    private Argument arg;
    private ContainerContext context;
    private ArgumentProperty containerCtrl;
    private ControlPort.Input containerPort;
    private String id;

    public ContainerProperty() {
        output = new DefaultControlOutputPort();
        arg = PString.EMPTY;
        Binding binding = new Binding();
        ArgumentProperty.Builder builder = ArgumentProperty.builder()
                .binding(binding)
                .defaultValue(arg);
        ArgumentProperty value = builder.build();
        builder.markTransient();
        containerCtrl = builder.build();
        containerPort = containerCtrl.createPort();
        registerControl("value", value);
        registerPort(Port.OUT, output);
    }

    public void stateChanged(ExecutionContext source) {
        if (source.getState() == ExecutionContext.State.ACTIVE) {
            output.send(source.getTime(), arg);
        }
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        LOG.fine("Called super.hierarchyChanged()");
        ContainerContext ctxt = getLookup().get(ContainerContext.class);
        if (context != ctxt) {
            LOG.log(Level.FINE, "Found changed ContainerContext : {0}", context);
            if (context != null) {
                LOG.fine("Attempting to unregister previous control and port.");
                context.unregisterControl(id, containerCtrl);
                context.unregisterPort(id, containerPort);
            }
            if (ctxt != null) {
                try {
                    LOG.fine("Attempting to register control and port.");
                    LOG.fine("Address is " + getAddress());
                    id = getAddress().getID();
                    LOG.fine("Found ID : " + id);
                    ctxt.registerControl(id, containerCtrl);
                    LOG.fine("Registered Control");
                    ctxt.registerPort(id, containerPort);
                    LOG.fine("Registered Port");
                } catch (Exception ex) {
                    Logger.getLogger(ContainerProperty.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            context = ctxt;
        }
    }

    private class Binding implements ArgumentProperty.Binding {

        // @TODO should binding only send value if root state is running?
        public void setBoundValue(long time, Argument value) {
            arg = value;
            output.send(time, arg);
        }

        public Argument getBoundValue() {
            return arg;
        }
    }
}
