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
 */
package net.neilcsmith.praxis.gui.components;

import net.neilcsmith.praxis.gui.impl.SingleBindingGuiComponent;
import net.neilcsmith.praxis.gui.impl.BoundedValueAdaptor;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.gui.ControlBinding.Adaptor;
import net.neilcsmith.praxis.impl.ArgumentProperty;

/**
 *
 * @author Neil C Smith
 */
class Slider extends SingleBindingGuiComponent {

    private static final Logger LOG = Logger.getLogger(Slider.class.getName());
    private String labelText;
    private Box box;
    private JSlider slider;
    private BoundedValueAdaptor adaptor;
    private boolean vertical;
    private PNumber prefMin;
    private PNumber prefMax;
    private PString prefScale;

    public Slider(boolean vertical) {
        labelText = "";
        this.vertical = vertical;
//        registerControl("label", StringProperty.create( new LabelBinding(), labelText));
//        registerControl("minimum", ArgumentProperty.create( new MinBinding(), PString.EMPTY));
//        registerControl("maximum", ArgumentProperty.create( new MaxBinding(), PString.EMPTY));
//        registerControl("scale", ArgumentProperty.create( new ScaleBinding(), PString.EMPTY));
    }

    @Override
    protected void initControls() {
        super.initControls();
        ArgumentInfo info = ArgumentInfo.create(Argument.class,
                PMap.create(ArgumentInfo.KEY_ALLOW_EMPTY, true, ArgumentInfo.KEY_EMPTY_IS_DEFAULT, true));
        registerControl("minimum", ArgumentProperty.create(info, new MinBinding(), PString.EMPTY));
        registerControl("maximum", ArgumentProperty.create(info, new MaxBinding(), PString.EMPTY));
        info = ArgumentInfo.create(PString.class, PMap.create(ArgumentInfo.KEY_EMPTY_IS_DEFAULT, true));
        registerControl("scale", ArgumentProperty.create(info, new ScaleBinding(), PString.EMPTY));
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
        slider = new JSlider(vertical ? JSlider.VERTICAL : JSlider.HORIZONTAL, 0, 500, 0);
        slider.putClientProperty("JSlider.isFilled", Boolean.TRUE);
        adaptor = new BoundedValueAdaptor(slider.getModel());
        if (prefMin != null) {
            adaptor.setPreferredMinimum(prefMin);
        }
        if (prefMax != null) {
            adaptor.setPreferredMaximum(prefMax);
        }
        if (prefScale != null) {
            adaptor.setPreferredScale(prefScale);
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
        updateBorders();

    }

    private void updateBorders() {
        if (box != null) {
            Border etched = Utils.getBorder();
//                    BorderFactory.createEtchedBorder(slider.getBackground().brighter().brighter(),
//                    slider.getBackground().brighter());
            if (labelText.isEmpty()) {
                box.setBorder(etched);
            } else {
                box.setBorder(BorderFactory.createTitledBorder(
                    etched, labelText));
            }
            box.revalidate();
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
    
    



//    private class LabelBinding implements StringProperty.Binding {
//
//        public void setBoundValue(long time, String value) {
//            labelText = value;
//            if (box != null) {
//                updateBorders();
//            }
//        }
//
//        public String getBoundValue() {
//            return labelText;
//        }
//    }

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

    private class ScaleBinding implements ArgumentProperty.Binding {

        public void setBoundValue(long time, Argument value) {
            if (value.isEmpty()) {
                prefScale = null;
            } else {
                if (value instanceof PString) {
                    prefScale = (PString) value;
                } else {
                    prefScale = PString.valueOf(value);
                }
            }
            if (adaptor != null) {
                adaptor.setPreferredScale(prefScale);
            }
        }

        public Argument getBoundValue() {
            if (prefScale == null) {
                return PString.EMPTY;
            } else {
                return prefScale;
            }
        }

    }


}
