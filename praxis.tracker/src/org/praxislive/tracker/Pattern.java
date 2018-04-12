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
package org.praxislive.tracker;

import org.praxislive.core.Value;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class Pattern {
    
    public final static Pattern EMPTY = new Empty();
    
    public abstract Value getValueAt(int row, int column);
    
    public abstract int getRowCount();
    
    public abstract int getColumnCount();
    
    private static class Empty extends Pattern {

        @Override
        public Value getValueAt(int row, int column) {
            return null;
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public int getColumnCount() {
            return 1;
        }
        
    }
    
}
