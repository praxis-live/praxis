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
 *
 */
package net.neilcsmith.praxis.code.userapi;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public interface Linkable<T> {

    public void link(Consumer<T> consumer);

    public default <R> Linkable<R> map(Function<? super T, ? extends R> function) {
        return new AbstractLinkable<T, R>(this) {
            @Override
            void process(T value, Consumer<R> sink) {
                sink.accept(function.apply(value));
            }
        };
    }

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

    public static interface Double {

        public void link(DoubleConsumer consumer);

        public default Linkable.Double map(DoubleUnaryOperator function) {
            return new AbstractLinkable.Double(this) {
                @Override
                void process(double value, DoubleConsumer sink) {
                    sink.accept(function.applyAsDouble(value));
                }
            };
        }

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
    
    public static interface Int {

        public void link(IntConsumer consumer);

        public default Linkable.Int map(IntUnaryOperator function) {
            return new AbstractLinkable.Int(this) {
                @Override
                void process(int value, IntConsumer sink) {
                    sink.accept(function.applyAsInt(value));
                }
            };
        }

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
