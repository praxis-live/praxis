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

package net.neilcsmith.ripl.utils;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class PixelArrayCache {

    private final static Logger LOG = Logger.getLogger(PixelArrayCache.class.getName());
    private final static int SMALL_THRESHOLD = 512;

    private static ThreadLocal<CacheImpl> cache = new ThreadLocal<CacheImpl>() {

        @Override
        protected CacheImpl initialValue() {
            return new CacheImpl();
        }

    };

    private PixelArrayCache() {}

    public static int[] acquire(int size, boolean clear) {
        if (size > SMALL_THRESHOLD) {
            return cache.get().acquire(size, clear);
        } else {
            LOG.log(Level.FINEST, "Creating array below threshold of size : {0}", size);
            return new int[size];
        }
        
    }

    public static void release(int[] array) {
        if (array.length > SMALL_THRESHOLD) {
            cache.get().release(array);
        } 
    }

    private static class CacheImpl {

        private List<SoftReference<int[]>> arrays = new ArrayList<SoftReference<int[]>>();

        private int[] acquire(int size, boolean clear) {
            int[] chosen = null;
            SoftReference<int[]> chosenRef = null;
            int minDiff = Integer.MAX_VALUE;
            int biggest = 0;
            Iterator<SoftReference<int[]>> itr = arrays.iterator();
            while (itr.hasNext()) {
                SoftReference<int[]> entry = itr.next();
                int[] ar = entry.get();
                if (ar == null) {
                    LOG.log(Level.FINEST,
                            "Pixel array collected, removing SoftReference.\nList size : {0}",
                            arrays.size());
                    itr.remove();
                    continue;
                }
                if (ar.length >= size) {
                    int diff = ar.length - size;
                    if (diff == 0) {
                        chosen = ar;
                        chosenRef = entry;
                        break;
                    } else if (chosen == null || minDiff < diff) {
                        chosen = ar;
                        chosenRef = entry;
                        minDiff = diff;
                    }
                } else {
                    if (ar.length > biggest) {
                        biggest = ar.length;
                    }
                }
            }
            if (chosen != null) {
                arrays.remove(chosenRef);
                if (clear) {
                    Arrays.fill(chosen, 0);
                }
                return chosen;
            } else {
                biggest *= 2;
                int[] ret;
                if (biggest >= size) {
                    ret = new int[biggest];
                } else {
                    ret = new int[size];
                }
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST,
                            "Creating new array of size : {0}.\nCache list size : {1}",
                            new Object[]{ret.length, arrays.size()});
                }
                return ret;
            }
            
            
        }

        private void release(int[] array) {
            arrays.add(new SoftReference<int[]>(array));
        }

    }

}
