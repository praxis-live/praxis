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
 */

package net.neilcsmith.praxis.components.routing;

import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.ArgumentInputPort;
import net.neilcsmith.praxis.impl.ArgumentProperty;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 * 
 */
public class Send extends AbstractComponent {
    
    private final static Logger LOG = Logger.getLogger(Send.class.getName());

    private ControlAddress destination;

    public Send() {
        build();
    }

    private void build() {
        registerControl("address", ArgumentProperty.create(
                ArgumentInfo.create(ControlAddress.class,
                    PMap.create(ArgumentInfo.KEY_ALLOW_EMPTY, true)),
                new AddressBinding(),
                PString.EMPTY));
        registerPort(Port.IN, ArgumentInputPort.create(new InputBinding()));
        registerControl("_log", new LogControl());
    }
    
    private class InputBinding implements ArgumentInputPort.Binding {

        public void receive(long time, Argument arg) {
            if (destination != null) {
                PacketRouter router = getPacketRouter();
                if (router != null) {
                    Call call = Call.createQuietCall(destination, ControlAddress.create(getAddress(), "_log"), time, arg);
                    router.route(call);
                }
            }
        }
        
    }
    
    private class AddressBinding implements ArgumentProperty.Binding {

        public void setBoundValue(long time, Argument value) throws Exception {
            if (value.isEmpty()) {
                destination = null;
            } else {
                destination = ControlAddress.coerce(value);
            }
        }

        public Argument getBoundValue() {
            return destination == null ? PString.EMPTY : destination;
        }
        
    }
    
    private class LogControl implements Control {

        public void call(Call call, PacketRouter router) throws Exception {
            if (call.getType() != Call.Type.RETURN) {
                LOG.warning(call.toString());
            }
        }

        public ControlInfo getInfo() {
            return null;
        }

    }


}
