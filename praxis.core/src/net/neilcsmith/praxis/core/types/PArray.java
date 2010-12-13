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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.syntax.Token;
import net.neilcsmith.praxis.core.syntax.Tokenizer;


/**
 *
 * @author Neil C Smith
 */
public final class PArray extends Argument implements Iterable<Argument> {

    public final static PArray EMPTY = new PArray(new Argument[0], "");
    private Argument[] data;
    private String str;

    private PArray(Argument[] data, String str) {
        this.data = data;
        this.str = str;
    }

    public Argument get(int index) {
        return data[index];
    }

    public Argument[] getAll() {
        return data.clone();
    }

    public int getSize() {
        return data.length;
    }

    @Override
    public String toString() {
        if (str == null) {
            if (data.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (Argument entry : data) {
                    if (sb.length() == 0) {
                        sb.append("{");
                    } else {
                        sb.append(" {");
                    }

                    String s = String.valueOf(entry);
                    if (s.indexOf('{') > -1 || s.indexOf('}') > -1) {
                        s = s.replace("{", "\\{");
                        s = s.replace("}", "\\}");
                    }
                    sb.append(s);
//                sb.append(String.valueOf(entry));

                    sb.append("}");
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

    public Iterator<Argument> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<Argument> {

        int cursor = 0;

        public boolean hasNext() {
            return cursor < data.length;
        }

        public Argument next() {
            Argument arg = data[cursor];
            cursor++;
            return arg;
        }

        public void remove() {
            throw new UnsupportedOperationException("PArrays are immutable");
        }
    }

    public static PArray valueOf(Collection<? extends Argument> collection) {
//        return valueOf(collection.toArray(new Argument[collection.size()]));
        if (collection.contains(null)) {
            throw new NullPointerException();
        }
        return new PArray(collection.toArray(new Argument[collection.size()]), null);

    }

    public static PArray valueOf(Argument ... args) {
        int size = args.length;
        if (size == 0) {
            return PArray.EMPTY;
        }
        Argument[] copy = new Argument[size];
        for (int i = 0; i < size; i++) {
            Argument arg = args[i];
            if (arg == null) {
                throw new NullPointerException();
            }
            copy[i] = arg;
        }
        return new PArray(copy, null);
    }

    public static PArray valueOf(CallArguments args) {
        return new PArray(args.getAll(), null);
    }

    public static PArray valueOf(String str) throws ArgumentFormatException {
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
                        list.add(PString.valueOf(t.getText()));
                        break;
                    case BRACED:
                        String s = t.getText();
                        if (s.indexOf('{') > -1 || s.indexOf('}') > -1) {
                            s = s.replace("\\{", "{");
                            s = s.replace("\\}", "}");
                        }
                        list.add(PString.valueOf(s));
                        break;
                    case EOL:
                        break tokenize;
                    default:
                        throw new ArgumentFormatException();
                }
            }
            int size = list.size();
            if (size == 0) {
                return PArray.EMPTY;
            } else {
                return new PArray(list.toArray(new Argument[size]), str);
            }
        } catch (Exception ex) {
            throw new ArgumentFormatException(ex);
        }


    }

    public static PArray coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof PArray) {
            return (PArray) arg;
        } else {
            return valueOf(arg.toString());
        }
    }
    
    public static ArgumentInfo info() {
        return ArgumentInfo.create(PArray.class, null);
    }
}
