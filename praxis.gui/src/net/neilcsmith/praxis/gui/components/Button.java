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
 *
 */
package net.neilcsmith.praxis.gui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.interfaces.ScriptService;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.gui.ControlBinding.Adaptor;
import net.neilcsmith.praxis.gui.impl.ActionAdaptor;
import net.neilcsmith.praxis.gui.impl.SingleBindingGuiComponent;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.ArrayProperty;
import net.neilcsmith.praxis.impl.StringProperty;

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
//        onClickInfo = ControlInfo.createPropertyInfo(new ArgumentInfo[] {ControlAddress.info(), Argument.info()},
//                new Argument[] {PString.EMPTY}, null);
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

//    private class OnClickProperty extends AbstractProperty {
//
//        private final CallArguments unbound = CallArguments.create(PString.EMPTY);
//        private CallArguments cache = unbound;
//
//        OnClickProperty(AbstractComponent component, ControlInfo info) {
//            super(info);
//        }
//
//        @Override
//        protected void setArguments(long time, CallArguments args) throws Exception {
//            Argument arg = args.get(0);
//            if (arg.isEmpty()) {
//                onClickAddress = null;
//                onClickArgs = null;
//                cache = unbound;
//            } else {
//                try {
//                    onClickAddress = ControlAddress.coerce(arg);
//                    int argCount = args.getSize();
//                    if (argCount > 1) {
//                        Argument[] clArgs = new Argument[argCount - 1];
//                        for (int i = 0; i < clArgs.length; i++) {
//                            clArgs[i] = args.get(i + 1);
//                        }
//                        onClickArgs = CallArguments.create(clArgs);
//                    } else {
//                        onClickArgs = CallArguments.EMPTY;
//                    }
//                    cache = args;
//                } catch (Exception ex) {
//                    onClickAddress = null;
//                    onClickArgs = null;
//                    cache = unbound;
//                }
//            }
//            updateAdaptor();
//        }
//
//        @Override
//        protected CallArguments getArguments() {
//            return cache;
//        }
//    }
}
