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
package org.praxislive.core.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
//import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.praxislive.core.Argument;
import org.praxislive.core.ArgumentFormatException;
import org.praxislive.core.info.ArgumentInfo;
import org.praxislive.core.DataObject;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public final class PBytes extends Value {

    public final static PBytes EMPTY = new PBytes(new byte[0], "");

    private final byte[] bytes;

    private String str;

    private PBytes(byte[] bytes, String str) {
        this.bytes = bytes;
        this.str = str;
    }

    @Override
    public String toString() {
        if (str == null) {
            if (bytes.length == 0) {
                str = "";
            } else {
                str = Base64.getMimeEncoder().encodeToString(bytes);
            }
        }
        return str;
    }

    public void read(byte[] dst) {
        System.arraycopy(bytes, 0, dst, 0, bytes.length);
    }

//    public ByteBuffer asByteBuffer() {
//        return ByteBuffer.wrap(bytes).asReadOnlyBuffer();
//    }
    public InputStream asInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    public int getSize() {
        return bytes.length;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PBytes) {
            final PBytes other = (PBytes) obj;
            return Arrays.equals(this.bytes, other.bytes);
        }
        return false;
    }

    @Override
    public boolean equivalent(Value arg) {
        try {
            if (arg == this) {
                return true;
            }
            PBytes other = PBytes.coerce(arg);
            return Arrays.equals(bytes, other.bytes);
        } catch (ArgumentFormatException ex) {
            return false;
        }
    }

    @Override
    public boolean isEmpty() {
        return bytes.length == 0;
    }
    
    /**
     * Extract serialized object from data. Will throw an exception if this PBytes
     * doesn't contain a valid object of the correct type.
     * @param <T>
     * @param type class of expected object
     * @return deserialized object
     * @throws IOException
     */
    public <T extends Serializable> T deserialize(Class<T> type) throws IOException {
        ObjectInputStream ois = new ObjectInputStream(asInputStream());
        try {
            Object obj = ois.readObject();
            if (type.isInstance(obj)) {
                return type.cast(obj);
            }
            throw new IOException("PBytes contains a different class");
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Iterate through data by decoding into provided container DataObject and calling consumer.
     * Changes to the container are ignored outside of the consumer.
     * @param <T> DataObject sub-type
     * @param container
     * @param consumer
     */
    public <T extends DataObject> void forEachIn(T container, Consumer<T> consumer) {
        Spliterator<T> splitr = new StreamableSpliterator<>(new ByteArrayInputStream(bytes), () -> container);
        splitr.forEachRemaining(consumer);
    }

    /**
     * Transform data by iterating into provided container and calling provided consumer before
     * writing container into new PBytes
     * @param <T> DataObject sub-type
     * @param container
     * @param transformer
     * @return transformed data
     */
    public <T extends DataObject> PBytes transformIn(T container, Consumer<T> transformer) {
        OutputStream os = new OutputStream(getSize());
        DataOutputStream dos = new DataOutputStream(os);
        forEachIn(container, s -> {
            transformer.accept(s);
            try {
                s.writeTo(dos);
            } catch (Exception ex) {
                throw new IllegalArgumentException(ex);
            }
        });
        try {
            dos.flush();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
        return os.toBytes();
    }

    /**
     * Create a Stream over the data by decoding into DataObjects provided by supplier
     * @param <T>
     * @param supplier of DataObject
     * @return Stream of DataObject
     */
    public <T extends DataObject> Stream<T> streamOf(Supplier<T> supplier) {
        return isEmpty() ? Stream.empty()
                : StreamSupport.stream(new StreamableSpliterator<>(
                        new ByteArrayInputStream(bytes), supplier), false);
    }

    /**
     * Create a Stream over the data by decoding into count number of DataObjects
     * provided by supplier. Extra DataObjects with default values will be generated
     * if required to reach count.
     * @param <T>
     * @param count
     * @param supplier
     * @return Stream of DataObject
     */
    public <T extends DataObject> Stream<T> streamOf(int count, Supplier<T> supplier) {
        return Stream.concat(streamOf(supplier), Stream.generate(supplier)).limit(count);
    }

    /**
     * Collector to take Stream of DataObject subclasses and write into new PBytes.
     * @param <T>
     * @return collector
     */
    public static <T extends DataObject> Collector<T, ?, PBytes> collector() {
        return new StreamableCollector<>();
    }

    public static PBytes valueOf(byte[] bytes) {
        return new PBytes(bytes.clone(), null);
    }

    public static PBytes valueOf(String str) throws ArgumentFormatException {
        if (str.trim().isEmpty()) {
            return PBytes.EMPTY;
        }
        try {
            byte[] bytes = Base64.getMimeDecoder().decode(str);
            return new PBytes(bytes, str);
        } catch (Exception ex) {
            throw new ArgumentFormatException(ex);
        }
    }
    
    /**
     * Encode the provided List of DataObject subclasses into a new PBytes
     * @param list
     * @return PBytes of data
     */
    public static PBytes valueOf(List<? extends DataObject> list) {
        if (list.isEmpty()) {
            return PBytes.EMPTY;
        }
        try {
            OutputStream os = new OutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            for (DataObject s : list) {
                s.writeTo(dos);
            }
            dos.flush();
            return os.toBytes();
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    /**
     * Create a PBytes of the serialized form of the provided object.
     * @param obj
     * @return PBytes of serialized data
     * @throws IOException
     */
    public static PBytes serialize(Serializable obj) throws IOException {
        OutputStream os = new OutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(obj);
            oos.flush();
        }
        return os.toBytes();
    }

    public static PBytes coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof PBytes) {
            return (PBytes) arg;
        } else {
            return valueOf(arg.toString());
        }
    }

    public static Optional<PBytes> from(Argument arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ArgumentFormatException ex) {
            return Optional.empty();
        }
    }

    public static ArgumentInfo info() {
        return ArgumentInfo.create(PBytes.class, PMap.EMPTY);
    }

    public static class OutputStream extends ByteArrayOutputStream {

        public OutputStream() {
        }

        public OutputStream(int size) {
            super(size);
        }

        public synchronized PBytes toBytes() {
            // @TODO zero copy if buf.length == count?
            return new PBytes(toByteArray(), null);
        }

    }

    private static class StreamableCollector<T extends DataObject> implements Collector<T, List<T>, PBytes> {

        @Override
        public Supplier<List<T>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<T>, T> accumulator() {
            return List::add;
        }

        @Override
        public BinaryOperator<List<T>> combiner() {
            return (list1, list2) -> {
                list1.addAll(list2);
                return list1;
            };
        }

        @Override
        public Function<List<T>, PBytes> finisher() {
            return list -> PBytes.valueOf(list);
        }

        @Override
        public Set<Characteristics> characteristics() {
            return EnumSet.noneOf(Characteristics.class);
        }

    }

    private static class StreamableSpliterator<T extends DataObject> implements Spliterator<T> {

        private final ByteArrayInputStream is;
        private final DataInputStream dis;
        private final Supplier<T> supplier;

        private StreamableSpliterator(ByteArrayInputStream is, Supplier<T> supplier) {
            this.is = is;
            this.dis = new DataInputStream(is);
            this.supplier = supplier;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            try {
                int available = is.available();
                if (available > 0) {
                    T streamable = supplier.get();
                    streamable.readFrom(dis);
                    if (available == is.available()) {
                        throw new IllegalArgumentException("DataObject not reading from data");
                    }
                    action.accept(streamable);
                    return true;
                } else {
                    return false;
                }
            } catch (IllegalArgumentException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return IMMUTABLE | NONNULL | ORDERED;
        }

    }

}
