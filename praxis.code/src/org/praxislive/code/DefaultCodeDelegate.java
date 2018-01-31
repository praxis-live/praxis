/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Neil C Smith.
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
package org.praxislive.code;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import org.praxislive.code.userapi.Constants;
import org.praxislive.code.userapi.Property;
import org.praxislive.core.Component;
import org.praxislive.core.Container;
import org.praxislive.core.Control;
import org.praxislive.core.ControlPort;
import org.praxislive.core.Port;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PBoolean;
import org.praxislive.core.types.PNumber;
import org.praxislive.core.types.PString;
import org.praxislive.core.types.Value;
import org.praxislive.logging.LogLevel;
import processing.core.PApplet;

/**
 * Default base for code delegates providing a variety of functions, many derived
 * from the Processing API.
 * 
 * @author Neil C Smith
 */
public class DefaultCodeDelegate extends CodeDelegate {

    final static String[] IMPORTS = {
        "java.util.*",
        "org.praxislive.core.Argument",
        "org.praxislive.core.ArgumentFormatException",
        "org.praxislive.core.types.*",
        "org.praxislive.code.userapi.*",
        "static org.praxislive.code.userapi.Constants.*"
    };

    protected final Random RND;

    public DefaultCodeDelegate() {
        RND = new Random();
    }

    /**
     * Send a log message.
     * 
     * @param level
     * @param msg
     */
    public final void log(LogLevel level, String msg) {
        getContext().getLog().log(level, msg);
    }

    /**
     * Send a log message with associated Exception type.
     * 
     * @param level
     * @param ex
     */
    public final void log(LogLevel level, Exception ex) {
        getContext().getLog().log(level, ex);
    }

    /**
     * Send a log message with associated Exception.
     * 
     * @param level
     * @param ex
     * @param msg
     */
    public final void log(LogLevel level, Exception ex, String msg) {
        getContext().getLog().log(level, ex, msg);
    }

    /**
     * Send a log message with associated Exception type.
     * 
     * @param level
     * @param type
     * @param msg
     */
    public final void log(LogLevel level, Class<? extends Exception> type, String msg) {
        getContext().getLog().log(level, type, msg);
    }

    /**
     * Check whether the messages at the given log level are being sent.
     * 
     * @param level
     * @return
     */
    public final boolean isLoggable(LogLevel level) {
        return getContext().getLogLevel().isLoggable(level);
    }

    /**
     * Send a value to a port on another component. The other component must have
     * the same parent.
     * 
     * @param componentID ID of the other component
     * @param portID ID of the port on the other component
     * @param value
     */
    public final void transmit(String componentID, String portID, String value) {
        this.transmit(componentID, portID, PString.valueOf(value));
    }

    /**
     * Send a value to a port on another component. The other component must have
     * the same parent.
     * 
     * @param componentID ID of the other component
     * @param portID ID of the port on the other component
     * @param value
     */
    public final void transmit(String componentID, String portID, Value value) {
        ControlPort.Input port = findPort(componentID, portID);
        if (port == null) {
            log(LogLevel.ERROR, "Can't find an input port at " + componentID + "!" + portID);
        } else {
            try {
                port.receive(time(), value);
            } catch (Exception ex) {
                log(LogLevel.ERROR, ex);
            }
        }
    }

    /**
     * Send a value to a port on another component. The other component must have
     * the same parent.
     * 
     * @param componentID ID of the other component
     * @param portID ID of the port on the other component
     * @param value
     */
    public final void transmit(String componentID, String portID, double value) {
        ControlPort.Input port = findPort(componentID, portID);
        if (port == null) {
            log(LogLevel.ERROR, "Can't find an input port at " + componentID + "!" + portID);
        } else {
            try {
                port.receive(time(), value);
            } catch (Exception ex) {
                log(LogLevel.ERROR, ex);
            }
        }
    }

    private ControlPort.Input findPort(String cmp, String port) {
        Component thisCmp = getContext().getComponent();
        Container parent = thisCmp.getParent();
        if (parent == null) {
            return null;
        }
        Component thatCmp = parent.getChild(cmp);
        if (thatCmp == null) {
            return null;
        }
        Port thatPort = thatCmp.getPort(port);
        if (thatPort instanceof ControlPort.Input) {
            return (ControlPort.Input) thatPort;
        } else {
            return null;
        }
    }
    
    /**
     * Search for an instance of the given type.
     * @param <T>
     * @param type class to search for
     * @return Optional wrapping the result if found, or empty if not
     */
    public <T> Optional<T> find(Class<T> type) {
        return Optional.ofNullable(getContext().getLookup().get(type));
    }

    /**
     * The current clocktime in nanoseconds. May only be used relatively to itself, 
     * and may be negative.
     * 
     * @return
     */
    public final long time() {
        return getContext().getTime();
    }

    /**
     * The current time in milliseconds since the root was started.
     * 
     * @return
     */
    public final long millis() {
        if (getContext().getExecutionContext().supportsStartTime()) {
            return (time() - getContext().getExecutionContext().getStartTime())
                    / 1_000_000;
        } else {
            return time() / 1_000_000;
        }
    }

    /**
     * Extract a double from the Property's current Value, or zero if the value
     * cannot be coerced.
     * 
     * @param p
     * @return
     */
    public final double d(Property p) {
        return p.getDouble();
    }

    /**
     * Convert the provided Value into a double, or zero if the Value cannot be
     * coerced.
     * 
     * @param v
     * @return
     */
    public final double d(Value v) {
        if (v instanceof PNumber) {
            return ((PNumber) v).value();
        } else {
            return PNumber.from(v).orElse(PNumber.ZERO).value();
        }
    }

    /**
     * Parse the provided String into a double, or zero if invalid.
     * 
     * @param s
     * @return
     */
    public final double d(String s) {
        return d(PString.valueOf(s));
    }

    /**
     *
     * @param p
     * @return
     * @deprecated
     */
    @Deprecated
    public final float f(Property p) {
        return (float) p.getDouble();
    }

    /**
     * Extract an int from the Property's current Value, or zero if the value
     * cannot be coerced.
     * 
     * @param p
     * @return
     */
    public final int i(Property p) {
        return p.getInt();
    }

    /**
     * Convert the provided Value into an int, or zero if the Value cannot be
     * coerced.
     * 
     * @param v
     * @return
     */
    public final int i(Value v) {
        if (v instanceof PNumber) {
            return ((PNumber) v).toIntValue();
        } else {
            return PNumber.from(v).orElse(PNumber.ZERO).toIntValue();
        }
    }

    /**
     * Parse the provided String into an int, or zero if invalid.
     * 
     * @param s
     * @return
     */
    public final int i(String s) {
        return i(PString.valueOf(s));
    }

    /**
     * Extract the Property's current value as a boolean. If the value cannot be
     * coerced, returns false.
     * 
     * @param p
     * @return
     */
    public final boolean b(Property p) {
        return p.getBoolean();
    }

    /**
     * Convert the provided Value into a boolean according to the parsing rules of
     * {@link PBoolean}. If the Value cannot be coerced, returns false.
     * 
     * @param v
     * @return
     */
    public final boolean b(Value v) {
        if (v instanceof PBoolean) {
            return ((PBoolean) v).value();
        } else {
            return PBoolean.from(v).orElse(PBoolean.FALSE).value();
        }
    }

    /**
     * Parse the given String into a boolean according to the parsing rules of
     * {@link PBoolean}. If the String is invalid, returns false.
     * 
     * @param s
     * @return
     */
    public final boolean b(String s) {
        return b(PString.valueOf(s));
    }

    /**
     * Extract the Property's current value into a String representation.
     * 
     * @param p
     * @return
     */
    public final String s(Property p) {
        return p.get().toString();
    }

    /**
     * Convert the provided Value into a String representation.
     * 
     * @param v
     * @return
     */
    public final String s(Value v) {
        return v.toString();
    }

    /**
     * Attempt to extract a {@link PArray} from the given Property. An empty PArray
     * will be returned if the property's value is not a PArray and cannot be coerced.
     * 
     * @see #array(org.praxislive.core.types.Value) 
     * 
     * @param p
     * @return
     */
    public final PArray array(Property p) {
        return PArray.from(p.get()).orElse(PArray.EMPTY);
    }

    /**
     * Convert the given Value into a {@link PArray}. If the Value is already a
     * PArray it will be returned, otherwise an attempt will be made to coerce it.
     * If the Value cannot be converted, an empty PArray will be returned.
     * 
     * @param s
     * @return
     */
    public final PArray array(Value v) {
        return PArray.from(v).orElse(PArray.EMPTY);
    }

    /**
     * Parse the given String into a {@link PArray}. If the String is not a valid
     * representation of an array, returns an empty PArray.
     * 
     * @param s
     * @return
     */
    public final PArray array(String s) {
        return array(PString.valueOf(s));
    }

    /**
     * Get a Property from a given ID.
     * 
     * @param id
     * @return Property, or null if the given ID is not valid
     */
    public final Property p(String id) {
        Control c = getContext().getControl(id);
        if (c instanceof Property) {
            return (Property) c;
        } else {
            return null;
        }
    }

    /**
     * Return a random number between zero and max (exclusive)
     * 
     * @param max the upper bound of the range
     * @return
     */
    public final double random(double max) {
        return RND.nextDouble() * max;
    }

    /**
     * Return a random number between min (inclusive) and max (exclusive)
     * 
     * @param min the lower bound of the range
     * @param max the upper bound of the range
     * @return
     */
    public final double random(double min, double max) {
        if (min >= max) {
            return min;
        }
        return random(max - min) + min;
    }

    /**
     * Return a random element from an array of values.
     *
     * @param values list of values, may not be empty
     * @return random element
     */
    public final double randomOf(double... values) {
        return values[RND.nextInt(values.length)];
    }

    /**
     * Return a random element from an array of values.
     *
     * @param values list of values, may not be empty
     * @return random element
     */
    public final int randomOf(int... values) {
        return values[RND.nextInt(values.length)];
    }

    /**
     * Return a random element from an array of values.
     *
     * @param values list of values, may not be empty
     * @return random element
     */
    public final String randomOf(String... values) {
        return values[RND.nextInt(values.length)];
    }

    /**
     * Calculate the absolute value of the given value. If the value is
     * positive, the value is returned. If the value is negative, the negation
     * of the value is returned.
     *
     * @param n
     * @return
     */
    public final double abs(double n) {
        return (n < 0) ? -n : n;
    }

    /**
     * Calculate the square of the given value.
     *
     * @param a
     * @return
     */
    public final double sq(double a) {
        return a * a;
    }

    /**
     * Calculate the square root of the given value.
     *
     * @see Math#sqrt(double)
     *
     * @param a
     * @return
     */
    public final double sqrt(double a) {
        return Math.sqrt(a);
    }

    /**
     * Calculate the natural logarithm if the given value.
     *
     * @see Math#log(double)
     *
     * @param a
     * @return
     */
    public final double log(double a) {
        return Math.log(a);
    }

    /**
     * Calculate Euler's number raised to the power of the given value.
     *
     * @see Math#exp(double)
     *
     * @param a
     * @return
     */
    public final double exp(double a) {
        return Math.exp(a);
    }

    /**
     * Calculate the value of the first argument raised to the power of the
     * second argument.
     *
     * @see Math#pow(double, double)
     *
     * @param a the base
     * @param b the exponent
     * @return the value a<sup>b</sup>
     */
    public final double pow(double a, double b) {
        return Math.pow(a, b);
    }

    /**
     * Calculate the maximum of two values.
     *
     * @param a
     * @param b
     * @return
     */
    public final int max(int a, int b) {
        return PApplet.max(a, b);
    }

    /**
     * Calculate the maximum of three values.
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    public final int max(int a, int b, int c) {
        return PApplet.max(a, b, c);
    }

    /**
     * Calculate the maximum value in the provided array.
     *
     * @param list value list - must not be empty
     * @return maximum value
     */
    public final int max(int[] values) {
        return PApplet.max(values);
    }

    /**
     * Calculate the maximum of two values.
     *
     * @param a
     * @param b
     * @return
     */
    public final double max(double a, double b) {
        return (a > b) ? a : b;
    }

    /**
     * Calculate the maximum of three values.
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    public final double max(double a, double b, double c) {
        return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
    }

    /**
     * Calculate the maximum value in the provided array.
     *
     * @param list value list - must not be empty
     * @return maximum value
     */
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

    /**
     * Calculate the minimum of two values.
     *
     * @param a
     * @param b
     * @return
     */
    public final int min(int a, int b) {
        return PApplet.min(a, b);
    }

    /**
     * Calculate the minimum of three values.
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    public final int min(int a, int b, int c) {
        return PApplet.min(a, b, c);
    }

    /**
     * Calculate the minimum value in the provided array.
     *
     * @param list value list - must not be empty
     * @return minimum value
     */
    public final int min(int[] values) {
        return PApplet.min(values);
    }

    /**
     * Calculate the minimum of two values.
     *
     * @param a
     * @param b
     * @return
     */
    public final double min(double a, double b) {
        return (a < b) ? a : b;
    }

    /**
     * Calculate the minimum of three values.
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    public final double min(double a, double b, double c) {
        return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
    }

    /**
     * Calculate the minimum value in the provided array.
     *
     * @param list value list - must not be empty
     * @return minimum value
     */
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

    /**
     * Constrain a value between the range of the given low and high values.
     *
     * @param amt input value
     * @param low lowest allowed value
     * @param high highest allowed value
     * @return constrained value
     */
    public final int constrain(int amt, int low, int high) {
        return (amt < low) ? low : ((amt > high) ? high : amt);
    }

    /**
     * Constrain a value between the range of the given low and high values.
     *
     * @param amt input value
     * @param low lowest allowed value
     * @param high highest allowed value
     * @return constrained value
     */
    public final double constrain(double amt, double low, double high) {
        return (amt < low) ? low : ((amt > high) ? high : amt);
    }

    /**
     * Converts an angle in radians to an angle in degrees.
     *
     * @see Math#toDegrees(double)
     *
     * @param radians
     * @return
     */
    public final double degrees(double radians) {
        return Math.toDegrees(radians);
    }

    /**
     * Converts an angle in degrees to an angle in radians.
     *
     * @see Math#toRadians(double)
     *
     * @param degrees
     * @return
     */
    public final double radians(double degrees) {
        return Math.toRadians(degrees);
    }

    /**
     * Returns the trigonometric sine of an angle
     *
     * @see Math#sin(double)
     *
     * @param angle
     * @return
     */
    public final double sin(double angle) {
        return Math.sin(angle);
    }

    /**
     * Returns the trigonometric cosine of an angle.
     *
     * @see Math#cos(double)
     *
     * @param angle
     * @return
     */
    public final double cos(double angle) {
        return Math.cos(angle);
    }

    /**
     * Returns the trigonometric tangent of an angle.
     *
     * @see Math#tan(double)
     *
     * @param angle
     * @return
     */
    public final double tan(double angle) {
        return Math.tan(angle);
    }

    /**
     * Returns the arc sine of a value.
     *
     * @see Math#asin(double)
     *
     * @param value
     * @return
     */
    public final double asin(double value) {
        return Math.asin(value);
    }

    /**
     * Returns the arc cosine of a value.
     *
     * @see Math#acos(double)
     *
     * @param value
     * @return
     */
    public final double acos(double value) {
        return Math.acos(value);
    }

    /**
     * Returns the arc tangent of a value.
     *
     * @see Math#atan(double)
     *
     * @param value
     * @return
     */
    public final double atan(double value) {
        return Math.atan(value);
    }

    /**
     * Returns the angle theta from the conversion of rectangular coordinates
     * (x, y) to polar coordinates (r, theta).
     *
     * @see Math#atan2(double, double)
     *
     * @param y
     * @param x
     * @return
     */
    public final double atan2(double y, double x) {
        return Math.atan2(y, x);
    }

    /**
     * Re-map (scale) an input value from one range to another. Numbers outside
     * the range are not clamped.
     *
     * @param value the value to be converted
     * @param start1 lower bound of the value's current range
     * @param stop1 upper bound of the value's current range
     * @param start2 lower bound of the value's target range
     * @param stop2 upper bound of the value's target range
     * @return
     */
    public final double map(double value,
            double start1, double stop1,
            double start2, double stop2) {
        return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    }

    /**
     * Calculates the distance between two points.
     *
     * @return
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
     * @return
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
     * @return
     * @param start first value
     * @param stop second value
     * @param amt between 0.0 and 1.0
     */
    public final double lerp(double start, double stop, double amt) {
        return start + (stop - start) * amt;
    }

    /**
     * Normalizes a number from another range into a value between 0 and 1.
     * <p>
     * Identical to map(value, low, high, 0, 1);
     * <p>
     * Numbers outside the range are not clamped to 0 and 1, because
     * out-of-range values are often intentional and useful.
     *
     * @return
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
     *
     * @param x
     * @return
     */
    public final double noise(double x) {
        return noise(x, 0f, 0f);
    }

    /**
     * Computes the Perlin noise function value at the point x, y.
     *
     * @param x
     * @param y
     * @return
     */
    public final double noise(double x, double y) {
        return noise(x, y, 0f);
    }

    /**
     * Computes the Perlin noise function value at x, y, z.
     *
     * @param x
     * @param z
     * @param y
     * @return
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
    /**
     *
     * @param lod
     */
    public final void noiseDetail(int lod) {
        if (lod > 0) {
            perlin_octaves = lod;
        }
    }

    /**
     *
     * @param lod
     * @param falloff
     */
    public final void noiseDetail(int lod, double falloff) {
        if (lod > 0) {
            perlin_octaves = lod;
        }
        if (falloff > 0) {
            perlin_amp_falloff = (float) falloff;
        }
    }

    /**
     *
     * @param what
     */
    public final void noiseSeed(long what) {
        if (perlinRandom == null) {
            perlinRandom = new Random();
        }
        perlinRandom.setSeed(what);
        perlin = null;
    }

    // end of Perlin noise functions
    // start of PApplet statics
    
    /**
     * The minute of the current time as a value from 0 - 59.
     * 
     * @return
     */
    public final int minute() {
        return PApplet.minute();
    }

    /**
     * The hour of the current time as a value from 0 - 23.
     * 
     * @return
     */
    public final int hour() {
        return PApplet.hour();
    }

    /**
     * The current day of the month as a value from 1 - 31.
     * 
     * @return
     */
    public final int day() {
        return PApplet.day();
    }

    /**
     * The current month as a value from 1- 12.
     * 
     * @return
     */
    public final int month() {
        return PApplet.month();
    }

    /**
     * The current year.
     * 
     * @return
     */
    public final int year() {
        return PApplet.year();
    }

    /**
     * Returns a sorted copy of the provided array.
     *
     * @param list
     * @return
     */
    public final byte[] sort(byte[] list) {
        return PApplet.sort(list);
    }

    /**
     * Returns a sorted array containing the first count elements of the provided
     * array.
     *
     * @param list
     * @param count
     * @return
     */
    public final byte[] sort(byte[] list, int count) {
        return PApplet.sort(list, count);
    }

    /**
     * Returns a sorted copy of the provided array.
     *
     * @param list
     * @return
     */
    public final char[] sort(char[] list) {
        return PApplet.sort(list);
    }

    /**
     * Returns a sorted array containing the first count elements of the provided
     * array.
     *
     * @param list
     * @param count
     * @return
     */
    public final char[] sort(char[] list, int count) {
        return PApplet.sort(list, count);
    }

    /**
     * Returns a sorted copy of the provided array.
     *
     * @param list
     * @return
     */
    public final int[] sort(int[] list) {
        return PApplet.sort(list);
    }

    /**
     * Returns a sorted array containing the first count elements of the provided
     * array.
     *
     * @param list
     * @param count
     * @return
     */
    public final int[] sort(int[] list, int count) {
        return PApplet.sort(list, count);
    }

    /**
     * Returns a sorted copy of the provided array.
     *
     * @param list
     * @return
     */
    public final float[] sort(float[] list) {
        return PApplet.sort(list);
    }

    /**
     * Returns a sorted array containing the first count elements of the provided
     * array.
     *
     * @param list
     * @param count
     * @return
     */
    public final float[] sort(float[] list, int count) {
        return PApplet.sort(list, count);
    }

    /**
     * Returns a sorted copy of the provided array.
     *
     * @param list
     * @return
     */
    public final double[] sort(double[] list) {
        return sort(list, list.length);
    }

    /**
     * Returns a sorted array containing the first count elements of the provided
     * array.
     *
     * @param list
     * @param count
     * @return
     */
    public final double[] sort(double[] list, int count) {
        double[] outgoing = new double[list.length];
        System.arraycopy(list, 0, outgoing, 0, list.length);
        Arrays.sort(outgoing, 0, count);
        return outgoing;
    }

    /**
     * Returns a sorted copy of the provided array.
     *
     * @param list
     * @return
     */
    public final String[] sort(String[] list) {
        return PApplet.sort(list);
    }

    /**
     * Returns a sorted array containing the first count elements of the provided
     * array.
     *
     * @param list
     * @param count
     * @return
     */
    public final String[] sort(String[] list, int count) {
        return PApplet.sort(list, count);
    }

    /**
     * Copies an array (or part of an array) to another array. The src array is
     * copied to the dst array, beginning at the position specified by srcPos 
     * and into the position specified by dstPos. The number of elements to copy
     * is determined by length.
     * 
     * @param src
     * @param srcPosition
     * @param dst
     * @param dstPosition
     * @param length
     */
    public final void arrayCopy(Object src, int srcPosition, Object dst, int dstPosition, int length) {
        PApplet.arrayCopy(src, srcPosition, dst, dstPosition, length);
    }

    /**
     * Copies an array (or part of an array) to another array. The src array is
     * copied to the dst array. The number of elements to copy
     * is determined by length.
     * 
     * @param src
     * @param dst
     * @param length
     */
    public final void arrayCopy(Object src, Object dst, int length) {
        PApplet.arrayCopy(src, dst, length);
    }

    /**
     * Copies an array to another array. The src array is copied to the dst array.
     * 
     * @param src
     * @param dst
     */
    public final void arrayCopy(Object src, Object dst) {
        PApplet.arrayCopy(src, dst);
    }

    /**
     * Returns a new array of double the length containing the elements of list.
     * 
     * @param list
     * @return
     */
    public final boolean[] expand(boolean[] list) {
        return PApplet.expand(list);
    }

    /**
     * Returns an array of newSize length containing the elements of list.
     * 
     * @param list
     * @param newSize
     * @return
     */
    public final boolean[] expand(boolean[] list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    /**
     * Returns a new array of double the length containing the elements of list.
     * 
     * @param list
     * @return
     */
    public final byte[] expand(byte[] list) {
        return PApplet.expand(list);
    }

    /**
     * Returns an array of newSize length containing the elements of list.
     * 
     * @param list
     * @param newSize
     * @return
     */
    public final byte[] expand(byte[] list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    /**
     * Returns a new array of double the length containing the elements of list.
     * 
     * @param list
     * @return
     */
    public final char[] expand(char[] list) {
        return PApplet.expand(list);
    }

    /**
     * Returns an array of newSize length containing the elements of list.
     * 
     * @param list
     * @param newSize
     * @return
     */
    public final char[] expand(char[] list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    /**
     * Returns a new array of double the length containing the elements of list.
     * 
     * @param list
     * @return
     */
    public final int[] expand(int[] list) {
        return PApplet.expand(list);
    }

    /**
     * Returns an array of newSize length containing the elements of list.
     * 
     * @param list
     * @param newSize
     * @return
     */
    public final int[] expand(int[] list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    /**
     * Returns a new array of double the length containing the elements of list.
     * 
     * @param list
     * @return
     */
    public final long[] expand(long[] list) {
        return PApplet.expand(list);
    }

    /**
     * Returns an array of newSize length containing the elements of list.
     * 
     * @param list
     * @param newSize
     * @return
     */
    public final long[] expand(long[] list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    /**
     * Returns a new array of double the length containing the elements of list.
     * 
     * @param list
     * @return
     */
    public final float[] expand(float[] list) {
        return PApplet.expand(list);
    }

    /**
     * Returns an array of newSize length containing the elements of list.
     * 
     * @param list
     * @param newSize
     * @return
     */
    public final float[] expand(float[] list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    /**
     * Returns a new array of double the length containing the elements of list.
     * 
     * @param list
     * @return
     */
    public final double[] expand(double[] list) {
        return PApplet.expand(list);
    }

    /**
     * Returns an array of newSize length containing the elements of list.
     * 
     * @param list
     * @param newSize
     * @return
     */
    public final double[] expand(double[] list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    /**
     * Returns a new array of double the length containing the elements of list.
     * 
     * @param list
     * @return
     */
    public final String[] expand(String[] list) {
        return PApplet.expand(list);
    }

    /**
     * Returns an array of newSize length containing the elements of list.
     * 
     * @param list
     * @param newSize
     * @return
     */
    public final String[] expand(String[] list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    /**
     * Returns a new array of double the length containing the elements of list.
     * 
     * @param array
     * @return
     */
    public final Object expand(Object array) {
        return PApplet.expand(array);
    }

    /**
     * Returns an array of newSize length containing the elements of list.
     * 
     * @param list
     * @param newSize
     * @return
     */
    public final Object expand(Object list, int newSize) {
        return PApplet.expand(list, newSize);
    }

    /**
     * Returns a new array of length + 1 with the provided value at the last position.
     * 
     * @param array
     * @param value
     * @return
     */
    public final byte[] append(byte[] array, byte value) {
        return PApplet.append(array, value);
    }

    /**
     * Returns a new array of length + 1 with the provided value at the last position.
     * 
     * @param array
     * @param value
     * @return
     */
    public final char[] append(char[] array, char value) {
        return PApplet.append(array, value);
    }

    /**
     * Returns a new array of length + 1 with the provided value at the last position.
     * 
     * @param array
     * @param value
     * @return
     */
    public final int[] append(int[] array, int value) {
        return PApplet.append(array, value);
    }

    /**
     * Returns a new array of length + 1 with the provided value at the last position.
     * 
     * @param array
     * @param value
     * @return
     */
    public final float[] append(float[] array, float value) {
        return PApplet.append(array, value);
    }

    /**
     * Returns a new array of length + 1 with the provided value at the last position.
     * 
     * @param array
     * @param value
     * @return
     */
    public final double[] append(double array[], double value) {
        array = expand(array, array.length + 1);
        array[array.length - 1] = value;
        return array;
    }

    /**
     * Returns a new array of length + 1 with the provided value at the last position.
     * 
     * @param array
     * @param value
     * @return
     */
    public final String[] append(String[] array, String value) {
        return PApplet.append(array, value);
    }

    /**
     * Returns a new array of length + 1 with the provided value at the last position.
     * 
     * @param array
     * @param value
     * @return
     */
    public final PArray append(PArray array, Value value) {
        return PArray.append(array, value);
    }

    /**
     * Returns a new array of length + 1 with the provided value at the last position.
     * 
     * @param array
     * @param value
     * @return
     */
    public final PArray append(PArray array, String value) {
        return PArray.append(array, PString.valueOf(value));
    }

    /**
     * Returns a new array of length + 1 with the provided value at the last position.
     * 
     * @param array
     * @param value
     * @return
     */
    public final PArray append(PArray array, double value) {
        return PArray.append(array, PNumber.valueOf(value));
    }

    /**
     * Returns a new array of length + 1 with the provided value at the last position.
     * 
     * @param array
     * @param value
     * @return
     */
    public final Object append(Object array, Object value) {
        return PApplet.append(array, value);
    }

    /**
     * Returns a new array with the elements of list except for the last element.
     * 
     * @param list
     * @return
     */
    public final boolean[] shorten(boolean[] list) {
        return PApplet.shorten(list);
    }

    /**
     * Returns a new array with the elements of list except for the last element.
     * 
     * @param list
     * @return
     */
    public final byte[] shorten(byte[] list) {
        return PApplet.shorten(list);
    }

    /**
     * Returns a new array with the elements of list except for the last element.
     * 
     * @param list
     * @return
     */
    public final char[] shorten(char[] list) {
        return PApplet.shorten(list);
    }

    /**
     * Returns a new array with the elements of list except for the last element.
     * 
     * @param list
     * @return
     */
    public final int[] shorten(int[] list) {
        return PApplet.shorten(list);
    }

    /**
     * Returns a new array with the elements of list except for the last element.
     * 
     * @param list
     * @return
     */
    public final float[] shorten(float[] list) {
        return PApplet.shorten(list);
    }

    /**
     * Returns a new array with the elements of list except for the last element.
     * 
     * @param list
     * @return
     */
    public final double[] shorten(double list[]) {
        return subset(list, 0, list.length - 1);
    }

    /**
     * Returns a new array with the elements of list except for the last element.
     * 
     * @param list
     * @return
     */
    public final String[] shorten(String[] list) {
        return PApplet.shorten(list);
    }

    /**
     * Returns a new array with the elements of list except for the last element.
     * 
     * @param list
     * @return
     */
    public final Object shorten(Object list) {
        return PApplet.shorten(list);
    }

    /**
     * Returns a new array containing list, with the value inserted at index.
     * 
     * @param list
     * @param value
     * @param index
     * @return
     */
    public final boolean[] splice(boolean[] list, boolean value, int index) {
        return PApplet.splice(list, value, index);
    }

    /**
     * Returns a new array containing list, with the values inserted at index.
     * 
     * @param list
     * @param value
     * @param index
     * @return
     */
    public final boolean[] splice(boolean[] list, boolean[] value, int index) {
        return PApplet.splice(list, value, index);
    }

    /**
     * Returns a new array containing list, with the value inserted at index.
     * 
     * @param list
     * @param value
     * @param index
     * @return
     */
    public final byte[] splice(byte[] list, byte value, int index) {
        return PApplet.splice(list, value, index);
    }

    /**
     * Returns a new array containing list, with the values inserted at index.
     * 
     * @param list
     * @param value
     * @param index
     * @return
     */
    public final byte[] splice(byte[] list, byte[] value, int index) {
        return PApplet.splice(list, value, index);
    }

    /**
     * Returns a new array containing list, with the value inserted at index.
     * 
     * @param list
     * @param value
     * @param index
     * @return
     */
    public final char[] splice(char[] list, char value, int index) {
        return PApplet.splice(list, value, index);
    }

    /**
     * Returns a new array containing list, with the values inserted at index.
     * 
     * @param list
     * @param value
     * @param index
     * @return
     */
    public final char[] splice(char[] list, char[] value, int index) {
        return PApplet.splice(list, value, index);
    }

    /**
     * Returns a new array containing list, with the value inserted at index.
     * 
     * @param list
     * @param value
     * @param index
     * @return
     */
    public final int[] splice(int[] list, int value, int index) {
        return PApplet.splice(list, value, index);
    }

    /**
     * Returns a new array containing list, with the values inserted at index.
     * 
     * @param list
     * @param value
     * @param index
     * @return
     */
    public final int[] splice(int[] list, int[] value, int index) {
        return PApplet.splice(list, value, index);
    }

    /**
     * Returns a new array containing list, with the value inserted at index.
     * 
     * @param list
     * @param value
     * @param index
     * @return
     */
    public final float[] splice(float[] list, float value, int index) {
        return PApplet.splice(list, value, index);
    }

    /**
     * Returns a new array containing list, with the values inserted at index.
     * 
     * @param list
     * @param value
     * @param index
     * @return
     */
    public final float[] splice(float[] list, float[] value, int index) {
        return PApplet.splice(list, value, index);
    }

    /**
     * Returns a new array containing list, with the value inserted at index.
     * 
     * @param list
     * @param value
     * @param index
     * @return
     */
    public final double[] splice(double list[],
            double value, int index) {
        double[] outgoing = new double[list.length + 1];
        System.arraycopy(list, 0, outgoing, 0, index);
        outgoing[index] = value;
        System.arraycopy(list, index, outgoing, index + 1,
                list.length - index);
        return outgoing;
    }

    /**
     * Returns a new array containing list, with the values inserted at index.
     * 
     * @param list
     * @param value
     * @param index
     * @return
     */
    public final double[] splice(double list[],
            double value[], int index) {
        double[] outgoing = new double[list.length + value.length];
        System.arraycopy(list, 0, outgoing, 0, index);
        System.arraycopy(value, 0, outgoing, index, value.length);
        System.arraycopy(list, index, outgoing, index + value.length,
                list.length - index);
        return outgoing;
    }

    /**
     * Returns a new array containing list, with the value inserted at index.
     * 
     * @param list
     * @param value
     * @param index
     * @return
     */
    public final String[] splice(String[] list, String value, int index) {
        return PApplet.splice(list, value, index);
    }

    /**
     * Returns a new array containing list, with the values inserted at index.
     * 
     * @param list
     * @param value
     * @param index
     * @return
     */
    public final String[] splice(String[] list, String[] value, int index) {
        return PApplet.splice(list, value, index);
    }

    /**
     * Returns a new array containing list, with the value inserted at index.
     * 
     * @param array
     * @param value
     * @param index
     * @return
     */
    public final PArray splice(PArray array, Value value, int index) {
        return PArray.insert(array, index, value);
    }

    /**
     * Returns a new array containing list, with the value inserted at index.
     * 
     * @param array
     * @param value
     * @param index
     * @return
     */
    public final PArray splice(PArray array, String value, int index) {
        return PArray.insert(array, index, PString.valueOf(value));
    }

    /**
     * Returns a new array containing list, with the value inserted at index.
     * 
     * @param array
     * @param value
     * @param index
     * @return
     */
    public final PArray splice(PArray array, double value, int index) {
        return PArray.insert(array, index, PNumber.valueOf(value));
    }

    /**
     * Returns a new array containing list, with the value inserted at index.
     * 
     * @param list
     * @param value
     * @param index
     * @return
     */
    public final Object splice(Object list, Object value, int index) {
        return PApplet.splice(list, value, index);
    }

    /**
     * Return a new array containing a subset of list starting from start.
     * 
     * @param list
     * @param start
     * @return
     */
    public final boolean[] subset(boolean[] list, int start) {
        return PApplet.subset(list, start);
    }

    /**
     * Return a new array containing a subset of list containing count elements 
     * starting from start.
     * 
     * @param list
     * @param start
     * @param count
     * @return
     */
    public final boolean[] subset(boolean[] list, int start, int count) {
        return PApplet.subset(list, start, count);
    }

    /**
     * Return a new array containing a subset of list starting from start.
     * 
     * @param list
     * @param start
     * @return
     */
    public final byte[] subset(byte[] list, int start) {
        return PApplet.subset(list, start);
    }

    /**
     * Return a new array containing a subset of list containing count elements 
     * starting from start.
     * 
     * @param list
     * @param start
     * @param count
     * @return
     */
    public final byte[] subset(byte[] list, int start, int count) {
        return PApplet.subset(list, start, count);
    }

    /**
     * Return a new array containing a subset of list starting from start.
     * 
     * @param list
     * @param start
     * @return
     */
    public final char[] subset(char[] list, int start) {
        return PApplet.subset(list, start);
    }

    /**
     * Return a new array containing a subset of list containing count elements 
     * starting from start.
     * 
     * @param list
     * @param start
     * @param count
     * @return
     */
    public final char[] subset(char[] list, int start, int count) {
        return PApplet.subset(list, start, count);
    }

    /**
     * Return a new array containing a subset of list starting from start.
     * 
     * @param list
     * @param start
     * @return
     */
    public final int[] subset(int[] list, int start) {
        return PApplet.subset(list, start);
    }

    /**
     * Return a new array containing a subset of list containing count elements 
     * starting from start.
     * 
     * @param list
     * @param start
     * @param count
     * @return
     */
    public final int[] subset(int[] list, int start, int count) {
        return PApplet.subset(list, start, count);
    }

    /**
     * Return a new array containing a subset of list starting from start.
     * 
     * @param list
     * @param start
     * @return
     */
    public final float[] subset(float[] list, int start) {
        return PApplet.subset(list, start);
    }

    /**
     * Return a new array containing a subset of list containing count elements 
     * starting from start.
     * 
     * @param list
     * @param start
     * @param count
     * @return
     */
    public final float[] subset(float[] list, int start, int count) {
        return PApplet.subset(list, start, count);
    }

    /**
     * Return a new array containing a subset of list starting from start.
     * 
     * @param list
     * @param start
     * @return
     */
    public final double[] subset(double list[], int start) {
        return subset(list, start, list.length - start);
    }

    /**
     * Return a new array containing a subset of list containing count elements 
     * starting from start.
     * 
     * @param list
     * @param start
     * @param count
     * @return
     */
    public final double[] subset(double list[], int start, int count) {
        double output[] = new double[count];
        System.arraycopy(list, start, output, 0, count);
        return output;
    }

    /**
     * Return a new array containing a subset of list starting from start.
     * 
     * @param list
     * @param start
     * @return
     */
    public final String[] subset(String[] list, int start) {
        return PApplet.subset(list, start);
    }

    /**
     * Return a new array containing a subset of list containing count elements 
     * starting from start.
     * 
     * @param list
     * @param start
     * @param count
     * @return
     */
    public final String[] subset(String[] list, int start, int count) {
        return PApplet.subset(list, start, count);
    }

    /**
     * Return a new array containing a subset of list starting from start.
     * 
     * @param list
     * @param start
     * @return
     */
    public final PArray subset(PArray array, int start) {
        return subset(array, start, array.getSize() - start);
    }

    /**
     * Return a new array containing a subset of list containing count elements 
     * starting from start.
     * 
     * @param list
     * @param start
     * @param count
     * @return
     */
    public final PArray subset(PArray array, int start, int count) {
        return PArray.subset(array, start, count);
    }

    /**
     * Return a new array containing a subset of list starting from start.
     * 
     * @param list
     * @param start
     * @return
     */
    public final Object subset(Object list, int start) {
        return PApplet.subset(list, start);
    }

    /**
     * Return a new array containing a subset of list containing count elements 
     * starting from start.
     * 
     * @param list
     * @param start
     * @param count
     * @return
     */
    public final Object subset(Object list, int start, int count) {
        return PApplet.subset(list, start, count);
    }

    /**
     * Return a new array containing elements from both supplied arrays.
     * 
     * @param a
     * @param b
     * @return
     */
    public final boolean[] concat(boolean[] a, boolean[] b) {
        return PApplet.concat(a, b);
    }

    /**
     * Return a new array containing elements from both supplied arrays.
     * 
     * @param a
     * @param b
     * @return
     */
    public final byte[] concat(byte[] a, byte[] b) {
        return PApplet.concat(a, b);
    }

    /**
     * Return a new array containing elements from both supplied arrays.
     * 
     * @param a
     * @param b
     * @return
     */
    public final char[] concat(char[] a, char[] b) {
        return PApplet.concat(a, b);
    }

    /**
     * Return a new array containing elements from both supplied arrays.
     * 
     * @param a
     * @param b
     * @return
     */
    public final int[] concat(int[] a, int[] b) {
        return PApplet.concat(a, b);
    }

    /**
     * Return a new array containing elements from both supplied arrays.
     * 
     * @param a
     * @param b
     * @return
     */
    public final float[] concat(float[] a, float[] b) {
        return PApplet.concat(a, b);
    }

    /**
     * Return a new array containing elements from both supplied arrays.
     * 
     * @param a
     * @param b
     * @return
     */
    public final double[] concat(double a[], double b[]) {
        double c[] = new double[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    /**
     * Return a new array containing elements from both supplied arrays.
     * 
     * @param a
     * @param b
     * @return
     */
    public final String[] concat(String[] a, String[] b) {
        return PApplet.concat(a, b);
    }

    /**
     * Return a new array containing elements from both supplied arrays.
     * 
     * @param a
     * @param b
     * @return
     */
    public final PArray concat(PArray a, PArray b) {
        return PArray.concat(a, b);
    }

    /**
     * Return a new array containing elements from both supplied arrays.
     * 
     * @param a
     * @param b
     * @return
     */
    public final Object concat(Object a, Object b) {
        return PApplet.concat(a, b);
    }

    /**
     * Return a new array containing the elements of the provided list in reverse.
     * 
     * @param list
     * @return
     */
    public final boolean[] reverse(boolean[] list) {
        return PApplet.reverse(list);
    }

    /**
     * Return a new array containing the elements of the provided list in reverse.
     * 
     * @param list
     * @return
     */
    public final byte[] reverse(byte[] list) {
        return PApplet.reverse(list);
    }

    /**
     * Return a new array containing the elements of the provided list in reverse.
     * 
     * @param list
     * @return
     */
    public final char[] reverse(char[] list) {
        return PApplet.reverse(list);
    }

    /**
     * Return a new array containing the elements of the provided list in reverse.
     * 
     * @param list
     * @return
     */
    public final int[] reverse(int[] list) {
        return PApplet.reverse(list);
    }

    /**
     * Return a new array containing the elements of the provided list in reverse.
     * 
     * @param list
     * @return
     */
    public final float[] reverse(float[] list) {
        return PApplet.reverse(list);
    }

    /**
     * Return a new array containing the elements of the provided list in reverse.
     * 
     * @param list
     * @return
     */
    public final double[] reverse(double[] list) {
        double[] outgoing = new double[list.length];
        int length1 = list.length - 1;
        for (int i = 0; i < list.length; i++) {
            outgoing[i] = list[length1 - i];
        }
        return outgoing;
    }

    /**
     * Return a new array containing the elements of the provided list in reverse.
     * 
     * @param list
     * @return
     */
    public final String[] reverse(String[] list) {
        return PApplet.reverse(list);
    }

    /**
     * Return a new array containing the elements of the provided list in reverse.
     * 
     * @param list
     * @return
     */
    public final Object reverse(Object list) {
        return PApplet.reverse(list);
    }

    /**
     * Return a String with all whitespace removed from the beginning and end.
     * 
     * @param str
     * @return
     */
    public final String trim(String str) {
        return PApplet.trim(str);
    }

    /**
     * Return an array of Strings with all whitespace removed from the beginning and end.
     * 
     * @param array
     * @return
     */
    public final String[] trim(String[] array) {
        return PApplet.trim(array);
    }

    /**
     * Return a String consisting of all the values of list joined by the separator.
     * 
     * @param list
     * @param separator
     * @return
     */
    public final String join(String[] list, char separator) {
        return PApplet.join(list, separator);
    }

    /**
     * Return a String consisting of all the values of list joined by the separator.
     * 
     * @param list
     * @param separator
     * @return
     */
    public final String join(String[] list, String separator) {
        return PApplet.join(list, separator);
    }

    /**
     * Return an array of Strings by splitting the provided String at all
     * whitespace characters.
     * 
     * @param value
     * @return
     */
    public final String[] splitTokens(String value) {
        return PApplet.splitTokens(value);
    }

    /**
     * Return an array of Strings by splitting the provided String at any of the
     * provided tokens.
     * 
     * @param value
     * @param delim
     * @return
     */
    public final String[] splitTokens(String value, String delim) {
        return PApplet.splitTokens(value, delim);
    }

    /**
     * Return an array of Strings by splitting the provided String at the
     * provided delimeter.
     * 
     * @param value
     * @param delim
     * @return
     */
    public final String[] split(String value, char delim) {
        return PApplet.split(value, delim);
    }

    /**
     * Return an array of Strings by splitting the provided String at the
     * provided delimeter.
     * 
     * @param value
     * @param delim
     * @return
     */
    public final String[] split(String value, String delim) {
        return PApplet.split(value, delim);
    }

    /**
     * The match() function is used to apply a regular expression to a piece of text,
     * and return matching groups (elements found inside parentheses) as a String array.
     * No match will return null. If no groups are specified in the regexp, but the
     * sequence matches, an array of length one (with the matched text as the first
     * element of the array) will be returned.
     * <p>
     * To use the function, first check to see if the result is null. If the result
     * is null, then the sequence did not match. If the sequence did match, an array 
     * is returned. If there are groups (specified by sets of parentheses) in the regexp, 
     * then the contents of each will be returned in the array. Element [0] of a regexp 
     * match returns the entire matching string, and the match groups start at element [1]
     * (the first group is [1], the second [2], and so on).
     * 
     * @param str
     * @param regexp
     * @return
     */
    public final String[] match(String str, String regexp) {
        return PApplet.match(str, regexp);
    }

    /**
     *
     * @param str
     * @param regexp
     * @return
     */
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
    /**
     * Convert the provided value to an equivalent hexadecimal notation.
     * 
     * @param value
     * @return
     */
    public final String hex(byte value) {
        return PApplet.hex(value);
    }

    /**
     * Convert the provided value to an equivalent hexadecimal notation.
     * 
     * @param value
     * @return
     */
    public final String hex(char value) {
        return PApplet.hex(value);
    }

    /**
     * Convert the provided value to an equivalent hexadecimal notation.
     * 
     * @param value
     * @return
     */
    public final String hex(int value) {
        return PApplet.hex(value);
    }

    /**
     * Convert the provided value to an equivalent hexadecimal notation.
     * 
     * @param value
     * @param digits
     * @return
     */
    public final String hex(int value, int digits) {
        return PApplet.hex(value, digits);
    }

    /**
     * Convert a hexadecimal String into an int.
     * 
     * @param value
     * @return
     */
    public final int unhex(String value) {
        return PApplet.unhex(value);
    }

    /**
     * Convert the provided value to an equivalent binary notation.
     * 
     * @param value
     * @return
     */
    public final String binary(byte value) {
        return PApplet.binary(value);
    }

    /**
     * Convert the provided value to an equivalent binary notation.
     * 
     * @param value
     * @return
     */
    public final String binary(char value) {
        return PApplet.binary(value);
    }

    /**
     * Convert the provided value to an equivalent binary notation.
     * 
     * @param value
     * @return
     */
    public final String binary(int value) {
        return PApplet.binary(value);
    }

    /**
     * Convert the provided value to an equivalent binary notation.
     * 
     * @param value
     * @param digits
     * @return
     */
    public final String binary(int value, int digits) {
        return PApplet.binary(value, digits);
    }

    /**
     * Convert a binary String into an int.
     * 
     * @param value
     * @return
     */
    public final int unbinary(String value) {
        return PApplet.unbinary(value);
    }

}
