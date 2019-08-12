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
 */
package org.praxislive.code;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.praxislive.code.userapi.AuxIn;
import org.praxislive.code.userapi.AuxOut;
import org.praxislive.code.userapi.Data;
import org.praxislive.code.userapi.In;
import org.praxislive.code.userapi.Out;
import org.praxislive.core.Port;
import org.praxislive.core.PortConnectionException;
import org.praxislive.core.PortInfo;
import org.praxislive.core.PortListener;
import org.praxislive.core.types.PMap;
import org.praxislive.logging.LogLevel;

/**
 *
 * @author Neil C Smith
 */
public abstract class DataPort<T> implements Port {

    public final static class Input<T> extends DataPort<T> {

        private final InPipe<T> in;
        private final java.lang.reflect.Type type;
        private final PortInfo info;
        private final List<Output<T>> connections;
        private final PortListenerSupport pls;

        private Input(java.lang.reflect.Type type, PortInfo info) {
            this.type = Objects.requireNonNull(type);
            this.info = Objects.requireNonNull(info);
            in = new InPipe<>();
            connections = new ArrayList<>();
            pls = new PortListenerSupport(this);
        }

        @Override
        public void connect(Port port) throws PortConnectionException {
            if (port instanceof Output) {
                port.connect(this);
            } else {
                throw new PortConnectionException();
            }
        }

        @Override
        public void disconnect(Port port) {
            if (port instanceof Output) {
                port.disconnect(this);
            }
        }

        public PortInfo getInfo() {
            return info;
        }

        private void addDataOutputPort(Output<T> port, Data.Pipe<T> source) throws PortConnectionException {
            if (connections.contains(port)) {
                throw new PortConnectionException();
            }
            try {
                in.addSource(source);
                connections.add(port);
                pls.fireListeners();
            } catch (Exception ex) {
                throw new PortConnectionException(ex);
            }
        }

        private void removeDataOutputPort(Output<T> port, Data.Pipe<T> source) {
            if (connections.remove(port)) {
                in.removeSource(source);
                pls.fireListeners();
            }
        }

        @Override
        public void disconnectAll() {
            for (Output<T> connection : getConnections()) {
                disconnect(connection);
            }
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public Output<T>[] getConnections() {
            return connections.toArray(new Output[connections.size()]);
        }

        @Override
        public void addListener(PortListener listener) {
            pls.addListener(listener);
        }

        @Override
        public void removeListener(PortListener listener) {
            pls.removeListener(listener);
        }
    }
    
    private static class InPipe<T> extends Data.In<T> {
        
        private void reset(boolean full) {
            disconnectSinks();
            if (full) {
                clearCaches();
            }
        }
        
    }

    static final class InputDescriptor extends PortDescriptor {

        private final Field field;
        private final java.lang.reflect.Type type;
        private final PortInfo info;
        
        private Input<?> port;
        
        private InputDescriptor(String id,
                Category category,
                int index,
                Field field,
                java.lang.reflect.Type type) {
            super(id, category, index);
            this.field = field;
            this.type = type;
            this.info = PortInfo.create(DataPort.class,
                    PortInfo.Direction.IN,
                    PMap.of("category", typeToCategory(type)));
        }

        @Override
        @SuppressWarnings("unchecked")
        public void attach(CodeContext<?> context, Port previous) {
            if (previous instanceof Input && 
                    compatibleDataTypes(type, ((Input<?>) previous).type)) {
                port = (Input<?>) previous;
            } else {
                if (previous != null) {
                    previous.disconnectAll();
                }
                port = new Input<>(type, info);
            }
            try {
                field.set(context.getDelegate(), port.in);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }

        @Override
        public Port getPort() {
            return port;
        }

        @Override
        public PortInfo getInfo() {
            return info;
        }

        @Override
        public void reset(boolean full) {
            if (port != null) {
                port.in.reset(full);
            }
        }
        
        static InputDescriptor create(CodeConnector<?> connector, In ann, Field field) {
            return create(connector, PortDescriptor.Category.In, ann.value(), field);
        }
        
        static InputDescriptor create(CodeConnector<?> connector, AuxIn ann, Field field) {
            return create(connector, PortDescriptor.Category.AuxIn, ann.value(), field);
        }
        
        private static InputDescriptor create(CodeConnector<?> connector,
                PortDescriptor.Category category, int index, Field field) {
            if (Data.In.class.equals(field.getType()) && 
                    field.getGenericType() instanceof ParameterizedType) {
                java.lang.reflect.Type type = extractDataType((ParameterizedType) field.getGenericType());
                field.setAccessible(true);
                return new InputDescriptor(connector.findID(field), category, index, field, type);
            } else {
                return null;
            }
        }

    }

    public final static class Output<T> extends DataPort<T> {

        private final OutPipe<T> out;
        private final java.lang.reflect.Type type;
        private final PortInfo info;
        private final List<Input<T>> connections;
        private final PortListenerSupport pls;

        private Output(java.lang.reflect.Type type, PortInfo info) {
            this.type = Objects.requireNonNull(type);
            this.info = Objects.requireNonNull(info);
            out = new OutPipe<>();
            connections = new ArrayList<>();
            pls = new PortListenerSupport(this);
        }

        public final PortInfo getInfo() {
            return info;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void connect(Port port) throws PortConnectionException {
            if (port instanceof Input) {
                Input<T> iPort = (Input<T>) port;
                if (connections.contains(iPort)) {
                    throw new PortConnectionException();
                }
                if (!type.equals(iPort.type)) {
                    throw new PortConnectionException();
                }
                iPort.addDataOutputPort(this, out);
                connections.add(iPort);
                pls.fireListeners();
            } else {
                throw new PortConnectionException();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void disconnect(Port port) {
            if (port instanceof Input) {
                Input<T> iPort = (Input<T>) port;
                if (connections.contains(iPort)) {
                    iPort.removeDataOutputPort(this, out);
                    connections.remove(iPort);
                    pls.fireListeners();
                }
            }
        }

        @Override
        public void disconnectAll() {
            for (Input<T> port : getConnections()) {
                disconnect(port);
            }
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public Input<T>[] getConnections() {
            return connections.toArray(new Input[connections.size()]);
        }

        @Override
        public void addListener(PortListener listener) {
            pls.addListener(listener);
        }

        @Override
        public void removeListener(PortListener listener) {
            pls.removeListener(listener);
        }
    }

    private static class OutPipe<T> extends Data.Out<T> {
        
        private void reset(boolean full) {
            disconnectSources();
            if (full) {
                clearCaches();
            }
        }
        
    }
    
    static class OutputDescriptor extends PortDescriptor {

        private final Field field;
        private final java.lang.reflect.Type type;
        private final PortInfo info;
        
        private Output<?> port;
        
        private OutputDescriptor(String id,
                Category category,
                int index,
                Field field,
                java.lang.reflect.Type type) {
            super(id, category, index);
            this.field = field;
            this.type = type;
            this.info = PortInfo.create(DataPort.class,
                    PortInfo.Direction.OUT,
                    PMap.of("category", typeToCategory(type)));
        }

        @Override
        public void attach(CodeContext<?> context, Port previous) {
            if (previous instanceof Output && 
                    compatibleDataTypes(type, ((Output<?>) previous).type)) {
                port = (Output<?>) previous;
            } else {
                if (previous != null) {
                    previous.disconnectAll();
                }
                port = new Output<>(type, info);
            }
            try {
                field.set(context.getDelegate(), port.out);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }

        @Override
        public Port getPort() {
            return port;
        }

        @Override
        public PortInfo getInfo() {
            return info;
        }

        @Override
        public void reset(boolean full) {
            if (port != null) {
                port.out.reset(full);
            }
        }
        
        static OutputDescriptor create(CodeConnector<?> connector, Out ann, Field field) {
            return create(connector, PortDescriptor.Category.Out, ann.value(), field);
        }
        
        static OutputDescriptor create(CodeConnector<?> connector, AuxOut ann, Field field) {
            return create(connector, PortDescriptor.Category.AuxOut, ann.value(), field);
        }
        
        private static OutputDescriptor create(CodeConnector<?> connector,
                PortDescriptor.Category category, int index, Field field) {
            if (Data.Out.class.equals(field.getType()) && 
                    field.getGenericType() instanceof ParameterizedType) {
                java.lang.reflect.Type type = extractDataType((ParameterizedType) field.getGenericType());
                field.setAccessible(true);
                return new OutputDescriptor(connector.findID(field), category, index, field, type);
            } else {
                return null;
            }
        }

    }

    public static class Provider implements Port.TypeProvider {

        @Override
        public Stream<Type<?>> types() {
            return Stream.of(new Type<>(DataPort.class));
        }


    }

    
    private static java.lang.reflect.Type extractDataType(ParameterizedType type) {
        java.lang.reflect.Type[] types = type.getActualTypeArguments();
        if (types.length > 0) {
            return types[0];
        } else {
            return Object.class; // throw Exception here?
        }
    }
    
    private static boolean compatibleDataTypes(java.lang.reflect.Type type1, java.lang.reflect.Type type2) {
        return type1.equals(type2);
    }
    
    
    private static String typeToCategory(java.lang.reflect.Type type) {
        StringBuilder sb = new StringBuilder();
        buildSimpleName(sb, type);
        return sb.toString();
    }
    
    private static void buildSimpleName(StringBuilder sb, java.lang.reflect.Type type) {
        if (type instanceof Class) {
            sb.append(((Class<?>) type).getSimpleName());
        } else if (type instanceof ParameterizedType) {
            buildSimpleName(sb, ((ParameterizedType) type).getRawType());
            java.lang.reflect.Type[] parTypes = ((ParameterizedType) type).getActualTypeArguments();
            sb.append("<");
            for (int i = 0; i < parTypes.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                buildSimpleName(sb, parTypes[i]);
            }
            sb.append(">");
        } else if (type instanceof WildcardType) {
            java.lang.reflect.Type[] bounds = ((WildcardType) type).getLowerBounds();
            if (bounds.length > 0) {
                sb.append("? super ");
            } else {
                bounds = ((WildcardType) type).getUpperBounds();
                if (bounds.length > 0 && !Object.class.equals(bounds[0])) {
                    sb.append("? extends ");
                } else {
                    sb.append("?");
                    return;
                }
            }
            for (int i = 0; i < bounds.length; i++) {
                if (i > 0) {
                    sb.append(" & ");
                }
                buildSimpleName(sb, bounds[i]);
            }
        } else {
            sb.append("?");
        }
    }
    
}
