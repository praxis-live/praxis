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

import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.ConstraintParser;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import net.neilcsmith.praxis.gui.impl.AbstractGuiContainer;
import net.neilcsmith.praxis.gui.Keys;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 *
 * @author Neil C Smith
 */
public class Panel extends AbstractGuiContainer {

    private JPanel panel;
    private MigLayout layout;


    public Panel() {
        registerControl("setup", StringProperty.create(new SetupBinding(), "fill"));
        registerControl("rows", StringProperty.create(new AxisBinding(false), "[fill]"));
        registerControl("columns", StringProperty.create(new AxisBinding(true), "[fill]"));
    }

    @Override
    protected JComponent createSwingContainer() {
        if (panel != null) {
            throw new IllegalStateException();
        }
//        layout = vertical ? new MigLayout("flowy, fill", "[fill]") //, "[grow]")
//                : new MigLayout("fill", "[fill]");//, "[grow,fill]", "[grow]");
        layout = new MigLayout("fill", "[fill]");
        panel = new JPanel(layout);
        panel.addContainerListener(new ChildrenListener());
//        panel.putClientProperty(Keys.LayoutConstraint, "grow");
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
//    @Override
//    protected void addToContainer(String id, JComponent component) throws Exception {
////        if (box == null) {
////            createBox();
////        }
//        if (vertical) {
//            component.setAlignmentX(JComponent.LEFT_ALIGNMENT);
//        } else {
//            component.setAlignmentY(JComponent.TOP_ALIGNMENT);
//        }
////        box.add(component);
//        Object constraints = component.getClientProperty(Keys.LayoutConstraint);
//        panel.add(component, constraints);
//        panel.revalidate();
//        component.addPropertyChangeListener(Keys.LayoutConstraint, layoutListener);
//    }
//
//    @Override
//    protected void removeFromContainer(String id, JComponent component) {
////        if (box != null) {
////            box.remove(component);
////        }
//        panel.remove(component);
//        panel.revalidate();
//        component.removePropertyChangeListener(Keys.LayoutConstraint, layoutListener);
//    }
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


    private class SetupBinding implements StringProperty.Binding {

        private String consString = "";
        private LC constraint = null;

        public void setBoundValue(long time, String value) {
            if (layout == null) {
                throw new IllegalStateException("Layout not yet initialised");
            }
            constraint = ConstraintParser.parseLayoutConstraint(value);
            layout.setLayoutConstraints(constraint);
            panel.revalidate();
            panel.repaint();
            consString = value;
        }

        public String getBoundValue() {
            return consString;
        }

    }

    private class AxisBinding implements StringProperty.Binding {

        private final boolean column;
        private String consString = "";
        private AC constraint = null;

        private AxisBinding(boolean column) {
            this.column = column;
        }

        public void setBoundValue(long time, String value) {
            if (layout == null) {
                throw new IllegalStateException("Layout not yet initialised");
            }
            if (column) {
                constraint = ConstraintParser.parseColumnConstraints(value);
                layout.setColumnConstraints(constraint);
            } else {
                constraint = ConstraintParser.parseRowConstraints(value);
                layout.setRowConstraints(constraint);
            }
            panel.revalidate();
            panel.repaint();
            consString = value;
        }

        public String getBoundValue() {
            return consString;
        }

    }

}
