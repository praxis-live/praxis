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
package net.neilcsmith.praxis.gui;

import java.awt.EventQueue;
import javax.swing.JComponent;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.Container;
import net.neilcsmith.praxis.core.VetoException;
import net.neilcsmith.praxis.impl.AbstractContainer;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractGuiContainer extends AbstractContainer implements GuiComponent {

    private JComponent component;

    public final JComponent getSwingComponent() {
        if (EventQueue.isDispatchThread()) {
            if (component == null) {
                component = createSwingContainer();
            }
            return component;
        } else {
            return null;
        }
    }

    @Override
    public void addChild(String id, Component child) throws VetoException {
        super.addChild(id, child);
        if (child instanceof GuiComponent) {
            try {
                JComponent comp = ((GuiComponent) child).getSwingComponent();
                addToContainer(id, comp);
            } catch (Exception e) {
                super.removeChild(id);
                throw new VetoException();
            }
        }
    }

    @Override
    public Component removeChild(String id) {
        Component child = super.removeChild(id);
        if (child instanceof GuiComponent) {
            JComponent comp = ((GuiComponent) child).getSwingComponent();
            removeFromContainer(id, comp);
        }
        return child;
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

    protected abstract JComponent createSwingContainer();

    protected abstract void addToContainer(String id, JComponent component) throws Exception;

    protected abstract void removeFromContainer(String id, JComponent component);
}
