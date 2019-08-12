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
package org.praxislive.code;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.praxislive.code.userapi.T;
import org.praxislive.code.userapi.Trigger;
import org.praxislive.core.Value;
import org.praxislive.core.Call;
import org.praxislive.core.Control;
import org.praxislive.core.PacketRouter;
import org.praxislive.core.Port;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.PortInfo;
import org.praxislive.core.types.PMap;
import org.praxislive.logging.LogLevel;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class TriggerControl extends Trigger implements Control {

    private final static ControlInfo INFO = ControlInfo.createActionInfo(PMap.EMPTY);

    private final Binding binding;
    private CodeContext<?> context;

    TriggerControl(Binding binding) {
        binding = binding == null ? new DefaultBinding() : binding;
        this.binding = binding;
    }

    @Override
    public void call(Call call, PacketRouter router) throws Exception {
        if (call.isRequest()) {
            trigger(call.time());
            if (call.isReplyRequired()) {
                router.route(call.reply());
            }
        }
    }

    protected void trigger(long time) {
        context.invoke(time, () -> {
            try {
                binding.trigger(time);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
            super.trigger(time);
        });
    }

    private void attach(CodeContext<?> context, Control previous) {
        this.context = context;
        binding.attach(context);
        if (previous instanceof TriggerControl) {
            try {
                boolean val = ((TriggerControl) previous).poll();
                if (val) {
                    binding.trigger(context.getTime());
                }
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
            super.attach(context, (TriggerControl) previous);
        }
    }

    @Override
    public boolean poll() {
        return binding.poll();
    }

    public static abstract class Binding {

        protected void attach(CodeContext<?> delegate) {
            // no op hook
        }

        public abstract void trigger(long time) throws Exception;

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

    private static class BooleanBinding extends Binding {

        private final Field field;
        private CodeDelegate delegate;

        private BooleanBinding(Field field) {
            this.field = field;
        }

        @Override
        protected void attach(CodeContext<?> context) {
            this.delegate = context.getDelegate();
        }

        @Override
        public void trigger(long time) throws Exception {
            field.setBoolean(delegate, true);
        }

        @Override
        public boolean poll() {
            try {
                boolean val = field.getBoolean(delegate);
                if (val) {
                    field.setBoolean(delegate, false);
                }
                return val;
            } catch (Exception ex) {
                return false;
            }
        }

        @Override
        public boolean peek() {
            try {
                return field.getBoolean(delegate);
            } catch (Exception ex) {
                return false;
            }
        }

    }

    private static class MethodBinding extends Binding {

        private final Method method;
        private CodeContext<?> context;
        private boolean triggered;

        private MethodBinding(Method method) {
            this.method = method;
        }

        @Override
        protected void attach(CodeContext<?> context) {
            this.context = context;
        }

        @Override
        public void trigger(long time) throws Exception {
            triggered = true;
            context.invoke(time, method);
            triggered = false;
        }

        @Override
        public boolean poll() {
            return triggered;
        }

        @Override
        public boolean peek() {
            return triggered;
        }

    }

    public static class Descriptor extends ControlDescriptor {

        private final TriggerControl control;
        private Field triggerField;

        public Descriptor(String id, int index, Binding binding) {
            this(id, index, binding, null);
        }

        public Descriptor(String id, int index, Binding binding, Field triggerField) {
            super(id, Category.Action, index);
            control = new TriggerControl(binding);
            this.triggerField = triggerField;
        }

        @Override
        public ControlInfo getInfo() {
            return INFO;
        }

        @Override
        public void attach(CodeContext<?> context, Control previous) {
            control.attach(context, previous);
            if (triggerField != null) {
                try {
                    triggerField.set(context.getDelegate(), control);
                } catch (Exception ex) {
                    context.getLog().log(LogLevel.ERROR, ex);
                }
            }
        }

        @Override
        public void reset(boolean full) {
            control.clearLinks();
            control.maxIndex(Integer.MAX_VALUE);
            if (full) {
                control.index(0);
            }
        }

        @Override
        public Control getControl() {
            return control;
        }

        public PortDescriptor createPortDescriptor() {
            return new PortDescImpl(getID(), getIndex(), control);
        }

        public static Descriptor create(CodeConnector<?> connector,
                T ann, Field field) {
            field.setAccessible(true);
            String id = connector.findID(field);
            int index = ann.value();
            Class<?> type = field.getType();
            if (type == boolean.class) {
                return new Descriptor(id, index, new BooleanBinding(field));
            } else if (Trigger.class.isAssignableFrom(type)) {
                return new Descriptor(id, index, new DefaultBinding(), field);
            } else {
                return null;
            }
        }

        public static Descriptor create(CodeConnector<?> connector,
                T ann, Method method) {
            method.setAccessible(true);
            if (method.getParameterTypes().length > 0) {
                return null;
            }
            String id = connector.findID(method);
            int index = ann.value();
            return new Descriptor(id, index, new MethodBinding(method));
        }

    }

    private static class PortDescImpl extends PortDescriptor implements ControlInput.Link {

        private final TriggerControl control;

        private ControlInput port;

        private PortDescImpl(String id, int index, TriggerControl control) {
            super(id, Category.Action, index);
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
            try {
                control.trigger(time);
            } catch (Exception ex) {

            }
        }

        @Override
        public void receive(long time, Value value) {
            try {
                control.trigger(time);
            } catch (Exception ex) {

            }
        }

    }

}
