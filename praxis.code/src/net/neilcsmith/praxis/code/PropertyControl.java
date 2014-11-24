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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.code.userapi.OnChange;
import net.neilcsmith.praxis.code.userapi.OnError;
import net.neilcsmith.praxis.code.userapi.P;
import net.neilcsmith.praxis.code.userapi.Property;
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

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class PropertyControl extends Property implements Control {

    private final static Logger LOG = Logger.getLogger(PropertyControl.class.getName());

    private final ControlInfo info;
    private final Binding binding;
    private final MethodInvoker invoker;
    
    private CodeContext<?> context;
    
    private boolean latestSet;
    private long latest;

    private PropertyControl(ControlInfo info,
            Binding binding,
            Method onChange,
            Method onError) {
        this.info = info;
        this.binding = binding;
        if (onChange != null || onError != null) {
            this.invoker = new MethodInvoker(onChange, onError);
        } else {
            this.invoker = null;
        }
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
        try {
            binding.set(time, arg);
            setLatest(time);
            checkInvoke(time, false);
        } catch (Exception ex) {
            checkInvoke(time, true);
            throw ex;
        }
    }

    @Override
    protected void setImpl(long time, double value) throws Exception {
        try {
            binding.set(time, value);
            setLatest(time);
            checkInvoke(time, false);
        } catch (Exception ex) {
            checkInvoke(time, true);
            throw ex;
        }
    }
    
    private void checkInvoke(long time, boolean error) {
        if (invoker == null) {
            return;
        }
        invoker.lastError = error;
        context.invoke(time, invoker);
    }

    private void attach(CodeContext<?> context, Control previous) {
        this.context = context;
        binding.attach(context.getDelegate());
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
            if (argCount > 0) {
                if (isLatest(time)) {
                    finishAnimating();
                    setImpl(time, args.get(0));
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
    
    private class MethodInvoker implements CodeContext.Invoker {
        
        private final Method onChange, onError;
        private boolean lastError;
        
        private MethodInvoker(Method onChange, Method onError) {
            this.onChange = onChange;
            this.onError = onError;
        }

        @Override
        public void invoke() {
            if (lastError) {
                if (onError != null) {
                    try {
                        onError.invoke(context.getDelegate());
                    } catch (Exception ex) {
                        Logger.getLogger(PropertyControl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                if (onChange != null) {
                    try {
                        onChange.invoke(context.getDelegate());
                    } catch (Exception ex) {
                        Logger.getLogger(PropertyControl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        
    }

    public static abstract class Binding {
        
        protected void attach(CodeDelegate delegate) {
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

        private Argument argValue;
        private double dblValue;
        
        private DefaultBinding() {
            this.argValue = getDefaultValue();
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
            return Argument.info();
        }

        @Override
        public Argument getDefaultValue() {
            return PString.EMPTY;
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
        
        public static Descriptor create(CodeConnector<?> connector,
                P ann, Field field) {
            field.setAccessible(true);
            String id = connector.findID(field);
            int index = ann.value();
            Binding binding = findBinding(connector, field);
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
            ControlInfo info = ControlInfo.createPropertyInfo(
                new ArgumentInfo[]{binding.getArgumentInfo()},
                new Argument[]{binding.getDefaultValue()}, PMap.EMPTY);
            
            return new Descriptor(id, index, info, binding, propertyField, onChange, onError);
        }
        
        private static Binding findBinding(CodeConnector<?> connector, Field field) {
            Class<?> type = field.getType();
            Binding binding = null;
            if (field.isAnnotationPresent(Type.Number.class) || 
                    NumberBinding.isBindableFieldType(type)) {
                binding = NumberBinding.create(connector, field);
            } else if (field.isAnnotationPresent(Type.String.class) ||
                    StringBinding.isBindableFieldType(type)) {
                binding = StringBinding.create(connector, field);
            }
            if (binding == null) {
                binding = new DefaultBinding();
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
            } catch (Exception ex) {
                // @TODO log to user
            }
        }

        @Override
        public void receive(long time, Argument value) {
            try {
                control.setImpl(time, value);
            } catch (Exception ex) {
                // @TODO log to user
            }
        }

    }

}
