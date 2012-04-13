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
package net.neilcsmith.praxis.components.timing;

import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.ControlPort;
import net.neilcsmith.praxis.core.ExecutionContext;
import net.neilcsmith.praxis.core.Port;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.impl.AbstractClockComponent;
import net.neilcsmith.praxis.impl.DefaultControlOutputPort;
import net.neilcsmith.praxis.impl.FloatInputPort;
import net.neilcsmith.praxis.impl.FloatProperty;
import net.neilcsmith.praxis.impl.SimpleControl;
import net.neilcsmith.praxis.util.interpolation.Interpolator;
import net.neilcsmith.praxis.util.interpolation.LinearInterpolator;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class Animator extends AbstractClockComponent {

    private final static String TO = "to";
    private final static String VALUE = "value";
    private final static String TIME = "time";

    private final static long TO_NANO = 1000000000;
    
    private double fromValue;
    private long fromTime;
    private double toValue;
    private double pendingToValue;
    private long toTime;
    private FloatProperty duration;
    private boolean animating;
    private boolean needsConfiguring;
    private double currentValue;
    private ControlPort.Output output;
    private Interpolator interpolator;

    public Animator() {
        interpolator = LinearInterpolator.getInstance();
        ToControl to = new ToControl();
        FloatProperty value = FloatProperty.create(new ValueBinding(), currentValue);
        duration = FloatProperty.create(0, 60 * 5, 0);
        output = new DefaultControlOutputPort(this);
        registerControl(TO, to);
        registerPort(TO, FloatInputPort.create(to));
        registerPort(Port.OUT, output);    
        registerControl(VALUE, value);
        registerPort(VALUE, value.createPort());
        registerControl(TIME, duration);
        registerPort(TIME, duration.createPort());
        
    }

    public void tick(ExecutionContext source) {
        if (!animating) {
            return;
        }
        long time = source.getTime();
//        if (needsConfiguring) {
//            configure(time);
//        } else {
//            processAnimation(time);
//        }
        processAnimation(time);
        if (needsConfiguring) {
            configure(time);
        }
        output.send(time, currentValue);
    }

    private void configure(long time) {
        animating = true;
        needsConfiguring = false;
        fromValue = currentValue;
        toValue = pendingToValue;
        fromTime = time;
        toTime = fromTime + ((long) (duration.getValue() * TO_NANO));
        if (toTime == fromTime) {
            animating = false;
            currentValue = toValue;
        }
    }

    private void processAnimation(long time) {
        double duration = (double) (toTime - fromTime);
        double proportion = (time - fromTime) / duration;
        if (proportion >= 1) {
            animating = false;
            currentValue = toValue;
        } else if (proportion > 0) {
            double interpolated = interpolator.interpolate(proportion);
            currentValue = (interpolated * (toValue - fromValue)) + fromValue;
        } else {
            currentValue = fromValue;
        }
    }

    private class ValueBinding implements FloatProperty.Binding {

        public void setBoundValue(long time, double value) {
            currentValue = toValue = fromValue = value;
            animating = false;
            needsConfiguring = false;
            output.send(time, value);
        }

        public double getBoundValue() {
            return currentValue;
        }
    }

    private class ToControl extends SimpleControl implements FloatInputPort.Binding {

        private ToControl() {
            super(null);
        }

        @Override
        protected CallArguments process(long time, CallArguments args, boolean quiet) throws Exception {
            double value = PNumber.coerce(args.get(0)).value();
            receive(time, value);
            if (quiet) {
                return null;
            } else {
                return CallArguments.EMPTY;
            }
        }

        public void receive(long time, double value) {
            pendingToValue = value;
            needsConfiguring = true;
            animating = true;
        }
    }
}
