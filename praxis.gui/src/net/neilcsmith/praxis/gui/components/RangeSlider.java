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
 */
package net.neilcsmith.praxis.gui.components;

import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.gui.*;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.gui.ControlBinding.Adaptor;
import net.neilcsmith.praxis.swing.JRangeSlider;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 *
 * @author Neil C Smith
 */
class RangeSlider extends SingleBindingGuiComponent {

    private static Logger logger = Logger.getLogger(RangeSlider.class.getName());
    private String labelText;
    private Box box;
    private JRangeSlider slider;
    private BoundedRangeAdaptor adaptor;
    private boolean vertical;
    private PNumber prefMin;
    private PNumber prefMax;

    public RangeSlider(boolean vertical) {
        labelText = "";
        this.vertical = vertical;
        registerControl("label", StringProperty.create( new LabelBinding(), labelText));
        registerControl("minimum", ArgumentProperty.create( new MinBinding(), PString.EMPTY));
        registerControl("maximum", ArgumentProperty.create( new MaxBinding(), PString.EMPTY));
    }

    @Override
    protected JComponent createSwingComponent() {
        if (box == null) {
            createComponentAndAdaptor();
        }
        return box;
    }

    @Override
    protected Adaptor getBindingAdaptor() {
        if (adaptor == null) {
            createComponentAndAdaptor();
        }
        return adaptor;
    }

    private void createComponentAndAdaptor() {
        BoundedRangeModel model = new DefaultBoundedRangeModel(0, 500, 0, 500);
        slider = new JRangeSlider(model, vertical ? JRangeSlider.VERTICAL : JRangeSlider.HORIZONTAL,
                JRangeSlider.LEFTRIGHT_TOPBOTTOM);
        adaptor = new BoundedRangeAdaptor(model);
        if (prefMin != null) {
            adaptor.setPreferredMinimum(prefMin);
        }
        if (prefMax != null) {
            adaptor.setPreferredMaximum(prefMax);
        }
        
        slider.addAncestorListener(new AncestorListener() {

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
        box = vertical ? Box.createVerticalBox() : Box.createHorizontalBox();
        box.add(slider);
        setBorders();

    }

    private void setBorders() {
//        if (labelText.length() > 0) {
            box.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), labelText));
//        } else {
//            box.setBorder(BorderFactory.createEtchedBorder());
//        }
    }

    private class LabelBinding implements StringProperty.Binding {

        public void setBoundValue(long time, String value) {
            labelText = value;
            if (box != null) {
                setBorders();
                box.revalidate();
            }
        }

        public String getBoundValue() {
            return labelText;
        }
    }

    private class MinBinding implements ArgumentProperty.Binding {

        public void setBoundValue(long time, Argument value) {
            if (value.isEmpty()) {
                prefMin = null;
            } else {
                try {
                    prefMin = PNumber.coerce(value);
                } catch (Exception ex) {
                    prefMin = null;
                }
            }
            if (adaptor != null) {
                adaptor.setPreferredMinimum(prefMin);
            }
        }

        public Argument getBoundValue() {
            if (prefMin == null) {
                return PString.EMPTY;
            } else {
                return prefMin;
            }
        }

    }

    private class MaxBinding implements ArgumentProperty.Binding {

        public void setBoundValue(long time, Argument value) {
            if (value.isEmpty()) {
                prefMax = null;
            } else {
                try {
                    prefMax = PNumber.coerce(value);
                } catch (Exception ex) {
                    prefMax = null;
                }
            }
            if (adaptor != null) {
                adaptor.setPreferredMaximum(prefMax);
            }
        }

        public Argument getBoundValue() {
            if (prefMax == null) {
                return PString.EMPTY;
            } else {
                return prefMax;
            }
        }

    }


}
