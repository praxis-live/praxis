/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Neil C Smith. All rights reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
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
import net.neilcsmith.praxis.core.InvalidChildException;
import net.neilcsmith.praxis.core.ParentVetoException;
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
    public void addChild(String id, Component child) throws InvalidChildException {
        super.addChild(id, child);
        if (child instanceof GuiComponent) {
            try {
                JComponent comp = ((GuiComponent) child).getSwingComponent();
                addToContainer(id, comp);
            } catch (Exception e) {
                super.removeChild(id);
                throw new InvalidChildException();
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
    public void parentNotify(Container parent) throws ParentVetoException {
        if (EventQueue.isDispatchThread()) {
            super.parentNotify(parent);
            // call getSwingComponent() early to ensure JComponent creation
            getSwingComponent();
        } else {
            throw new ParentVetoException("Trying to install GUI component in GUI incompatible container.");

        }

    }

    protected abstract JComponent createSwingContainer();

    protected abstract void addToContainer(String id, JComponent component) throws Exception;

    protected abstract void removeFromContainer(String id, JComponent component);
}
