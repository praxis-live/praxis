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
package org.praxislive.core.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.praxislive.core.Value;
import org.praxislive.core.ValueFormatException;
import org.praxislive.core.CallArguments;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.syntax.Token;
import org.praxislive.core.syntax.Tokenizer;

/**
 *
 * @author Neil C Smith
 */
public final class PArray extends Value implements Iterable<Value> {

    public final static PArray EMPTY = new PArray(new Value[0], "");
    private final Value[] data;
    private volatile String str;

    private PArray(Value[] data, String str) {
        this.data = data;
        this.str = str;
    }

    public Value get(int index) {
        int count = data.length;
        if (count > 0) {
            index %= count;
            return index < 0 ? data[index + count] : data[index];
        } else {
            return this;
        }
    }

    @Deprecated
    public Value[] getAll() {
        return data.clone();
    }

    public int size() {
        return data.length;
    }
    
    @Deprecated
    public int getSize() {
        return data.length;
    }

    @Override
    public String toString() {
        if (str == null) {
            if (data.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (Value entry : data) {
                    if (sb.length() > 0) {
                        sb.append(' ');
                    }
                    if (entry instanceof PArray || entry instanceof PMap) {
                        sb.append('{')
                                .append(entry.toString())
                                .append('}');
                    } else {
                        sb.append(Utils.escape(String.valueOf(entry)));
                    }
                }
                str = sb.toString();
            } else {
                str = "";
            }
        }

        return str;

    }

    @Override
    public boolean isEmpty() {
        return data.length == 0;
    }

    @Override
    public boolean equivalent(Value arg) {
        try {
            if (arg == this) {
                return true;
            }
            PArray other = PArray.coerce(arg);
            int size = data.length;
            if (size != other.data.length) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (!Utils.equivalent(data[i], other.data[i])) {
                    return false;
                }
            }
            return true;
        } catch (ValueFormatException ex) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PArray) {
            PArray o = (PArray) obj;
            return Arrays.equals(data, o.data);
        }
        return false;
    }

    public Iterator<Value> iterator() {
        return new Itr();
    }

    public Stream<Value> stream() {
        return Stream.of(data);
    }

    private class Itr implements Iterator<Value> {

        int cursor = 0;

        public boolean hasNext() {
            return cursor < data.length;
        }

        public Value next() {
            Value arg = data[cursor];
            cursor++;
            return arg;
        }

        public void remove() {
            throw new UnsupportedOperationException("PArrays are immutable");
        }
    }

    public static PArray of(Collection<? extends Value> collection) {
        return valueOf(collection.toArray(new Value[collection.size()]), false);
    }

    @Deprecated
    public static PArray valueOf(Collection<? extends Value> collection) {
        return valueOf(collection.toArray(new Value[collection.size()]), false);
    }

    public static PArray of(Value... args) {
        return valueOf(args, true);
    }
    
    @Deprecated
    public static PArray valueOf(Value... args) {
        return valueOf(args, true);
    }
    
    private static PArray valueOf(Value[] args, boolean clone) {
        if (args.length == 0) {
            return PArray.EMPTY;
        }
        Value[] safeArgs = clone ? args.clone() : args;
        for (Value v : safeArgs) {
            if (v == null) {
                throw new NullPointerException();
            }
        }
        return new PArray(args, null);
    }

    @Deprecated
    public static PArray valueOf(CallArguments args) {
        return of(args.getAll());
    }

    public static PArray parse(String str) throws ValueFormatException {
        if (str.length() == 0) {
            return PArray.EMPTY;
        }
        try {
            Tokenizer tk = new Tokenizer(str);
            List<PString> list = new ArrayList<PString>();
            tokenize:
            for (Token t : tk) {
                Token.Type type = t.getType();
                switch (type) {
                    case PLAIN:
                    case QUOTED:
                        list.add(PString.of(t.getText()));
                        break;
                    case BRACED:
                        String s = t.getText();
                        list.add(PString.of(s));
                        break;
                    case EOL:
                        break tokenize;
                    default:
                        throw new ValueFormatException();
                }
            }
            int size = list.size();
            if (size == 0) {
                return PArray.EMPTY;
            } else {
                return new PArray(list.toArray(new Value[size]), str);
            }
        } catch (Exception ex) {
            throw new ValueFormatException(ex);
        }

    }

    @Deprecated
    public static PArray valueOf(String str) throws ValueFormatException {
        return parse(str);
    }

    @Deprecated
    public static PArray coerce(Value arg) throws ValueFormatException {
        if (arg instanceof PArray) {
            return (PArray) arg;
        } else {
            return parse(arg.toString());
        }
    }

    public static Optional<PArray> from(Value arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ValueFormatException ex) {
            return Optional.empty();
        }
    }

    public static ArgumentInfo info() {
        return ArgumentInfo.of(PArray.class, null);
    }

    public static <T extends Value> Collector<T, ?, PArray> collector() {

        return Collector.<T, List<T>, PArray>of(ArrayList::new,
                List::add,
                (list1, list2) -> {
                    list1.addAll(list2);
                    return list1;
                },
                PArray::of
        );
    }

    @Deprecated
    public static PArray concat(PArray a, PArray b) {
        Value[] values = new Value[a.data.length + b.data.length];
        System.arraycopy(a.data, 0, values, 0, a.data.length);
        System.arraycopy(b.data, 0, values, a.data.length, b.data.length);
        return new PArray(values, null);
    }

    @Deprecated
    public static PArray subset(PArray array, int start, int count) {
        Value[] values = new Value[count];
        System.arraycopy(array.data, start, values, 0, count);
        return new PArray(values, null);
    }

    @Deprecated
    public static PArray insert(PArray array, int index, Value value) {
        Value[] values = new Value[array.data.length + 1];
        System.arraycopy(array.data, 0, values, 0, index);
        values[index] = value;
        System.arraycopy(array.data, index, values, index + 1,
                array.data.length - index);
        return new PArray(values, null);
    }
    
    @Deprecated
    public static PArray append(PArray array, Value value) {
        Value[] values = new Value[array.data.length + 1];
        System.arraycopy(array.data, 0, values, 0, array.data.length);
        values[values.length - 1] = value;
        return new PArray(values, null);
    }

}
