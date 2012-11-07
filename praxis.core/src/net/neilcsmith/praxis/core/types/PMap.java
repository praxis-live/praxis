/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Neil C Smith.
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;

/**
 *
 * @author Neil C Smith
 */
public class PMap extends Argument {

    public final static PMap EMPTY = new PMap(Collections.<String, Argument>emptyMap(), "");
    private Map<String, Argument> map;
    private String str;

    private PMap(Map<String, Argument> map, String str) {
        this.map = map;
        this.str = str;
    }
    
    public Argument get(String key) {
        return map.get(key);
    }
    
    public boolean getBoolean(String key, boolean def) {
        Argument val = map.get(key);
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
        Argument val = map.get(key);
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
        Argument val = map.get(key);
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
        Argument val = map.get(key);
        if (val != null) {
            return val.toString();
        }
        return def;
    }

    public int getSize() {
        return map.size();
    }

    @Override
    public String toString() {
        if (str == null) {
            if (map.isEmpty()) {
                str = "";
            } else {
                List<Argument> vals = new ArrayList<Argument>(map.size() * 2);
                for (Map.Entry<String, Argument> entry : map.entrySet()) {
                    vals.add(PString.valueOf(entry.getKey()));
                    vals.add(entry.getValue());
                }
                str = PArray.valueOf(vals).toString();

            }
        }

        return str;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    

    @Override
    public int hashCode() {
        return map.hashCode(); // should we cache?
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PMap) {
            if (this.map.equals(((PMap) obj).map)) {
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
            return ((Boolean)obj).booleanValue() ? PBoolean.TRUE : PBoolean.FALSE;
        }
        if (obj instanceof Integer) {
            return PNumber.valueOf(((Integer)obj).intValue());
        }
        if (obj instanceof Number) {
            return PNumber.valueOf(((Number)obj).doubleValue());
        }
        if (obj == null) {
            return PString.EMPTY;
        }
        return PString.valueOf(obj);
    }
    
    public static PMap create(String key, Object value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        return new PMap(Collections.singletonMap(key, objToArg(value)), null);
    }
    
    public static PMap create(String key1, Object value1,
            String key2, Object value2) {
        if (key1 == null || key2 == null) {
            throw new NullPointerException();
        }
        Map<String, Argument> m = new LinkedHashMap<String, Argument>();
        m.put(key1, objToArg(value1));
        m.put(key2, objToArg(value2));
        return new PMap(m, null);
    }
    
    public static PMap create(String key1, Object value1,
            String key2, Object value2,
            String key3, Object value3) {
        if (key1 == null || key2 == null || key3 == null) {
            throw new NullPointerException();
        }
        Map<String, Argument> m = new LinkedHashMap<String, Argument>();
        m.put(key1, objToArg(value1));
        m.put(key2, objToArg(value2));
        m.put(key3, objToArg(value3));
        return new PMap(m, null);
    }
    
    
    
    public static PMap create(Map<String, ? extends Argument> map) {
        if (map.isEmpty()) {
            return PMap.EMPTY;
        }
        Map<String, Argument> m = new LinkedHashMap<String, Argument>(map);
        if (m.containsKey(null) || m.containsValue(null)) {
            throw new NullPointerException(); // need to replace with something more efficient
        }
//        m = Collections.unmodifiableMap(m);
        return new PMap(m, null);
    }



    public static PMap valueOf(String str) throws ArgumentFormatException {
        if (str.length() == 0) {
            return PMap.EMPTY;
        }        
        
        PArray vals = PArray.valueOf(str);
        int size = vals.getSize();
        if (size == 0) {
            return PMap.EMPTY;
        }
        if ((size % 2) != 0) {
            throw new ArgumentFormatException();
        }
        Map<String, Argument> map = new LinkedHashMap<String, Argument>();
        for (int i=0; i<size;) {
            String key = vals.get(i++).toString();
            Argument value = vals.get(i++);
            value = map.put(key, value);
            if (value != null) {
                throw new ArgumentFormatException();
            }
        }
//        map = Collections.unmodifiableMap(map);
        return new PMap(map, str);
    }

    public static PMap coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof PMap) {
            return (PMap) arg;
        }
        throw new UnsupportedOperationException();
    }
}
