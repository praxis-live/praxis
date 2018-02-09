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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import org.praxislive.core.Call;
import org.praxislive.core.CallArguments;
import org.praxislive.core.Control;
import org.praxislive.core.ControlAddress;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.interfaces.ScriptService;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PString;
import org.praxislive.gui.ControlBinding.Adaptor;
import org.praxislive.gui.impl.ActionAdaptor;
import org.praxislive.gui.impl.SingleBindingGuiComponent;
import org.praxislive.impl.ArgumentProperty;
import org.praxislive.impl.ArrayProperty;

/**
 *
 * @author Neil C Smith
 */
public class Button extends SingleBindingGuiComponent {

    private final static Logger LOG = Logger.getLogger(Button.class.getName());

    private JButton button;
    private ActionAdaptor adaptor;
    private String label;
    private CallArguments values;
    private ArgumentProperty onClick;

    public Button() {
        label = "";
        values = CallArguments.EMPTY;
//        registerControl("values", ArrayProperty.create(new ValuesBinding(), PArray.EMPTY));
//        registerControl("label", StringProperty.create(new LabelBinding(), label));
//        onClick = StringProperty.create("");
//        registerControl("on-click", onClick);
//        registerControl("_on-click-log", new OnClickLog());
        // @TODO Fix ControlInfo here.
//        onClickInfo = ControlInfo.createPropertyInfo(new ArgumentInfo[] {ControlAddress.info(), Value.info()},
//                new Value[] {PString.EMPTY}, null);
//        registerControl("on-click", new OnClickProperty(this, onClickInfo));
    }

    @Override
    protected void initControls() {
        super.initControls();
        registerControl("values", ArrayProperty.create(new ValuesBinding(), PArray.EMPTY));
        onClick = ArgumentProperty.create(ArgumentInfo.create(PString.class,
                PMap.create(PString.KEY_MIME_TYPE, "text/x-praxis-script")));
        registerControl("on-click", onClick);
        registerControl("_on-click-log", new OnClickLog());
    }

    @Override
    protected void updateLabel() {
        super.updateLabel();
        button.setText(getLabel());
    }
    
    
    @Override
    protected JComponent createSwingComponent() {
        if (button == null) {
            createComponentAndAdaptor();
        }
        return button;
    }

    @Override
    protected Adaptor getBindingAdaptor() {
        if (adaptor == null) {
            createComponentAndAdaptor();
        }
        return adaptor;
    }

    private void createComponentAndAdaptor() {
        button = new JButton(label);
        button.setUI(new UI());
        adaptor = new ActionAdaptor();
        button.addActionListener(adaptor);
        adaptor.setCallArguments(values);
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                processOnClick();
            }
        });
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

    private void processOnClick() {
        try {
            String script = onClick.getValue().toString().trim();
            if (script.isEmpty()) {
                return;
            }
            ControlAddress to = ControlAddress.create(
                    findService(ScriptService.INSTANCE),
                    ScriptService.EVAL);
            ControlAddress from = ControlAddress.create(
                    getAddress(), "_on-click-log");
            Call call = Call.createQuietCall(to, from, System.nanoTime(), PString.valueOf(script));
            getPacketRouter().route(call);
            
        } catch (Exception ex) {
            Logger.getLogger(Button.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    private class LabelBinding implements StringProperty.Binding {
//
//        public void setBoundValue(long time, String value) {
//            label = value;
//            if (button != null) {
//                button.setText(value);
//                button.revalidate();
//            }
//        }
//
//        public String getBoundValue() {
//            return label;
//        }
//    }

    private class ValuesBinding implements ArrayProperty.Binding {

        private PArray value = PArray.EMPTY;

        public void setBoundValue(long time, PArray value) {
            if (value.isEmpty()) {
                values = CallArguments.EMPTY;
            } else {
                values = CallArguments.create(value.getAll());
            }
            this.value = value;
            adaptor.setCallArguments(values);
        }

        public PArray getBoundValue() {
            return this.value;
        }
    }

    private class OnClickLog implements Control {

        public void call(Call call, PacketRouter router) throws Exception {
            if (call.getType() == Call.Type.ERROR) {
                LOG.warning(call.toString());
            }
        }

        public ControlInfo getInfo() {
            return null;
        }

    }

    private static class UI extends BasicButtonUI {

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
            g.setColor(b.hasFocus() || b.getModel().isRollover() ?
                    Utils.mix(c.getBackground(), c.getForeground(), 0.8) :
                    Utils.mix(c.getBackground(), c.getForeground(), 0.6));
            g.drawRect(0, 0, c.getWidth() - 1, c.getHeight() - 1);
            super.paint(g, c);
        }

        @Override
        protected void paintButtonPressed(Graphics g, AbstractButton b) {
            g.setColor(b.getForeground());
            g.fillRect(0, 0, b.getWidth(), b.getHeight());
        }

        @Override
        protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {           
            Color fg = b.getForeground();
            ButtonModel model = b.getModel();
            FontMetrics fm = g.getFontMetrics();
            int mnemonicIndex = b.getDisplayedMnemonicIndex();
            if (model.isPressed() || model.isSelected()) {
                g.setColor(b.getBackground());
            } else if (!model.isRollover()) {
                g.setColor(Utils.mix(b.getBackground(), fg, 0.8));
            }else {
                g.setColor(b.getForeground());
            }
            BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemonicIndex,
                    textRect.x + getTextShiftOffset(),
                    textRect.y + fm.getAscent() + getTextShiftOffset());
        }
        
        
        
        
    }
    
    
}
