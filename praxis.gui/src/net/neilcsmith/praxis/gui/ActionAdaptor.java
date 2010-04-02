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

package net.neilcsmith.praxis.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.info.ControlInfo;

/**
 *
 * @author Neil C Smith
 */
public class ActionAdaptor extends ControlBinding.Adaptor implements ActionListener {

    private static Logger logger = Logger.getLogger(ActionAdaptor.class.getName());

    private CallArguments args;
    private ControlInfo info;
    private boolean isProperty;

    public ActionAdaptor() {
        setSyncRate(ControlBinding.SyncRate.None);
        this.args = CallArguments.EMPTY;
    }

    public void setCallArguments(CallArguments args) {
        if (args == null) {
            throw new NullPointerException();
        }
        this.args = args;
    }

    public CallArguments getCallArguments() {
        return args;
    }

    @Override
    public void update() {
        // no op - nothing to sync
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

    public void actionPerformed(ActionEvent e) {
        if (isProperty && args.getCount() == 0) {
                logger.warning("Can't send zero length arguments to property control");
            } else {
                send(args);
            }
    }


}
