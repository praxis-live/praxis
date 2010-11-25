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

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import net.neilcsmith.praxis.gui.AbstractGuiContainer;
import net.neilcsmith.praxis.gui.ClientKeys;

/**
 *
 * @author Neil C Smith
 */
public class Tabs extends AbstractGuiContainer {

    private JTabbedPane tabs;

    @Override
    protected JComponent createSwingContainer() {
        if (tabs == null) {
            tabs = new JTabbedPane();
            tabs.putClientProperty(ClientKeys.LayoutConstraint, "grow, push");
        }
        return tabs;
    }

    @Override
    protected void addToContainer(String id, JComponent component) throws Exception {
        tabs.add(id, component);
    }

    @Override
    protected void removeFromContainer(String id, JComponent component) {
        tabs.remove(component);
    }

}
