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
 *
 */
package net.neilcsmith.praxis.gui.impl;

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.gui.ControlBinding;
import net.neilcsmith.praxis.gui.BindingContext;
import net.neilcsmith.praxis.impl.ArgumentProperty;

/**
 *
 * @author Neil C Smith
 */
public abstract class SingleBindingGuiComponent extends AbstractGuiComponent {

    private ControlAddress binding;
    private ControlBinding.Adaptor adaptor;
    private BindingContext bindingContext;

    protected SingleBindingGuiComponent() {
        registerControl("binding", ArgumentProperty.create( new AddressBinding(), PString.EMPTY));
    }

    private class AddressBinding implements ArgumentProperty.Binding {

        public void setBoundValue(long time, Argument value) {
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
                    } catch (ArgumentFormatException ex) {
                        binding = null;
                    }
                }
            }
//            if (value.isEmpty() && adaptor.getBinding() != null) {
//                root.unbind(adaptor);
//                binding = null;
//            } else if (root != null) {
//                if (binding != null) {
//                    root.unbind(binding, adaptor);
//                }
//                try {
//                    binding = ControlAddress.coerce(value);
//                    root.bind(binding, adaptor);
//                } catch (ArgumentFormatException ex) {
//                    binding = null;
//                }
//            }
        }

        public Argument getBoundValue() {
            return binding == null ? PString.EMPTY : binding;
        }
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
//        Root r = getRoot();
//        if (r instanceof BindingContext) {
//            bindingContext = (BindingContext) r;
//        } else {
//            if (binding != null) {
//                bindingContext.unbind(adaptor);
//                binding = null;
//                bindingContext = null;
//            }
//        }
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
