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
 *
 */
package org.praxislive.gui.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.ButtonModel;
import org.praxislive.core.Value;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.types.PString;
import org.praxislive.impl.swing.ControlBinding;

/**
 *
 * @author Neil C Smith
 */
public class ToggleButtonModelAdaptor extends ControlBinding.Adaptor {

    private static Logger logger = Logger.getLogger(ToggleButtonModelAdaptor.class.getName());
    private ButtonModel model;
    private Value onArg;
    private Value offArg;
    private boolean isProperty;
    private ControlInfo info;
    private boolean isUpdating;

    public ToggleButtonModelAdaptor(ButtonModel model) {
        if (model == null) {
            throw new NullPointerException();
        }
        model.addActionListener(new ActionHandler());
        this.model = model;
        onArg = offArg = PString.EMPTY;
        // @TODO temporary sync fix
        setSyncRate(ControlBinding.SyncRate.Low);
    }

    public void setOnArgument(Value onArg) {
        if (onArg == null) {
            throw new NullPointerException();
        }
        this.onArg = onArg;
        if (isProperty) {
            update();
        }
    }

    public Value getOnArgument() {
        return onArg;
    }

    public void setOffArgument(Value offArg) {
        if (offArg == null) {
            throw new NullPointerException();
        }
        this.offArg = offArg;
        if (isProperty) {
            update();
        }
    }

    public Value getOffArgument() {
        return offArg;
    }

    @Override
    public void update() {
        isUpdating = true;
        model.setSelected(checkSelection());
        isUpdating = false;
    }

    @Override
    public void updateBindingConfiguration() {
        isProperty = false;
        ControlBinding binding = getBinding();
        if (binding == null) {
            info = null;
        } else {
            info = binding.getBindingInfo();
            if (info != null) {
                isProperty = info.isProperty();
            }
        }
    }

    protected boolean checkSelection() {
        ControlBinding binding = getBinding();
        if (binding == null) {
            return false;
        }
        CallArguments args = binding.getArguments();
        if (args.getSize() > 0) {
            Value arg = args.get(0);
            return arg.equivalent(onArg) || onArg.equivalent(arg);
        }
        return false;
    }

    private class ActionHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (isUpdating) {
                return;
            }
            Value arg = model.isSelected() ? onArg : offArg;
            send(CallArguments.create(arg));
        }
    }
}
