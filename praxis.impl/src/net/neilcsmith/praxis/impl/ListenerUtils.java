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
 *
 */

package net.neilcsmith.praxis.impl;

import java.lang.reflect.Array;
import net.neilcsmith.praxis.util.ArrayUtils;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
@Deprecated
public class ListenerUtils {

    private ListenerUtils() {}

    public static <T> T[] add(T[] listeners, T listener) {
        return ArrayUtils.add(listeners, listener);
//        if (listener == null) {
//            throw new NullPointerException();
//        }
//        for (int i=0; i < listeners.length; i++) {
//            if (listeners[i] == listener) {
//                return listeners;
//            }
//        }
//        int length = listeners.length + 1;
//        T[] ret = (T[]) Array.newInstance(listeners.getClass().getComponentType(), length);
//        System.arraycopy(listeners, 0, ret, 0, length - 1);
//        ret[length - 1] = listener;
//        return ret;
    }

    public static <T> T[] remove(T[] listeners, T listener) {
        return ArrayUtils.remove(listeners, listener);
//        if (listener == null) {
//            throw new NullPointerException();
//        }
//        int idx = -1;
//        for (int i=0; i < listeners.length; i++) {
//            if (listeners[i] == listener) {
//                idx = i;
//                break;
//            }
//        }
//        if (idx == -1) {
//            return listeners;
//        }
//        int length = listeners.length - 1;
//        T[] ret = (T[]) Array.newInstance(listeners.getClass().getComponentType(), length);
//        System.arraycopy(listeners, 0, ret, 0, idx);
//        System.arraycopy(listeners, idx + 1, ret, idx, length - idx);
//        return ret;
    }

}
