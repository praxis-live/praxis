/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Neil C Smith. All rights reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 *
 */
package net.neilcsmith.praxis.gui.components;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.gui.AbstractGuiComponent;
import net.neilcsmith.praxis.gui.BoundedValueAdaptor;
import net.neilcsmith.praxis.gui.GuiRoot;
import net.neilcsmith.praxis.swing.JXYController;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 *
 * @author Neil C Smith
 */
public class XYController extends AbstractGuiComponent {

    private static Logger logger = Logger.getLogger(XYController.class.getName());
    private GuiRoot root;
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
        registerControl("label", StringProperty.create( new LabelBinding(), labelText));
        registerControl("binding-x", ArgumentProperty.create( new XAddressBinding(), PString.EMPTY));
        registerControl("binding-y", ArgumentProperty.create( new YAddressBinding(), PString.EMPTY));
        registerControl("minimum-x", ArgumentProperty.create( new MinBinding(xPrefs), PString.EMPTY));
        registerControl("minimum-y", ArgumentProperty.create( new MinBinding(yPrefs), PString.EMPTY));
        registerControl("maximum-x", ArgumentProperty.create( new MaxBinding(xPrefs), PString.EMPTY));
        registerControl("maximum-y", ArgumentProperty.create( new MaxBinding(yPrefs), PString.EMPTY));
        registerControl("scale-x", ArgumentProperty.create( new ScaleBinding(xPrefs), PString.EMPTY));
        registerControl("scale-y", ArgumentProperty.create( new ScaleBinding(yPrefs), PString.EMPTY));
    }

    @Override
    protected JComponent createSwingComponent() {
        if (container == null) {
            createComponentAndAdaptors();
        }
        return container;
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
            xAdaptor.setPreferredScale(xPrefs.scale);
            yAdaptor.setPreferredMinimum(yPrefs.minimum);
            yAdaptor.setPreferredMaximum(yPrefs.maximum);
            yAdaptor.setPreferredScale(yPrefs.scale);
        }
    }

    private void updateBorders() {
        if (container != null) {
            if (labelText.isEmpty()) {
                container.setBorder(BorderFactory.createEtchedBorder());
            } else {
                container.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), labelText));
            }
            container.revalidate();
        }
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        Root r = getRoot();
        if (r instanceof GuiRoot) {
            root = (GuiRoot) r;
        } else {
            if (xBinding != null) {
                root.unbind(xAdaptor);
                xBinding = null;
            }
            if (yBinding != null) {
                root.unbind(yAdaptor);
                yBinding = null;
            }
            root = null;
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

        public void setBoundValue(long time, Argument value) {
            if (xAdaptor == null) {
                createComponentAndAdaptors();
            }
            if (root != null) {
                root.unbind(xAdaptor);
                if (value.isEmpty()) {
                    xBinding = null;
                } else {
                    try {
                        xBinding = ControlAddress.coerce(value);
                        root.bind(xBinding, xAdaptor);
                    } catch (ArgumentFormatException ex) {
                        logger.log(Level.WARNING, "Could not create binding-x", ex);
                        xBinding = null;
                    }
                }
            }

        }

        public Argument getBoundValue() {
            return xBinding == null ? PString.EMPTY : xBinding;
        }
    }

    private class YAddressBinding implements ArgumentProperty.Binding {

        public void setBoundValue(long time, Argument value) {
            if (yAdaptor == null) {
                createComponentAndAdaptors();
            }
            if (root != null) {
                root.unbind(yAdaptor);
                if (value.isEmpty()) {
                    yBinding = null;
                } else {
                    try {
                        yBinding = ControlAddress.coerce(value);
                        root.bind(yBinding, yAdaptor);
                    } catch (ArgumentFormatException ex) {
                        logger.log(Level.WARNING, "Could not create binding-y", ex);
                        yBinding = null;
                    }
                }
            }
        }

        public Argument getBoundValue() {
            return yBinding == null ? PString.EMPTY : yBinding;
        }
    }

    private class MinBinding implements ArgumentProperty.Binding {

        private Preferences prefs;

        private MinBinding(Preferences prefs) {
            this.prefs = prefs;
        }

        public void setBoundValue(long time, Argument value) {
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

        public Argument getBoundValue() {
            Argument arg = prefs.minimum;
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

        public void setBoundValue(long time, Argument value) {
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

        public Argument getBoundValue() {
            Argument arg = prefs.maximum;
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

        public void setBoundValue(long time, Argument value) {
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

        public Argument getBoundValue() {
            Argument arg = prefs.scale;
            if (arg == null) {
                return PString.EMPTY;
            } else {
                return arg;
            }
        }
    }
}
