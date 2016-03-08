/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Neil C Smith.
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
import java.lang.reflect.Method;
import net.neilcsmith.praxis.code.userapi.Inject;
import net.neilcsmith.praxis.code.userapi.OnChange;
import net.neilcsmith.praxis.code.userapi.OnError;
import net.neilcsmith.praxis.code.userapi.P;
import net.neilcsmith.praxis.code.userapi.Property;
import net.neilcsmith.praxis.code.userapi.ReadOnly;
import net.neilcsmith.praxis.code.userapi.Transient;
import net.neilcsmith.praxis.code.userapi.Type;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.Call;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.PacketRouter;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.info.PortInfo;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.logging.LogLevel;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class PropertyControl extends Property implements Control {

    private final ControlInfo info;
    private final Binding binding;
    private final Method onChange;
    private final Method onError;
    private final boolean readOnly;

    private CodeContext<?> context;

    private boolean latestSet;
    private long latest;

    private PropertyControl(ControlInfo info,
            Binding binding,
            Method onChange,
            Method onError) {
        this.info = info;
        this.binding = binding;
        this.readOnly = info == null
                || info.getType() == ControlInfo.Type.ReadOnlyProperty;
        this.onChange = onChange;
        this.onError = onError;
    }

    @Override
    protected Argument getImpl() {
        return binding.get();
    }

    @Override
    protected double getImpl(double def) {
        return binding.get(def);
    }

    @Override
    protected void setImpl(long time, Argument arg) throws Exception {
        binding.set(time, arg);
        setLatest(time);
    }

    @Override
    protected void setImpl(long time, double value) throws Exception {
        binding.set(time, value);
        setLatest(time);

    }

    private void checkInvoke(long time, boolean error) {
        if (error) {
            if (onError != null) {
                context.invoke(time, onError);
            }
        } else {
            if (onChange != null) {
                context.invoke(time, onChange);
            }
        }
    }

    private void attach(CodeContext<?> context, Control previous) {
        this.context = context;
        binding.attach(context);
        if (previous instanceof PropertyControl) {
            PropertyControl pc = (PropertyControl) previous;
            latest = pc.latest;
            latestSet = pc.latestSet;
            try {
                binding.set(latest, pc.binding.get());
            } catch (Exception ex) {
                // do nothing?
            }
            super.attach(context, (Property) previous);
        } else {
            super.attach(context, null);
        }
    }

    @Override
    public void call(Call call, PacketRouter router) throws Exception {
        Call.Type type = call.getType();
        if (type == Call.Type.INVOKE || type == Call.Type.INVOKE_QUIET) {
            CallArguments args = call.getArgs();
            int argCount = args.getSize();
            long time = call.getTimecode();
            if (argCount > 0 && !readOnly) {
                if (isLatest(time)) {
                    finishAnimating();
                    try {
                        setImpl(time, args.get(0));
                        checkInvoke(time, false);
                    } catch (Exception ex) {
                        checkInvoke(time, true);
                        throw ex;
                    }
                }
                if (type == Call.Type.INVOKE) {
                    router.route(Call.createReturnCall(call, args));
                }
            } else {
                // ignore quiet hint?
                router.route(Call.createReturnCall(call, get()));
            }
        } else {
//            throw new IllegalArgumentException();
        }
    }

    @Override
    public ControlInfo getInfo() {
        return info;
    }

    private void setLatest(long time) {
        latestSet = true;
        latest = time;
    }

    private boolean isLatest(long time) {
        if (latestSet) {
            return (time - latest) >= 0;
        } else {
            return true;
        }

    }

    public static abstract class Binding {

        protected void attach(CodeContext<?> context) {
            // no op hook
        }

        public abstract void set(long time, Argument value) throws Exception;

        public abstract void set(long time, double value) throws Exception;

        public abstract Argument get();

        public double get(double def) {
            try {
                return PNumber.coerce(get()).value();
            } catch (ArgumentFormatException ex) {
                return def;
            }
        }

        public abstract ArgumentInfo getArgumentInfo();

        public abstract Argument getDefaultValue();

    }

    private static class DefaultBinding extends Binding {

        private final ArgumentInfo argInfo;
        private final Argument def;

        private Argument argValue;
        private double dblValue;

        private DefaultBinding() {
            this(Argument.info(), PString.EMPTY);
        }

        private DefaultBinding(ArgumentInfo argInfo, Argument def) {
            this.argInfo = argInfo;
            this.def = def;
            this.argValue = def;
        }

        @Override
        public void set(long time, Argument value) throws Exception {
            this.argValue = value;
        }

        @Override
        public void set(long time, double value) throws Exception {
            this.dblValue = value;
            this.argValue = null;
        }

        @Override
        public Argument get() {
            if (argValue == null) {
                return PNumber.valueOf(dblValue);
            } else {
                return argValue;
            }
        }

        @Override
        public double get(double def) {
            if (argValue == null) {
                return dblValue;
            } else {
                return super.get(def);
            }
        }

        @Override
        public ArgumentInfo getArgumentInfo() {
            return argInfo;
        }

        @Override
        public Argument getDefaultValue() {
            return def;
        }

    }

    public static class Descriptor extends ControlDescriptor {

        private final PropertyControl control;
        private final Field propertyField;

        private Descriptor(String id,
                int index,
                ControlInfo info,
                Binding binding,
                Field field,
                Method onChange,
                Method onError
        ) {
            super(id, Category.Property, index);
            control = new PropertyControl(info, binding, onChange, onError);
            this.propertyField = field;
        }

        private Descriptor(String id, int index, Binding binding, Field field) {
            super(id, Category.Synthetic, index);
            control = new PropertyControl(null, binding, null, null);
            propertyField = field;
        }

        @Override
        public ControlInfo getInfo() {
            return control.info;
        }

        @Override
        public void attach(CodeContext<?> context, Control previous) {
            control.attach(context, previous);
            if (propertyField != null) {
                try {
                    propertyField.set(context.getDelegate(), control);
                } catch (Exception ex) {
                    context.getLog().log(LogLevel.ERROR, ex);
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

        public static Descriptor create(CodeConnector<?> connector,
                P ann, Field field) {
            Binding binding = findBinding(connector, field);
            if (binding == null) {
                return null;
            }
            return create(connector, ann.value(), field, binding);
        }

        private static Descriptor create(CodeConnector<?> connector, int index,
                Field field, Binding binding) {
            field.setAccessible(true);
            String id = connector.findID(field);
            Method onChange = null;
            Method onError = null;
            OnChange onChangeAnn = field.getAnnotation(OnChange.class);
            if (onChangeAnn != null) {
                onChange = extractMethod(connector, onChangeAnn.value());
            }
            OnError onErrorAnn = field.getAnnotation(OnError.class);
            if (onErrorAnn != null) {
                onError = extractMethod(connector, onErrorAnn.value());
            }
            Field propertyField = null;
            if (Property.class.isAssignableFrom(field.getType())) {
                propertyField = field;
            }
            ControlInfo info;
            if (field.isAnnotationPresent(ReadOnly.class)) {
                info = ControlInfo.createReadOnlyPropertyInfo(
                        new ArgumentInfo[]{binding.getArgumentInfo()},
                        PMap.EMPTY);
            } else {
                info = ControlInfo.createPropertyInfo(
                        new ArgumentInfo[]{binding.getArgumentInfo()},
                        new Argument[]{binding.getDefaultValue()},
                        field.isAnnotationPresent(Transient.class)
                                ? PMap.create(ControlInfo.KEY_TRANSIENT, true)
                                : PMap.EMPTY);
            }
            return new Descriptor(id, index, info, binding, propertyField, onChange, onError);
        }

        static Descriptor create(CodeConnector<?> connector, Inject ann, Field field) {
            Binding binding = findBinding(connector, field);
            if (binding == null) {
                return null;
            }
            field.setAccessible(true);
            String id = connector.findID(field);
            int index = connector.getSyntheticIndex();
            Field propertyField = null;
            if (Property.class.isAssignableFrom(field.getType())) {
                propertyField = field;
            }
            return new Descriptor(id, index, binding, propertyField);
        }

        private static Binding findBinding(CodeConnector<?> connector, Field field) {
            Class<?> type = field.getType();
            Binding binding = null;
            if (field.isAnnotationPresent(Type.Number.class)
                    || NumberBinding.isBindableFieldType(type)) {
                binding = NumberBinding.create(connector, field);
            } else if (field.isAnnotationPresent(Type.Integer.class)
                    || IntegerBinding.isBindableFieldType(type)) {
                binding = IntegerBinding.create(connector, field);
            } else if (field.isAnnotationPresent(Type.String.class)
                    || StringBinding.isBindableFieldType(type)) {
                binding = StringBinding.create(connector, field);
            } else if (field.isAnnotationPresent(Type.Boolean.class)
                    || BooleanBinding.isBindableFieldType(type)) {
                binding = BooleanBinding.create(connector, field);
            }

            if (binding == null && Property.class.isAssignableFrom(type)) {
                Type typeAnn = field.getAnnotation(Type.class);
                if (typeAnn != null) {
                    binding = new DefaultBinding(
                            ArgumentInfo.create(typeAnn.cls(), PMap.EMPTY),
                            PString.EMPTY);
                } else {
                    binding = new DefaultBinding();
                }
            }

            return binding;
        }

        private static Method extractMethod(CodeConnector<?> connector, String methodName) {
            try {
                Method m = connector.getDelegate().getClass().getDeclaredMethod(methodName);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException ex) {

            } catch (Exception ex) {

            }
            return null;
        }

    }

    private static class PortDescImpl extends PortDescriptor implements ControlInput.Link {

        private final PropertyControl control;

        private ControlInput port;

        private PortDescImpl(String id, int index, PropertyControl control) {
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
            try {
                control.setImpl(time, value);
                control.checkInvoke(time, false);
            } catch (Exception ex) {
                control.checkInvoke(time, true);
                // @TODO log to user
            }
        }

        @Override
        public void receive(long time, Argument value) {
            try {
                control.setImpl(time, value);
                control.checkInvoke(time, false);
            } catch (Exception ex) {
                control.checkInvoke(time, true);
                // @TODO log to user
            }
        }

    }

}
