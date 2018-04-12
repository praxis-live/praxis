package org.praxislive.code.userapi;

///*
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// *
// * Copyright 2018 Neil C Smith.
// *
// * This code is free software; you can redistribute it and/or modify it
// * under the terms of the GNU Lesser General Public License version 3 only, as
// * published by the Free Software Foundation.
// *
// * This code is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
// * version 3 for more details.
// *
// * You should have received a copy of the GNU Lesser General Public License version 3
// * along with this work; if not, see http://www.gnu.org/licenses/
// *
// *
// * Please visit https://www.praxislive.org if you need additional information or
// * have any questions.
// *
// */
//package org.praxislive.code.userapi;
//
//import java.util.Objects;
//import org.praxislive.core.types.Value;
//import org.praxislive.core.types.PArray;
//import org.praxislive.core.types.PString;
//
///**
// *
// * @author Neil C Smith (http://neilcsmith.net)
// */
//public abstract class Cycle {
//    
//    private PArray values = PArray.EMPTY;
//    private int index;
//    
//    public Value next() {
//        return next(1);
//    }
//    
//    public Value next(int skip) {
//        if (values.isEmpty()) {
//            return PString.EMPTY;
//        }
//        Value val = values.get(index);
//        int count = values.getSize();
//        index += Math.max(1, Math.abs(skip) % count);
//        index %= count;
//        return val;
//        
//    }
//    
//    public Value previous() {
//        return previous(1);
//    }
//    
//    public Value previous(int skip) {
//        if (values.isEmpty()) {
//            return PString.EMPTY;
//        }
//        int count = values.getSize();
//        Value val = values.get(index == 0 ? count - 1 : index);
//        index -= Math.max(1, Math.abs(skip) % count);
//        while (index < 0) {
//            index += count;
//        }
//        return val;
//    }
//    
//    public Cycle values(PArray values) {
//        this.values = Objects.requireNonNull(values);
//        if (values.isEmpty()) {
//            index = 0;
//        } else {
//            index %= values.getSize();
//        }
//        return this;
//    }
//    
//    public PArray values() {
//        return values;
//    }
//    
//    public Cycle index(int index) {
//        if (index < 0 || index >= values.getSize()) {
//            throw new IndexOutOfBoundsException();
//        }
//        this.index = index;
//        return this;
//    }
//    
//    public int index() {
//        return index;
//    }
//    
//    public Cycle reset() {
//        index(0);
//        return this;
//    }
//
//}
