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
 *
 */
package net.neilcsmith.praxis.code.userapi;

import java.util.logging.Logger;
import net.neilcsmith.praxis.code.CodeContext;
import net.neilcsmith.praxis.core.Argument;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class Property {

    private final static Logger LOG = Logger.getLogger(Property.class.getName());

    private final static long TO_NANO = 1000000000;

    private final static Interpolator LINEAR = LinearInterpolator.getInstance();
    private final static Interpolator EASE_IN = new SplineInterpolator(0.42, 0, 1, 1);
    private final static Interpolator EASE_OUT = new SplineInterpolator(0, 0, 0.58, 1);
    private final static Interpolator EASE_IN_OUT = new SplineInterpolator(0.42, 0, 0.58, 1);
    private final static Interpolator EASE = new SplineInterpolator(0.25, 0.1, 0.25, 1);
    
    private final Listener listener;

    private CodeContext<?> context;
    private boolean animating;
    private double toValue;
    private long duration;
    private double fromValue;
    private long fromTime;
    private Interpolator interpolator;

    protected Property() {
        this.listener = new Listener();
        interpolator = LINEAR;
    }
    
    protected void attach(CodeContext<?> context, Property previous) {
        this.context = context;
        if (previous != null && previous.animating) {
            animating = true;
            fromValue = previous.fromValue;
            toValue = previous.toValue;
            duration = previous.duration;
            fromTime = previous.fromTime;
            interpolator = previous.interpolator;
            context.addClockListener(listener);
        }
    }

    protected abstract void setImpl(long time, Argument arg) throws Exception;

    protected abstract void setImpl(long time, double value) throws Exception;

    protected abstract Argument getImpl();

    protected abstract double getImpl(double def);

    public Argument get() {
        return getImpl();
    }

    public double getDouble() {
        return getDouble(0);
    }

    public double getDouble(double def) {
        return getImpl(def);
    }

    public int getInt() {
        return (int) Math.round(getDouble());
    }

    public int getInt(int def) {
        return (int) Math.round(getDouble(def));
    }

    public Property set(Argument arg) {
        if (arg == null) {
            throw new NullPointerException();
        }
        finishAnimating();
        try {
            setImpl(context.getTime(), arg);
        } catch (Exception ex) {
            // no op?
        }
        return this;
    }

    public Property set(double value) {
        finishAnimating();
        try {
            setImpl(context.getTime(), value);
        } catch (Exception ex) {
            // no op?
        }
        return this;
    }

    public Property to(double to) {
        startAnimating();
        fromValue = getDouble(0);
        fromTime = context.getTime();
        try {
            setImpl(fromTime, fromValue);
        } catch (Exception ex) {
            finishAnimating();
        }
        toValue = to;
        return this;
    }

    public Property in(double time) {
        startAnimating();
        duration = (long) (time * TO_NANO);
        if (duration < 0) {
            duration = 0;
        }
        return this;
    }

    public boolean isAnimating() {
        return animating;
    }

    public Property linear() {
        interpolator = LINEAR;
        return this;
    }

    public Property ease() {
        interpolator = EASE;
        return this;
    }

    public Property easeIn() {
        interpolator = EASE_IN;
        return this;
    }

    public Property easeOut() {
        interpolator = EASE_OUT;
        return this;
    }

    public Property easeInOut() {
        interpolator = EASE_IN_OUT;
        return this;
    }

    private void tick() {
        if (!animating) {
            LOG.warning("Tick called when not animating");
            return;
        }
        try {
            long currentTime = context.getTime();
            double proportion;
            if (duration < 1) {
                proportion = 1;
            } else {
                proportion = (currentTime - fromTime) / (double) duration;
            }
            if (proportion >= 1) {
                finishAnimating();
//            dblValue = toValue;
                setImpl(fromTime, toValue);
            } else if (proportion > 0) {
                double d = interpolator.interpolate(proportion);
                d = (d * (toValue - fromValue)) + fromValue;
                setImpl(fromTime, d);
            } else {
                setImpl(fromTime, fromValue);
            }
        } catch (Exception exception) {
            finishAnimating();
        }

    }

    protected void startAnimating() {
        if (!animating) {
            animating = true;
            context.addClockListener(listener);
            duration = 0;
            toValue = getDouble(0);
        }
    }

    protected void finishAnimating() {
        if (animating) {
            animating = false;
            context.removeClockListener(listener);

        }

    }

    private class Listener implements CodeContext.ClockListener {

        @Override
        public void tick() {
            Property.this.tick();
        }

    }






}
