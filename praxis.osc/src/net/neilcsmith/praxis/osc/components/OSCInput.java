/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Neil C Smith.
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
package net.neilcsmith.praxis.osc.components;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import java.net.SocketAddress;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.ControlAddress;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PBoolean;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.AbstractComponent;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.StringProperty;

/**
 *
 * @author Neil C Smith
 */
public class OSCInput extends AbstractComponent {

    private final OSCListener listener;
    
    private OSCContext context;
    private ControlAddress sendAddress;
    private String oscAddress;
    private ControlAddress returnAddress;

    public OSCInput() {
        listener = new OSCListenerImpl();
        sendAddress = null;
        oscAddress = "";
        initControls();
    }
    
    private void initControls() {
        registerControl("address", ArgumentProperty.builder()
                .type(ControlAddress.class)
                .allowEmpty()
                .binding(new SendAddressBinding())
                .build());
        registerControl("osc-address", StringProperty.builder()
                .emptyIsDefault()
                .binding(new OSCAddressBinding())
                .build());
        registerControl("_log", new OSCLogControl());
    }
    

    @Override
    public void hierarchyChanged() {
        super.hierarchyChanged();
        OSCContext ctxt = getLookup().get(OSCContext.class);
        if (ctxt != context) {
            if (context != null) {
                unregisterListener();
                context = null;
            }
            if (ctxt == null) {
                return;
            }
            context = ctxt;
            registerListener();
        }
        ComponentAddress c = getAddress();
        if (c == null) {
            returnAddress = null;
        } else {
            returnAddress = ControlAddress.create(c, "_log");
        }
    }
    
    private void registerListener() {
        if (sendAddress != null) {
            String osc = oscAddress.isEmpty() ?
                    sendAddress.toString() : oscAddress;
            context.addListener(osc, listener);
        }
    }
    
    private void unregisterListener() {
        if (sendAddress != null) {
            String osc = oscAddress.isEmpty() ?
                    sendAddress.toString() : oscAddress;
            context.removeListener(osc, listener);
        }
    }
    
    private void dispatch(OSCMessage msg, long time) {
        if (sendAddress == null) {
            return;
        }
        PacketRouter router = getPacketRouter();
        int count = msg.getArgCount();
        CallArguments arguments;
        if (count == 0) {
            arguments = CallArguments.EMPTY;
        } else if (count == 1) {
            arguments = CallArguments.create(objectToArg(msg.getArg(0)));
        } else {
            Argument[] args = new Argument[count];
            for (int i=0; i<count; i++) {
                args[i] = objectToArg(msg.getArg(i));
            }
            arguments = CallArguments.create(args);
        }
        router.route(Call.createQuietCall(sendAddress, returnAddress, time, arguments));
    }
    
    private Argument objectToArg(Object obj) {
       if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue() ? PBoolean.TRUE : PBoolean.FALSE;
        }
        if (obj instanceof Integer) {
            return PNumber.valueOf(((Integer) obj).intValue());
        }
        if (obj instanceof Number) {
            return PNumber.valueOf(((Number) obj).doubleValue());
        }
        if (obj == null) {
            return PString.EMPTY;
        }
        return PString.valueOf(obj);
    }
    
    private class SendAddressBinding implements ArgumentProperty.Binding {

        @Override
        public void setBoundValue(long time, Argument value) throws Exception {
            ControlAddress send = value.isEmpty() ? null : ControlAddress.coerce(value);
            unregisterListener();
            sendAddress = send;
            registerListener();
        }

        @Override
        public Argument getBoundValue() {
            if (sendAddress == null) {
                return PString.EMPTY;
            } else {
                return sendAddress;
            }
        }
        
    }
    
    private class OSCAddressBinding implements StringProperty.Binding {

        @Override
        public void setBoundValue(long time, String value) {
            unregisterListener();
            oscAddress = value;
            registerListener();
        }

        @Override
        public String getBoundValue() {
            return oscAddress;
        }
        
    }

    private class OSCListenerImpl implements OSCListener {

        @Override
        public void messageReceived(OSCMessage oscm, SocketAddress sa, long time) {
            dispatch(oscm, time);
        }
    }
    
    private class OSCLogControl implements Control {

        @Override
        public void call(Call call, PacketRouter router) throws Exception {
            
        }

        @Override
        public ControlInfo getInfo() {
            return null;
        }
        
    }
}
