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
package net.neilcsmith.praxis.gui.components;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.types.PBoolean;
import net.neilcsmith.praxis.gui.impl.SingleBindingGuiComponent;
import net.neilcsmith.praxis.gui.ControlBinding.Adaptor;
import net.neilcsmith.praxis.gui.impl.ToggleButtonModelAdaptor;
import net.neilcsmith.praxis.impl.ArgumentProperty;

/**
 *
 * @author Neil C Smith
 */
public class ToggleButton extends SingleBindingGuiComponent {

    private JToggleButton button;
    private Argument onArg;
    private Argument offArg;
    private ToggleButtonModelAdaptor adaptor;
    private String label;

    public ToggleButton() {
        label = "";
        onArg = PBoolean.TRUE;
        offArg = PBoolean.FALSE;
//        registerControl("on-value", ArgumentProperty.create( new OnBinding(), onArg));
//        registerControl("off-value", ArgumentProperty.create( new OffBinding(), offArg));
//        registerControl("label", StringProperty.create( new LabelBinding(), label));
    }

    @Override
    protected void initControls() {
        super.initControls();
        registerControl("on-value", ArgumentProperty.create(new OnBinding(), onArg));
        registerControl("off-value", ArgumentProperty.create(new OffBinding(), offArg));
    }

    @Override
    protected void updateLabel() {
        super.updateLabel();
        button.setText(getLabel());
    }

    @Override
    protected Adaptor getBindingAdaptor() {
        if (adaptor == null) {
            createComponentAndAdaptor();
        }
        return adaptor;
    }

    @Override
    protected JComponent createSwingComponent() {
        if (button == null) {
            createComponentAndAdaptor();
        }
        return button;
    }

    private void createComponentAndAdaptor() {
        button = new JToggleButton(label);
        button.setUI(new UI());
        adaptor = new ToggleButtonModelAdaptor(button.getModel());
        setAdaptorArguments();
        button.addAncestorListener(new AncestorListener() {

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
    }

    private void setAdaptorArguments() {
        if (adaptor != null) {
            adaptor.setOnArgument(onArg);
            adaptor.setOffArgument(offArg);
        }
    }

    private class OnBinding implements ArgumentProperty.Binding {

        public void setBoundValue(long time, Argument value) {
            onArg = value;
            setAdaptorArguments();
        }

        public Argument getBoundValue() {
            return onArg;
        }

    }

    private class OffBinding implements ArgumentProperty.Binding {

        public void setBoundValue(long time, Argument value) {
            offArg = value;
            setAdaptorArguments();
        }

        public Argument getBoundValue() {
            return offArg;
        }

    }

    private static class UI extends BasicToggleButtonUI {

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            AbstractButton b = (AbstractButton) c;
            b.setRolloverEnabled(true);
            b.setBorder(new EmptyBorder(8, 8, 8, 8));
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            g.setColor(b.hasFocus() || b.getModel().isRollover()
                    ? Utils.mix(c.getBackground(), c.getForeground(), 0.8)
                    : Utils.mix(c.getBackground(), c.getForeground(), 0.6));

            g.drawRect(0, 0, c.getWidth() - 1, c.getHeight() - 1);
            super.paint(g, c);
        }

        @Override
        protected void paintButtonPressed(Graphics g, AbstractButton b) {
            g.setColor(b.getForeground());
            g.fillRect(4, 4, b.getWidth() - 8, b.getHeight() - 8);
        }

        @Override
        protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
            ButtonModel model = b.getModel();
            FontMetrics fm = g.getFontMetrics();
            int mnemonicIndex = b.getDisplayedMnemonicIndex();
            if (model.isPressed() || model.isSelected()) {
                g.setColor(b.getBackground());
            } else if (!model.isRollover()) {
                g.setColor(Utils.mix(b.getBackground(), b.getForeground(), 0.8));
            } else {
                g.setColor(b.getForeground());
            }
            BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemonicIndex,
                    textRect.x + getTextShiftOffset(),
                    textRect.y + fm.getAscent() + getTextShiftOffset());

        }
    }

}
