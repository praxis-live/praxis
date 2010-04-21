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

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.gui.AbstractGuiComponent;
import net.neilcsmith.praxis.gui.ActionAdaptor;
import net.neilcsmith.praxis.gui.GuiRoot;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.AbstractProperty;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 *
 * @author Neil C Smith
 */
public class Button extends AbstractGuiComponent {

    private JButton button;
    private PArray argsArray;
    private ActionAdaptor adaptor;
    private String label;
    private ControlAddress onClickAddress;
    private CallArguments onClickArgs;
    private ControlInfo onClickInfo;
    private GuiRoot root;

    public Button() {
        label = "";
        onClickArgs = CallArguments.create(PString.EMPTY);
        registerControl("label", StringProperty.create(this, new LabelBinding(), label));
        // @TODO Fix ControlInfo here.
        onClickInfo = ControlInfo.createPropertyInfo(new ArgumentInfo[] {ControlAddress.info(), Argument.info()},
                new Argument[] {PString.EMPTY}, null);
        registerControl("on-click", new OnClickProperty(this, onClickInfo));
    }

    @Override
    protected JComponent createSwingComponent() {
        if (button == null) {
            createComponentAndAdaptor();
        }
        return button;
    }

    private void createComponentAndAdaptor() {
        button = new JButton(label);
        adaptor = new ActionAdaptor();
        button.addActionListener(adaptor);
//        setAdaptorArguments();
        updateAdaptor();
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

    private void updateAdaptor() {
        if (root != null && adaptor != null) {
            root.unbind(adaptor);
            if (onClickAddress != null) {
                root.bind(onClickAddress, adaptor);
                adaptor.setCallArguments(onClickArgs);
            }
        }
    }

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        Root r = getRoot();
        if (r instanceof GuiRoot) {
            root = (GuiRoot) r;
        } else {
            if (root != null) {
                if (adaptor != null) {
                    root.unbind(adaptor);
                }
                root = null;
            }
        }
    }

    private class LabelBinding implements StringProperty.Binding {

        public void setBoundValue(long time, String value) {
            label = value;
            if (button != null) {
                button.setText(value);
                button.revalidate();
            }
        }

        public String getBoundValue() {
            return label;
        }
    }


    private class OnClickProperty extends AbstractProperty {

        private final CallArguments unbound = CallArguments.create(PString.EMPTY);
        private CallArguments cache = unbound;

        OnClickProperty(AbstractComponent component, ControlInfo info) {
            super(component, info);
        }

        @Override
        protected void setArguments(long time, CallArguments args) throws Exception {
            Argument arg = args.getArg(0);
            if (arg.isEmpty()) {
                onClickAddress = null;
                onClickArgs = null;
                cache = unbound;
            } else {
                try {
                    onClickAddress = ControlAddress.coerce(arg);
                    int argCount = args.getCount();
                    if (argCount > 1) {
                        Argument[] clArgs = new Argument[argCount - 1];
                        for (int i = 0; i < clArgs.length; i++) {
                            clArgs[i] = args.getArg(i + 1);
                        }
                        onClickArgs = CallArguments.create(clArgs);
                    } else {
                        onClickArgs = CallArguments.EMPTY;
                    }
                    cache = args;
                } catch (Exception ex) {
                    onClickAddress = null;
                    onClickArgs = null;
                    cache = unbound;
                }
            }
            updateAdaptor();
        }

        @Override
        protected CallArguments getArguments() {
            return cache;
        }
    }


}
