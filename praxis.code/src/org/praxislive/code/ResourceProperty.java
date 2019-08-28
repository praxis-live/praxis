/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2019 Neil C Smith.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.praxislive.code.userapi.Config;
import org.praxislive.code.userapi.OnChange;
import org.praxislive.code.userapi.OnError;
import org.praxislive.code.userapi.P;
import org.praxislive.core.Value;
import org.praxislive.core.CallArguments;
import org.praxislive.core.Control;
import org.praxislive.core.Lookup;
import org.praxislive.core.Port;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.PortInfo;
import org.praxislive.core.services.TaskService;
import org.praxislive.core.types.PError;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PReference;
import org.praxislive.core.types.PResource;
import org.praxislive.core.types.PString;
import org.praxislive.logging.LogLevel;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public final class ResourceProperty<V> extends AbstractAsyncProperty<V> {

    private final static ControlInfo INFO = ControlInfo.createPropertyInfo(
            new ArgumentInfo[]{PResource.info(true)},
            new Value[]{PString.EMPTY},
            PMap.EMPTY);

    private final static ControlInfo PREF_INFO = ControlInfo.createPropertyInfo(
            new ArgumentInfo[]{PResource.info(true)},
            new Value[]{PString.EMPTY},
            PMap.of("preferred", true));

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
        setFieldValue();
        this.onChange = onChange;
        this.onError = onError;
    }

    private void setFieldValue() {
        try {
            V v = getValue();
            field.set(context.getDelegate(), v == null ? loader.getEmptyValue() : v);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            context.getLog().log(LogLevel.ERROR, ex);
        }
    }

    @Override
    protected TaskService.Task createTask(CallArguments keys) throws Exception {
        Value arg = keys.get(0);
        if (arg.isEmpty()) {
            return null;
        }
        Lookup lkp = context.getLookup();
        return new Task(loader, lkp, PResource.coerce(arg));
    }

    @Override
    protected void valueChanged(long time) {
        setFieldValue();
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
        public Value execute() throws Exception {
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
                    if (ret instanceof Value) {
                        return (Value) ret;
                    } else {
                        return PReference.of(ret);
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

        public V getEmptyValue() {
            return null;
        }

    }

    public static Loader<String> getStringLoader() {
        return StringLoader.INSTANCE;
    }

    private static class StringLoader extends Loader<String> {

        private final static StringLoader INSTANCE = new StringLoader();

        private StringLoader() {
            super(String.class);
        }

        @Override
        public String load(URI uri) throws IOException {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(uri.toURL().openStream(), StandardCharsets.UTF_8)
            )) {
                return br.lines().collect(Collectors.joining("\n"));
            } catch (IOException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public String getEmptyValue() {
            return "";
        }

    }

    public static class Descriptor<V> extends ControlDescriptor {

        private final Loader<V> loader;
        private final Field field;
        private final Method onChange, onError;
        private final ControlInfo info;

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
            this.info = field.isAnnotationPresent(Config.Preferred.class) ?
                    PREF_INFO : INFO;
        }

        @Override
        public ControlInfo getInfo() {
            return info;
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
            receive(time, PNumber.of(value));
        }

        @Override
        public void receive(long time, Value value) {
            dsc.control.portInvoke(time, value);
        }

    }

}
