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
 *
 *
 * Parts of the API of this package, as well as some of the code, is derived from
 * the Processing project (http://processing.org)
 *
 * Copyright (c) 2004-09 Ben Fry and Casey Reas
 * Copyright (c) 2001-04 Massachusetts Institute of Technology
 *
 */
package net.neilcsmith.praxis.java;

import java.util.logging.Logger;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.impl.ArgumentProperty;
import net.neilcsmith.praxis.impl.ListenerUtils;
import net.neilcsmith.praxis.util.interpolation.Interpolator;
import net.neilcsmith.praxis.util.interpolation.LinearInterpolator;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Param implements ArgumentProperty.Binding {

    private final static Logger LOG = Logger.getLogger(Param.class.getName());

    private final static long TO_NANO = 1000000000;

    private final Clock clock;
    private ClockListener clockListener;
    private Argument argValue;
    private double dblValue;
    private Listener[] listeners;
    private boolean animating;
    private double toValue;
    private long duration;
    private double fromValue;
    private long fromTime;
    private Interpolator interpolator;


    public Param(Clock clock) {
        if (clock == null) {
            throw new NullPointerException();
        }
        this.clock = clock;
        clockListener = new ClockListener() {

            public void tick() {
                Param.this.tick();
            }
        };
        this.argValue = PString.EMPTY;
        this.listeners = new Listener[0];
        interpolator = LinearInterpolator.getInstance();
    }

    public final void setBoundValue(long time, Argument value) throws Exception {
        set(value);
    }

    public final Argument getBoundValue() {
        if (argValue == null) {
            return PNumber.valueOf(dblValue);
        } else {
            return argValue;
        }
    }

    public void addListener(Listener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        listeners = ListenerUtils.add(listeners, listener);
    }

    public void removeListener(Listener listener) {
        listeners = ListenerUtils.remove(listeners, listener);
    }

    public Argument get() {
        return getBoundValue();
    }

    public double getDouble() throws ArgumentFormatException {
        if (argValue == null) {
            return dblValue;
        } else {
            return PNumber.coerce(argValue).value();
        }
    }

    public double getDouble(double def) {
        try {
            return getDouble();
        } catch (ArgumentFormatException ex) {
            return def;
        }
    }

    public int getInt() throws ArgumentFormatException {
        return (int) Math.round(getDouble());
    }

    public int getInt(int def) {
        try {
            return getInt();
        } catch (ArgumentFormatException ex) {
            return def;
        }
    }

    public Param set(Argument arg) {
        if (arg == null) {
            throw new NullPointerException();
        }
        finishAnimating();
        argValue = arg;
        return this;
    }

    public Param set(double value) {
        finishAnimating();
        argValue = null;
        dblValue = value;
        return this;
    }


    public Param to(double to) {
        startAnimating();
        fromValue = getDouble(0);
        fromTime = clock.getTime();
        argValue = null;
        dblValue = fromValue;
        toValue = to;
        return this;
    }

    public Param in(double time) {
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

    private void tick() {
        if (!animating) {
            LOG.warning("Tick called when not animating");
            return;
        }
        long currentTime = clock.getTime();
        double proportion;
        if (duration < 1) {
            proportion = 1;
        } else {
            proportion = (currentTime - fromTime) / (double) duration;
        }
        if (proportion >= 1) {
            finishAnimating();
            dblValue = toValue;
        } else if (proportion > 0) {
            double interpolated = interpolator.interpolate(proportion);
            dblValue = (interpolated * (toValue - fromValue)) + fromValue;
        } else {
            dblValue = fromValue;
        }


    }
    
    private void startAnimating() {
        if (!animating) {
            animating = true;
            clock.connect(clockListener);
            duration = 0;
            toValue = getDouble(0);
        }
    }

    private void finishAnimating() {
        if (animating) {
            animating = false;
            clock.disconnect(clockListener);
        }

    }

    public static interface Listener {

        public void valueChanged(Param p);
    }

    public static interface Clock {

        public long getTime();

        public void connect(ClockListener p);

        public void disconnect(ClockListener p);
    }

    public static interface ClockListener {

        public void tick();
    }
}
