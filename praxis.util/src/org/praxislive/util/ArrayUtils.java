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

package org.praxislive.util;

import java.lang.reflect.Array;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ArrayUtils {

    private ArrayUtils() {}

    public static <T> T[] add(T[] array, T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        for (int i=0; i < array.length; i++) {
            if (array[i] == obj) {
                return array;
            }
        }
        int length = array.length + 1;
        T[] ret = (T[]) Array.newInstance(array.getClass().getComponentType(), length);
        System.arraycopy(array, 0, ret, 0, length - 1);
        ret[length - 1] = obj;
        return ret;
    }

    public static <T> T[] remove(T[] array, T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        int idx = -1;
        for (int i=0; i < array.length; i++) {
            if (array[i] == obj) {
                idx = i;
                break;
            }
        }
        if (idx == -1) {
            return array;
        }
        int length = array.length - 1;
        T[] ret = (T[]) Array.newInstance(array.getClass().getComponentType(), length);
        System.arraycopy(array, 0, ret, 0, idx);
        System.arraycopy(array, idx + 1, ret, idx, length - idx);
        return ret;
    }

}
