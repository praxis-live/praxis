/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * Copying and distribution of this file, with or without modification,
 * are permitted in any medium without royalty provided the copyright
 * notice and this notice are preserved.  This file is offered as-is,
 * without any warranty.
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 *
 */
package org.praxislive.video.render.utils;

/**
 *
 * @author Neil C Smith
 */
public class RGBMath {

    public static final int ALPHA_MASK = 0xff000000;
    public static final int RED_MASK = 0x00ff0000;
    public static final int GREEN_MASK = 0x0000ff00;
    public static final int BLUE_MASK = 0x000000ff;

    private RGBMath() {
    }

    /** Calculates lowest value of supplied args
     * @param a first integer
     * @param b second integer
     * @return int lowest value
     */
    public static int min(int a, int b) {
        return (a < b) ? a : b;
    }
    
    public static int min(int a, int b, int c) {
        return min(min(a, b), c);
    }

    /** Calculates mix of supplied args.  All args should be in range 0 - 255.
     * param a first integer value
     * param b second integer value
     * param f mix integer value (0 - 255) 0 all a / 255 all b
     */
    public static int mix(int a, int b, int f) {
        return a + (((b - a) * f) >> 8);
    }

    /** Calculates mix of supplied args.  All args should be in range 0 - 255.
     * param a first integer value
     * param b second integer value
     * param af level of a (0 - 255)
     * param bf mix integer value (0 - 255) 0 all a / 255 all b
     */
    public static int mix(int a, int b, int af, int bf) {
        return ((a * af) >> 8) + (((b - a) * bf) >> 8);
    }

    /** Calculates highest value of supplied args
     * @param a first integer
     * @param b second integer
     * @return int highest value
     */
    public static int max(int a, int b) {
        return (a > b) ? a : b;
    }
    
    public static int max(int a, int b, int c) {
        return max(max(a,b),c);
    }

    /** Calculates difference between supplied values. Returned value is always positive.
     * @param a first integer
     * @param b second integer
     * @return int difference
     */
    public static int diff(int a, int b) {
        return (a >= b) ? (a - b) : (b - a);
    }

    public static int multRGB(int src, int multiplier) {
        multiplier++;
        return ((src & RED_MASK) * multiplier) >> 8 & RED_MASK |
                ((src & GREEN_MASK) * multiplier) >> 8 & GREEN_MASK |
                ((src & BLUE_MASK) * multiplier) >> 8;
    }

    public static int multARGB(int src, int multiplier) {
        multiplier++;
        return ((src >>> 24) * multiplier) << 16 & ALPHA_MASK |
                ((src & RED_MASK) * multiplier) >> 8 & RED_MASK |
                ((src & GREEN_MASK) * multiplier) >> 8 & GREEN_MASK |
                ((src & BLUE_MASK) * multiplier) >> 8;
    }

    public static int blend(int src, int dest, int alpha) {
        return src + (((0xFF - alpha) * dest) >> 8);
    }

    public static int mult(int val, int multiplier) {
        return (val * (multiplier + 1)) >> 8;
    }
    
    private static int rngseed = 0;

    public static int random() {
        rngseed = rngseed * 1103515245 + 12345;
        return ((rngseed >> 16) & 0xFF);
    }

     public static int premultiply(int argb) {
        int a = argb >>> 24;

        if (a == 0) {
            return 0;
        }
        else if (a == 255) {
            return argb;
        }
        else {
            return (a << 24) | multRGB(argb, a);
        }
    }
}
