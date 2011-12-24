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

import java.awt.EventQueue;
import javax.swing.JComponent;
import net.neilcsmith.praxis.core.Container;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.VetoException;
import net.neilcsmith.praxis.gui.GuiContext;
import net.neilcsmith.praxis.gui.Keys;
import net.neilcsmith.praxis.impl.AbstractContainer;
import net.neilcsmith.praxis.impl.InstanceLookup;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractGuiContainer extends AbstractContainer {

    private JComponent component;
    private Lookup lookup;
    private GuiContext context;
    private LayoutBinding layout;

    public final JComponent getSwingContainer() {
        if (EventQueue.isDispatchThread()) {
            if (component == null) {
                component = createSwingContainer();
                layout = new LayoutBinding(component);
                registerControl("layout", StringProperty.create(layout, ""));
            }
            return component;
        } else {
            return null;
        }
    }

    @Override
    public void parentNotify(Container parent) throws VetoException {
        if (EventQueue.isDispatchThread()) {
            super.parentNotify(parent);
        } else {
            throw new VetoException("Trying to install GUI component in GUI incompatible container.");
        }
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        // use super.getLookup() - don't want our own!
        GuiContext ctxt = super.getLookup().get(GuiContext.class);
        if (context != ctxt) {
            if (context != null) {
                context.getContainer().remove(getSwingContainer());
            }
            if (ctxt != null) {
                ctxt.getContainer().add(getSwingContainer());
            }
            context = ctxt;
        }
        getSwingContainer().putClientProperty(Keys.Address, getAddress());
    }

    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = InstanceLookup.create(super.getLookup(), new GuiContext() {

                @Override
                public JComponent getContainer() {
                    return getSwingContainer();
                }
            });
        }
        return lookup;
    }

    protected abstract JComponent createSwingContainer();

}
