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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.praxislive.code.CodeContext;
import org.praxislive.core.Lookup;
import org.praxislive.logging.LogLevel;

/**
 * Support for creating data pipes to work with data of any type. All data
 * chains are driven by a Data.Sink. Input and output ports of type Data.In and
 * Data.Out can be created. Only pipes and ports of the identical generic type
 * can be connected together.
 *
 * @author Neil C Smith - https://www.neilcsmith.net
 */
public class Data {

    private Data() {
    }

    /**
     * Link provided Data.Pipes together.
     *
     * @param <T> common type of data supported by pipes
     * @param pipes pipes to connect
     * @return last pipe, for convenience
     */
    @SafeVarargs
    public final static <T> Pipe<T> link(Pipe<T>... pipes) {
        if (pipes.length < 2) {
            throw new IllegalArgumentException();
        }
        for (int i = pipes.length - 1; i > 0; i--) {
            pipes[i].addSource(pipes[i - 1]);
        }
        return pipes[pipes.length - 1];
    }

    /**
     * Create a pipe that applies the consumer to every type T passing through.
     * This assumes that either the data type is mutable or that its contents
     * will be used but not changed. To map the type to a different instance of
     * T, use apply().
     *
     * @param <T> type of data
     * @param consumer consumer function to apply to data of type T
     * @return pipe
     */
    public final static <T> Pipe<T> with(Consumer<? super T> consumer) {
        return new Pipe<T>() {
            @Override
            protected void process(List<Packet<T>> data) {
                data.forEach(p -> consumer.accept(p.data()));
            }
        };
    }

    /**
     * Create a pipe that supplies new instances of type T. This pipe does not
     * support sources.
     *
     * @param <T> type of data to supply
     * @param supplier function to supply instance of T
     * @return pipe
     */
    public final static <T> Pipe<T> supply(Supplier<? extends T> supplier) {
        return new Pipe<T>() {
            @Override
            protected void process(List<Packet<T>> data) {
                data.forEach(p -> p.apply(t -> supplier.get()));
            }

            @Override
            protected void registerSource(Pipe<T> source) {
                throw new UnsupportedOperationException();
            }

        };
    }

    /**
     * Create a pipe that applies the function to every type T passing through.
     * The function may return the supplied input or another instance of type T.
     *
     * @param <T> type of data
     * @param function function to apply to data
     * @return pipe
     */
    public final static <T> Pipe<T> apply(Function<? super T, ? extends T> function) {
        return new Pipe<T>() {
            @Override
            protected void process(List<Packet<T>> data) {
                data.forEach(p -> p.apply(function));
            }
        };
    }

    /**
     * Data sink to drive pipe graph.
     *
     * Use {@code @Inject Sink<TYPE> sink;} to create a sink.
     *
     * By default the pass the same instance of T through the pipe graph. To
     * create new type T, accumulate values, validate values, etc. provide the
     * related functions.
     *
     * Use input() to get a Data.Pipe to link to the sink.
     *
     * Use process() every time you want to process a graph of T.
     *
     * @param <T> type of data
     */
    public static abstract class Sink<T> implements Lookup.Provider {

        private final Pipe<T> input;
        private final SinkPacket<T> packet;

        private UnaryOperator<T> creator;
        private UnaryOperator<T> clearer;
        private BinaryOperator<T> accumulator;
        private BiPredicate<T, T> validator;
        private Consumer<T> disposer;

        private CodeContext<?> context;

        public Sink() {
            packet = new SinkPacket<>(this, null);
            defaultFunctions();
            input = new Pipe<T>() {
                @Override
                protected void process(List<Packet<T>> data) {
                }

                @Override
                protected void registerSink(Pipe<T> sink) {
                    throw new UnsupportedOperationException();
                }

                @Override
                protected boolean isOutputRequired(Pipe<T> source, long time) {
                    return true;
                }

            };
        }

        private void defaultFunctions() {
            creator = UnaryOperator.identity();
            clearer = UnaryOperator.identity();
            accumulator = (dst, src) -> src;
            validator = (dst, src) -> true;
            disposer = t -> {
                if (t instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) t).close();
                    } catch (Exception ex) {
                        log(ex);
                    }
                }
            };
        }

        protected void attach(CodeContext<?> context) {
            this.context = context;
        }

        /**
         * Reset all functions and disconnect all sources.
         */
        public void reset() {
            defaultFunctions();
            input.disconnectSources();
        }

        /**
         * Get the input pipe for this sink. The input pipe only supports the
         * addition of sources - it cannot be used as a source.
         *
         * @return input pipe
         */
        public Pipe<T> input() {
            return input;
        }

        /**
         * Process an instance of type T through the data graph. The data
         * returned may not be the same as the data provided, depending on how
         * you have configured the sink, whether you use Data.supply() /
         * Data.apply(), etc.
         *
         * @param data instance of T to process
         * @return data of type T (may or may not be the input data)
         */
        public T process(T data) {
            packet.data = Objects.requireNonNull(data);
            try {
                if (input.sources.size() == 1) {
                    input.processInPlace(packet, true, context.getTime());
                } else {
                    input.processCached(packet, true, context.getTime());
                    input.writeOutput(input.dataPackets, packet, 0);
                }
            } catch (Exception ex) {
                log(ex);
            }
            return packet.data;
        }

        /**
         * Function to get an instance of T when a new data packet is being
         * created. This function is not required to return a new instance. The
         * default onCreate function returns the provided value.
         *
         * @param creator function to get an instance of T
         * @return this sink for chaining
         */
        public Sink<T> onCreate(UnaryOperator<T> creator) {
            this.creator = Objects.requireNonNull(creator);
            return this;
        }

        /**
         * Function to clear an instance of T when required, at the head of a
         * pipe chain, prior to accumulation, etc. This might eg. zero out an
         * array or empty a list. The default onClear function does nothing.
         *
         * @param clearer function to clear an instance of T
         * @return this sink for chaining
         */
        public Sink<T> onClear(UnaryOperator<T> clearer) {
            this.clearer = Objects.requireNonNull(clearer);
            return this;
        }

        public Sink<T> onAccumulate(BinaryOperator<T> accumulator) {
            this.accumulator = Objects.requireNonNull(accumulator);
            return this;
        }

        /**
         * Function to validate a source Data.Packet value against a destination
         * Data.Packet value. The first argument is the destination, the second
         * the existing source value. If this function returns false then the
         * onCreate function will be called to create a new value for the source
         * Data.Packet
         *
         * Packets from different sinks are always treated as invalid.
         *
         * The default function always returns true.
         *
         * @param validator function to validate source T against destination T
         * @return this sink for chaining
         */
        public Sink<T> onValidate(BiPredicate<T, T> validator) {
            this.validator = Objects.requireNonNull(validator);
            return this;
        }

//        public Sink<T> onDispose(Consumer<T> disposer) {
//            this.disposer = Objects.requireNonNull(disposer);
//            return this;
//        }
        private void log(Exception ex) {
            context.getLog().log(LogLevel.ERROR, ex);
        }

    }

    private static class SinkPacket<T> implements Packet<T> {

        private final Sink<T> sink;
        private T data;

        private SinkPacket(Sink<T> sink, T data) {
            this.sink = sink;
            this.data = data;
        }

        @Override
        public T data() {
            return data;
        }

        @Override
        public void clear() {
            apply(sink.clearer);
        }

        @Override
        public void apply(Function<? super T, ? extends T> operator) {
            try {
                T cur = data;
                data = operator.apply(data);
                if (data != cur) {
                    sink.disposer.accept(cur);
                }
            } catch (Exception ex) {
                sink.log(ex);
            }

        }

        @Override
        public void accumulate(List<Packet<T>> packets) {
            if (!packets.contains(this)) {
                clear();
            }
            try {
                packets.forEach(src -> {
                    if (src != this) {
                        T cur = data;
                        data = sink.accumulator.apply(data, src.data());
                        if (data != cur) {
                            sink.disposer.accept(cur);
                        }
                    }
                });
            } catch (Exception ex) {
                sink.log(ex);
            }

        }

        @Override
        public boolean isCompatible(Packet<T> packet) {
            try {
                return packet == this || sink.validator.test(data, packet.data());
            } catch (Exception ex) {
                sink.log(ex);
                return false;
            }
        }

        @Override
        public Packet<T> createPacket() {
            return new SinkPacket<>(this.sink, sink.creator.apply(data));
        }

        @Override
        public void dispose() {
            try {
                sink.disposer.accept(data);
                data = null;
            } catch (Exception ex) {
                sink.log(ex);
            }
        }

        @Override
        public Lookup getLookup() {
            return sink.getLookup();
        }

    }

    /**
     * Input port pipe.
     *
     * Create using eg. {@code @In(1) Data.In<TYPE> in;}
     *
     * @param <T> data type
     */
    public static abstract class In<T> extends Pipe<T> {

        @Override
        protected void process(List<Packet<T>> data) {
        }

    }

    /**
     * Input port pipe.
     *
     * Create using eg. {@code @Out(1) Data.Out<TYPE> in;}
     *
     * @param <T> data type
     */
    public static abstract class Out<T> extends Pipe<T> {

        @Override
        protected void process(List<Packet<T>> data) {
        }

    }

    /**
     * The base type of pipes that can be connected to form processing graphs.
     * Generally use the various factory methods (eg. Data.with() ) or Data.In /
     * Data.Out
     *
     * @param <T> data type of Pipe
     */
    public static abstract class Pipe<T> {

        private final List<Pipe<T>> sources;
        private final List<Pipe<T>> sinks;
        private final List<Packet<T>> dataPackets;
        private long time;
        private long renderReqTime;
        private boolean renderReqCache;
        private int renderIdx = 0;

        public Pipe() {
            this.sources = new ArrayList<>();
            this.sinks = new ArrayList<>();
            this.dataPackets = new ArrayList<>();
        }

        public final void addSource(Pipe<T> source) {
            source.registerSink(this);
            try {
                registerSource(source);
            } catch (RuntimeException ex) {
                source.unregisterSink(this);
                throw ex;
            }
        }

        public final void removeSource(Pipe<T> source) {
            source.unregisterSink(this);
            unregisterSource(source);
        }

        protected final void disconnectSources() {
            for (int i = sources.size(); i > 0; i--) {
                removeSource(sources.get(i - 1));
            }
        }

        protected final void disconnectSinks() {
            for (int i = sinks.size(); i > 0; i--) {
                sinks.get(i - 1).removeSource(this);
            }
        }

        protected final void clearCaches() {
            dataPackets.forEach(Packet::dispose);
            dataPackets.clear();
        }

        protected void process(Pipe<T> sink, Packet<T> buffer, long time) {
            int sinkIndex = sinks.indexOf(sink);

            if (sinkIndex < 0) {
                // throw exception?
                return;
            }
            boolean inPlace = sinks.size() == 1 && sources.size() < 2;

            if (this.time != time) {
                boolean outputRequired = isOutputRequired(time);
                this.time = time;
                if (inPlace) {
                    processInPlace(buffer, outputRequired, time);
                } else {
                    processCached(buffer, outputRequired, time);
                }
            }

            if (!inPlace) {
                writeOutput(dataPackets, buffer, sinkIndex);
            }
        }

        private void processInPlace(Packet<T> buffer, boolean outputRequired, long time) {
            if (!dataPackets.isEmpty()) {
                dataPackets.forEach(Packet::dispose);
                dataPackets.clear();
            }
            if (sources.isEmpty()) {
                buffer.clear();
            } else {
                sources.get(0).process(this, buffer, time);
            }
            if (outputRequired) {
                dataPackets.add(buffer);
                process(dataPackets);
                dataPackets.clear();
            }
        }

        private void processCached(Packet<T> buffer, boolean outputRequired, long time) {
            while (dataPackets.size() > sources.size()) {
                dataPackets.remove(dataPackets.size() - 1).dispose();
            }
            for (int i = 0; i < sources.size(); i++) {
                Packet<T> in;
                if (i < dataPackets.size()) {
                    in = dataPackets.get(i);
                    if (!buffer.isCompatible(in)) {
                        in.dispose();
                        in = buffer.createPacket();
                        dataPackets.set(i, in);
                    }
                } else {
                    in = buffer.createPacket();
                    dataPackets.add(in);
                }
                sources.get(i).process(this, in, time);
            }
            if (outputRequired) {
                process(dataPackets);
            }
        }

        protected abstract void process(List<Packet<T>> data);

        protected void writeOutput(List<Packet<T>> data, Packet<T> output, int sinkIndex) {
            output.accumulate(data);
        }

        protected boolean isOutputRequired(Pipe<T> source, long time) {
            return isOutputRequired(time);
        }

        protected boolean isOutputRequired(long time) {
            if (sinks.size() == 1) {
                return simpleOutputCheck(time);
            } else {
                return multipleOutputCheck(time);
            }
        }

        protected void registerSource(Pipe<T> source) {
            if (source == null) {
                throw new NullPointerException();
            }
            if (sources.contains(source)) {
                throw new IllegalArgumentException();
            }
            sources.add(source);
        }

        protected void unregisterSource(Pipe<T> source) {
            sources.remove(source);
        }

        protected void registerSink(Pipe<T> sink) {
            if (sink == null) {
                throw new NullPointerException();
            }
            if (sinks.contains(sink)) {
                throw new IllegalArgumentException();
            }
            sinks.add(sink);
        }

        protected void unregisterSink(Pipe<T> sink) {
            sinks.remove(sink);
        }

        private boolean simpleOutputCheck(long time) {
            if (time != renderReqTime) {
                renderReqTime = time;
                renderReqCache = sinks.get(0).isOutputRequired(this, time);
            }
            return renderReqCache;
        }

        private boolean multipleOutputCheck(long time) {
            if (renderIdx > 0) {
                while (renderIdx < sinks.size()) {
                    if (sinks.get(renderIdx++).isOutputRequired(this, time)) {
                        renderIdx = 0;
                        return true;
                    }
                }
                return false;
            } else {
                if (renderReqTime != time) {
                    renderReqTime = time;
                    renderReqCache = false;
                    while (renderIdx < sinks.size()) {
                        if (sinks.get(renderIdx++).isOutputRequired(this, time)) {
                            renderReqCache = true;
                            break;
                        }
                    }
                    renderIdx = 0;
                }
                return renderReqCache;
            }
        }

    }

    /**
     * A data holder used to wrap data of type T to be passed around a Pipe
     * graph.
     *
     * Implementations of this interface are provided by the Data.Sink.
     *
     * @param <T> type of wrapped data
     */
    public static interface Packet<T> extends Lookup.Provider {

        public T data();

        public void clear();

        public void apply(Function<? super T, ? extends T> operator);

        public void accumulate(List<Packet<T>> packets);

        public boolean isCompatible(Packet<T> packet);

        public Packet<T> createPacket();

        public void dispose();

    }

}
