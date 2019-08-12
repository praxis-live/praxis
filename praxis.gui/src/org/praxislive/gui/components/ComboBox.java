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
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.praxislive.core.Value;
import org.praxislive.core.ValueFormatException;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PString;
import org.praxislive.impl.swing.ControlBinding;
import org.praxislive.gui.impl.SingleBindingGuiComponent;
import org.praxislive.impl.ArrayProperty;

/**
 *
 * @author Neil C Smith
 */
public class ComboBox extends SingleBindingGuiComponent {

    private final static Logger LOG = Logger.getLogger(ComboBox.class.getName());
    private PArray userValues = PArray.EMPTY;
    private Value current = PString.EMPTY;
    private ArgumentInfo boundInfo;
    private List<Value> values;
    private Value temp;
    private Box container;
    private JComboBox combo;
    private DefaultComboBoxModel model;
    private boolean isUpdating;
    private Adaptor adaptor;
    private String labelText = "";

    public ComboBox() {
        values = new ArrayList<Value>();
    }

    @Override
    protected void initControls() {
        super.initControls();
        registerControl("values", ArrayProperty.create(new ValuesBinding(), userValues));
    }

    @Override
    protected ControlBinding.Adaptor getBindingAdaptor() {
        if (adaptor == null) {
            createComponentAndAdaptor();
        }
        return adaptor;
    }

    @Override
    protected JComponent createSwingComponent() {
        if (container == null) {
            createComponentAndAdaptor();
        }
        return container;
    }

    private void createComponentAndAdaptor() {
        model = new DefaultComboBoxModel();
        combo = new JComboBox(model);
        combo.setBorder(new ComboBorder());
        combo.putClientProperty("JComboBox.isTableCellEditor", true);
        adaptor = new Adaptor();
        adaptor.setSyncRate(ControlBinding.SyncRate.Medium);
        combo.addActionListener(adaptor);
        container = Box.createHorizontalBox();
        container.add(combo);
        combo.addAncestorListener(new AncestorListener() {

            public void ancestorAdded(AncestorEvent event) {
                adaptor.setActive(true);
            }

            public void ancestorRemoved(AncestorEvent event) {
                adaptor.setActive(false);
            }

            public void ancestorMoved(AncestorEvent event) {
                // no op
            }
        });
        updateBorders();
    }

    @Override
    protected void updateLabel() {
        super.updateLabel();
        if (isLabelOnParent()) {
            labelText = "";
        } else {
            labelText = getLabel();
        }
        updateBorders();
    }

    private void updateBorders() {
        if (container != null) {
            if (labelText.isEmpty()) {
                container.setBorder(BorderFactory.createEmptyBorder());
            } else {
                container.setBorder(BorderFactory.createTitledBorder(labelText));
            }
            container.revalidate();
        }
    }

    private void updateCurrent(Value current) {
        LOG.log(Level.FINEST, "Update current : {0}", current);
        if (Utils.equivalent(this.current, current)) {
            LOG.finest("Current is equivalent, returning.");
            return;
        }
        this.current = current;
        updateSelection();
    }

    private void updateSelection() {
        LOG.finest("Updating selection");
        isUpdating = true;
        if (temp != null && current != temp) {
            model.removeElement(temp);
            temp = null;
        }
        int idx = -1;
        for (int i = 0, count = model.getSize(); i < count; i++) {
            Object o = model.getElementAt(i);
            if (o instanceof Value
                    && Utils.equivalent(current, (Value) o)) {
                idx = i;
                LOG.log(Level.FINEST, "Found current in model at position : {0}", idx);
                break;
            }
        }
        if (idx == -1) {
            // not found
            temp = current;
            model.addElement(temp);
            model.setSelectedItem(temp);
        } else {
            model.setSelectedItem(model.getElementAt(idx));
        }
        LOG.log(Level.FINEST, "Combobox selection changed to : {0}", combo.getSelectedItem());
        isUpdating = false;
    }

    private void updateModel() {
        LOG.finest("Updating model");
        isUpdating = true;
        model.removeAllElements();
        temp = null;
        for (Value value : values) {
            LOG.log(Level.FINEST, "Adding to model from values : {0}", value);
            model.addElement(value);
        }
        isUpdating = false;
    }

    private void updateValues() {
        values.clear();
        boolean intersect = false;
        PArray infoValues = PArray.EMPTY;
        if (boundInfo != null) {
            Value p = boundInfo.properties().get(ArgumentInfo.KEY_ALLOWED_VALUES);
            if (p == null) {
                p = boundInfo.properties().get(ArgumentInfo.KEY_SUGGESTED_VALUES);
            } else {
                LOG.log(Level.FINEST, "Found allowed-values : {0}", p);
                intersect = true;
            }
            if (p != null) {
                try {
                    infoValues = PArray.coerce(p);
                } catch (ValueFormatException ex) {/*
                     * fall through
                     */

                }
            }
        }
        if (userValues.isEmpty()) {
            for (Value value : infoValues) {
                LOG.log(Level.FINEST, "Adding to values : {0}", value);
                values.add(value);
            }
        } else {
            for (Value value : userValues) {
                if (intersect) {
                    for (Value allowed : infoValues) {
                        if (Utils.equivalent(allowed, value)) {
                            LOG.log(Level.FINEST, "Adding to values : {0}", value);
                            values.add(value);
                            break;
                        }
                    }
                } else {
                    LOG.log(Level.FINEST, "Adding to values : {0}", value);
                    values.add(value);
                }
            }
        }
    }

    private class ComboBorder implements Border {

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            if (c.hasFocus()) {
                g.setColor(Utils.mix(c.getBackground(), c.getForeground(), 0.8));
            } else {
                g.setColor(Utils.mix(c.getBackground(), c.getForeground(), 0.6));
            }
            g.drawRect(x, y, width - 1, height - 1);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 4, 4, 4);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
        
    }
    
    private class ValuesBinding implements ArrayProperty.Binding {

        public void setBoundValue(long time, PArray value) {
            userValues = value;
            updateValues();
            updateModel();
            updateSelection();
        }

        public PArray getBoundValue() {
            return userValues;
        }
    }

    private class Adaptor extends ControlBinding.Adaptor implements ActionListener {

        @Override
        public void update() {
            LOG.finest("Binding update called");
            ControlBinding binding = getBinding();
            if (binding != null) {
                CallArguments curArgs = binding.getArguments();
                if (curArgs.getSize() > 0) {
                    updateCurrent(curArgs.get(0));
                }
            }
        }

        @Override
        public void updateBindingConfiguration() {
            LOG.finest("Binding configuration update called");
            boundInfo = null;
            ControlBinding binding = getBinding();
            if (binding != null) {
                ControlInfo cin = binding.getBindingInfo();
                if (cin != null) {
                    ArgumentInfo[] ain = binding.getBindingInfo().getInputsInfo();
                    if (ain.length > 0) {
                        boundInfo = ain[0];
                    }
                }
            }
            updateValues();
            updateModel();
            updateSelection();
        }

        public void actionPerformed(ActionEvent e) {
            LOG.finest("ComboBox actionPerformed called");
            if (isUpdating) {
                LOG.finest("isUpdating - returning");
                return;
            }
            Object obj = combo.getSelectedItem();
            Value arg;
            if (obj instanceof Value) {
                arg = (Value) obj;
                send(CallArguments.create(arg));
                updateCurrent(current);
            } else if (obj != null) {
                arg = PString.of(obj);
                send(CallArguments.create(arg));
                updateCurrent(current);
            }

        }
    }
}
