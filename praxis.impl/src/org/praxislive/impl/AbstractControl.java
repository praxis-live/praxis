/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2014 Neil C Smith.
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
package org.praxislive.impl;

import org.praxislive.core.Argument;
import org.praxislive.core.ComponentAddress;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.InterfaceDefinition;
import org.praxislive.core.Lookup;
import org.praxislive.core.info.ArgumentInfo;
import org.praxislive.core.info.ControlInfo;
import org.praxislive.core.interfaces.Service;
import org.praxislive.core.interfaces.ServiceManager;
import org.praxislive.core.interfaces.ServiceUnavailableException;
import org.praxislive.core.interfaces.Services;
import org.praxislive.core.types.PBoolean;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PString;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractControl implements AbstractComponent.ExtendedControl {

    private AbstractComponent host;
    private ControlAddress address;

    public void addNotify(AbstractComponent component) {
        this.host = component;
    }

    public void removeNotify(AbstractComponent component) {
        if (this.host == component) {
            this.host = null;
        }
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

    @Deprecated
    protected ComponentAddress findService(InterfaceDefinition service)
            throws ServiceUnavailableException {
        ServiceManager sm = getLookup().get(ServiceManager.class);
        if (sm == null) {
            throw new ServiceUnavailableException("No ServiceManager in Lookup");
        }
        return sm.findService(service);

    }
    
    protected ComponentAddress findService(Class<? extends Service> service)
            throws ServiceUnavailableException {
        Services srvs = getLookup().get(Services.class);
        if (srvs == null) {
            throw new ServiceUnavailableException("No Services found in Lookup");
        }
        return srvs.findService(service);
    }
    
    public static abstract class Builder<B extends Builder<B>> {
        
        private final static ArgumentInfo[] EMPTY_INFO = new ArgumentInfo[0];
        
        private ControlInfo.Type type;
        private ArgumentInfo[] inputs;
        private ArgumentInfo[] outputs;
        private Argument[] defaults;
        private PMap.Builder controlProps;
        
        protected Builder() {
            this.type = ControlInfo.Type.Function;
        }
         
        public B markDeprecated() {
            return putControlProperty(ControlInfo.KEY_DEPRECATED, PBoolean.TRUE);
        }
        
        public B markTransient() {
            return putControlProperty(ControlInfo.KEY_TRANSIENT, PBoolean.TRUE);
        }
        
        @SuppressWarnings("unchecked")
        protected B putControlProperty(String key, Argument value) {
            if (controlProps == null) {
                controlProps = PMap.builder();
            }
            controlProps.put(PString.valueOf(key), value);
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        protected B controlType(ControlInfo.Type type) {
            if (type == null) {
                throw new NullPointerException();
            }
            this.type = type;
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        protected B inputs(ArgumentInfo ... inputs) {
            this.inputs = inputs;
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        protected B outputs(ArgumentInfo ... outputs) {
            this.outputs = outputs;
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        protected B arguments(ArgumentInfo ... args) {
            this.inputs = args;
            this.outputs = args;
            return (B) this;
        }
        
        @SuppressWarnings("unchecked")
        protected B defaults(Argument ... defaults) {
            this.defaults = defaults;
            return (B) this;
        }
        
        protected ControlInfo buildInfo() {
            ArgumentInfo[] ins = inputs == null ? EMPTY_INFO : inputs;
            ArgumentInfo[] outs = outputs == null ? EMPTY_INFO : outputs;
            PMap props = controlProps == null ? PMap.EMPTY : controlProps.build();
            switch (type) {
                case Action:
                    return ControlInfo.createActionInfo(props);
                case Property:
                    return ControlInfo.createPropertyInfo(outs, defaults, props);
                case ReadOnlyProperty:
                    return ControlInfo.createReadOnlyPropertyInfo(outs, props);
                default:
                    return ControlInfo.createFunctionInfo(ins, outs, props);
                
            }
        }
        
    }
}
