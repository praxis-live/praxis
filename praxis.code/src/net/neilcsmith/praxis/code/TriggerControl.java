/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Neil C Smith.
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
package net.neilcsmith.praxis.code;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.code.userapi.Trigger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.info.PortInfo;
import net.neilcsmith.praxis.core.types.PMap;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class TriggerControl extends Trigger implements Control {

    private final static Logger LOG = Logger.getLogger(TriggerControl.class.getName());
    private final static ControlInfo INFO = ControlInfo.createActionInfo(PMap.EMPTY);

    private final Binding binding;

    TriggerControl(Binding binding) {
        binding = binding == null ? new DefaultBinding() : binding;
        this.binding = binding;
    }

    @Override
    public void call(Call call, PacketRouter router) throws Exception {
        Call.Type type = call.getType();
        if (type == Call.Type.INVOKE) {
            trigger(call.getTimecode());
            router.route(Call.createReturnCall(call, CallArguments.EMPTY));
        } else if (type == Call.Type.INVOKE_QUIET) {
            trigger(call.getTimecode());
        } else {
//            throw new IllegalArgumentException();
        }
    }

    @Override
    public ControlInfo getInfo() {
        return INFO;
    }

    private void trigger(long time) {
        binding.trigger(time);
    }

    @Override
    public boolean poll() {
        return binding.poll();
    }

    public static abstract class Binding {

        protected void attach(CodeDelegate delegate) {
            // no op hook
        }

        public abstract void trigger(long time);

        public abstract boolean poll();

        public abstract boolean peek();

    }

    private static class DefaultBinding extends Binding {

        private boolean triggered;

        @Override
        public void trigger(long time) {
            triggered = true;
        }

        @Override
        public boolean poll() {
            if (triggered) {
                triggered = false;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean peek() {
            return triggered;
        }

    }

    public static class Descriptor extends ControlDescriptor {

        private final TriggerControl control;
        private Field field;

        public Descriptor(String id, int index, Binding binding) {
            this(id, index, binding, null);
        }

        public Descriptor(String id, int index, Binding binding, Field field) {
            super(id, Category.Action, index);
            control = new TriggerControl(binding);
            this.field = field;
        }

        @Override
        public ControlInfo getInfo() {
            return INFO;
        }

        @Override
        public void attach(CodeContext<?> context, Control previous) {
            if (field != null) {
                try {
                    field.setAccessible(true);
                    field.set(context.getDelegate(), control);
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }

        @Override
        public Control getControl() {
            return control;
        }

        public PortDescriptor createPortDescriptor() {
            return new PortDescImpl(getID(), getIndex(), control);
        }

    }

    private static class PortDescImpl extends PortDescriptor implements ControlInput.Link {

        private final TriggerControl control;

        private ControlInput port;

        private PortDescImpl(String id, int index, TriggerControl control) {
            super(id, Category.Property, index);
            this.control = control;
        }

        @Override
        public void attach(CodeContext<?> context, Port previous) {
            if (previous instanceof ControlInput) {
                port = (ControlInput) previous;
                port.setLink(this);
            } else {
                if (previous != null) {
                    previous.disconnectAll();
                }
                port = new ControlInput(this);
            }
        }

        @Override
        public Port getPort() {
            assert port != null;
            return port;
        }

        @Override
        public PortInfo getInfo() {
            return ControlInput.INFO;
        }

        @Override
        public void receive(long time, double value) {
            control.trigger(time);
        }

        @Override
        public void receive(long time, Argument value) {
            control.trigger(time);
        }

    }

}
