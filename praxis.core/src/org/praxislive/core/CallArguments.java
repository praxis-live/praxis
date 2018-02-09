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

package org.praxislive.core;

import java.util.Arrays;
import java.util.List;

/**
 * Holder for Arguments in Calls.
 *
 * @author Neil C Smith
 */
public abstract class CallArguments {
    
//    public final static CallArguments EMPTY = new CallArguments(new Value[0]);
    /**
     * EMPTY CallArguments, returns 0 for getSize().
     */
    public final static CallArguments EMPTY = new Empty();
    
    /**
     * Get number of Arguments
     *
     * @return int Count
     */
    public abstract int getSize();

    /**
     * Get Value. Index must be between 0 and count-1.
     *
     * @param index int position in Value array
     * @return Value
     */
    public abstract Value get(int index);

    public abstract Value[] getAll();
    
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




    }
    
        


//    private CallArguments(Value[] args) {
//        this.args = args;
//    }
    
//    public int getSize() {
//        return args.length;
//    }
//
//    public Value get(int index) {
//        return args[index];
//    }

    
//    public static CallArguments create() {
//        return EMPTY;
//    }
    
    /**
     * Create a CallArguments wrapping the given Value. The CallArguments
     * returned will be optimized for single Arguments.
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
    public static CallArguments create(List<Value> list) {
        return create(list.toArray(new Value[list.size()]));
        

    }
    
    /**
     * Create a CallArguments from the supplied array of Arguments. CallArguments
     * are immutable and will not reflect any subsequent changes to the array.
     * @param args
     * @return CallArguments
     */
    public static CallArguments create(Value ... args) {
        if (args.length == 0) {
            return EMPTY;
        } else if (args.length == 1) {
            Value arg = args[0];
            if (arg == null) {
                throw new NullPointerException();
            }
            return new Single(arg);
        } else {
            for (Value arg : args) {
                if (arg == null) {
                    throw new NullPointerException();
                }
            }
            return new Multi(args.clone());
        }
    }


    
//    public static CallArguments create(Value[] args) {
//        int size = args.length;
//        if (size == 0) {
//            return EMPTY;
//        }
//        Value[] copy = new Value[size];
//        for (int i=0; i < size; i++) {
//            Value arg = args[i];
//            if (arg == null) {
//                throw new NullPointerException();
//            }
//            copy[i] = arg;
//        }
//        return new CallArguments(copy);
//    }
    
    

}
