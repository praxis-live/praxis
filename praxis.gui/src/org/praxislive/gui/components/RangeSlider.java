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
 */
package org.praxislive.gui.components;

import java.util.logging.Logger;
import org.praxislive.core.Value;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PString;
import org.praxislive.impl.swing.BindingContext;
import org.praxislive.gui.impl.AbstractGuiComponent;
import org.praxislive.gui.impl.BoundedValueAdaptor;
import org.praxislive.impl.ArgumentProperty;

/**
 *
 * @author Neil C Smith
 */
class RangeSlider extends AbstractGuiComponent {

    private static Logger logger = Logger.getLogger(RangeSlider.class.getName());
    
    private final boolean vertical;
    
    private BindingContext bindingContext;
    private String labelText;
    private Box box;
    private JRangeSlider slider;
    private BoundedValueAdaptor lowAdaptor;
    private BoundedValueAdaptor highAdaptor;
    private ControlAddress lowBinding;
    private ControlAddress highBinding;
    private ModelConverter converter;
    
    private PNumber prefMin;
    private PNumber prefMax;

    public RangeSlider(boolean vertical) {
        labelText = "";
        this.vertical = vertical;
    }

    @Override
    protected void initControls() {
        super.initControls();

        ArgumentInfo bindingInfo = ArgumentInfo.create(ControlAddress.class, PMap.create(ArgumentInfo.KEY_ALLOW_EMPTY, true));
        registerControl("binding-low", ArgumentProperty.create(bindingInfo, new AddressBinding(false), PString.EMPTY));
        registerControl("binding-high", ArgumentProperty.create(bindingInfo, new AddressBinding(true), PString.EMPTY));

        ArgumentInfo info = ArgumentInfo.create(Value.class,
                PMap.create(ArgumentInfo.KEY_ALLOW_EMPTY, true, ArgumentInfo.KEY_EMPTY_IS_DEFAULT, true));
        registerControl("minimum", ArgumentProperty.create(info, new MinBinding(), PString.EMPTY));
        registerControl("maximum", ArgumentProperty.create(info, new MaxBinding(), PString.EMPTY));
    }

    @Override
    protected JComponent createSwingComponent() {
        if (box == null) {
            createComponentAndAdaptors();
        }
        return box;
    }
    
        @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        BindingContext ctxt = getLookup().find(BindingContext.class).orElse(null);
        if (bindingContext != ctxt) {
            if (bindingContext != null) {
                if (lowBinding != null) {
                    bindingContext.unbind(lowAdaptor);
                }
                if (highBinding != null) {
                    bindingContext.unbind(highAdaptor);
                }
            }
            if (ctxt != null) {
                if (lowBinding != null) {
                    ctxt.bind(lowBinding, lowAdaptor);
                }
                if (highBinding != null) {
                    ctxt.bind(highBinding, highAdaptor);
                }
            }
            bindingContext = ctxt;
        }
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

    private void createComponentAndAdaptors() {
        BoundedRangeModel rangeModel = new DefaultBoundedRangeModel(0, 500, 0, 500);
        slider = new JRangeSlider(rangeModel, vertical ? JRangeSlider.VERTICAL : JRangeSlider.HORIZONTAL,
                JRangeSlider.LEFTRIGHT_TOPBOTTOM);
        BoundedRangeModel lowModel = new DefaultBoundedRangeModel(0, 0, 0, 500);
        BoundedRangeModel highModel = new DefaultBoundedRangeModel(500, 0, 0, 500);
        lowAdaptor = new BoundedValueAdaptor(lowModel);
        highAdaptor = new BoundedValueAdaptor(highModel);
        
        converter = new ModelConverter(rangeModel, lowModel, highModel);
        
        slider.addAncestorListener(new AncestorListener() {

            public void ancestorAdded(AncestorEvent event) {
                lowAdaptor.setActive(true);
                highAdaptor.setActive(true);
            }

            public void ancestorRemoved(AncestorEvent event) {
                lowAdaptor.setActive(false);
                highAdaptor.setActive(false);
            }

            public void ancestorMoved(AncestorEvent event) {
                // no op
            }
        });
        box = vertical ? Box.createVerticalBox() : Box.createHorizontalBox();
        box.add(slider);
//        setBorders();
        updateBorders();

    }

    private void updateAdaptors() {
        if (lowAdaptor != null && highAdaptor != null) {
            lowAdaptor.setPreferredMinimum(prefMin);
            lowAdaptor.setPreferredMaximum(prefMax);
            highAdaptor.setPreferredMinimum(prefMin);
            highAdaptor.setPreferredMaximum(prefMax);
        }
    }

    private void updateBorders() {
        if (box != null) {
            Border etched = Utils.getBorder();
            if (labelText.isEmpty()) {
                box.setBorder(etched);
            } else {
                box.setBorder(BorderFactory.createTitledBorder(
                        etched, labelText));
            }
            box.revalidate();
        }

    }

    private class MinBinding implements ArgumentProperty.Binding {

        public void setBoundValue(long time, Value value) {
            if (value.isEmpty()) {
                prefMin = null;
            } else {
                try {
                    prefMin = PNumber.coerce(value);
                } catch (Exception ex) {
                    prefMin = null;
                }
            }
            updateAdaptors();
        }

        public Value getBoundValue() {
            if (prefMin == null) {
                return PString.EMPTY;
            } else {
                return prefMin;
            }
        }
    }

    private class MaxBinding implements ArgumentProperty.Binding {

        public void setBoundValue(long time, Value value) {
            if (value.isEmpty()) {
                prefMax = null;
            } else {
                try {
                    prefMax = PNumber.coerce(value);
                } catch (Exception ex) {
                    prefMax = null;
                }
            }
            updateAdaptors();
        }

        public Value getBoundValue() {
            if (prefMax == null) {
                return PString.EMPTY;
            } else {
                return prefMax;
            }
        }
    }
    
    private class AddressBinding implements ArgumentProperty.Binding {

        final boolean high;
        
        AddressBinding(boolean high) {
            this.high = high;
        }
        
        @Override
        public void setBoundValue(long time, Value value) throws Exception {
            if (lowAdaptor == null) {
                createComponentAndAdaptors();
            }
            if (bindingContext != null) {
                bindingContext.unbind(high ? highAdaptor : lowAdaptor);
                if (value.isEmpty()) {
                    if (high) {
                        highBinding = null;
                    } else {
                        lowBinding = null;
                    }
                } else {
                    if (high) {
                        highBinding = ControlAddress.from(value).get();
                        bindingContext.bind(highBinding, highAdaptor);
                    } else {
                        lowBinding = ControlAddress.from(value).get();
                        bindingContext.bind(lowBinding, lowAdaptor);
                    }
                }
                
            }
        }

        @Override
        public Value getBoundValue() {
            ControlAddress ret = high ? highBinding : lowBinding;
            return ret == null ? PString.EMPTY : ret;
        }
        
    }
    
    private class ModelConverter implements ChangeListener {
        
        private final BoundedRangeModel rangeModel;
        private final BoundedRangeModel lowModel;
        private final BoundedRangeModel highModel;
        
        private boolean updating;
        
        private ModelConverter(BoundedRangeModel rangeModel,
                BoundedRangeModel lowModel,
                BoundedRangeModel highModel) {
            this.rangeModel = rangeModel;
            this.lowModel = lowModel;
            this.highModel = highModel;
            rangeModel.addChangeListener(this);
            lowModel.addChangeListener(this);
            highModel.addChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (updating) {
                return;
            }
            updating = true;
            
            if (e.getSource() == rangeModel) {
                lowModel.setValueIsAdjusting(rangeModel.getValueIsAdjusting());
                highModel.setValueIsAdjusting(rangeModel.getValueIsAdjusting());
                lowModel.setValue(rangeModel.getValue());
                highModel.setValue(rangeModel.getValue() + rangeModel.getExtent());
            } else {
                int low = lowModel.getValue();
                int high = highModel.getValue();
                int ext = high - low;
                ext = ext < 0 ? 0 : ext;
                rangeModel.setRangeProperties(low, ext, 0, 500, false);
            }
            
            updating = false;
        }
        
        
    }
    
    
}
