/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.neilcsmith.praxis.gui.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.ConstraintParser;
import net.miginfocom.layout.IDEUtil;
import net.neilcsmith.praxis.gui.Keys;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class LayoutBinding implements StringProperty.Binding, PropertyChangeListener {
    
    private String layoutString = "";
    private JComponent component;
    
    LayoutBinding(JComponent component) {
        this.component = component;
        component.addPropertyChangeListener(this);
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
