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
package net.neilcsmith.praxis.java;

import java.util.Random;
import net.neilcsmith.praxis.core.Argument;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class CodeDelegate {

    private CodeContext context;
    private boolean installable;
    private Random rnd;

    public CodeDelegate() {
        this(true);
    }

    public CodeDelegate(boolean installable) {
        rnd = new Random();
    }

    public void init(CodeContext context, long time) throws Exception {
        if (context == null) {
            throw new NullPointerException();
        }
        this.context = context;
        if (installable) {
            // add param listeners
        }
    }

    public void tick() {
        
    }

    public void dispose() {
    }

    public Param p(int idx) {
        return context.getParam(idx - 1);
    }

    public double d(int idx) {
        return context.getParam(idx - 1).getDouble(0);
    }

    public int i(int idx) {
        return context.getParam(idx - 1).getInt(0);
    }

    public boolean t(int idx) {
        return context.getTrigger(idx - 1).getAndReset();
    }

    public void send(int idx, Argument arg) {
        context.getOutput(idx - 1).send(arg);
    }

    public void send(int idx, double value) {
        context.getOutput(idx - 1).send(value);
    }

    public long getTime() {
        return context.getTime();
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

    public final double max(double a, double b) {
        return (a > b) ? a : b;
    }

    public final double max(double a, double b, double c) {
        return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
    }
}
