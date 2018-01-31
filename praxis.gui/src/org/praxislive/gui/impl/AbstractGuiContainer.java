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

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.praxislive.core.Container;
import org.praxislive.core.Lookup;
import org.praxislive.core.VetoException;
import org.praxislive.gui.GuiContext;
import org.praxislive.gui.Keys;
import org.praxislive.impl.AbstractContainer;
import org.praxislive.impl.InstanceLookup;
import org.praxislive.impl.StringProperty;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractGuiContainer extends AbstractContainer {

    private JComponent component;
    private Lookup lookup;
    private LabelBinding label;
    private GuiContext context;
    private LayoutBinding layout;

    public final JComponent getSwingContainer() {
        if (EventQueue.isDispatchThread()) {
            if (component == null) {
                component = createSwingContainer();
                label = new LabelBinding(component);
                label.addPropertyChangeListener(new LabelListener());
                registerControl("label", StringProperty.create(label, ""));
                initControls();
                layout = new LayoutBinding(component);
                registerControl("layout", StringProperty.create(layout, ""));
                updateLabel();
            }
            return component;
        } else {
            return null;
        }
    }
    
    protected void initControls() {
        // no op hook
    }

    @Override
    public void parentNotify(Container parent) throws VetoException {
        if (EventQueue.isDispatchThread()) {
            super.parentNotify(parent);
            getSwingContainer();
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
    
    protected void updateLabel() {
        // no op hook
    }
    
    protected String getLabel() {
        return label.getBoundValue();
    }
    
    protected boolean isLabelOnParent() {
        return label.isLabelOnParent();
    }
    
    private class LabelListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent pce) {
            updateLabel();
        }
        
    }

}
