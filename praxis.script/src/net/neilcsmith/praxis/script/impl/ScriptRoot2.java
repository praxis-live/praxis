/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 - Neil C Smith. All rights reserved.
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
 */

package net.neilcsmith.praxis.script.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.Component;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.InvalidChildException;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.InterfaceDefinition;
import net.neilcsmith.praxis.core.interfaces.ScriptInterpreter;
import net.neilcsmith.praxis.impl.AbstractRoot;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ScriptRoot2 extends AbstractRoot {

    private ScriptComponent scriptComponent;

    public ScriptRoot2() {
        scriptComponent = new ScriptComponent();
        try {
            addChild("_script", scriptComponent);
        } catch (InvalidChildException ex) {
            Logger.getLogger(ScriptRoot2.class.getName()).log(Level.SEVERE, null, ex);
        }
        registerControl("eval", new DispatchControl("eval"));
    }

    @Override
    public InterfaceDefinition[] getInterfaces() {
        return new InterfaceDefinition[] {ScriptInterpreter.getInstance()};
    }

    private class DispatchControl implements Control {

        private String id;

        private DispatchControl(String id) {
            this.id = id;
        }

        public void call(Call call, PacketRouter router) throws Exception {
            scriptComponent.getControl(id).call(call, router);
        }

        public ControlInfo getInfo() {
            Control ctrl = scriptComponent.getControl(id);
            if (ctrl != null) {
                return ctrl.getInfo();
            } else {
                return null;
            }
        }

        public Component getComponent() {
            return ScriptRoot2.this;
        }

    }


}
