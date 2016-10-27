/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Neil C Smith.
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import net.neilcsmith.praxis.code.userapi.OnChange;
import net.neilcsmith.praxis.code.userapi.OnError;
import net.neilcsmith.praxis.code.userapi.P;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.core.Lookup;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.info.PortInfo;
import net.neilcsmith.praxis.core.interfaces.TaskService;
import net.neilcsmith.praxis.core.types.PError;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PReference;
import net.neilcsmith.praxis.core.types.PResource;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.logging.LogLevel;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public final class ResourceProperty<V> extends AbstractAsyncProperty<V> {

    private final static ControlInfo INFO = ControlInfo.createPropertyInfo(
            new ArgumentInfo[]{PResource.info(true)},
            new Argument[]{PString.EMPTY},
            PMap.EMPTY);

    private final Loader<V> loader;
    private Field field;
    private Method onChange;
    private Method onError;
    private CodeContext<?> context;

    private ResourceProperty(Loader<V> loader) {
        super(PString.EMPTY, loader.getType(), null);
        this.loader = loader;
    }

    private void attach(CodeContext<?> context,
            Field field, Method onChange, Method onError) {
        super.attach(context);
        this.context = context;
        this.field = field;
        try {
            field.set(context.getDelegate(), getValue());
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            context.getLog().log(LogLevel.WARNING, ex);
        }
        this.onChange = onChange;
        this.onError = onError;
    }

    @Override
    protected TaskService.Task createTask(CallArguments keys) throws Exception {
        Argument arg = keys.get(0);
        if (arg.isEmpty()) {
            return null;
        }
        Lookup lkp = context.getLookup();
        return new Task(loader, lkp, PResource.coerce(arg));
    }

    @Override
    public ControlInfo getInfo() {
        return INFO;
    }

    @Override
    protected void valueChanged(long time) {
        try {
            field.set(context.getDelegate(), getValue());
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            context.getLog().log(LogLevel.ERROR, ex);
        }
        if (onChange != null) {
            context.invoke(time, onChange);
        }
    }

    @Override
    protected void taskError(long time, PError error) {
        if (onError != null) {
            context.invoke(time, onError);
        }
    }

    private static class Task implements TaskService.Task {

        private final PResource resource;
        private final Lookup lookup;
        private final Loader<?> loader;

        private Task(Loader<?> loader, Lookup lookup, PResource resource) {
            this.loader = loader;
            this.lookup = lookup;
            this.resource = resource;
        }

        @Override
        public Argument execute() throws Exception {
            List<URI> uris = resource.resolve(lookup);
            Exception caughtException = null;
            for (URI uri : uris) {
                try {
                    if ("file".equals(uri.getScheme())) {
                        if (!new File(uri).exists()) {
                            continue;
                        }
                    }
                    Object ret = loader.load(uri);
                    if (ret instanceof Argument) {
                        return (Argument) ret;
                    } else {
                        return PReference.wrap(ret);
                    }
                } catch (Exception exception) {
                    caughtException = exception;
                }
            }
            if (caughtException == null) {
                caughtException = new IOException("Unknown resource");
            }
            throw caughtException;
        }

    }

    public static abstract class Loader<V> {

        private final Class<V> type;

        protected Loader(Class<V> type) {
            this.type = Objects.requireNonNull(type);
        }

        public final Class<V> getType() {
            return type;
        }

        public abstract V load(URI uri) throws IOException;

    }

    public static class Descriptor<V> extends ControlDescriptor {

        private final Loader<V> loader;
        private final Field field;
        private final Method onChange, onError;

        private ResourceProperty<V> control;

        private Descriptor(
                String id,
                int index,
                Field field,
                Loader<V> loader,
                Method onChange,
                Method onError
        ) {
            super(id, Category.Property, index);
            this.loader = loader;
            this.field = field;
            this.onChange = onChange;
            this.onError = onError;
        }

        @Override
        public ControlInfo getInfo() {
            return INFO;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void attach(CodeContext<?> context, Control previous) {
            if (previous instanceof ResourceProperty
                    && ((ResourceProperty) previous).loader.getType() == loader.getType()) {
                control = (ResourceProperty<V>) previous;
            } else {
                control = new ResourceProperty<>(loader);
            }
            control.attach(context, field, onChange, onError);
        }

        @Override
        public Control getControl() {
            return control;
        }

        public PortDescriptor createPortDescriptor() {
            return new PortDescImpl(getID(), getIndex(), this);
        }

        public static <V> Descriptor<V> create(CodeConnector<?> connector, P ann,
                Field field, Loader<V> loader) {
            if (!field.getType().isAssignableFrom(loader.getType())) {
                return null;
            }
            field.setAccessible(true);
            String id = connector.findID(field);
            int index = ann.value();
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
            return new Descriptor<>(id, index, field, loader, onChange, onError);
        }

        private static Method extractMethod(CodeConnector<?> connector, String methodName) {
            try {
                Method m = connector.getDelegate().getClass().getDeclaredMethod(methodName);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException | SecurityException ex) {
                connector.getLog().log(LogLevel.WARNING, ex);
                return null;
            }
        }

    }

    private static class PortDescImpl extends PortDescriptor implements ControlInput.Link {

        private final Descriptor<?> dsc;

        private ControlInput port;

        private PortDescImpl(String id, int index, Descriptor<?> dsc) {
            super(id, PortDescriptor.Category.Property, index);
            this.dsc = dsc;
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
            receive(time, PNumber.valueOf(value));
        }

        @Override
        public void receive(long time, Argument value) {
            dsc.control.portInvoke(time, value);
        }

    }

}
