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

package org.praxislive.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * A lightweight read-only holder of Values for use as 
 * arguments in a Call.
 *
 * @author Neil C Smith
 */
@Deprecated
public abstract class CallArguments implements Iterable<Value> {
    
    /**
     * Empty CallArguments.
     */
    public final static CallArguments EMPTY = new Empty();
    
    /**
     * Get number of Arguments
     *
     * @return int Count
     */
    public abstract int getSize();

    /**
     * Get Value at index. Index must be between 0 and getSize()-1.
     *
     * @param index int position in Value array
     * @throws IndexOutOfBoundsException
     * @return Value
     */
    public abstract Value get(int index);

    @Deprecated
    public abstract Value[] getAll();
    
    /**
     * Convenience method to check whether the arguments is empty.
     * 
     * @return boolean true when getSize() == 0
     */
    public abstract boolean isEmpty();
    
    /**
     * Create a Stream of Values from the arguments.
     * @return Stream of Value
     */
    public abstract Stream<Value> stream();
    
    private static class Empty extends CallArguments {

        private final static Value[] NONE = new Value[0];

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public Value get(int index) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        public Value[] getAll() {
            return NONE;
        }


        @Override
        public boolean equals(Object obj) {
            return (obj instanceof Empty);
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "{}";
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Iterator<Value> iterator() {
            return Collections.EMPTY_LIST.iterator();
        }

        @Override
        public Stream<Value> stream() {
            return Stream.empty();
        }

    }
    
    private static class Single extends CallArguments {
        
        private final Value arg;
        
        private Single(Value arg) {
            this.arg = arg;
        }

        @Override
        public int getSize() {
            return 1;
        }

        @Override
        public Value get(int index) {
            if (index == 0) {
                return arg;
            }
            throw new IndexOutOfBoundsException();
        }

        @Override
        public Value[] getAll() {
            return new Value[] {arg};
        }


        @Override
        public String toString() {
            return ("{{" + arg.toString() + "}}");
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Iterator<Value> iterator() {
            return Collections.singletonList(arg).iterator();
        }

        @Override
        public Stream<Value> stream() {
            return Stream.of(arg);
        }
        
    }
    
    
    private static class Multi extends CallArguments {
        
        private final Value[] args;
        
        private Multi(Value[] args) {
            this.args = args;
        }

        @Override
        public int getSize() {
            return args.length;
        }

        @Override
        public Value get(int index) {
            return args[index];
        }

        @Override
        public Value[] getAll() {
            return args.clone();
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder("{");
            for (Value arg : args) {
                str.append("{");
                str.append(arg.toString());
                str.append("}");
            }
            str.append("}");
            return str.toString();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Iterator<Value> iterator() {
            return Stream.of(args).iterator();
        }

        @Override
        public Stream<Value> stream() {
            return Stream.of(args);
        }

    }
    /**
     * Create a CallArguments wrapping the given Value. The CallArguments
     * returned will be optimized for single values.
     *
     * @param arg
     * @return CallArguments
     */
    public static CallArguments create(Value arg) {
        if (arg == null) {
            throw new NullPointerException();
        }
        return new Single(arg);
    }
    
    /**
     * Create a CallArguments from the supplied list of Arguments. CallArguments
     * are immutable and will not reflect any subsequent changes to the list.
     * @param list
     * @return CallArguments
     */
    public static CallArguments create(Collection<? extends Value> list) {
        return create(list.toArray(new Value[list.size()]), false);
    }
    
    /**
     * Create a CallArguments from the supplied array of Arguments. CallArguments
     * are immutable and will not reflect any subsequent changes to the array.
     * @param args
     * @return CallArguments
     */
    public static CallArguments create(Value ... args) {
        return create(args, true);
    }
    
    private static CallArguments create(Value[] args, boolean clone) {
        if (args.length == 0) {
            return EMPTY;
        } else if (args.length == 1) {
            Value arg = args[0];
            if (arg == null) {
                throw new NullPointerException();
            }
            return new Single(arg);
        } else {
            Value[] safeArgs = clone ? args.clone() : args;
            for (Value arg : safeArgs) {
                if (arg == null) {
                    throw new NullPointerException();
                }
            }
            return new Multi(safeArgs);
        }
    }
    
    public static <T extends Value> Collector<T, ?, CallArguments> collector() {

        return Collector.<T, List<T>, CallArguments>of(
                ArrayList::new,
                List::add,
                (list1, list2) -> {
                    list1.addAll(list2);
                    return list1;
                },
                l -> CallArguments.create(l.toArray(new Value[l.size()]), false)
        );
    }
    

}
