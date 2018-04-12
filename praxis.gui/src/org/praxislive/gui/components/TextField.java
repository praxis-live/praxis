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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.praxislive.core.CallArguments;
import org.praxislive.core.types.PString;
import org.praxislive.impl.swing.ControlBinding;
import org.praxislive.impl.swing.ControlBinding.Adaptor;
import org.praxislive.gui.impl.SingleBindingGuiComponent;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class TextField extends SingleBindingGuiComponent {
    
    private final static int DEFAULT_COLUMNS = 8;

    private Box panel;
    private JTextField text;
    private TextAdaptor adaptor;
    private boolean activeEdit;
    private Action sendAction;
    private Action resetAction;
    private boolean syncing;
    private String labelText;
    
    public TextField() {
        labelText = "";
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
        if (panel == null) {
            createComponentAndAdaptor();
        }
        return panel;
    }

    private void createComponentAndAdaptor() {
        panel = Box.createHorizontalBox();
        text = new JTextField(DEFAULT_COLUMNS);
        text.getDocument().addDocumentListener(new TextListener());
        panel.add(text);
        adaptor = new TextAdaptor();
        text.addAncestorListener(adaptor);
        text.addFocusListener(adaptor);
        sendAction = new SendAction();
        sendAction.setEnabled(false);
        resetAction = new ResetAction();
        resetAction.setEnabled(false);

        Keymap map = JTextComponent.addKeymap(null, text.getKeymap());

        map.addActionForKeyStroke(KeyStroke.getKeyStroke("ENTER"), sendAction);
        map.addActionForKeyStroke(KeyStroke.getKeyStroke("ESCAPE"), resetAction);

        text.setKeymap(map);
        
        updateBorders();
    }

    private void setActiveEdit(boolean active) {
        if (active == activeEdit) {
            return;
        }
        sendAction.setEnabled(active);
        resetAction.setEnabled(active);
        activeEdit = active;
    }
    
     private void updateBorders() {
        if (panel != null) {
            Border etched = Utils.getBorder();
            if (labelText.isEmpty()) {
                panel.setBorder(etched);
            } else {
                panel.setBorder(BorderFactory.createTitledBorder(
                    etched, labelText));
            }
            panel.revalidate();
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

    private class TextAdaptor extends ControlBinding.Adaptor implements AncestorListener, FocusListener {

        private TextAdaptor() {
            setSyncRate(ControlBinding.SyncRate.Low);
        }

        private void send(String text) {
            PString val = PString.valueOf(text);
            send(CallArguments.create(val));
        }

        @Override
        public void update() {
            if (activeEdit|| text.isFocusOwner()) {
                return;
            }
            sync();
        }

        private void sync() {
            syncing = true;
            ControlBinding binding = getBinding();
            if (binding == null) {
                text.setText("");
            } else {
                CallArguments args = binding.getArguments();
                if (args.getSize() < 1) {
                    text.setText("");
                } else {
                    text.setText(args.get(0).toString());
                }
            }
            syncing = false;
        }

        @Override
        public void onError(CallArguments args) {
        }

        @Override
        public void updateBindingConfiguration() {
            // no op
        }

        public void ancestorAdded(AncestorEvent event) {
            setActive(true);
        }

        public void ancestorRemoved(AncestorEvent event) {
            setActive(false);
        }

        public void ancestorMoved(AncestorEvent event) {
            // no op
        }

        public void focusGained(FocusEvent e) {
            text.selectAll();
        }

        public void focusLost(FocusEvent e) {
            // no op?
        }
    }

    private class TextListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            changed();
        }

        public void removeUpdate(DocumentEvent e) {
            changed();
        }

        public void changedUpdate(DocumentEvent e) {
            changed();
        }
        
        private void changed() {
            if (!syncing) {
                setActiveEdit(true);
            }
        }

    }

    private class SendAction extends AbstractAction {

        private SendAction() {
            super("Send");
        }

        public void actionPerformed(ActionEvent e) {
            adaptor.send(text.getText());
            setActiveEdit(false);
        }
    }

    private class ResetAction extends AbstractAction {

        private ResetAction() {
            super("Reset");
        }

        public void actionPerformed(ActionEvent e) {           
            setActiveEdit(false);
            adaptor.sync();
        }
    }
}
