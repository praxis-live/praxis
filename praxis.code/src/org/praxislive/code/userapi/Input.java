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
package org.praxislive.code.userapi;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import org.praxislive.code.CodeContext;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.Value;
import org.praxislive.logging.LogLevel;
import org.praxislive.util.ArrayUtils;

/**
 * A field type providing a control input port. Use with @In or @AuxIn.
 */
public abstract class Input {

    private BaseLink[] links;
    private CodeContext<?> context;

    protected Input() {
        links = new BaseLink[0];
    }
    
    protected void attach(CodeContext<?> context) {
        this.context = context;
    }

    /**
     * Return a {@link Linkable.Double} for reacting on inputs. None numeric
     * inputs will be ignored.
     * 
     * @return Linkable.Double of input
     */
    public Linkable.Double values() {
        return new DoubleLink();
    }

    /**
     * Return a {@link Linkable} of inputs transformed by the provided converter
     * from Value to the required type.
     * 
     * @param <T>
     * @param converter convert Value to required type
     * @return Linkable of input
     */
    public <T> Linkable<T> valuesAs(Function<Value, T> converter) {
        return new ValueLink().map(converter);
    }

    /**
     * Return a {@link Linkable} of inputs as the provided Value subclass. If the
     * input Value cannot be coerced to the requested type it will be ignored.
     * 
     * @param <T>
     * @param type required Value subclass
     * @return Linkable of input
     */
    public <T extends Value> Linkable<T> valuesAs(Class<T> type) {
        Function<Value, Optional<T>> converter = Value.Type.of(type).converter();
        return new ValueLink()
                .map(converter::apply)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    /**
     * Clear all Linkables from this Input. All previously created Linkables will
     * cease to receive input values.
     * 
     * @return this
     */
    public Input clearLinks() {
        links = new BaseLink[0];
        return this;
    }

    /**
     *
     * @param value
     */
    protected void updateLinks(double value) {
        for (BaseLink link : links) {
            try {
                link.update(value);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }
    }

    /**
     *
     * @param value
     */
    protected void updateLinks(Value value) {
        for (BaseLink link : links) {
            try {
                link.update(value);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }
    }

    /**
     *
     * @param full
     */
    @Deprecated
    protected void reset(boolean full) {
        clearLinks();
    }

    private static interface BaseLink {

        void update(double value);

        void update(Value value);

    }

    private class DoubleLink implements BaseLink, Linkable.Double {

        private DoubleConsumer consumer;

        @Override
        public void link(DoubleConsumer consumer) {
            if (this.consumer != null) {
                throw new IllegalStateException("Cannot link multiple consumers in one chain");
            }
            this.consumer = Objects.requireNonNull(consumer);
            links = ArrayUtils.add(links, this);
        }

        @Override
        public void update(double value) {
            try {
                consumer.accept(value);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }

        @Override
        public void update(Value value) {
            PNumber.from(value).ifPresent((pn) -> consumer.accept(pn.value()));
        }

    }

    private class ValueLink implements BaseLink, Linkable<Value> {

        private Consumer<Value> consumer;

        @Override
        public void link(Consumer<Value> consumer) {
            if (this.consumer != null) {
                throw new IllegalStateException("Cannot link multiple consumers in one chain");
            }
            this.consumer = Objects.requireNonNull(consumer);
            links = ArrayUtils.add(links, this);
        }

        @Override
        public void update(double value) {
            update(PNumber.of(value));
        }

        @Override
        public void update(Value value) {
            try {
                consumer.accept(value);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }

    }
}
