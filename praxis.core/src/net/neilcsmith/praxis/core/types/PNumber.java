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
package net.neilcsmith.praxis.core.types;

import java.util.Optional;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.info.ArgumentInfo;

/**
 *
 * @author Neil C Smith
 */
public final class PNumber extends Value implements Comparable<PNumber> {

    public final static PNumber ONE = PNumber.valueOf(1);
    public final static PNumber ZERO = PNumber.valueOf(0);

    public final static String KEY_MINIMUM = "minimum";
    public final static String KEY_MAXIMUM = "maximum";
    public final static String KEY_IS_INTEGER = "is-integer";
    public final static String KEY_SKEW = "skew";

    public final static int MAX_VALUE = Integer.MAX_VALUE;
    public final static int MIN_VALUE = Integer.MIN_VALUE;

    private final double value;
    private final boolean isInteger;
    private final String string;

    private PNumber(double value, String str) {
        this.value = value;
        this.isInteger = false;
        this.string = str;
    }

    private PNumber(int value, String str) {
        this.value = value;
        this.isInteger = true;
        this.string = str;
    }

    public double value() {
        return value;
    }

    public int toIntValue() {
        if (isInteger) {
            return (int) value;
        } else {
            return (int) Math.round(value);
        }

    }

    @Override
    public String toString() {
        if (string != null) {
            return string;
        } else if (isInteger) {
            return Integer.toString(toIntValue());
        } else {
            return Double.toString(value);
        }
    }

    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(value);
        return (int) (bits ^ (bits >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PNumber) {
            return Double.doubleToLongBits(((PNumber) obj).value) == Double.doubleToLongBits(value);
        }
        return false;
    }

    @Override
    public boolean equivalent(Value arg) {
        // @TODO should we allow small margin of error???
        try {
            return equals(PNumber.coerce(arg));
        } catch (ArgumentFormatException ex) {
            return false;
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public int compareTo(PNumber o) {
        return Double.compare(value, o.value);
    }

    public boolean isInteger() {
        return isInteger;
    }

    public static PNumber valueOf(double val) {
        return valueOf(val, null);
    }

    private static PNumber valueOf(double val, String str) {
        if (val > MAX_VALUE) {
            val = MAX_VALUE;
        } else if (val < MIN_VALUE) {
            val = MIN_VALUE;
        } else if (Double.isInfinite(val) || Double.isNaN(val)) {
            val = 0;
        }
        return new PNumber(val, str);
    }

    public static PNumber valueOf(int val) {
        return new PNumber(val, null);
    }

    public static PNumber valueOf(String str) throws ArgumentFormatException {
        try {
            if (str.indexOf('.') > -1) {
                return valueOf(Double.parseDouble(str), str);
            } else if ("true".equals(str)) {
                return ONE;
            } else if ("false".equals(str)) {
                return ZERO;
            } else {
                return new PNumber(Integer.parseInt(str), str);
            }
        } catch (Exception ex) {
            throw new ArgumentFormatException(ex);
        }
    }

    public static PNumber coerce(
            Argument arg) throws ArgumentFormatException {
        if (arg instanceof PNumber) {
            return (PNumber) arg;
        } else if (arg instanceof PBoolean) {
            return ((PBoolean) arg).value() ? ONE : ZERO;
        } else {
            return valueOf(arg.toString());
        }
    }

    public static Optional<PNumber> from(Argument arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ArgumentFormatException ex) {
            return Optional.empty();
        }
    }

    public static ArgumentInfo info() {
        return ArgumentInfo.create(PNumber.class, null);
    }

    public static ArgumentInfo info(double min, double max) {
        PMap map = PMap.create(
                KEY_MINIMUM, min,
                KEY_MAXIMUM, max);
        return ArgumentInfo.create(PNumber.class, map);
    }

    public static ArgumentInfo info(double min, double max, double skew) {
        if (skew < 0.01) {
            skew = 0.01;
        }
        PMap map = PMap.create(
                KEY_MINIMUM, min,
                KEY_MAXIMUM, max,
                KEY_SKEW, skew);
        return ArgumentInfo.create(PNumber.class, map);
    }

    public static ArgumentInfo integerInfo() {
        return ArgumentInfo.create(PNumber.class, PMap.create(KEY_IS_INTEGER, true));
    }

    public static ArgumentInfo integerInfo(int min, int max) {
        PMap map = PMap.create(KEY_MINIMUM, min,
                KEY_MAXIMUM, max, KEY_IS_INTEGER, true);
        return ArgumentInfo.create(PNumber.class, map);
    }

}
