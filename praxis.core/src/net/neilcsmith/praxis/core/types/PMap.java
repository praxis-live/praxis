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
import java.util.Set;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;

/**
 *
 * @author Neil C Smith
 * @TODO should keys be Strings - need to ensure string equal keys return same value!
 */
public class PMap extends Argument {

    public final static PMap EMPTY = new PMap(Collections.<Argument, Argument>emptyMap(), "");
    private Map<Argument, Argument> map;
    private String str;

    private PMap(Map<Argument, Argument> map, String str) {
        this.map = map;
        this.str = str;
    }

    public Argument get(Argument key) {
        return map.get(key);
    }
    
    public Argument get(String key) {
        return map.get(PString.valueOf(key));
    }

    public int getSize() {
        return map.size();
    }

    public PArray getKeys() {
        return PArray.valueOf(map.keySet());
    }

    public PArray getValues() {
        return PArray.valueOf(map.values());
    }

    @Override
    public String toString() {
        if (str == null) {
            if (map.isEmpty()) {
                str = "";
            } else {
                List<Argument> vals = new ArrayList<Argument>(map.size() * 2);
                for (Map.Entry<Argument, Argument> entry : map.entrySet()) {
                    vals.add(entry.getKey());
                    vals.add(entry.getValue());
                }
                str = PArray.valueOf(vals).toString();
//                List<PArray> entries = new ArrayList<PArray>(map.size());
////                Set<Map.Entry<Argument, Argument>> entries = map.entrySet();
//                for (Map.Entry<Argument, Argument> entry : map.entrySet()) {
//                    entries.add(PArray.valueOf(new Argument[]{entry.getKey(), entry.getValue()}));
//                }
//                str = PArray.valueOf(entries).toString();
////                StringBuilder sb = new StringBuilder();
//                Set<Map.Entry<Argument, Argument>> entries = map.entrySet();
//                for (Map.Entry<Argument, Argument> entry : entries) {
//                    if (sb.length() == 0) {
//                        sb.append("{ {");
//                    } else {
//                        sb.append(" { {");
//                    }
//                    
//                    String s = String.valueOf(entry.getKey());
//                    if (s.indexOf('{') > -1 || s.indexOf('}') > -1) {
//                        s = s.replace("{", "\\{");
//                        s = s.replace("}", "\\}");
//                    }
//                    sb.append(s);
//                    sb.append("} {");
//                    
//                    s = String.valueOf(entry.getValue());
//                    if (s.indexOf('{') > -1 || s.indexOf('}') > -1) {
//                        s = s.replace("{", "\\{");
//                        s = s.replace("}", "\\}");
//                    }
//                    sb.append(s);
//                    sb.append("} }");
//                }
//                str = sb.toString();

//                StringBuilder sb = new StringBuilder();
//                Set<Map.Entry<Argument, Argument>> entries = map.entrySet();
//                for (Map.Entry<Argument, Argument> entry : entries) {
//                    if (sb.length() == 0) {
//                        sb.append("{ \"");
//                    } else {
//                        sb.append(" { \"");
//                    }
//
//                    String s = String.valueOf(entry.getKey());
//                    if (s.indexOf('{') > -1 || s.indexOf('}') > -1) {
//                        s = s.replace("{", "\\{");
//                        s = s.replace("}", "\\}");
//                    }
//                    sb.append(s);
//                    sb.append("\" \"");
//
//                    s = String.valueOf(entry.getValue());
//                    if (s.indexOf('{') > -1 || s.indexOf('}') > -1) {
//                        s = s.replace("{", "\\{");
//                        s = s.replace("}", "\\}");
//                    }
//                    sb.append(s);
//                    sb.append("\" }");
//                }
//                str = sb.toString();

            }
        }

        return str;
    }

    public Map<Argument, Argument> toMap() {
        return new LinkedHashMap<Argument, Argument>(map);
    }

    @Override
    public boolean isEmpty() {
        return map.size() == 0;
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

    public static PMap valueOf(Map<? extends Argument, ? extends Argument> map) {
        if (map.isEmpty()) {
            return PMap.EMPTY;
        }
        Map<Argument, Argument> m = new LinkedHashMap<Argument, Argument>(map);
        if (m.containsKey(null) || m.containsValue(null)) {
            throw new NullPointerException(); // need to replace with something more efficient
        }
        m = Collections.unmodifiableMap(m);
        return new PMap(m, null);
    }

    public static PMap valueOf(Argument... args) {
        if (args.length == 0 || (args.length % 2) != 0) {
            throw new IllegalArgumentException();
        }
        Map<Argument, Argument> map = new LinkedHashMap<Argument, Argument>();
        for (int i = 0; i < args.length; i++) {
            Argument key = args[i];
            if (key == null) {
                throw new NullPointerException();
            }
            i++;
            Argument value = args[i];
            if (value == null) {
                throw new NullPointerException();
            }
            if (map.containsKey(key)) {
                throw new IllegalArgumentException();
            }
            map.put(key, value);
        }
        map = Collections.unmodifiableMap(map);
        return new PMap(map, null);
    }

    public static PMap valueOf(String ... args) {
        if (args.length == 0 || (args.length % 2) != 0) {
            throw new IllegalArgumentException();
        }
        Map<Argument, Argument> map = new LinkedHashMap<Argument, Argument>();
        for (int i = 0; i < args.length; i++) {
            String k = args[i];
            if (k == null) {
                throw new NullPointerException();
            }
            i++;
            String v = args[i];
            if (v == null) {
                throw new NullPointerException();
            }
            PString key = PString.valueOf(k);
            PString value = PString.valueOf(v);
            if (map.containsKey(key)) {
                throw new IllegalArgumentException();
            }
            map.put(key, value);
        }
        map = Collections.unmodifiableMap(map);
        return new PMap(map, null);
    }

    public static PMap valueOf(String str) throws ArgumentFormatException {
        if (str.length() == 0) {
            return PMap.EMPTY;
        }
//        Map<Argument, Argument> map = new LinkedHashMap<Argument, Argument>();
//        PArray entries = PArray.valueOf(str);
//        for (Argument arg : entries) {
//            PArray entry = PArray.coerce(arg);
//            if (entry.getSize() != 2) {
//                throw new ArgumentFormatException();
//            }
//            Argument key = entry.get(0);
//            Argument value = entry.get(1);
//            if (map.containsKey(key)) {
//                throw new ArgumentFormatException();
//            }
//            map.put(key, value);
//        }
//        map = Collections.unmodifiableMap(map);
//        return new PMap(map, str);
        
        
        PArray vals = PArray.valueOf(str);
        int size = vals.getSize();
        if (size == 0) {
            return PMap.EMPTY;
        }
        if ((size % 2) != 0) {
            throw new ArgumentFormatException();
        }
        Map<Argument, Argument> map = new LinkedHashMap<Argument, Argument>();
        for (int i=0; i<size;) {
            Argument key = vals.get(i++);
            Argument value = vals.get(i++);
            key = map.put(key, value);
            if (key != null) {
                throw new ArgumentFormatException();
            }
        }
        map = Collections.unmodifiableMap(map);
        return new PMap(map, str);
    }

    public static PMap coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof PMap) {
            return (PMap) arg;
        }
        throw new UnsupportedOperationException();
    }
}
