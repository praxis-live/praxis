/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Neil C Smith.
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
package net.neilcsmith.praxis.code.userapi;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class Easing {
    
    public final static Easing linear = new LinearEasing();
    public final static Easing easeIn = new SplineEasing(0.42, 0, 1, 1);
    public final static Easing easeOut = new SplineEasing(0, 0, 0.58, 1);
    public final static Easing easeInOut = new SplineEasing(0.42, 0, 0.58, 1);
    public final static Easing ease = new SplineEasing(0.25, 0.1, 0.25, 1);
    public final static Easing expoIn = new SplineEasing(0.71,0.01,0.83,0);
    public final static Easing expoOut = new SplineEasing(0.14,1,0.32,0.99);
    public final static Easing expoInOut = new SplineEasing(0.85,0,0.15,1);
   
    public abstract double calculate(double fraction);
    
    
    static final class LinearEasing extends Easing {

        @Override
        public double calculate(double fraction) {
            return fraction;
        }
        
    }
  
//https://gist.github.com/amadeus/983364
//'linear:in': cubic-bezier(0,0,1,1)
//'linear:out': cubic-bezier(0,0,1,1)
//'linear:in:out': cubic-bezier(0,0,1,1)
//'expo:in': cubic-bezier(0.71,0.01,0.83,0)
//'expo:out': cubic-bezier(0.14,1,0.32,0.99)
//'expo:in:out': cubic-bezier(0.85,0,0.15,1)
//'circ:in': cubic-bezier(0.34,0,0.96,0.23)
//'circ:out': cubic-bezier(0,0.5,0.37,0.98)
//'circ:in:out': cubic-bezier(0.88,0.1,0.12,0.9)
//'sine:in': cubic-bezier(0.22,0.04,0.36,0)
//'sine:out': cubic-bezier(0.04,0,0.5,1)
//'sine:in:out': cubic-bezier(0.37,0.01,0.63,1)
//'quad:in': cubic-bezier(0.14,0.01,0.49,0)
//'quad:out': cubic-bezier(0.01,0,0.43,1)
//'quad:in:out': cubic-bezier(0.47,0.04,0.53,0.96)
//'cubic:in': cubic-bezier(0.35,0,0.65,0)
//'cubic:out': cubic-bezier(0.09,0.25,0.24,1)
//'cubic:in:out': cubic-bezier(0.66,0,0.34,1)
//'quart:in': cubic-bezier(0.69,0,0.76,0.17)
//'quart:out': cubic-bezier(0.26,0.96,0.44,1)
//'quart:in:out': cubic-bezier(0.76,0,0.24,1)
//'quint:in': cubic-bezier(0.64,0,0.78,0)
//'quint:out': cubic-bezier(0.22,1,0.35,1)
//'quint:in:out': cubic-bezier(0.9,0,0.1,1)
    
}
