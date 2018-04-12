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
package org.praxislive.gui.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.ConstraintParser;
import net.miginfocom.layout.IDEUtil;
import org.praxislive.gui.Keys;
import org.praxislive.impl.StringProperty;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class LayoutBinding implements StringProperty.Binding, PropertyChangeListener {
    
    private String layoutString = "";
    private JComponent component;
    
    LayoutBinding(JComponent component) {
        this.component = component;
        component.addPropertyChangeListener(Keys.LayoutConstraint, this);
    }
    
    public void setBoundValue(long time, String value) {
        CC constraint;
        if (value.isEmpty()) {
            constraint = null;
        } else {
            constraint = ConstraintParser.parseComponentConstraint(value);
        }
        component.putClientProperty(Keys.LayoutConstraint, constraint);
        layoutString = value;
    }

    public String getBoundValue() {
        if (layoutString == null) {
            try {
                CC constraint = (CC) component.getClientProperty(Keys.LayoutConstraint);
                layoutString = IDEUtil.getConstraintString(constraint, false);
            } catch (Exception ex) {
                layoutString = "";
            }
        }
        return layoutString;
    }

    public void propertyChange(PropertyChangeEvent pce) {
        layoutString = null;
    }
    
}
