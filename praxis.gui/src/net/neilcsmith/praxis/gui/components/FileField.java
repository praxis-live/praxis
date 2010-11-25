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
 *
 */
package net.neilcsmith.praxis.gui.components;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.types.PUri;
import net.neilcsmith.praxis.gui.ControlBinding;
import net.neilcsmith.praxis.gui.ControlBinding.Adaptor;
import net.neilcsmith.praxis.gui.SingleBindingGuiComponent;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 *
 * @author Neil C Smith
 */
// @TODO Add size control
public class FileField extends SingleBindingGuiComponent {

    private String labelText;
    private URIAdaptor adaptor;
    private Box box;
    private JTextField field;
    private JButton button;
    private PUri uri;

    public FileField() {
        labelText = "";
        registerControl("label", StringProperty.create( new LabelBinding(), labelText));
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
        if (box == null) {
            createComponentAndAdaptor();
        }
        return box;
    }

    private void createComponentAndAdaptor() {
        box = Box.createHorizontalBox();
        field = new JTextField(8);
        field.setEditable(false);
        field.setMaximumSize(new Dimension(field.getMaximumSize().width, field.getPreferredSize().height));
        button = new JButton("...");
        button.addActionListener(new FileButtonAction());
        adaptor = new URIAdaptor();
        box.add(field);
        box.add(button);
        box.addAncestorListener(adaptor);
        updateBorders();
    }

    private void updateField() {
        if (field == null) {
            return;
        }
        if (uri == null) {
            field.setText("");
        } else {
            try {
                File file = new File(uri.value());
                field.setText(file.getName());
            } catch (Exception ex) {
                field.setText(uri.toString());
            }

        }
    }

    private void updateBorders() {
        if (box != null) {
            if (labelText.isEmpty()) {
                box.setBorder(BorderFactory.createEtchedBorder());
            } else {
                box.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), labelText));
            }
            box.revalidate();
        }

    }

    private class FileButtonAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {


            File cur = null;
            if (uri != null) {
                try {
                    cur = new File(uri.value());
                } catch (Exception ex) {
                }
            }
            JFileChooser chooser;
            if (cur == null) {
                chooser = new JFileChooser();
            } else {
                chooser = new JFileChooser(cur);
            }
            int ret = chooser.showOpenDialog(box.getTopLevelAncestor());
            System.out.println("File Dialog returned " + ret);
            if (ret == JFileChooser.APPROVE_OPTION) {
                URI u = chooser.getSelectedFile().toURI();
                uri = PUri.valueOf(u);
                adaptor.send(uri);
                updateField();

            }
        }
    }

    private class URIAdaptor extends ControlBinding.Adaptor implements AncestorListener {

        private URIAdaptor() {
            setSyncRate(ControlBinding.SyncRate.Low);
        }

        @Override
        public void update() {
            ControlBinding binding = getBinding();
            if (binding == null) {
                return;
            }
            CallArguments args = binding.getArguments();
            if (args.getCount() > 0) {
                Argument arg = args.getArg(0);
                if (arg.isEmpty()) {
                    if (uri != null) {
                        uri = null;
                        updateField();
                    }
                } else {
                    PUri u;
                    try {
                        u = PUri.coerce(args.getArg(0));
                        if (!u.equals(uri)) {
                            uri = u;
                            updateField();
                        }
                    } catch (ArgumentFormatException ex) {
                        u = null;
                        if (uri != null) {
                            uri = null;
                            updateField();
                        }
                    }
                }

            }
        }

        @Override
        public void updateBindingConfiguration() {
        }

        public void send(PUri uri) {
            super.send(CallArguments.create(uri));
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
}
