/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
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
 *
 */
package org.praxislive.gui.impl;

import org.praxislive.core.Value;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.ValueFormatException;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PString;
import org.praxislive.gui.ControlBinding;
import org.praxislive.gui.BindingContext;
import org.praxislive.impl.ArgumentProperty;

/**
 *
 * @author Neil C Smith
 */
public abstract class SingleBindingGuiComponent extends AbstractGuiComponent {
    
    private ControlAddress binding;
    private ControlBinding.Adaptor adaptor;
    private BindingContext bindingContext;
    
    protected SingleBindingGuiComponent() {
    }
    
    @Override
    protected void initControls() {
        super.initControls();
        registerControl("binding", ArgumentProperty.create(
                ArgumentInfo.create(ControlAddress.class,
                    PMap.create(ArgumentInfo.KEY_ALLOW_EMPTY, true)),
                new AddressBinding(),
                PString.EMPTY));
    }
    
    private class AddressBinding implements ArgumentProperty.Binding {
        
        public void setBoundValue(long time, Value value) {
            if (adaptor == null) {
                adaptor = getBindingAdaptor();
            }
            if (bindingContext != null) {
                bindingContext.unbind(adaptor);
                if (value.isEmpty()) {
                    binding = null;
                } else {
                    try {
                        binding = ControlAddress.coerce(value);
                        bindingContext.bind(binding, adaptor);
                    } catch (ValueFormatException ex) {
                        binding = null;
                    }
                }
            }
        }
        
        public Value getBoundValue() {
            return binding == null ? PString.EMPTY : binding;
        }
    }
    
    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        BindingContext ctxt = getLookup().get(BindingContext.class);
        if (bindingContext != ctxt) {
            if (bindingContext != null && binding != null) {
                bindingContext.unbind(adaptor);
            }
            if (ctxt != null && binding != null) {
                ctxt.bind(binding, adaptor);
            }
            bindingContext = ctxt;
        }
    }
    
    protected abstract ControlBinding.Adaptor getBindingAdaptor();
}
