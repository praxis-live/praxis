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
package net.neilcsmith.praxis.gui.components;

import java.awt.Component;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.gui.Keys;
import net.neilcsmith.praxis.gui.impl.AbstractGuiContainer;

/**
 *
 * @author Neil C Smith
 */
public class Tabs extends AbstractGuiContainer {

    private JTabbedPane tabs;
    private AddressListener addressListener;

    @Override
    protected JComponent createSwingContainer() {
        if (tabs == null) {
            tabs = new JTabbedPane();
            tabs.putClientProperty(Keys.LayoutConstraint, "grow, push");
            tabs.addContainerListener(new ChildrenListener());

        }
        return tabs;
    }

    private void setTabName(JComponent cmp) {
        Object val = cmp.getClientProperty(Keys.Address);
        if (val instanceof ComponentAddress) {
            ComponentAddress ad = (ComponentAddress) val;
            int index = tabs.indexOfComponent(cmp);
            if (index > -1) {
                tabs.setTitleAt(index, ad.getComponentID(ad.getDepth() - 1));
            }
        }
    }

    private class ChildrenListener implements ContainerListener {

        public void componentAdded(ContainerEvent e) {
            if (addressListener == null) {
                addressListener = new AddressListener();
            }
            Component child = e.getChild();
            child.addPropertyChangeListener(Keys.Address,
                    addressListener);
            if (child instanceof JComponent) {
                setTabName((JComponent) child);
            }
        }

        public void componentRemoved(ContainerEvent e) {
            e.getChild().removePropertyChangeListener(Keys.Address,
                    addressListener);
        }
    }

    private class AddressListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() instanceof JComponent) {
                JComponent cmp = (JComponent) evt.getSource();
                setTabName(cmp);
            }
        }
    }
}
