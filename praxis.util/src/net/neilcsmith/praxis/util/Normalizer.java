/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Neil C Smith.
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
package net.neilcsmith.praxis.util;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
@Deprecated
public class Normalizer {

    private final static double MIN_VALUE = Double.MIN_NORMAL;
    
    private double max;
    private double average;
    private double correction;

    public Normalizer() {
        reset();
        average = 0;
        correction = 0;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        if (average < 0 || average > 1) {
            throw new IllegalArgumentException();
        }
        this.average = average;
    }

    public double getCorrection() {
        return correction;
    }

    public void setCorrection(double correction) {
        if (correction < 0 || correction > 1) {
            throw new IllegalArgumentException();
        }
        this.correction = correction;
    }

    public final double normalize(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < MIN_VALUE) {
            return 0;
        }
  
        boolean negative = (value < 0);
        if (negative) {
            value = -value;
        }

        if (value > max) {
            max = value;
            return negative ? -1 : 1;
        }

        double result = value / max;

//        if (value > average) {
//            max *= (1 + correction);
//        } else if (value > MIN_VALUE) {
//            max *= (1 - correction);
//        }
        
        if (average > MIN_VALUE && correction > MIN_VALUE) {
            max += ((value / average) - max) * correction;
        } else {
            max -= max * correction;
        }

        return negative ? -result : result;
    }

    public final void reset() {
        max = MIN_VALUE;
    }
}
