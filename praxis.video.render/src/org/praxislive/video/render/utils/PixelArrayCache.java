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

package org.praxislive.video.render.utils;

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
    private final static int SMALL_THRESHOLD = 256;
    private final static int TILE_SIZE = 256 * 256;

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
            int[] array = null;
            SoftReference<int[]> ref = null;
            int minDiff = Integer.MAX_VALUE;
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
                    if (array == null || diff < minDiff) {
                        array = ar;
                        ref = entry;
                        minDiff = diff;
                    }
                    if (diff == 0) {
                        break;
                    }
                }
            }
            if ( (array != null) && (minDiff < (calculateSize(size)) ) ) {
                arrays.remove(ref);
                if (clear) {
                    Arrays.fill(array, 0);
                }
                return array;
            } else {
                array = new int[calculateSize(size)];
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST,
                            "Creating new array of size : " + array.length +
                            "\n - Requested size : " + size +
                            "\n - Cache list size : " + arrays.size() +
                            "\n - Minimum Difference found : " + minDiff);

                }
                return array;
            }      
        }

        private int calculateSize(int minSize) {
//            if (minSize < TILE_SIZE) {
                int size = SMALL_THRESHOLD * 2;
                while (size < minSize) {
                    size *= 2;
                }
                return size;
//            } else {
//                int size = minSize / TILE_SIZE;
//                size += 1;
//                size *= TILE_SIZE;
//                return size;
//            }
        }

        private void release(int[] array) {
            arrays.add(new SoftReference<int[]>(array));
        }

    }

}
