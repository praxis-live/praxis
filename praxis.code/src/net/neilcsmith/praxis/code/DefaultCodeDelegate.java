/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Neil C Smith.
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
package net.neilcsmith.praxis.code;

import java.util.Arrays;
import java.util.Random;
import net.neilcsmith.praxis.code.userapi.Constants;
import net.neilcsmith.praxis.code.userapi.Property;
import net.neilcsmith.praxis.core.Control;
import net.neilcsmith.praxis.logging.LogLevel;
import processing.core.PApplet;

public class DefaultCodeDelegate extends CodeDelegate {
    
    final static String[] IMPORTS = {
        "java.util.*",
        "net.neilcsmith.praxis.code.userapi.*",
        "static net.neilcsmith.praxis.code.userapi.Constants.*"
    };

    private final Random rnd;

    public DefaultCodeDelegate() {
        rnd = new Random();
    }

    public final void log(LogLevel level, String msg) {
        getContext().getLog().log(level, msg);
    }
    
    public final void log(LogLevel level, Exception ex) {
        getContext().getLog().log(level, ex);
    }
    
    public final void log(LogLevel level, Exception ex, String msg) {
        getContext().getLog().log(level, ex, msg);
    }
    
    public final void log(LogLevel level, Class<? extends Exception> type, String msg) {
        getContext().getLog().log(level, type, msg);
    }
    
    public final boolean isLoggable(LogLevel level) {
        return getContext().getLogLevel().isLoggable(level);
    }
    
    public final long time() {
        return getContext().getTime();
    }

    public final double d(Property p) {
        return p.getDouble();
    }

    public final float f(Property p) {
        return (float) p.getDouble();
    }

    public final int i(Property p) {
        return p.getInt();
    }

    public final String s(Property p) {
        return p.get().toString();
    }

    public final Property p(String id) {
        Control c = getContext().getControl(id);
//        return c instanceof Property ? (Property) c : null;
        if (c instanceof Property) {
            return (Property) c;
        } else {
            return null;
        }
    }

    public final double random(double max) {
        return rnd.nextDouble() * max;
    }

    public final double random(double min, double max) {
        if (min >= max) {
            return min;
        }
        return random(max - min) + min;
    }

    public final double abs(double n) {
        return (n < 0) ? -n : n;
    }

    public final double sq(double a) {
        return a * a;
    }

    public final double sqrt(double a) {
        return Math.sqrt(a);
    }

    public final double log(double a) {
        return Math.log(a);
    }

    public final double exp(double a) {
        return Math.exp(a);
    }

    public final double pow(double a, double b) {
        return Math.pow(a, b);
    }

    public final int max(int a, int b) {
        return PApplet.max(a, b);
    }

    public final int max(int a, int b, int c) {
        return PApplet.max(a, b, c);
    }

    public final int max(int[] values) {
        return PApplet.max(values);
    }

    public final double max(double a, double b) {
        return (a > b) ? a : b;
    }

    public final double max(double a, double b, double c) {
        return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
    }

    public final double max(double[] values) {
        if (values.length == 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        double max = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    public final int min(int a, int b) {
        return PApplet.min(a, b);
    }

    public final int min(int a, int b, int c) {
        return PApplet.min(a, b, c);
    }

    public final int min(int[] values) {
        return PApplet.min(values);
    }

    public final double min(double a, double b) {
        return (a < b) ? a : b;
    }

    public final double min(double a, double b, double c) {
        return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
    }

    public final double min(double[] list) {
        if (list.length == 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        double min = list[0];
        for (int i = 1; i < list.length; i++) {
            if (list[i] < min) {
                min = list[i];
            }
        }
        return min;
    }

    public final int constrain(int amt, int low, int high) {
        return (amt < low) ? low : ((amt > high) ? high : amt);
    }

    public final double constrain(double amt, double low, double high) {
        return (amt < low) ? low : ((amt > high) ? high : amt);
    }

    public final double degrees(double radians) {
        return Math.toDegrees(radians);
    }

    public final double radians(double degrees) {
        return Math.toRadians(degrees);
    }

    public final double sin(double angle) {
        return Math.sin(angle);
    }

    public final double cos(double angle) {
        return Math.cos(angle);
    }

    public final double tan(double angle) {
        return Math.tan(angle);
    }

    public final double asin(double value) {
        return Math.asin(value);
    }

    public final double acos(double value) {
        return Math.acos(value);
    }

    public final double atan(double value) {
        return Math.atan(value);
    }

    public final double atan2(double a, double b) {
        return Math.atan2(a, b);
    }

    public final double map(double value,
            double start1, double stop1,
            double start2, double stop2) {
        return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    }

    /**
     * Calculates the distance between two points.
     *
     * @webref math:calculation
     * @param x1 x-coordinate of the first point
     * @param y1 y-coordinate of the first point
     * @param x2 x-coordinate of the second point
     * @param y2 y-coordinate of the second point
     */
    public final double dist(double x1, double y1, double x2, double y2) {
        return sqrt(sq(x2 - x1) + sq(y2 - y1));
    }

    /**
     * Calculates the distance between two points.
     *
     * ( end auto-generated )
     *
     * @webref math:calculation
     * @param x1 x-coordinate of the first point
     * @param y1 y-coordinate of the first point
     * @param z1 z-coordinate of the first point
     * @param x2 x-coordinate of the second point
     * @param y2 y-coordinate of the second point
     * @param z2 z-coordinate of the second point
     */
    public final double dist(double x1, double y1, double z1,
            double x2, double y2, double z2) {
        return sqrt(sq(x2 - x1) + sq(y2 - y1) + sq(z2 - z1));
    }

    /**
     * Calculates a number between two numbers at a specific increment. The
     * <b>amt</b> parameter is the amount to interpolate between the two values
     * where 0.0 equal to the first point, 0.1 is very near the first point, 0.5
     * is half-way in between, etc. The lerp function is convenient for creating
     * motion along a straight path and for drawing dotted lines.
     *
     * @webref math:calculation
     * @param start first value
     * @param stop second value
     * @param amt float between 0.0 and 1.0
     */
    public final double lerp(double start, double stop, double amt) {
        return start + (stop - start) * amt;
    }

    /**
     * Normalizes a number from another range into a value between 0 and 1.
     * <br/> <br/>
     * Identical to map(value, low, high, 0, 1);
     * <br/> <br/>
     * Numbers outside the range are not clamped to 0 and 1, because
     * out-of-range values are often intentional and useful.
     *
     * ( end auto-generated )
     *
     * @webref math:calculation
     * @param value the incoming value to be converted
     * @param start lower bound of the value's current range
     * @param stop upper bound of the value's current range
     */
    public final double norm(double value, double start, double stop) {
        return (value - start) / (stop - start);
    }

    // PERLIN NOISE - copied from Processing core.
    // @TODO fully convert to double???
    private static final int PERLIN_YWRAPB = 4;
    private static final int PERLIN_YWRAP = 1 << PERLIN_YWRAPB;
    private static final int PERLIN_ZWRAPB = 8;
    private static final int PERLIN_ZWRAP = 1 << PERLIN_ZWRAPB;
    private static final int PERLIN_SIZE = 4095;
//  private static final float sinLUT[];
    private static final float cosLUT[];
    private static final float SINCOS_PRECISION = 0.5f;
    private static final int SINCOS_LENGTH = (int) (360f / SINCOS_PRECISION);

    static {
//    sinLUT = new float[SINCOS_LENGTH];
        cosLUT = new float[SINCOS_LENGTH];
        for (int i = 0; i < SINCOS_LENGTH; i++) {
//      sinLUT[i] = (float) Math.sin(i * Constants.DEG_TO_RAD * SINCOS_PRECISION);
            cosLUT[i] = (float) Math.cos(i * Constants.DEG_TO_RAD * SINCOS_PRECISION);
        }
    }
    private int perlin_octaves = 4; // default to medium smooth
    private float perlin_amp_falloff = 0.5f; // 50% reduction/octave
    private int perlin_TWOPI, perlin_PI;
    private float[] perlin_cosTable;
    private float[] perlin;
    private Random perlinRandom;

    /**
     * Computes the Perlin noise function value at point x.
     */
    public final double noise(double x) {
        return noise(x, 0f, 0f);
    }

    /**
     * Computes the Perlin noise function value at the point x, y.
     */
    public final double noise(double x, double y) {
        return noise(x, y, 0f);
    }

    /**
     * Computes the Perlin noise function value at x, y, z.
     */
    public final double noise(double x, double y, double z) {
        if (perlin == null) {
            if (perlinRandom == null) {
                perlinRandom = new Random();
            }
            perlin = new float[PERLIN_SIZE + 1];
            for (int i = 0; i < PERLIN_SIZE + 1; i++) {
                perlin[i] = perlinRandom.nextFloat();
            }
            perlin_cosTable = cosLUT;
            perlin_TWOPI = perlin_PI = SINCOS_LENGTH;
            perlin_PI >>= 1;
        }

        if (x < 0) {
            x = -x;
        }
        if (y < 0) {
            y = -y;
        }
        if (z < 0) {
            z = -z;
        }

        int xi = (int) x, yi = (int) y, zi = (int) z;
        float xf = (float) (x - xi);
        float yf = (float) (y - yi);
        float zf = (float) (z - zi);
        float rxf, ryf;

        float r = 0;
        float ampl = 0.5f;

        float n1, n2, n3;

        for (int i = 0; i < perlin_octaves; i++) {
            int of = xi + (yi << PERLIN_YWRAPB) + (zi << PERLIN_ZWRAPB);

            rxf = noise_fsc(xf);
            ryf = noise_fsc(yf);

            n1 = perlin[of & PERLIN_SIZE];
            n1 += rxf * (perlin[(of + 1) & PERLIN_SIZE] - n1);
            n2 = perlin[(of + PERLIN_YWRAP) & PERLIN_SIZE];
            n2 += rxf * (perlin[(of + PERLIN_YWRAP + 1) & PERLIN_SIZE] - n2);
            n1 += ryf * (n2 - n1);

            of += PERLIN_ZWRAP;
            n2 = perlin[of & PERLIN_SIZE];
            n2 += rxf * (perlin[(of + 1) & PERLIN_SIZE] - n2);
            n3 = perlin[(of + PERLIN_YWRAP) & PERLIN_SIZE];
            n3 += rxf * (perlin[(of + PERLIN_YWRAP + 1) & PERLIN_SIZE] - n3);
            n2 += ryf * (n3 - n2);

            n1 += noise_fsc(zf) * (n2 - n1);

            r += n1 * ampl;
            ampl *= perlin_amp_falloff;
            xi <<= 1;
            xf *= 2;
            yi <<= 1;
            yf *= 2;
            zi <<= 1;
            zf *= 2;

            if (xf >= 1.0f) {
                xi++;
                xf--;
            }
            if (yf >= 1.0f) {
                yi++;
                yf--;
            }
            if (zf >= 1.0f) {
                zi++;
                zf--;
            }
        }
        return r;
    }

    private float noise_fsc(float i) {
        return 0.5f * (1.0f - perlin_cosTable[(int) (i * perlin_PI) % perlin_TWOPI]);
    }

    // make perlin noise quality user controlled to allow
    // for different levels of detail. lower values will produce
    // smoother results as higher octaves are surpressed
    public final void noiseDetail(int lod) {
        if (lod > 0) {
            perlin_octaves = lod;
        }
    }

    public final void noiseDetail(int lod, double falloff) {
        if (lod > 0) {
            perlin_octaves = lod;
        }
        if (falloff > 0) {
            perlin_amp_falloff = (float) falloff;
        }
    }

    public final void noiseSeed(long what) {
        if (perlinRandom == null) {
            perlinRandom = new Random();
        }
        perlinRandom.setSeed(what);
        perlin = null;
    }

    // end of Perlin noise functions
    // start of PApplet statics
    public final int minute() {
        return PApplet.minute();
    }

    public final int hour() {
        return PApplet.hour();
    }

    public final int day() {
        return PApplet.day();
    }

    public final int month() {
        return PApplet.month();
    }

    public final int year() {
        return PApplet.year();
    }

    public final byte[] sort(byte[] list) {
        return PApplet.sort(list);
    }

    public final byte[] sort(byte[] list, int count) {
        return PApplet.sort(list, count);
    }

    public final char[] sort(char[] list) {
        return PApplet.sort(list);
    }

    public final char[] sort(char[] list, int count) {
        return PApplet.sort(list, count);
    }

    public final int[] sort(int[] list) {
        return PApplet.sort(list);
    }

    public final int[] sort(int[] list, int count) {
        return PApplet.sort(list, count);
    }

    public final float[] sort(float[] list) {
        return PApplet.sort(list);
    }

    public final float[] sort(float[] list, int count) {
        return PApplet.sort(list, count);
    }

    public final double[] sort(double[] list) {
        return sort(list, list.length);
    }

    public final double[] sort(double[] list, int count) {
        double[] outgoing = new double[list.length];
        System.arraycopy(list, 0, outgoing, 0, list.length);
        Arrays.sort(outgoing, 0, count);
        return outgoing;
    }

    public final String[] sort(String[] list) {
        return PApplet.sort(list);
    }

    public final String[] sort(String[] list, int count) {
        return PApplet.sort(list, count);
    }

    public final void arrayCopy(Object src, int srcPosition, Object dst, int dstPosition, int length) {
        PApplet.arrayCopy(src, srcPosition, dst, dstPosition, length);
    }

    public final void arrayCopy(Object src, Object dst, int length) {
        PApplet.arrayCopy(src, dst, length);
    }

    public final void arrayCopy(Object src, Object dst) {
        PApplet.arrayCopy(src, dst);
    }

    public final boolean[] expand(boolean[] list) {
        return PApplet.expand(list);
    }

    public final boolean[] expand(boolean[] list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    public final byte[] expand(byte[] list) {
        return PApplet.expand(list);
    }

    public final byte[] expand(byte[] list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    public final char[] expand(char[] list) {
        return PApplet.expand(list);
    }

    public final char[] expand(char[] list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    public final int[] expand(int[] list) {
        return PApplet.expand(list);
    }

    public final int[] expand(int[] list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    public final long[] expand(long[] list) {
        return PApplet.expand(list);
    }

    public final long[] expand(long[] list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    public final float[] expand(float[] list) {
        return PApplet.expand(list);
    }

    public final float[] expand(float[] list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    public final double[] expand(double[] list) {
        return PApplet.expand(list);
    }

    public final double[] expand(double[] list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    public final String[] expand(String[] list) {
        return PApplet.expand(list);
    }

    public final String[] expand(String[] list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    public final Object expand(Object array) {
        return PApplet.expand(array);
    }

    public final Object expand(Object list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    public final byte[] append(byte[] array, byte value) {
        return PApplet.append(array, value);
    }

    public final char[] append(char[] array, char value) {
        return PApplet.append(array, value);
    }

    public final int[] append(int[] array, int value) {
        return PApplet.append(array, value);
    }

    public final float[] append(float[] array, float value) {
        return PApplet.append(array, value);
    }

    public final double[] append(double array[], double value) {
        array = expand(array, array.length + 1);
        array[array.length - 1] = value;
        return array;
    }

    public final String[] append(String[] array, String value) {
        return PApplet.append(array, value);
    }

    public final Object append(Object array, Object value) {
        return PApplet.append(array, value);
    }

    public final boolean[] shorten(boolean[] list) {
        return PApplet.shorten(list);
    }

    public final byte[] shorten(byte[] list) {
        return PApplet.shorten(list);
    }

    public final char[] shorten(char[] list) {
        return PApplet.shorten(list);
    }

    public final int[] shorten(int[] list) {
        return PApplet.shorten(list);
    }

    public final float[] shorten(float[] list) {
        return PApplet.shorten(list);
    }

    public final double[] shorten(double list[]) {
        return subset(list, 0, list.length - 1);
    }

    public final String[] shorten(String[] list) {
        return PApplet.shorten(list);
    }

    public final Object shorten(Object list) {
        return PApplet.shorten(list);
    }

    public final boolean[] splice(boolean[] list, boolean value, int index) {
        return PApplet.splice(list, value, index);
    }

    public final boolean[] splice(boolean[] list, boolean[] value, int index) {
        return PApplet.splice(list, value, index);
    }

    public final byte[] splice(byte[] list, byte value, int index) {
        return PApplet.splice(list, value, index);
    }

    public final byte[] splice(byte[] list, byte[] value, int index) {
        return PApplet.splice(list, value, index);
    }

    public final char[] splice(char[] list, char value, int index) {
        return PApplet.splice(list, value, index);
    }

    public final char[] splice(char[] list, char[] value, int index) {
        return PApplet.splice(list, value, index);
    }

    public final int[] splice(int[] list, int value, int index) {
        return PApplet.splice(list, value, index);
    }

    public final int[] splice(int[] list, int[] value, int index) {
        return PApplet.splice(list, value, index);
    }

    public final float[] splice(float[] list, float value, int index) {
        return PApplet.splice(list, value, index);
    }

    public final float[] splice(float[] list, float[] value, int index) {
        return PApplet.splice(list, value, index);
    }

    public final double[] splice(double list[],
            double value, int index) {
        double[] outgoing = new double[list.length + 1];
        System.arraycopy(list, 0, outgoing, 0, index);
        outgoing[index] = value;
        System.arraycopy(list, index, outgoing, index + 1,
                list.length - index);
        return outgoing;
    }

    public final double[] splice(double list[],
            double value[], int index) {
        double[] outgoing = new double[list.length + value.length];
        System.arraycopy(list, 0, outgoing, 0, index);
        System.arraycopy(value, 0, outgoing, index, value.length);
        System.arraycopy(list, index, outgoing, index + value.length,
                list.length - index);
        return outgoing;
    }

    public final String[] splice(String[] list, String value, int index) {
        return PApplet.splice(list, value, index);
    }

    public final String[] splice(String[] list, String[] value, int index) {
        return PApplet.splice(list, value, index);
    }

    public final Object splice(Object list, Object value, int index) {
        return PApplet.splice(list, value, index);
    }

    public final boolean[] subset(boolean[] list, int start) {
        return PApplet.subset(list, start);
    }

    public final boolean[] subset(boolean[] list, int start, int count) {
        return PApplet.subset(list, start, count);
    }

    public final byte[] subset(byte[] list, int start) {
        return PApplet.subset(list, start);
    }

    public final byte[] subset(byte[] list, int start, int count) {
        return PApplet.subset(list, start, count);
    }

    public final char[] subset(char[] list, int start) {
        return PApplet.subset(list, start);
    }

    public final char[] subset(char[] list, int start, int count) {
        return PApplet.subset(list, start, count);
    }

    public final int[] subset(int[] list, int start) {
        return PApplet.subset(list, start);
    }

    public final int[] subset(int[] list, int start, int count) {
        return PApplet.subset(list, start, count);
    }

    public final float[] subset(float[] list, int start) {
        return PApplet.subset(list, start);
    }

    public final float[] subset(float[] list, int start, int count) {
        return PApplet.subset(list, start, count);
    }

    public final double[] subset(double list[], int start) {
        return subset(list, start, list.length - start);
    }

    public final double[] subset(double list[], int start, int count) {
        double output[] = new double[count];
        System.arraycopy(list, start, output, 0, count);
        return output;
    }

    public final String[] subset(String[] list, int start) {
        return PApplet.subset(list, start);
    }

    public final String[] subset(String[] list, int start, int count) {
        return PApplet.subset(list, start, count);
    }

    public final Object subset(Object list, int start) {
        return PApplet.subset(list, start);
    }

    public final Object subset(Object list, int start, int count) {
        return PApplet.subset(list, start, count);
    }

    public final boolean[] concat(boolean[] a, boolean[] b) {
        return PApplet.concat(a, b);
    }

    public final byte[] concat(byte[] a, byte[] b) {
        return PApplet.concat(a, b);
    }

    public final char[] concat(char[] a, char[] b) {
        return PApplet.concat(a, b);
    }

    public final int[] concat(int[] a, int[] b) {
        return PApplet.concat(a, b);
    }

    public final float[] concat(float[] a, float[] b) {
        return PApplet.concat(a, b);
    }

    public final double[] concat(double a[], double b[]) {
        double c[] = new double[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public final String[] concat(String[] a, String[] b) {
        return PApplet.concat(a, b);
    }

    public final Object concat(Object a, Object b) {
        return PApplet.concat(a, b);
    }

    public final boolean[] reverse(boolean[] list) {
        return PApplet.reverse(list);
    }

    public final byte[] reverse(byte[] list) {
        return PApplet.reverse(list);
    }

    public final char[] reverse(char[] list) {
        return PApplet.reverse(list);
    }

    public final int[] reverse(int[] list) {
        return PApplet.reverse(list);
    }

    public final float[] reverse(float[] list) {
        return PApplet.reverse(list);
    }

    public final double[] reverse(double[] list) {
        double[] outgoing = new double[list.length];
        int length1 = list.length - 1;
        for (int i = 0; i < list.length; i++) {
            outgoing[i] = list[length1 - i];
        }
        return outgoing;
    }

    public final String[] reverse(String[] list) {
        return PApplet.reverse(list);
    }

    public final Object reverse(Object list) {
        return PApplet.reverse(list);
    }

    public final String trim(String str) {
        return PApplet.trim(str);
    }

    public final String[] trim(String[] array) {
        return PApplet.trim(array);
    }

    public final String join(String[] list, char separator) {
        return PApplet.join(list, separator);
    }

    public final String join(String[] list, String separator) {
        return PApplet.join(list, separator);
    }

    public final String[] splitTokens(String value) {
        return PApplet.splitTokens(value);
    }

    public final String[] splitTokens(String value, String delim) {
        return PApplet.splitTokens(value, delim);
    }

    public final String[] split(String value, char delim) {
        return PApplet.split(value, delim);
    }

    public final String[] split(String value, String delim) {
        return PApplet.split(value, delim);
    }

    public final String[] match(String str, String regexp) {
        return PApplet.match(str, regexp);
    }

    public final String[][] matchAll(String str, String regexp) {
        return PApplet.matchAll(str, regexp);
    }

//    public final boolean parseBoolean(int what) {
//        return PApplet.parseBoolean(what);
//    }
//
//    public final boolean parseBoolean(String what) {
//        return PApplet.parseBoolean(what);
//    }
//
//    public final boolean[] parseBoolean(int[] what) {
//        return PApplet.parseBoolean(what);
//    }
//
//    public final boolean[] parseBoolean(String[] what) {
//        return PApplet.parseBoolean(what);
//    }
//
//    public final byte parseByte(boolean what) {
//        return PApplet.parseByte(what);
//    }
//
//    public final byte parseByte(char what) {
//        return PApplet.parseByte(what);
//    }
//
//    public final byte parseByte(int what) {
//        return PApplet.parseByte(what);
//    }
//
//    public final byte parseByte(float what) {
//        return PApplet.parseByte(what);
//    }
//
//    public final byte[] parseByte(boolean[] what) {
//        return PApplet.parseByte(what);
//    }
//
//    public final byte[] parseByte(char[] what) {
//        return PApplet.parseByte(what);
//    }
//
//    public final byte[] parseByte(int[] what) {
//        return PApplet.parseByte(what);
//    }
//
//    public final byte[] parseByte(float[] what) {
//        return PApplet.parseByte(what);
//    }
//
//    public final char parseChar(byte what) {
//        return PApplet.parseChar(what);
//    }
//
//    public final char parseChar(int what) {
//        return PApplet.parseChar(what);
//    }
//
//    public final char[] parseChar(byte[] what) {
//        return PApplet.parseChar(what);
//    }
//
//    public final char[] parseChar(int[] what) {
//        return PApplet.parseChar(what);
//    }
//
//    public final int parseInt(boolean what) {
//        return PApplet.parseInt(what);
//    }
//
//    public final int parseInt(byte what) {
//        return PApplet.parseInt(what);
//    }
//
//    public final int parseInt(char what) {
//        return PApplet.parseInt(what);
//    }
//
//    public final int parseInt(float what) {
//        return PApplet.parseInt(what);
//    }
//
//    public final int parseInt(String what) {
//        return PApplet.parseInt(what);
//    }
//
//    public final int parseInt(String what, int otherwise) {
//        return PApplet.parseInt(what, otherwise);
//    }
//
//    public final int[] parseInt(boolean[] what) {
//        return PApplet.parseInt(what);
//    }
//
//    public final int[] parseInt(byte[] what) {
//        return PApplet.parseInt(what);
//    }
//
//    public final int[] parseInt(char[] what) {
//        return PApplet.parseInt(what);
//    }
//
//    public final int[] parseInt(float[] what) {
//        return PApplet.parseInt(what);
//    }
//
//    public final int[] parseInt(String[] what) {
//        return PApplet.parseInt(what);
//    }
//
//    public final int[] parseInt(String[] what, int missing) {
//        return PApplet.parseInt(what, missing);
//    }
//
//    public final float parseFloat(int what) {
//        return PApplet.parseFloat(what);
//    }
//
//    public final float parseFloat(String what) {
//        return PApplet.parseFloat(what);
//    }
//
//    public final float parseFloat(String what, float otherwise) {
//        return PApplet.parseFloat(what, otherwise);
//    }
//
//    public final float[] parseByte(byte[] what) {
//        return PApplet.parseByte(what);
//    }
//
//    public final float[] parseFloat(int[] what) {
//        return PApplet.parseFloat(what);
//    }
//
//    public final float[] parseFloat(String[] what) {
//        return PApplet.parseFloat(what);
//    }
//
//    public final float[] parseFloat(String[] what, float missing) {
//        return PApplet.parseFloat(what, missing);
//    }
//
//    public final double[] parseDouble(int what[]) {
//        double floaties[] = new double[what.length];
//        for (int i = 0; i < what.length; i++) {
//            floaties[i] = what[i];
//        }
//        return floaties;
//    }
//
//    public final double[] parseDouble(String what[]) {
//        return parseDouble(what, Double.NaN);
//    }
//
//    public final double[] parseDouble(String what[], double missing) {
//        double[] output = new double[what.length];
//        for (int i = 0; i < what.length; i++) {
//            try {
//                output[i] = Double.parseDouble(what[i]);
//            } catch (NumberFormatException e) {
//                output[i] = missing;
//            }
//        }
//        return output;
//    }
//    public final String str(boolean x) {
//        return PApplet.str(x);
//    }
//
//    public final String str(byte x) {
//        return PApplet.str(x);
//    }
//
//    public final String str(char x) {
//        return PApplet.str(x);
//    }
//
//    public final String str(int x) {
//        return PApplet.str(x);
//    }
//
//    public final String str(float x) {
//        return PApplet.str(x);
//    }
//
//    public final String[] str(boolean[] x) {
//        return PApplet.str(x);
//    }
//
//    public final String[] str(byte[] x) {
//        return PApplet.str(x);
//    }
//
//    public final String[] str(char[] x) {
//        return PApplet.str(x);
//    }
//
//    public final String[] str(int[] x) {
//        return PApplet.str(x);
//    }
//
//    public final String[] str(float[] x) {
//        return PApplet.str(x);
//    }
//
//    public final String[] nf(int[] num, int digits) {
//        return PApplet.nf(num, digits);
//    }
//
//    public final String nf(int num, int digits) {
//        return PApplet.nf(num, digits);
//    }
//
//    public final String[] nfc(int[] num) {
//        return PApplet.nfc(num);
//    }
//
//    public final String nfc(int num) {
//        return PApplet.nfc(num);
//    }
//
//    public final String nfs(int num, int digits) {
//        return PApplet.nfs(num, digits);
//    }
//
//    public final String[] nfs(int[] num, int digits) {
//        return PApplet.nfs(num, digits);
//    }
//
//    public final String nfp(int num, int digits) {
//        return PApplet.nfp(num, digits);
//    }
//
//    public final String[] nfp(int[] num, int digits) {
//        return PApplet.nfp(num, digits);
//    }
//
//    public final String[] nf(float[] num, int left, int right) {
//        return PApplet.nf(num, left, right);
//    }
//
//    public final String nf(float num, int left, int right) {
//        return PApplet.nf(num, left, right);
//    }
//
//    public final String[] nfc(float[] num, int right) {
//        return PApplet.nfc(num, right);
//    }
//
//    public final String nfc(float num, int right) {
//        return PApplet.nfc(num, right);
//    }
//
//    public final String[] nfs(float[] num, int left, int right) {
//        return PApplet.nfs(num, left, right);
//    }
//
//    public final String nfs(float num, int left, int right) {
//        return PApplet.nfs(num, left, right);
//    }
//
//    public final String[] nfp(float[] num, int left, int right) {
//        return PApplet.nfp(num, left, right);
//    }
//
//    public final String nfp(float num, int left, int right) {
//        return PApplet.nfp(num, left, right);
//    }
    public final String hex(byte value) {
        return PApplet.hex(value);
    }

    public final String hex(char value) {
        return PApplet.hex(value);
    }

    public final String hex(int value) {
        return PApplet.hex(value);
    }

    public final String hex(int value, int digits) {
        return PApplet.hex(value, digits);
    }

    public final int unhex(String value) {
        return PApplet.unhex(value);
    }

    public final String binary(byte value) {
        return PApplet.binary(value);
    }

    public final String binary(char value) {
        return PApplet.binary(value);
    }

    public final String binary(int value) {
        return PApplet.binary(value);
    }

    public final String binary(int value, int digits) {
        return PApplet.binary(value, digits);
    }

    public final int unbinary(String value) {
        return PApplet.unbinary(value);
    }

}
