/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */

package net.neilcsmith.praxis.core;

import java.util.Arrays;
import java.util.List;

/**
 * Holder for Arguments in Calls.
 *
 * @author Neil C Smith
 */
public abstract class CallArguments {
    
//    public final static CallArguments EMPTY = new CallArguments(new Argument[0]);
    /**
     * EMPTY CallArguments, returns 0 for getCount().
     */
    public final static CallArguments EMPTY = new Empty();
    
    /**
     * Get number of Arguments
     *
     * @return int Count
     */
    public abstract int getCount();

    /**
     * Get Argument. Index must be between 0 and count-1.
     *
     * @param index int position in Argument array
     * @return Argument
     */
    public abstract Argument getArg(int index);
    
    private static class Empty extends CallArguments {

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Argument getArg(int index) {
            throw new IndexOutOfBoundsException();
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
        
        private Argument arg;
        
        private Single(Argument arg) {
            this.arg = arg;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Argument getArg(int index) {
            if (index == 0) {
                return arg;
            }
            throw new IndexOutOfBoundsException();
        }

        @Override
        public String toString() {
            return ("{{" + arg.toString() + "}}");
        }


   
    }
    
    
    private static class Multi extends CallArguments {
        
        private Argument[] args;
        
        private Multi(Argument[] args) {
            this.args = args;
        }

        @Override
        public int getCount() {
            return args.length;
        }

        @Override
        public Argument getArg(int index) {
            return args[index];
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder("{");
            for (Argument arg : args) {
                str.append("{");
                str.append(arg.toString());
                str.append("}");
            }
            str.append("}");
            return str.toString();
        }




    }
    
        


//    private CallArguments(Argument[] args) {
//        this.args = args;
//    }
    
//    public int getCount() {
//        return args.length;
//    }
//
//    public Argument getArg(int index) {
//        return args[index];
//    }

    
//    public static CallArguments create() {
//        return EMPTY;
//    }
    
    /**
     * Create a CallArguments wrapping the given Argument. The CallArguments
     * returned will be optimized for single Arguments.
     *
     * @param arg
     * @return CallArguments
     */
    public static CallArguments create(Argument arg) {
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
    public static CallArguments create(List<Argument> list) {
        return create(list.toArray(new Argument[list.size()]));
        

    }
    
    /**
     * Create a CallArguments from the supplied array of Arguments. CallArguments
     * are immutable and will not reflect any subsequent changes to the array.
     * @param args
     * @return CallArguments
     */
    public static CallArguments create(Argument ... args) {
        if (args.length == 0) {
            return EMPTY;
        } else if (args.length == 1) {
            Argument arg = args[0];
            if (arg == null) {
                throw new NullPointerException();
            }
            return new Single(arg);
        } else {
            for (Argument arg : args) {
                if (arg == null) {
                    throw new NullPointerException();
                }
            }
            return new Multi(args.clone());
        }
    }


    
//    public static CallArguments create(Argument[] args) {
//        int size = args.length;
//        if (size == 0) {
//            return EMPTY;
//        }
//        Argument[] copy = new Argument[size];
//        for (int i=0; i < size; i++) {
//            Argument arg = args[i];
//            if (arg == null) {
//                throw new NullPointerException();
//            }
//            copy[i] = arg;
//        }
//        return new CallArguments(copy);
//    }
    
    

}
