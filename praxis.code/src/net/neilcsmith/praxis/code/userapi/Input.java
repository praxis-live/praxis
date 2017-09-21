/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Neil C Smith.
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
 */
package net.neilcsmith.praxis.code.userapi;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.Value;
import net.neilcsmith.praxis.logging.LogLevel;
import net.neilcsmith.praxis.util.ArrayUtils;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
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

    public Linkable.Double values() {
        return new DoubleLink();
    }

    public <T> Linkable<T> valuesAs(Function<Value, T> converter) {
        return new ValueLink().map(converter);
    }

    public <T extends Value> Linkable<T> valuesAs(Class<T> type) {
        Value.Type.Converter<T> converter = Value.Type.findConverter(type);
        return new ValueLink()
                .map(v -> converter.from((Value) v))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public Input clearLinks() {
        links = new BaseLink[0];
        return this;
    }

    protected void updateLinks(double value) {
        for (BaseLink link : links) {
            try {
                link.update(value);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }
    }

    protected void updateLinks(Value value) {
        for (BaseLink link : links) {
            try {
                link.update(value);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        }
    }

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
            update(PNumber.valueOf(value));
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
