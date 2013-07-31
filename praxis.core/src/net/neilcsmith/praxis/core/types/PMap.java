/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2013 Neil C Smith.
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
package net.neilcsmith.praxis.core.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;

/**
 *
 * @author Neil C Smith
 */
public class PMap extends Argument {

//    public final static PMap EMPTY = new PMap(Collections.<String, Argument>emptyMap(), "");
    public final static PMap EMPTY = new PMap(PArray.EMPTY, "");
    private final static int BUILDER_INIT_CAPACITY = 8;
//    private Map<String, Argument> map;
    private PArray array;
    private String string;

//    private PMap(Map<String, Argument> map, String str) {
//        this.map = map;
//        this.str = str;
//    }
    private PMap(PArray array, String string) {
        this.array = array;
        this.string = string;
    }

    public Argument get(String key) {
        int size = array.getSize();
        for (int i = 0; i < size; i += 2) {
            if (array.get(i).toString().equals(key)) {
                return array.get(i + 1);
            }
        }
        return null;
    }

    public boolean getBoolean(String key, boolean def) {
        Argument val = get(key);
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
        Argument val = get(key);
        if (val != null) {
            try {
                return PNumber.coerce(val).toIntValue();
            } catch (Exception ex) {
                // fall through
            }
        }
        return def;
    }

    public double getDouble(String key, int def) {
        Argument val = get(key);
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
        Argument val = get(key);
        if (val != null) {
            return val.toString();
        }
        return def;
    }

    public int getSize() {
        return array.getSize() / 2;
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
    public int hashCode() {
        return array.hashCode(); // should we cache?
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PMap) {
            if (this.array.equals(((PMap) obj).array)) {
                return true;
            }
        }
        return false;
    }

    private static Argument objToArg(Object obj) {
        if (obj instanceof Argument) {
            return (Argument) obj;
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
        PArray array = PArray.valueOf(PString.valueOf(key), objToArg(value));
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
                PString.valueOf(key1), objToArg(value1),
                PString.valueOf(key2), objToArg(value2));
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
                PString.valueOf(key1), objToArg(value1),
                PString.valueOf(key2), objToArg(value2),
                PString.valueOf(key3), objToArg(value3));
        return new PMap(array, null);
    }

    public static PMap create(Map<String, ? extends Argument> map) {
        throw new UnsupportedOperationException();
//        if (map.isEmpty()) {
//            return PMap.EMPTY;
//        }
//        Map<String, Argument> m = new LinkedHashMap<String, Argument>(map);
//        if (m.containsKey(null) || m.containsValue(null)) {
//            throw new NullPointerException(); // need to replace with something more efficient
//        }
////        m = Collections.unmodifiableMap(m);
//        return new PMap(m, null);
    }

    public static PMap valueOf(String str) throws ArgumentFormatException {
        throw new UnsupportedOperationException();
//        if (str.length() == 0) {
//            return PMap.EMPTY;
//        }        
//        
//        PArray vals = PArray.valueOf(str);
//        int size = vals.getSize();
//        if (size == 0) {
//            return PMap.EMPTY;
//        }
//        if ((size % 2) != 0) {
//            throw new ArgumentFormatException();
//        }
//        Map<String, Argument> map = new LinkedHashMap<String, Argument>();
//        for (int i=0; i<size;) {
//            String key = vals.get(i++).toString();
//            Argument value = vals.get(i++);
//            value = map.put(key, value);
//            if (value != null) {
//                throw new ArgumentFormatException();
//            }
//        }
////        map = Collections.unmodifiableMap(map);
//        return new PMap(map, str);
    }

    public static PMap coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof PMap) {
            return (PMap) arg;
        }
        throw new UnsupportedOperationException();
    }

    public static Builder builder() {
        return builder(BUILDER_INIT_CAPACITY);
    }

    public static Builder builder(int initialCapacity) {
        return new Builder(initialCapacity);
    }

    public static class Builder {

        private List<Argument> storage;

        private Builder(int capacity) {
            storage = new ArrayList<Argument>(capacity);
        }

        public Builder put(PString key, Argument value) {
            if (key == null || value == null) {
                throw new NullPointerException();
            }
            putImpl(key, value);
            return this;
        }
        
        public Builder put(String key, Argument value) {
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

        private void putImpl(PString key, Argument value) {
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
