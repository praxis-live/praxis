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
package org.praxislive.code.userapi;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
abstract class AbstractLinkable<IN, OUT> implements Consumer<IN>, Linkable<OUT> {

    private final Linkable<IN> source;
    private Consumer<OUT> sink;

    AbstractLinkable(Linkable<IN> source) {
        this.source = Objects.requireNonNull(source);
    }

    @Override
    public void accept(IN value) {
        process(value, sink);
    }

    @Override
    public void link(Consumer<OUT> consumer) {
        this.sink = Objects.requireNonNull(consumer);
        source.link(this);
    }

    abstract void process(IN value, Consumer<OUT> sink);

    abstract static class ToDouble<IN> implements Consumer<IN>, Linkable.Double {

        private final Linkable<IN> source;
        private DoubleConsumer sink;

        ToDouble(Linkable<IN> source) {
            this.source = Objects.requireNonNull(source);
        }

        @Override
        public void accept(IN value) {
            process(value, sink);
        }

        @Override
        public void link(DoubleConsumer consumer) {
            this.sink = Objects.requireNonNull(consumer);
            source.link(this);
        }

        abstract void process(IN value, DoubleConsumer sink);
        
    }
    
    abstract static class ToInt<IN> implements Consumer<IN>, Linkable.Int {

        private final Linkable<IN> source;
        private IntConsumer sink;

        ToInt(Linkable<IN> source) {
            this.source = Objects.requireNonNull(source);
        }

        @Override
        public void accept(IN value) {
            process(value, sink);
        }

        @Override
        public void link(IntConsumer consumer) {
            this.sink = Objects.requireNonNull(consumer);
            source.link(this);
        }

        abstract void process(IN value, IntConsumer sink);
        
    }

    abstract static class Double implements DoubleConsumer, Linkable.Double {

        private final Linkable.Double source;
        private DoubleConsumer sink;

        Double(Linkable.Double source) {
            this.source = Objects.requireNonNull(source);
        }

        @Override
        public void accept(double value) {
            process(value, sink);
        }

        @Override
        public void link(DoubleConsumer consumer) {
            this.sink = Objects.requireNonNull(consumer);
            source.link(this);
        }

        abstract void process(double value, DoubleConsumer sink);

    }

    abstract static class DoubleToObj<OUT> implements DoubleConsumer, Linkable<OUT> {

        private final Linkable.Double source;
        private Consumer<OUT> sink;

        DoubleToObj(Linkable.Double source) {
            this.source = Objects.requireNonNull(source);
        }

        @Override
        public void accept(double value) {
            process(value, sink);
        }

        @Override
        public void link(Consumer<OUT> consumer) {
            this.sink = Objects.requireNonNull(consumer);
            source.link(this);
        }

        abstract void process(double value, Consumer<OUT> sink);

    }

    abstract static class Int implements IntConsumer, Linkable.Int {

        private final Linkable.Int source;
        private IntConsumer sink;

        Int(Linkable.Int source) {
            this.source = Objects.requireNonNull(source);
        }

        @Override
        public void accept(int value) {
            process(value, sink);
        }

        @Override
        public void link(IntConsumer consumer) {
            this.sink = Objects.requireNonNull(consumer);
            source.link(this);
        }

        abstract void process(int value, IntConsumer sink);

    }

    abstract static class IntToObj<OUT> implements IntConsumer, Linkable<OUT> {

        private final Linkable.Int source;
        private Consumer<OUT> sink;

        IntToObj(Linkable.Int source) {
            this.source = Objects.requireNonNull(source);
        }

        @Override
        public void accept(int value) {
            process(value, sink);
        }

        @Override
        public void link(Consumer<OUT> consumer) {
            this.sink = Objects.requireNonNull(consumer);
            source.link(this);
        }

        abstract void process(int value, Consumer<OUT> sink);

    }

}
