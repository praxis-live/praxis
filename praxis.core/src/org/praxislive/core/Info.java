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
 */
package org.praxislive.core;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PBoolean;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PString;

/**
 * Builder utilities for creating {@link ComponentInfo} and related classes.
 */
public class Info {

    private Info() {
    }

    /**
     * Create a ComponentInfoBuilder
     *
     * @return builder
     */
    public static ComponentInfoBuilder component() {
        return new ComponentInfoBuilder();
    }

    /**
     * Apply the provided function to a new ComponentInfoBuilder and return the
     * resulting ComponentInfo.
     *
     * @param cmp function to modify builder
     * @return ComponentInfo from builder
     */
    public static ComponentInfo component(UnaryOperator<ComponentInfoBuilder> cmp) {
        return cmp.apply(Info.component()).build();
    }

    /**
     * Get a PortInfoChooser to choose a PortInfoBuilder
     *
     * @return builder chooser
     */
    public static PortInfoChooser port() {
        return new PortInfoChooser();
    }

    /**
     * Apply the provided function to a PortInfoChooser to choose and customize
     * a PortInfoBuilder and return the resulting PortInfo.
     *
     * @param p function to choose and configure builder
     * @return PortInfo from builder
     */
    public static PortInfo port(Function<PortInfoChooser, PortInfoBuilder> p) {
        return p.apply(Info.port()).build();
    }

    /**
     * Get a ControlInfoChooser to choose a ControlInfoBuilder
     *
     * @return builder chooser
     */
    public static ControlInfoChooser control() {
        return new ControlInfoChooser();
    }

    /**
     * Apply the provided function to a ControlInfoChooser to choose and
     * customize a ControlInfoBuilder and return the resulting ControlInfo.
     *
     * @param c function to choose and configure builder
     * @return ControlInfo from builder
     */
    public static ControlInfo control(Function<ControlInfoChooser, ControlInfoBuilder<?>> c) {
        return c.apply(Info.control()).build();
    }

    /**
     * Get an ArgumentInfoChooser to choose an ArgumentInfoBuilder.
     *
     * @return builder chooser
     */
    public static ArgumentInfoChooser argument() {
        return new ArgumentInfoChooser();
    }

    /**
     * Apply the provided function to an ArgumentInfoChooser to choose and
     * configure an ArgumentInfoBuilder and return the resulting ArgumentInfo.
     *
     * @param a function to choose and configure builder
     * @return ArgumentInfo from builder
     */
    public static ArgumentInfo argument(Function<ArgumentInfoChooser, ArgumentInfoBuilder<?>> a) {
        return a.apply(Info.argument()).build();
    }

    /**
     * ComponentInfoBuilder class
     */
    public final static class ComponentInfoBuilder {

        private final PMap.Builder controls;
        private final PMap.Builder ports;
        private Set<PString> protocols;
        private PMap.Builder properties;

        ComponentInfoBuilder() {
            controls = PMap.builder();
            ports = PMap.builder();
        }

        /**
         * Add control info.
         *
         * @param id control ID
         * @param info control info
         * @return this
         */
        public ComponentInfoBuilder control(String id, ControlInfo info) {
            controls.put(id, info);
            return this;
        }

        /**
         * Add control info by applying the supplied function to choose and
         * configure a builder.
         *
         * @param id control ID
         * @param ctrl function to choose and configure builder
         * @return this
         */
        public ComponentInfoBuilder control(String id,
                Function<ControlInfoChooser, ControlInfoBuilder<?>> ctrl) {
            control(id, ctrl.apply(new ControlInfoChooser()).build());
            return this;
        }

        /**
         * Add port info.
         *
         * @param id port ID
         * @param info port info
         * @return this
         */
        public ComponentInfoBuilder port(String id, PortInfo info) {
            ports.put(id, info);
            return this;
        }

        /**
         * Add port info by applying the supplied function to choose and
         * configure a builder.
         *
         * @param id port ID
         * @param p function to choose and configure builder
         * @return this
         */
        public ComponentInfoBuilder port(String id,
                Function<PortInfoChooser, PortInfoBuilder> p) {
            port(id, Info.port(p));
            return this;
        }

        /**
         * Add custom property.
         *
         * @param key String key
         * @param value Object value
         * @return this
         */
        public ComponentInfoBuilder property(String key, Object value) {
            if (properties == null) {
                properties = PMap.builder();
            }
            properties.put(key, value);
            return this;
        }

        /**
         * Add a protocol.
         *
         * @param protocol Class extending Protocol
         * @return this
         */
        public ComponentInfoBuilder protocol(Class<? extends Protocol> protocol) {
            if (protocols == null) {
                protocols = new CopyOnWriteArraySet<>();
            }
            protocols.add(PString.of(Protocol.Type.of(protocol).name()));
            return this;
        }

        /**
         * Merge all elements of the provided ComponentInfo.
         *
         * @param info ComponentInfo to merge
         * @return this
         */
        public ComponentInfoBuilder merge(ComponentInfo info) {
            for (String id : info.getControls()) {
                controls.put(id, info.controlInfo(id));
            }
            for (String id : info.getPorts()) {
                ports.put(id, info.portInfo(id));
            }
            for (String key : info.properties().getKeys()) {
                property(key, info.properties().get(key));
            }
            info.protocols().forEach(this::protocol);
            return this;
        }

        protected ComponentInfo build() {
            return new ComponentInfo(protocols == null ? PArray.EMPTY : PArray.of(protocols),
                    controls.build(),
                    ports.build(),
                    properties == null ? PMap.EMPTY : properties.build(),
                    null);
        }

    }

    /**
     * Helper class for choosing a ControlInfoBuilder type.
     */
    public final static class ControlInfoChooser {

        ControlInfoChooser() {
        }

        /**
         * Create a PropertyInfoBuilder for a property.
         *
         * @return builder
         */
        public PropertyInfoBuilder property() {
            return new PropertyInfoBuilder();
        }

        /**
         * Create a ReadOnlyPropertyBuilder for a property.
         *
         * @return builder
         */
        public ReadOnlyPropertyInfoBuilder readOnlyProperty() {
            return new ReadOnlyPropertyInfoBuilder();
        }

        /**
         * Create a FunctionInfoBuilder
         *
         * @return builder
         */
        public FunctionInfoBuilder function() {
            return new FunctionInfoBuilder();
        }

        /**
         * Create an ActionInfoBuilder
         *
         * @return builder
         */
        public ActionInfoBuilder action() {
            return new ActionInfoBuilder();
        }

    }

    /**
     * Abstract base class for ControlInfo builders.
     *
     * @param <T> concrete builder type
     */
    public static abstract class ControlInfoBuilder<T extends ControlInfoBuilder<T>> {

        private final ControlInfo.Type type;
        private PMap.Builder properties;

        ArgumentInfo[] inputs;
        ArgumentInfo[] outputs;
        Value[] defaults;

        ControlInfoBuilder(ControlInfo.Type type) {
            this.type = type;
            inputs = new ArgumentInfo[0];
            outputs = new ArgumentInfo[0];
            defaults = new Value[0];
        }

        /**
         * Add custom property.
         *
         * @param key String key
         * @param value Object value
         * @return this
         */
        @SuppressWarnings("unchecked")
        public T property(String key, Object value) {
            if (properties == null) {
                properties = PMap.builder();
            }
            properties.put(key, value);
            return (T) this;
        }

        ControlInfo build() {
            return new ControlInfo(inputs, outputs, defaults, type,
                    properties == null ? PMap.EMPTY : properties.build(), null);
        }

    }

    /**
     * Builder for ControlInfo of property controls.
     */
    public static final class PropertyInfoBuilder extends ControlInfoBuilder<PropertyInfoBuilder> {

        PropertyInfoBuilder() {
            super(ControlInfo.Type.Property);
        }

        /**
         * Add input ArgumentInfo.
         *
         * @param info
         * @return this
         */
        public PropertyInfoBuilder input(ArgumentInfo info) {
            inputs = new ArgumentInfo[]{info};
            outputs = inputs;
            return this;
        }

        /**
         * Add input ArgumentInfo for the provided value type.
         *
         * @param type value type
         * @return this
         */
        public PropertyInfoBuilder input(Class<? extends Value> type) {
            return input(ArgumentInfo.of(type));
        }

        /**
         * Add input ArgumentInfo by applying the provided function to choose
         * and configure an ArgumentInfoBuilder.
         *
         * @param a function to choose and configure builder
         * @return this
         */
        public PropertyInfoBuilder input(Function<ArgumentInfoChooser, ArgumentInfoBuilder<?>> a) {
            return input(Info.argument(a));
        }

        /**
         * Add a default value for this property.
         *
         * @param value default value
         * @return this
         */
        public PropertyInfoBuilder defaultValue(Value value) {
            defaults = new Value[]{value};
            return this;
        }

    }

    /**
     * Builder for ControlInfo of read-only properties.
     */
    public static final class ReadOnlyPropertyInfoBuilder extends ControlInfoBuilder<ReadOnlyPropertyInfoBuilder> {

        ReadOnlyPropertyInfoBuilder() {
            super(ControlInfo.Type.ReadOnlyProperty);
        }

        /**
         * Add output ArgumentInfo.
         *
         * @param info
         * @return this
         */
        public ReadOnlyPropertyInfoBuilder output(ArgumentInfo info) {
            outputs = new ArgumentInfo[]{info};
            return this;
        }

        /**
         * Add output ArgumentInfo for the provided value type.
         *
         * @param type value type
         * @return this
         */
        public ReadOnlyPropertyInfoBuilder output(Class<? extends Value> type) {
            return output(ArgumentInfo.of(type));
        }

        /**
         * Add output ArgumentInfo by applying the provided function to choose
         * and configure an ArgumentInfoBuilder.
         *
         * @param a function to choose and configure builder
         * @return this
         */
        public ReadOnlyPropertyInfoBuilder output(Function<ArgumentInfoChooser, ArgumentInfoBuilder<?>> a) {
            return output(Info.argument(a));
        }

    }

    /**
     * Builder for ControlInfo for function controls.
     */
    public static final class FunctionInfoBuilder extends ControlInfoBuilder<FunctionInfoBuilder> {

        FunctionInfoBuilder() {
            super(ControlInfo.Type.Function);
        }

        /**
         * Add ArgumentInfo for function inputs.
         *
         * @param inputs info for inputs
         * @return this
         */
        public FunctionInfoBuilder inputs(ArgumentInfo... inputs) {
            this.inputs = inputs;
            return this;
        }

        /**
         * Add ArgumentInfo for function inputs by applying the provided
         * functions to choose and configure ArgumentInfoBuilders.
         *
         * @param inputs functions to choose and configure builders
         * @return this
         */
        @SafeVarargs
        public final FunctionInfoBuilder inputs(Function<ArgumentInfoChooser, ArgumentInfoBuilder<?>>... inputs) {
            return inputs(Stream.of(inputs).map(f -> Info.argument(f)).toArray(ArgumentInfo[]::new));
        }

        /**
         * Add ArgumentInfo for function outputs.
         *
         * @param outputs info for outputs
         * @return this
         */
        public FunctionInfoBuilder outputs(ArgumentInfo... outputs) {
            this.outputs = outputs;
            return this;
        }

        /**
         * Add ArgumentInfo for function outputs by applying the provided
         * functions to choose and configure ArgumentInfoBuilders.
         *
         * @param outputs functions to choose and configure builders
         * @return this
         */
        public FunctionInfoBuilder outputs(Function<ArgumentInfoChooser, ArgumentInfoBuilder<?>>... outputs) {
            return outputs(Stream.of(outputs).map(f -> Info.argument(f)).toArray(ArgumentInfo[]::new));
        }

    }

    /**
     * Builder for ControlInfo of action controls.
     */
    public static final class ActionInfoBuilder extends ControlInfoBuilder<ActionInfoBuilder> {

        ActionInfoBuilder() {
            super(ControlInfo.Type.Action);
        }

    }

    /**
     * Helper class for choosing an ArgumentInfoBuilder type.
     */
    public static final class ArgumentInfoChooser {

        ArgumentInfoChooser() {
        }

        /**
         * Create a ValueInfoBuilder for the provided value type.
         *
         * @param cls type of value
         * @return builder
         */
        public ValueInfoBuilder type(Class<? extends Value> cls) {
            return new ValueInfoBuilder(Value.Type.of(cls));
        }

        /**
         * Create a NumberInfoBuilder for numeric values.
         *
         * @return builder
         */
        public NumberInfoBuilder number() {
            return new NumberInfoBuilder();
        }

        /**
         * Create a StringInfoBuilder for string values.
         *
         * @return builder
         */
        public StringInfoBuilder string() {
            return new StringInfoBuilder();
        }

    }

    /**
     * Abstract base class for ArgumentInfoBuilders.
     *
     * @param <T> concrete builder type
     */
    public static abstract class ArgumentInfoBuilder<T extends ArgumentInfoBuilder<T>> {

        private final Value.Type<?> type;
        private PMap.Builder properties;

        ArgumentInfoBuilder(Value.Type<?> type) {
            this.type = type;
        }

        /**
         * Add custom property.
         *
         * @param key String key
         * @param value Object value
         * @return this
         */
        @SuppressWarnings("unchecked")
        public T property(String key, Value value) {
            if (properties == null) {
                properties = PMap.builder();
            }
            properties.put(key, value);
            return (T) this;
        }

        ArgumentInfo build() {
            return new ArgumentInfo(type, ArgumentInfo.Presence.Always,
                    properties == null ? PMap.EMPTY : properties.build(), null);
        }

    }

    /**
     * Builder for ArgumentInfo of any Value type.
     */
    public static final class ValueInfoBuilder extends ArgumentInfoBuilder<ValueInfoBuilder> {

        ValueInfoBuilder(Value.Type<?> type) {
            super(type);
        }

    }

    /**
     * Builder for ArgumentInfo of PNumber.
     */
    public static final class NumberInfoBuilder extends ArgumentInfoBuilder<NumberInfoBuilder> {

        private final static Value.Type<PNumber> TYPE = Value.Type.of(PNumber.class);

        NumberInfoBuilder() {
            super(TYPE);
        }

        /**
         * Set minimum value property.
         *
         * @param min minimum value
         * @return this
         */
        public NumberInfoBuilder min(double min) {
            return property(PNumber.KEY_MINIMUM, PNumber.of(min));
        }

        /**
         * Set maximum value property.
         *
         * @param max maximum value
         * @return this
         */
        public NumberInfoBuilder max(double max) {
            return property(PNumber.KEY_MAXIMUM, PNumber.of(max));
        }

        /**
         * Set skew value property.
         *
         * @param skew skew value
         * @return this
         */
        public NumberInfoBuilder skew(double skew) {
            return property(PNumber.KEY_MINIMUM, PNumber.of(skew));
        }

    }

    /**
     * Builder for ArgumentInfo of PString.
     */
    public static final class StringInfoBuilder extends ArgumentInfoBuilder<StringInfoBuilder> {

        private final static Value.Type<PString> TYPE = Value.Type.of(PString.class);

        StringInfoBuilder() {
            super(TYPE);
        }

        /**
         * Set allowed values property.
         *
         * @param values allowed values
         * @return this
         */
        public StringInfoBuilder allowed(String... values) {
            return property(ArgumentInfo.KEY_ALLOWED_VALUES,
                    Stream.of(values).map(PString::of).collect(PArray.collector()));
        }

        /**
         * Set suggested values property.
         *
         * @param values suggested values
         * @return this
         */
        public StringInfoBuilder suggested(String... values) {
            return property(ArgumentInfo.KEY_SUGGESTED_VALUES,
                    Stream.of(values).map(PString::of).collect(PArray.collector()));
        }

        /**
         * Set empty is default property.
         *
         * @return this
         */
        public StringInfoBuilder emptyIsDefault() {
            return property(ArgumentInfo.KEY_EMPTY_IS_DEFAULT, PBoolean.TRUE);
        }

        /**
         * Set the template property.
         *
         * @param template
         * @return this
         */
        public StringInfoBuilder template(String template) {
            return property(ArgumentInfo.KEY_TEMPLATE, PString.of(template));
        }

        /**
         * Set the mime type property.
         *
         * @param mime
         * @return this
         */
        public StringInfoBuilder mime(String mime) {
            return property(ArgumentInfo.KEY_MIME_TYPE, PString.of(mime));
        }

    }

    /**
     * Helper class to choose a PortInfoBuilder type.
     */
    public static final class PortInfoChooser {

        PortInfoChooser() {
        }

        /**
         * Create a PortInfoBuilder for input ports of the provided base type.
         *
         * @param type base Port type
         * @return builder
         */
        public PortInfoBuilder input(Class<? extends Port> type) {
            return new PortInfoBuilder(Port.Type.of(type), PortInfo.Direction.IN);
        }

        /**
         * Create a PortInfoBuilder for output ports of the provided base type.
         *
         * @param type base Port type
         * @return builder
         */
        public PortInfoBuilder output(Class<? extends Port> type) {
            return new PortInfoBuilder(Port.Type.of(type), PortInfo.Direction.OUT);
        }

    }

    /**
     * PortInfoBuilder
     */
    public static final class PortInfoBuilder {

        private final Port.Type<?> type;
        private final PortInfo.Direction direction;
        private PMap.Builder properties;

        PortInfoBuilder(Port.Type<?> type, PortInfo.Direction direction) {
            this.type = type;
            this.direction = direction;
        }

        /**
         * Add custom property.
         *
         * @param key String key
         * @param value Object value
         * @return this
         */
        public PortInfoBuilder property(String key, Object value) {
            if (properties == null) {
                properties = PMap.builder();
            }
            properties.put(key, value);
            return this;
        }

        PortInfo build() {
            return new PortInfo(type, direction,
                    properties == null ? PMap.EMPTY : properties.build(), null);
        }

    }

}
