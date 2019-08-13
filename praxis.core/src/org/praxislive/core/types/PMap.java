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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.praxislive.core.Value;
import org.praxislive.core.ValueFormatException;

/**
 *
 * @author Neil C Smith
 */
public class PMap extends Value {

//    public final static PMap EMPTY = new PMap(Collections.<String, Value>emptyMap(), "");
    public final static PMap EMPTY = new PMap(PArray.EMPTY, "");
    private final static int BUILDER_INIT_CAPACITY = 8;
//    private Map<String, Value> map;
    private final PArray array;
    private final String string;

//    private PMap(Map<String, Value> map, String str) {
//        this.map = map;
//        this.str = str;
//    }
    private PMap(PArray array, String string) {
        this.array = array;
        this.string = string;
    }

    public Value get(String key) {
        int size = array.size();
        for (int i = 0; i < size; i += 2) {
            if (array.get(i).toString().equals(key)) {
                return array.get(i + 1);
            }
        }
        return null;
    }

    public boolean getBoolean(String key, boolean def) {
        Value val = get(key);
        if (val != null) {
            try {
                return PBoolean.coerce(val).value();
            } catch (Exception ex) {
                // fall through
            }
        }
        return def;
    }

    public int getInt(String key, int def) {
        Value val = get(key);
        if (val != null) {
            try {
                return PNumber.coerce(val).toIntValue();
            } catch (Exception ex) {
                // fall through
            }
        }
        return def;
    }

    public double getDouble(String key, double def) {
        Value val = get(key);
        if (val != null) {
            try {
                return PNumber.coerce(val).value();
            } catch (Exception ex) {
                // fall through
            }
        }
        return def;
    }

    public String getString(String key, String def) {
        Value val = get(key);
        if (val != null) {
            return val.toString();
        }
        return def;
    }

    public int size() {
        return array.size() / 2;
    }
    
    @Deprecated
    public int getSize() {
        return array.size() / 2;
    }
    
    public List<String> keys() {
        return Collections.unmodifiableList(Arrays.asList(getKeys()));
    }

    @Deprecated
    public String[] getKeys() {
        int size = size();
        String[] keys = new String[size];
        for (int i = 0; i < size; i++) {
            keys[i] = array.get(i * 2).toString();
        }
        return keys;
    }

    @Override
    public String toString() {
        if (string == null) {
            return array.toString();
        } else {
            return string;
        }
    }

    @Override
    public boolean isEmpty() {
        return array.isEmpty();
    }

    @Override
    public boolean equivalent(Value arg) {
        if (arg == this) {
            return true;
        }
        try {
            PMap other = PMap.coerce(arg);
            int size = size();
            if (size != other.size()) {
                return false;
            }
            size *= 2;
            for (int i=0; i<size; i+=2) {
                if (!array.get(i).toString().equals(other.array.get(i).toString())) {
                    return false;
                }
                if (!Utils.equivalent(array.get(i+1), other.array.get(i+1))) {
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
        return array.hashCode(); // should we cache?
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof PMap) {
            if (this.array.equals(((PMap) obj).array)) {
                return true;
            }
        }
        return false;
    }

    private static Value objToValue(Object obj) {
        if (obj instanceof Value) {
            return (Value) obj;
        }
        if (obj instanceof Boolean) {
            return ((Boolean) obj) ? PBoolean.TRUE : PBoolean.FALSE;
        }
        if (obj instanceof Integer) {
            return PNumber.of(((Integer) obj));
        }
        if (obj instanceof Number) {
            return PNumber.of(((Number) obj).doubleValue());
        }
        if (obj == null) {
            return PString.EMPTY;
        }
        return PString.of(obj);
    }

    public static PMap of(String key, Object value) {
        if (key == null) {
            throw new NullPointerException();
        }
        PArray array = PArray.of(PString.of(key), objToValue(value));
        return new PMap(array, null);
    }

    @Deprecated
    public static PMap create(String key, Object value) {
        return PMap.of(key, value);
    }

    public static PMap of(String key1, Object value1,
            String key2, Object value2) {
        if (key1 == null || key2 == null) {
            throw new NullPointerException();
        }
        if (key1.equals(key2)) {
            throw new IllegalArgumentException("Duplicate keys");
        }
        PArray array = PArray.of(
                PString.of(key1), objToValue(value1),
                PString.of(key2), objToValue(value2));
        return new PMap(array, null);
    }

    @Deprecated
    public static PMap create(String key1, Object value1,
            String key2, Object value2) {
        return PMap.of(key1, value1, key2, value2);
    }

    public static PMap of(String key1, Object value1,
            String key2, Object value2,
            String key3, Object value3) {
        if (key1 == null || key2 == null || key3 == null) {
            throw new NullPointerException();
        }
        if (key1.equals(key2) || key2.equals(key3) || key3.equals(key1)) {
            throw new IllegalArgumentException("Duplicate keys");
        }
        PArray array = PArray.of(
                PString.of(key1), objToValue(value1),
                PString.of(key2), objToValue(value2),
                PString.of(key3), objToValue(value3));
        return new PMap(array, null);
    }

    @Deprecated
    public static PMap create(String key1, Object value1,
            String key2, Object value2,
            String key3, Object value3) {
        return PMap.of(key1, value1, key2, value2, key3, value3);
    }

    public static PMap parse(String str) throws ValueFormatException {
        PArray arr = PArray.parse(str);
        if (arr.isEmpty()) {
            return PMap.EMPTY;
        }
        int size = arr.size();
        if (size % 2 != 0) {
            throw new ValueFormatException("Uneven number of tokens passed to PMap.valueOf()");
        }
//        switch (size) {
//            case 2:
//                return PMap.create(arr.get(0).toString(), arr.get(1));
//            case 4:
//                return PMap.create(arr.get(0).toString(), arr.get(1),
//                        arr.get(2).toString(), arr.get(3));
//            case 6:
//                return PMap.create(arr.get(0).toString(), arr.get(1),
//                        arr.get(2).toString(), arr.get(3),
//                        arr.get(4).toString(), arr.get(5));
//        }
        PMap.Builder builder = builder(size / 2);
        for (int i = 0; i < size; i += 2) {
            builder.putImpl(
                    PString.of(arr.get(i)),
                    arr.get(i + 1));
        }
        return builder.build(str);
    }

    @Deprecated
    public static PMap valueOf(String str) throws ValueFormatException {
        return parse(str);
    }

    @Deprecated
    public static PMap coerce(Value arg) throws ValueFormatException {
        if (arg instanceof PMap) {
            return (PMap) arg;
        } else {
            return parse(arg.toString());
        }
    }
    
    public static Optional<PMap> from(Value arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ValueFormatException ex) {
            return Optional.empty();
        }
    }

    public static Builder builder() {
        return builder(BUILDER_INIT_CAPACITY);
    }

    public static Builder builder(int initialCapacity) {
        return new Builder(initialCapacity * 2);
    }

    public static class Builder {

        private List<Value> storage;

        private Builder(int capacity) {
            storage = new ArrayList<>(capacity);
        }

//        @Deprecated
//        public Builder put(PString key, Value value) {
//            put(key, value instanceof Value ? (Value) value : PString.valueOf(value));
//            return this;
//        }
        
        @Deprecated
        public Builder put(PString key, Value value) {
            if (key == null || value == null) {
                throw new NullPointerException();
            }
            putImpl(key, value);
            return this;
        }
        
//        @Deprecated
//        public Builder put(String key, Value value) {
//            put(PString.valueOf(key), value);
//            return this;
//        }
        
        public Builder put(String key, Value value) {
            putImpl(PString.of(key), value);
            return this;
        }

        public Builder put(String key, boolean value) {
            putImpl(PString.of(key), PBoolean.of(value));
            return this;
        }

        public Builder put(String key, int value) {
            putImpl(PString.of(key), PNumber.of(value));
            return this;
        }

        public Builder put(String key, double value) {
            putImpl(PString.of(key), PNumber.of(value));
            return this;
        }

        public Builder put(String key, String value) {
            putImpl(PString.of(key), PString.of(value));
            return this;
        }
        
        public Builder put(String key, Object value) {
            putImpl(PString.of(key), objToValue(value));
            return this;
        }

        public PMap build() {
            PArray array = PArray.of(storage);
            return new PMap(array, null);
        }

        private PMap build(String str) {
            PArray array = PArray.of(storage);
            return new PMap(array, str);
        }

        private void putImpl(PString key, Value value) {
            int idx = indexOfKey(key);
            if (idx < 0) {
                storage.add(key);
                storage.add(value);
            } else {
                storage.set(idx, key);
                storage.set(idx + 1, value);
            }
        }

        private int indexOfKey(PString key) {
            for (int i = 0, size = storage.size(); i < size; i += 2) {
                if (storage.get(i).equals(key)) {
                    return i;
                }
            }
            return -1;
        }
    }
}
