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
package net.neilcsmith.praxis.gui.impl;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import net.neilcsmith.praxis.core.Container;
import net.neilcsmith.praxis.core.VetoException;
import net.neilcsmith.praxis.gui.Keys;
import net.neilcsmith.praxis.gui.GuiContext;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 * Abstract superclass for default GuiComponent model.
 * @author Neil C Smith
 */
public abstract class AbstractGuiComponent extends AbstractComponent {

    private JComponent component;
    private LabelBinding label;
    private LayoutBinding layout;
    private GuiContext context;

    protected AbstractGuiComponent() {
    }

    public final JComponent getSwingComponent() {
        if (EventQueue.isDispatchThread()) {
            if (component == null) {
                component = createSwingComponent();
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
            // call getSwingComponent() early to ensure JComponent creation
            getSwingComponent();
        } else {
            throw new VetoException("Trying to install GUI component in GUI incompatible container.");
        }

    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
//        getSwingComponent().putClientProperty(Keys.Address, getAddress());
        GuiContext ctxt = getLookup().get(GuiContext.class);
        if (context != ctxt) {
            if (context != null) {
                context.getContainer().remove(getSwingComponent());
            }
            if (ctxt != null) {
                ctxt.getContainer().add(getSwingComponent());
            }
            context = ctxt;
        }
        getSwingComponent().putClientProperty(Keys.Address, getAddress());
    }

    protected abstract JComponent createSwingComponent();
    
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
