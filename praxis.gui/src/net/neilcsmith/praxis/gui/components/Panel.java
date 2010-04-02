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
package net.neilcsmith.praxis.gui.components;

import java.awt.LayoutManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import net.neilcsmith.praxis.gui.AbstractGuiContainer;
import net.neilcsmith.praxis.gui.ClientKeys;

/**
 *
 * @author Neil C Smith
 */
abstract class Panel extends AbstractGuiContainer {

//    private Box box;
    private JPanel panel;
    private MigLayout layout;
    private boolean vertical;
    private LayoutChangeListener layoutListener;

    protected Panel(boolean vertical) {
        this.vertical = vertical;
    }

    @Override
    protected JComponent createSwingContainer() {
        if (panel != null) {
            throw new IllegalStateException();
        }
        layout = vertical ? new MigLayout("flowy, fill", "[fill]") //, "[grow]")
                : new MigLayout("fill", "[fill]");//, "[grow,fill]", "[grow]");
        panel = new JPanel(layout);
        layoutListener = new LayoutChangeListener(panel);
        panel.putClientProperty(ClientKeys.LayoutConstraint, "grow");
        return panel;
    }

//    @Override
//    protected JComponent createSwingContainer() {
//        if (box == null) {
//            createBox();
//        }
//        return box;
//    }
//
//    private void createBox() {
//        box = vertical ? Box.createVerticalBox() : Box.createHorizontalBox();
//    }
    @Override
    protected void addToContainer(String id, JComponent component) throws Exception {
//        if (box == null) {
//            createBox();
//        }
        if (vertical) {
            component.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        } else {
            component.setAlignmentY(JComponent.TOP_ALIGNMENT);
        }
//        box.add(component);
        Object constraints = component.getClientProperty(ClientKeys.LayoutConstraint);
        panel.add(component, constraints);
        panel.revalidate();
        component.addPropertyChangeListener(ClientKeys.LayoutConstraint, layoutListener);
    }

    @Override
    protected void removeFromContainer(String id, JComponent component) {
//        if (box != null) {
//            box.remove(component);
//        }
        panel.remove(component);
        panel.revalidate();
        component.removePropertyChangeListener(ClientKeys.LayoutConstraint, layoutListener);
    }

    private class LayoutChangeListener implements PropertyChangeListener {

        private JComponent container;

        public LayoutChangeListener(JComponent container) {
            this.container = container;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() instanceof JComponent) {
                JComponent comp = (JComponent) evt.getSource();
                LayoutManager lm = container.getLayout();
                if (lm instanceof MigLayout) {
                    ((MigLayout) lm).setComponentConstraints(comp, evt.getNewValue());
                    container.revalidate();
                }
            }
        }
    }
}
