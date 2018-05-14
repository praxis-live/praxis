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
 *
 */

package org.praxislive.gui.impl;

import java.util.ArrayList;
import org.praxislive.core.types.PNumber;

/**
 *
 * @author Neil C Smith
 */
// @TODO - replace this
class PMath {

    private PMath() {

    }

    public static PNumber getMaximum(PNumber ... numbers) {
        if (numbers == null || numbers.length == 0) {
            return null;
        }
        ArrayList<PNumber> list = new ArrayList<PNumber>(numbers.length);
        for (PNumber number : numbers) {
            if (number != null) {
                list.add(number);
            }
        }
        int size = list.size();
        if (size == 0) {
            return null;
        } else if (size == 1) {
            return list.get(0);
        } else {
            PNumber ret = list.get(0);
            for (int i=1; i < size; i++) {
                PNumber n = list.get(i);
                if (n.value() > ret.value()) {
                    ret = n;
                }
            }
            return ret;
        }
    }

    public static PNumber getMinimum(PNumber ... numbers) {
        if (numbers == null || numbers.length == 0) {
            return null;
        }
        ArrayList<PNumber> list = new ArrayList<PNumber>(numbers.length);
        for (PNumber number : numbers) {
            if (number != null) {
                list.add(number);
            }
        }
        int size = list.size();
        if (size == 0) {
            return null;
        } else if (size == 1) {
            return list.get(0);
        } else {
            PNumber ret = list.get(0);
            for (int i=1; i < size; i++) {
                PNumber n = list.get(i);
                if (n.value() < ret.value()) {
                    ret = n;
                }
            }
            return ret;
        }
    }

}
