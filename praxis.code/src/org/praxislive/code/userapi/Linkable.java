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

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

/**
 * Linkable is a lightweight form of reactive stream for listening to changing
 * values from inputs, properties, animation, etc. Functions can be used to
 * filter and map incoming values. Linkables must be {@link #link linked} to a
 * Consumer to complete the pipeline or no values will be processed.
 * <p>
 * Only stateless operations are currently supported. Operations that require
 * access to previous values (limit, sort, distinct, etc.) require combining
 * with one of the other mechanisms (eg. {@link Inject} or {@link Ref}) for
 * retaining state across code changes.
 *
 * @param <T>
 */
public interface Linkable<T> {

    /**
     * Link to a Consumer to process values. Setting a Consumer completes the
     * pipeline. Only one Consumer may be set on a Linkable pipeline - to use
     * multiple consumers, acquire a new Linkable from the original source.
     *
     * @param consumer function to process received values.
     */
    public void link(Consumer<T> consumer);

    /**
     * Returns a Linkable that wraps this Linkable and transforms values using
     * the provided mapping function.
     *
     * @param <R>
     * @param function transform values
     * @return
     */
    public default <R> Linkable<R> map(Function<? super T, ? extends R> function) {
        return new AbstractLinkable<T, R>(this) {
            @Override
            void process(T value, Consumer<R> sink) {
                sink.accept(function.apply(value));
            }
        };
    }

    /**
     * Returns a Linkable that wraps this Linkable and filters values using the
     * provided predicate function.
     *
     * @param predicate
     * @return
     */
    public default Linkable<T> filter(Predicate<? super T> predicate) {
        return new AbstractLinkable<T, T>(this) {
            @Override
            void process(T value, Consumer<T> sink) {
                if (predicate.test(value)) {
                    sink.accept(value);
                }
            }
        };
    }

    /**
     * A double primitive specialisation of Linkable.
     */
    public static interface Double {

        /**
         * Link to a Consumer to process values. Setting a Consumer completes
         * the pipeline. Only one Consumer may be set on a Linkable pipeline -
         * to use multiple consumers, acquire a new Linkable from the original
         * source.
         *
         * @param consumer function to process received values.
         */
        public void link(DoubleConsumer consumer);

        /**
         * Returns a Linkable.Double that wraps this Linkable.Double and transforms
         * values using the provided mapping function.
         *
         * @param function transform values
         * @return
         */
        public default Linkable.Double map(DoubleUnaryOperator function) {
            return new AbstractLinkable.Double(this) {
                @Override
                void process(double value, DoubleConsumer sink) {
                    sink.accept(function.applyAsDouble(value));
                }
            };
        }
        
        /**
         * Returns a Linkable that wraps this Linkable.Double and transforms
         * values using the provided mapping function.
         *
         * @param <R> generic type of returned Linkable
         * @param function transform values
         * @return Linkable
         */
        public default <R> Linkable<R> mapTo(DoubleFunction<? extends R> function) {
            return new AbstractLinkable.DoubleToObj<R>(this) {
                @Override
                void process(double value, Consumer<R> sink) {
                    sink.accept(function.apply(value));
                }
            };
        }

        /**
         * Returns a Linkable.Double that wraps this Linkable.Double and filters 
         * values using the provided predicate function.
         *
         * @param predicate
         * @return
         */
        public default Linkable.Double filter(DoublePredicate predicate) {
            return new AbstractLinkable.Double(this) {
                @Override
                void process(double value, DoubleConsumer sink) {
                    if (predicate.test(value)) {
                        sink.accept(value);
                    }
                }
            };
        }

    }

    /**
     * An int primitive specialisation of Linkable.
     */
    public static interface Int {

        /**
         * Link to a Consumer to process values. Setting a Consumer completes
         * the pipeline. Only one Consumer may be set on a Linkable pipeline -
         * to use multiple consumers, acquire a new Linkable from the original
         * source.
         *
         * @param consumer function to process received values.
         */
        public void link(IntConsumer consumer);

        /**
         * Returns a Linkable.Int that wraps this Linkable.Int and transforms
         * values using the provided mapping function.
         *
         * @param function transform values
         * @return
         */
        public default Linkable.Int map(IntUnaryOperator function) {
            return new AbstractLinkable.Int(this) {
                @Override
                void process(int value, IntConsumer sink) {
                    sink.accept(function.applyAsInt(value));
                }
            };
        }

        /**
         * Returns a Linkable that wraps this Linkable.Int and transforms
         * values using the provided mapping function.
         *
         * @param <R> generic type of returned Linkable
         * @param function transform values
         * @return Linkable
         */
        public default <R> Linkable<R> mapTo(IntFunction<? extends R> function) {
            return new AbstractLinkable.IntToObj<R>(this) {
                @Override
                void process(int value, Consumer<R> sink) {
                    sink.accept(function.apply(value));
                }
            };
        }
        
        /**
         * Returns a Linkable.Int that wraps this Linkable.Int and filters 
         * values using the provided predicate function.
         *
         * @param predicate
         * @return
         */
        public default Linkable.Int filter(IntPredicate predicate) {
            return new AbstractLinkable.Int(this) {
                @Override
                void process(int value, IntConsumer sink) {
                    if (predicate.test(value)) {
                        sink.accept(value);
                    }
                }
            };
        }

    }

}
