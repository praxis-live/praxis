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

package net.neilcsmith.praxis.components.routing;

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.Root;
import net.neilcsmith.praxis.core.Root.State;
import net.neilcsmith.praxis.impl.AbstractRootStateComponent;
import net.neilcsmith.praxis.impl.ArgumentInputPort;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.TriggerControl;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 * 
 */
// @TODO deactivate when root state inactive?
public class Join extends AbstractRootStateComponent {

    private Argument in1;
    private Argument in2;
    private ControlPort.Output out;

    public Join() {
        build();
    }

    private void build() {
        registerPort("in-1", ArgumentInputPort.create(this, new ArgumentInputPort.Binding() {

            public void receive(long time, Argument arg) {
                if (in2 != null) {
                    out.send(time, arg);
                    reset();
                } else {
                    in1 = arg;
                }
            }
        }));
        registerPort("in-2", ArgumentInputPort.create(this, new ArgumentInputPort.Binding() {

            public void receive(long time, Argument arg) {
                if (in1 != null) {
                    out.send(time, arg);
                    reset();
                } else {
                    in2 = arg;
                }
            }
        }));
        TriggerControl reset = TriggerControl.create(this, new TriggerControl.Binding() {

            public void trigger(long time) {
                reset();
            }
        });
        registerControl("reset", reset);
        registerPort("reset", reset.createPort());
        out = new DefaultControlOutputPort(this);
        registerPort("out", out);
    }

    private void reset() {
        in1 = null;
        in2 = null;
    }

    public void rootStateChanged(Root source, State state) {
        reset();
    }

}
