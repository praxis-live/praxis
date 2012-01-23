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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import net.neilcsmith.praxis.gui.Keys;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class LabelBinding implements StringProperty.Binding {

    private String label;
    private JComponent component;
    private boolean ignoreChange;
    private ComponentListener cl;
    private PropertyChangeSupport pcs;

    LabelBinding(JComponent component) {
        this.component = component;
        cl = new ComponentListener();
        component.addPropertyChangeListener(Keys.Label, cl);
        component.addPropertyChangeListener(Keys.LabelOnParent, cl);
        pcs = new PropertyChangeSupport(this);
    }

    public void setBoundValue(long time, String value) {
        label = value;
        ignoreChange = true;
        component.putClientProperty(Keys.Label, value);
        ignoreChange = false;
    }

    public String getBoundValue() {
        if (label == null) {
            Object o = component.getClientProperty(Keys.Label);
            if (o instanceof String) {
                label = (String) o;
            } else {
                label = "";
            }
        }
        return label;
    }
    
    boolean isLabelOnParent() {
        Object o = component.getClientProperty(Keys.LabelOnParent);
        if (o instanceof Boolean) {
            return ((Boolean)o).booleanValue();
        }
        return false;
    }

    void addPropertyChangeListener(PropertyChangeListener pl) {
        pcs.addPropertyChangeListener(pl);
    }
    
    void removePropertyChangeListener(PropertyChangeListener pl) {
        pcs.removePropertyChangeListener(pl);
    }
    
    
    

    private class ComponentListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent pce) {
            if (!ignoreChange) {
                label = null;
            }
            pcs.firePropertyChange(pce.getPropertyName(), pce.getOldValue(), pce.getNewValue());
        }
    }
}
