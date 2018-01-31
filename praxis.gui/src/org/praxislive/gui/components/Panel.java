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
package org.praxislive.gui.components;

import java.awt.Dimension;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.praxislive.gui.impl.AbstractGuiContainer;
import org.praxislive.gui.Keys;

/**
 *
 * @author Neil C Smith
 */
public class Panel extends AbstractGuiContainer {

    private JPanel panel;
    private MigLayout layout;
//    private String labelText;

    @Override
    protected JComponent createSwingContainer() {
        if (panel != null) {
            throw new IllegalStateException();
        }
        layout = new MigLayout("", "[fill]");
        panel = new JPanel(layout);
        panel.addContainerListener(new ChildrenListener());
        panel.setMinimumSize(new Dimension(50,20));
        return panel;
    }

    @Override
    protected void updateLabel() {
        super.updateLabel();
        updateBorder();
    }
    
    private void updateBorder() {
        if (isLabelOnParent()) {
            panel.setBorder(null);
        } else {
            String labelText = getLabel();
            if (labelText.isEmpty()) {
                panel.setBorder(Utils.getBorder());
            } else {
                panel.setBorder(BorderFactory.createTitledBorder(Utils.getBorder(), labelText));
            }
        }
    }


    private void setLayoutConstraint(JComponent child) {
        layout.setComponentConstraints(child, child.getClientProperty(Keys.LayoutConstraint));
        panel.revalidate();
        panel.repaint();
    }

    private class ChildrenListener implements ContainerListener {

        private PropertyChangeListener listener;

        public void componentAdded(ContainerEvent e) {
            if (listener == null) {
                listener = new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getSource() instanceof JComponent) {
                            setLayoutConstraint((JComponent) evt.getSource());
                        }
                    }
                };
            }
            if (e.getChild() instanceof JComponent) {
                JComponent child = (JComponent) e.getChild();
                child.addPropertyChangeListener(
                        Keys.LayoutConstraint, listener);
                setLayoutConstraint(child);
            }
        }

        public void componentRemoved(ContainerEvent e) {
            if (listener == null) {
                return;
            }
            if (e.getChild() instanceof JComponent) {
                ((JComponent) e.getChild()).removePropertyChangeListener(
                        Keys.LayoutConstraint, listener);
            }
        }
    }



}
