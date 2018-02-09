/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Neil C Smith.
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
package org.praxislive.gui.components;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.praxislive.core.Value;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.ValueFormatException;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PString;
import org.praxislive.gui.impl.AbstractGuiComponent;
import org.praxislive.gui.impl.BoundedValueAdaptor;
import org.praxislive.gui.BindingContext;
import org.praxislive.impl.ArgumentProperty;
import org.praxislive.impl.StringProperty;

/**
 *
 * @author Neil C Smith
 */
public class XYController extends AbstractGuiComponent {

    private static Logger logger = Logger.getLogger(XYController.class.getName());
    private BindingContext bindingContext;
    private Box container;
    private JXYController controller;
    private BoundedValueAdaptor xAdaptor;
    private BoundedValueAdaptor yAdaptor;
    private ControlAddress xBinding;
    private ControlAddress yBinding;
    private Preferences xPrefs;
    private Preferences yPrefs;
    private String labelText;

    public XYController() {
        xPrefs = new Preferences();
        yPrefs = new Preferences();
        labelText = "";
//        registerControl("label", StringProperty.create(new LabelBinding(), labelText));
//        registerControl("binding-x", ArgumentProperty.create(new XAddressBinding(), PString.EMPTY));
//        registerControl("binding-y", ArgumentProperty.create(new YAddressBinding(), PString.EMPTY));
//        registerControl("minimum-x", ArgumentProperty.create(new MinBinding(xPrefs), PString.EMPTY));
//        registerControl("minimum-y", ArgumentProperty.create(new MinBinding(yPrefs), PString.EMPTY));
//        registerControl("maximum-x", ArgumentProperty.create(new MaxBinding(xPrefs), PString.EMPTY));
//        registerControl("maximum-y", ArgumentProperty.create(new MaxBinding(yPrefs), PString.EMPTY));
//        registerControl("scale-x", ArgumentProperty.create(new ScaleBinding(xPrefs), PString.EMPTY));
//        registerControl("scale-y", ArgumentProperty.create(new ScaleBinding(yPrefs), PString.EMPTY));
    }

    @Override
    protected void initControls() {
        super.initControls();
        ArgumentInfo bindingInfo = ArgumentInfo.create(ControlAddress.class, PMap.create(ArgumentInfo.KEY_ALLOW_EMPTY, true));
        registerControl("binding-x", ArgumentProperty.create(bindingInfo, new XAddressBinding(), PString.EMPTY));
        registerControl("binding-y", ArgumentProperty.create(bindingInfo, new YAddressBinding(), PString.EMPTY));
        
        ArgumentInfo info = ArgumentInfo.create(Value.class,
                PMap.create(ArgumentInfo.KEY_ALLOW_EMPTY, true, ArgumentInfo.KEY_EMPTY_IS_DEFAULT, true));
        registerControl("minimum-x", ArgumentProperty.create(info, new MinBinding(xPrefs), PString.EMPTY));
        registerControl("minimum-y", ArgumentProperty.create(info, new MinBinding(yPrefs), PString.EMPTY));
        registerControl("maximum-x", ArgumentProperty.create(info, new MaxBinding(xPrefs), PString.EMPTY));
        registerControl("maximum-y", ArgumentProperty.create(info, new MaxBinding(yPrefs), PString.EMPTY));
        
//        info = ArgumentInfo.create(PString.class, PMap.create(ArgumentInfo.KEY_EMPTY_IS_DEFAULT, true));
//        registerControl("scale-x", ArgumentProperty.create(info, new ScaleBinding(xPrefs), PString.EMPTY));
//        registerControl("scale-y", ArgumentProperty.create(info, new ScaleBinding(yPrefs), PString.EMPTY));
    }
    
    

    @Override
    protected JComponent createSwingComponent() {
        if (container == null) {
            createComponentAndAdaptors();
        }
        return container;
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
        container = Box.createHorizontalBox();
        controller = new JXYController();
        controller.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        controller.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        xAdaptor = new BoundedValueAdaptor(controller.getXRangeModel());
        yAdaptor = new BoundedValueAdaptor(controller.getYRangeModel());
        controller.addAncestorListener(new AncestorAdaptor());
        updateAdaptors();
        updateBorders();
        container.add(controller);
    }

    private void updateAdaptors() {
        if (xAdaptor != null && yAdaptor != null) {
            xAdaptor.setPreferredMinimum(xPrefs.minimum);
            xAdaptor.setPreferredMaximum(xPrefs.maximum);
//            xAdaptor.setPreferredScale(xPrefs.scale);
            yAdaptor.setPreferredMinimum(yPrefs.minimum);
            yAdaptor.setPreferredMaximum(yPrefs.maximum);
//            yAdaptor.setPreferredScale(yPrefs.scale);
        }
    }

    private void updateBorders() {
        if (container != null) {
            if (labelText.isEmpty()) {
                container.setBorder(Utils.getBorder());
            } else {
                container.setBorder(BorderFactory.createTitledBorder(
                        Utils.getBorder(), labelText));
            }
            container.revalidate();
        }
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
//        Root r = getRoot();
//        if (r instanceof BindingContext) {
//            ctxt = (BindingContext) r;
//        } else {
//            if (xBinding != null) {
//                ctxt.unbind(xAdaptor);
//                xBinding = null;
//            }
//            if (yBinding != null) {
//                ctxt.unbind(yAdaptor);
//                yBinding = null;
//            }
//            ctxt = null;
//        }
        BindingContext ctxt = getLookup().get(BindingContext.class);
        if (bindingContext != ctxt) {
            if (bindingContext != null) {
                if (xBinding != null) {
                    bindingContext.unbind(xAdaptor);
                }
                if (yBinding != null) {
                    bindingContext.unbind(yAdaptor);
                }
            }
            if (ctxt != null) {
                if (xBinding != null) {
                    ctxt.bind(xBinding, xAdaptor);
                }
                if (yBinding != null) {
                    ctxt.bind(yBinding, yAdaptor);
                }
            }
            bindingContext = ctxt;
        }
    }

    private class AncestorAdaptor implements AncestorListener {

        public void ancestorAdded(AncestorEvent event) {
            xAdaptor.setActive(true);
            yAdaptor.setActive(true);
        }

        public void ancestorRemoved(AncestorEvent event) {
            xAdaptor.setActive(false);
            yAdaptor.setActive(false);
        }

        public void ancestorMoved(AncestorEvent event) {
            // no op
        }
    }

    private class Preferences {

        PNumber minimum;
        PNumber maximum;
        PString scale;
    }

    private class LabelBinding implements StringProperty.Binding {

        public void setBoundValue(long time, String value) {
            labelText = value;
            updateBorders();
        }

        public String getBoundValue() {
            return labelText;
        }
    }

    private class XAddressBinding implements ArgumentProperty.Binding {

        public void setBoundValue(long time, Value value) {
            if (xAdaptor == null) {
                createComponentAndAdaptors();
            }
            if (bindingContext != null) {
                bindingContext.unbind(xAdaptor);
                if (value.isEmpty()) {
                    xBinding = null;
                } else {
                    try {
                        xBinding = ControlAddress.coerce(value);
                        bindingContext.bind(xBinding, xAdaptor);
                    } catch (ValueFormatException ex) {
                        logger.log(Level.WARNING, "Could not create binding-x", ex);
                        xBinding = null;
                    }
                }
            }

        }

        public Value getBoundValue() {
            return xBinding == null ? PString.EMPTY : xBinding;
        }
    }

    private class YAddressBinding implements ArgumentProperty.Binding {

        public void setBoundValue(long time, Value value) {
            if (yAdaptor == null) {
                createComponentAndAdaptors();
            }
            if (bindingContext != null) {
                bindingContext.unbind(yAdaptor);
                if (value.isEmpty()) {
                    yBinding = null;
                } else {
                    try {
                        yBinding = ControlAddress.coerce(value);
                        bindingContext.bind(yBinding, yAdaptor);
                    } catch (ValueFormatException ex) {
                        logger.log(Level.WARNING, "Could not create binding-y", ex);
                        yBinding = null;
                    }
                }
            }
        }

        public Value getBoundValue() {
            return yBinding == null ? PString.EMPTY : yBinding;
        }
    }

    private class MinBinding implements ArgumentProperty.Binding {

        private Preferences prefs;

        private MinBinding(Preferences prefs) {
            this.prefs = prefs;
        }

        public void setBoundValue(long time, Value value) {
            if (value.isEmpty()) {
                prefs.minimum = null;
            } else {
                try {
                    prefs.minimum = PNumber.coerce(value);
                } catch (Exception ex) {
                    prefs.minimum = null;
                }
            }
            updateAdaptors();
        }

        public Value getBoundValue() {
            Value arg = prefs.minimum;
            if (arg == null) {
                return PString.EMPTY;
            } else {
                return arg;
            }
        }
    }

    private class MaxBinding implements ArgumentProperty.Binding {

        private Preferences prefs;

        private MaxBinding(Preferences prefs) {
            this.prefs = prefs;
        }

        public void setBoundValue(long time, Value value) {
            if (value.isEmpty()) {
                prefs.maximum = null;
            } else {
                try {
                    prefs.maximum = PNumber.coerce(value);
                } catch (Exception ex) {
                    prefs.maximum = null;
                }
            }
            updateAdaptors();
        }

        public Value getBoundValue() {
            Value arg = prefs.maximum;
            if (arg == null) {
                return PString.EMPTY;
            } else {
                return arg;
            }
        }
    }

    private class ScaleBinding implements ArgumentProperty.Binding {

        private Preferences prefs;

        private ScaleBinding(Preferences prefs) {
            this.prefs = prefs;
        }

        public void setBoundValue(long time, Value value) {
            if (value.isEmpty()) {
                prefs.scale = null;
            } else {
                if (value instanceof PString) {
                    prefs.scale = (PString) value;
                } else {
                    prefs.scale = PString.valueOf(value);
                }
            }
            updateAdaptors();
        }

        public Value getBoundValue() {
            Value arg = prefs.scale;
            if (arg == null) {
                return PString.EMPTY;
            } else {
                return arg;
            }
        }
    }
}
