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
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.ContainerContext;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PBoolean;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractExecutionContextComponent;
import net.neilcsmith.praxis.impl.AbstractSingleArgProperty;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 *
 * @author Neil C Smith
 */
public class ContainerProperty extends AbstractExecutionContextComponent {

    private final static Logger LOG = Logger.getLogger(ContainerProperty.class.getName());

    private static enum Type {

        Argument, Number
    };
    private final ControlPort.Output output;
    private final Property property;
    private final ContainerControl containerCtrl;
    private final ControlPort.Input containerPort;
    private Type type;
    private ContainerContext context;
    private boolean active;
    private String id;
    private ArgumentProperty minimum;
    private ArgumentProperty maximum;
    private double min = PNumber.MIN_VALUE;
    private double max = PNumber.MAX_VALUE;
    private ControlInfo propertyInfo;
    private ControlInfo containerInfo;

    public ContainerProperty() {
        output = new DefaultControlOutputPort();
        property = new Property();
        containerCtrl = new ContainerControl(property);
        containerPort = containerCtrl.createPort();
//        registerControl("value", property);
        StringProperty typeProperty = StringProperty.builder()
                .allowedValues("<Any>", "Number")
                .binding(new TypeBinding("<Any>"))
                .defaultValue("<Any>")
                .build();
        registerControl("type", typeProperty);
        configureType(Type.Argument);
        registerPort(Port.OUT, output);
        minimum = ArgumentProperty.builder()
                .type(PNumber.class)
                .allowEmpty()
                .defaultValue(PString.EMPTY)
                .binding(new RangeBinding(false))
                .build();
        maximum = ArgumentProperty.builder()
                .type(PNumber.class)
                .allowEmpty()
                .defaultValue(PString.EMPTY)
                .binding(new RangeBinding(true))
                .build();
        markDynamic();
    }

    public void stateChanged(ExecutionContext source) {
        if (source.getState() == ExecutionContext.State.ACTIVE) {
            active = true;
            property.send(output, source.getTime());
        } else {
            active = false;
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

    private void refreshInfo() {
        if (context != null) {
            context.refreshControlInfo(id, containerCtrl);
        }
//        refreshControlInfo("value");
        propertyInfo = null;
        containerInfo = null;
    }

    private void configureType(Type type) {
        Type old = this.type;
        if (old == type) {
            return;
        }
        unregisterControl("value");
        if (old == Type.Number) {
            unregisterControl("minimum");
            unregisterControl("maximum");
        } else if (type == Type.Number) {
            registerControl("minimum", minimum);
            registerControl("maximum", maximum);
        }
        registerControl("value", property);
        this.type = type;
        refreshInfo();
    }

    private void validate(Argument value) throws Exception {
        if (type == Type.Number) {
            validate(PNumber.coerce(value).value());
        }
    }

    private void validate(double value) throws Exception {
        if (value < min || value > max) {
            throw new IllegalArgumentException();
        }
    }

    private void buildInfo() {
        LOG.finest("Rebuilding info");
        ArgumentInfo[] args = new ArgumentInfo[1];
        Argument[] defs = new Argument[1];
        if (type == Type.Number) {
            boolean unbounded = minimum.getValue().isEmpty()
                    && maximum.getValue().isEmpty();
            if (unbounded) {
                LOG.log(Level.FINEST, "Unbounded range - {0} -> {1}",
                        new Object[]{minimum.getValue(), maximum.getValue()});
                args[0] = ArgumentInfo.create(PNumber.class, PMap.EMPTY);
            } else {
                LOG.log(Level.FINEST, "Adding range - {0} -> {1}",
                        new Object[]{minimum.getValue(), maximum.getValue()});
                args[0] = ArgumentInfo.create(PNumber.class, PMap.create(
                        PNumber.KEY_MINIMUM, minimum.getValue(),
                        PNumber.KEY_MAXIMUM, maximum.getValue()));
            }
            defs[0] = PNumber.valueOf(0);
        } else {
            args[0] = ArgumentInfo.create(Argument.class, PMap.EMPTY);
            defs[0] = PString.EMPTY;
        }
        propertyInfo = ControlInfo.createPropertyInfo(args, defs, PMap.EMPTY);
        containerInfo = ControlInfo.createPropertyInfo(args, defs, PMap.create(ControlInfo.KEY_TRANSIENT, PBoolean.TRUE));
    }

    private class Property extends AbstractSingleArgProperty {

        private Argument value = PString.EMPTY;
        private double dValue;

        private Property() {
            super(null);
        }

        @Override
        protected void set(long time, Argument value) throws Exception {
            validate(value);
            this.value = value;
            send(output, time);
        }

        @Override
        protected void set(long time, double value) throws Exception {
            validate(value);
            this.dValue = value;
            this.value = null;
            send(output, time);
        }

        @Override
        protected Argument get() {
            if (value == null) {
                return PNumber.valueOf(dValue);
            } else {
                return value;
            }
        }

        @Override
        public ControlInfo getInfo() {
            if (propertyInfo == null) {
                buildInfo();
            }
            return propertyInfo;
        }

        private void send(ControlPort.Output output, long time) {
            if (!active) {
                return;
            }
            if (value == null) {
                output.send(time, dValue);
            } else {
                output.send(time, value);
            }
        }
    }

    private class ContainerControl implements Control {

        private final Property property;

        private ContainerControl(Property property) {
            this.property = property;
        }

        public void call(Call call, PacketRouter router) throws Exception {
            property.call(call, router);
        }

        private ControlPort.Input createPort() {
            return property.createPort();
        }

        public ControlInfo getInfo() {
            if (containerInfo == null) {
                buildInfo();
            }
            return containerInfo;
        }
    }

    private class TypeBinding implements StringProperty.Binding {

        private String typeValue;

        private TypeBinding(String value) {
            this.typeValue = value;
        }

        public void setBoundValue(long time, String value) {
            if (typeValue.equals(value)) {
                return;
            }
            if ("<Any>".equals(value)) {
                configureType(Type.Argument);
                this.typeValue = value;
            } else if ("Number".equals(value)) {
                configureType(Type.Number);
                this.typeValue = value;
            } else {
                throw new IllegalArgumentException("Unknown type value");
            }
        }

        public String getBoundValue() {
            return typeValue;
        }
    }

    private class RangeBinding implements ArgumentProperty.Binding {

        private final boolean isMax;
        private PNumber value;

        private RangeBinding(boolean isMax) {
            this.isMax = isMax;
        }

        public void setBoundValue(long time, Argument value) throws Exception {
            if (value.isEmpty()) {
                this.value = null;
                if (isMax) {
                    max = PNumber.MAX_VALUE;
                } else {
                    min = PNumber.MIN_VALUE;
                }
            } else {
                this.value = PNumber.coerce(value);
                if (isMax) {
                    max = this.value.value();
                } else {
                    min = this.value.value();
                }
            }
            refreshInfo(); 
        }

        public Argument getBoundValue() {
            return value == null ? PString.EMPTY : value;
        }
    }
}
