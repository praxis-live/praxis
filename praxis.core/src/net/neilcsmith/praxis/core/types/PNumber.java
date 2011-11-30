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
package net.neilcsmith.praxis.core.types;

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.info.ArgumentInfo;

/**
 *
 * @author Neil C Smith
 */
public final class PNumber extends Argument implements Comparable<PNumber> {

    public final static String KEY_MINIMUM = "minimum";
    public final static String KEY_MAXIMUM = "maximum";

    public final static int MAX_VALUE = Integer.MAX_VALUE;
    public final static int MIN_VALUE = Integer.MIN_VALUE;
    private double value;
    private boolean isInteger;
    private String string;

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
    public boolean isEquivalent(Argument arg) {
        // @TODO should we allow small margin of error???
        try {
            return equals(PNumber.coerce(arg));
        } catch (ArgumentFormatException ex) {
            return false;
        }
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
        } else {
            return valueOf(arg.toString());
        }

    }

    public static ArgumentInfo info() {
        return ArgumentInfo.create(PNumber.class, null);
    }

    public static ArgumentInfo info(
            double min, double max) {
        return info(valueOf(min), valueOf(max));
    }

    public static ArgumentInfo info(
            int min, int max) {
        return info(valueOf(min), valueOf(max));
    }

    public static ArgumentInfo info(
            PNumber min, PNumber max) {
        PMap map = PMap.create(KEY_MINIMUM, min,
                KEY_MAXIMUM, max);
        return ArgumentInfo.create(PNumber.class, map);
    }

}
