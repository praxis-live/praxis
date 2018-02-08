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

import org.praxislive.core.Value;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.praxislive.core.Argument;
import org.praxislive.core.ArgumentFormatException;

/**
 *
 * @author Neil C Smith
 */
public class PMap extends Value {

//    public final static PMap EMPTY = new PMap(Collections.<String, Argument>emptyMap(), "");
    public final static PMap EMPTY = new PMap(PArray.EMPTY, "");
    private final static int BUILDER_INIT_CAPACITY = 8;
//    private Map<String, Argument> map;
    private final PArray array;
    private final String string;

//    private PMap(Map<String, Argument> map, String str) {
//        this.map = map;
//        this.str = str;
//    }
    private PMap(PArray array, String string) {
        this.array = array;
        this.string = string;
    }

    public Value get(String key) {
        int size = array.getSize();
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

    public int getSize() {
        return array.getSize() / 2;
    }

    public String[] getKeys() {
        int size = getSize();
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
            int size = getSize();
            if (size != other.getSize()) {
                return false;
            }
            size *= 2;
            for (int i=0; i<size; i+=2) {
                if (!array.get(i).toString().equals(other.array.get(i).toString())) {
                    return false;
                }
                if (!Argument.equivalent(null, array.get(i+1), other.array.get(i+1))) {
                    return false;
                }
            }
            return true;
        } catch (ArgumentFormatException ex) {
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
            return ((Boolean) obj).booleanValue() ? PBoolean.TRUE : PBoolean.FALSE;
        }
        if (obj instanceof Integer) {
            return PNumber.valueOf(((Integer) obj).intValue());
        }
        if (obj instanceof Number) {
            return PNumber.valueOf(((Number) obj).doubleValue());
        }
        if (obj == null) {
            return PString.EMPTY;
        }
        return PString.valueOf(obj);
    }

    public static PMap create(String key, Object value) {
        if (key == null) {
            throw new NullPointerException();
        }
        PArray array = PArray.valueOf(PString.valueOf(key), objToValue(value));
        return new PMap(array, null);
    }

    public static PMap create(String key1, Object value1,
            String key2, Object value2) {
        if (key1 == null || key2 == null) {
            throw new NullPointerException();
        }
        if (key1.equals(key2)) {
            throw new IllegalArgumentException("Duplicate keys");
        }
        PArray array = PArray.valueOf(
                PString.valueOf(key1), objToValue(value1),
                PString.valueOf(key2), objToValue(value2));
        return new PMap(array, null);
    }

    public static PMap create(String key1, Object value1,
            String key2, Object value2,
            String key3, Object value3) {
        if (key1 == null || key2 == null || key3 == null) {
            throw new NullPointerException();
        }
        if (key1.equals(key2) || key2.equals(key3) || key3.equals(key1)) {
            throw new IllegalArgumentException("Duplicate keys");
        }
        PArray array = PArray.valueOf(
                PString.valueOf(key1), objToValue(value1),
                PString.valueOf(key2), objToValue(value2),
                PString.valueOf(key3), objToValue(value3));
        return new PMap(array, null);
    }

    public static PMap valueOf(String str) throws ArgumentFormatException {
        PArray arr = PArray.valueOf(str);
        if (arr.isEmpty()) {
            return PMap.EMPTY;
        }
        int size = arr.getSize();
        if (size % 2 != 0) {
            throw new ArgumentFormatException("Uneven number of tokens passed to PMap.valueOf()");
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
            builder.put(
                    PString.coerce(arr.get(i)),
                    arr.get(i + 1));
        }
        return builder.build(str);
    }

    public static PMap coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof PMap) {
            return (PMap) arg;
        } else {
            return valueOf(arg.toString());
        }
    }
    
    public static Optional<PMap> from(Argument arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ArgumentFormatException ex) {
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

        @Deprecated
        public Builder put(PString key, Argument value) {
            put(key, value instanceof Value ? (Value) value : PString.valueOf(value));
            return this;
        }
        
        public Builder put(PString key, Value value) {
            if (key == null || value == null) {
                throw new NullPointerException();
            }
            putImpl(key, value);
            return this;
        }
        
        @Deprecated
        public Builder put(String key, Argument value) {
            put(PString.valueOf(key), value);
            return this;
        }
        
        public Builder put(String key, Value value) {
            put(PString.valueOf(key), value);
            return this;
        }

        public Builder put(String key, boolean value) {
            putImpl(PString.valueOf(key), PBoolean.valueOf(value));
            return this;
        }

        public Builder put(String key, int value) {
            putImpl(PString.valueOf(key), PNumber.valueOf(value));
            return this;
        }

        public Builder put(String key, double value) {
            putImpl(PString.valueOf(key), PNumber.valueOf(value));
            return this;
        }

        public Builder put(String key, String value) {
            putImpl(PString.valueOf(key), PString.valueOf(value));
            return this;
        }

        public PMap build() {
            PArray array = PArray.valueOf(storage);
            return new PMap(array, null);
        }

        private PMap build(String str) {
            PArray array = PArray.valueOf(storage);
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
