/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 *
 */
package org.praxislive.gui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import org.praxislive.core.ComponentAddress;
import org.praxislive.gui.Keys;
import org.praxislive.gui.impl.AbstractGuiContainer;

/**
 *
 * @author Neil C Smith
 */
public class Tabs extends AbstractGuiContainer {

    private JTabbedPane tabs;
    private LabelListener addressListener;

    @Override
    protected JComponent createSwingContainer() {
        if (tabs == null) {
            tabs = new JTabbedPane();
            tabs.setUI(new UI());
//            tabs.putClientProperty(Keys.LayoutConstraint, "grow, push");
            tabs.addContainerListener(new ChildrenListener());
            tabs.setMinimumSize(new Dimension(50, 20));
        }
        return tabs;
    }

    private void setTabName(JComponent cmp) {
        Object val = cmp.getClientProperty(Keys.Label);
        if (val instanceof String) {
            String label = (String) val;
            if (!label.isEmpty()) {
                setTabName(cmp, label);
                cmp.putClientProperty(Keys.LabelOnParent, true);
                return;
            }
        }
        // not found label
        cmp.putClientProperty(Keys.LabelOnParent, false);
        val = cmp.getClientProperty(Keys.Address);
        if (val instanceof ComponentAddress) {
            ComponentAddress ad = (ComponentAddress) val;
            setTabName(cmp, ad.componentID());
            return;
        }
        // no address???
        setTabName(cmp, cmp.getName());
    }

    private void setTabName(JComponent cmp, String name) {
        int index = tabs.indexOfComponent(cmp);
        if (index > -1) {
            tabs.setTitleAt(index, name);
        }
    }

    private class ChildrenListener implements ContainerListener {

        public void componentAdded(ContainerEvent e) {
            if (addressListener == null) {
                addressListener = new LabelListener();
            }
            Component child = e.getChild();
            child.addPropertyChangeListener(Keys.Label,
                    addressListener);
            if (child instanceof JComponent) {
                setTabName((JComponent) child);
            }
        }

        public void componentRemoved(ContainerEvent e) {
            e.getChild().removePropertyChangeListener(Keys.Label,
                    addressListener);
        }
    }

    private class LabelListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() instanceof JComponent) {
                JComponent cmp = (JComponent) evt.getSource();
                setTabName(cmp);
            }
        }
    }

    private static class UI extends BasicTabbedPaneUI {

        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(8, 8, 8, 8);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            lightHighlight = Utils.mix(tabPane.getBackground(), tabPane.getForeground(), 0.3);
            shadow = Utils.mix(tabPane.getBackground(), tabPane.getForeground(), 0.2);
            super.paint(g, c);
        }

        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            if (isSelected) {
                if (getRolloverTab() == tabIndex) {
                    g.setColor(tabPane.getForeground());
                } else {
                    g.setColor(Utils.mix(tabPane.getBackground(), tabPane.getForeground(), 0.8));
                }
                g.fillRect(x, y, w, h);
            } else {
                g.setColor(Utils.mix(tabPane.getBackground(), tabPane.getForeground(), 0.2));
                if (getRolloverTab() == tabIndex) {
                    g.fillRect(x, y, w, h);
                } else {
                    g.drawRect(x, y, w - 1, h - 1);
                }

            }
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            if (isSelected) {
                g.setColor(tabPane.getBackgroundAt(tabIndex));
            } else {
                g.setColor(tabPane.getForegroundAt(tabIndex));
            }
            int mnemIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);
            BasicGraphicsUtils.drawStringUnderlineCharAt(g, title, mnemIndex,
                    textRect.x, textRect.y + metrics.getAscent());
        }

    }

}
