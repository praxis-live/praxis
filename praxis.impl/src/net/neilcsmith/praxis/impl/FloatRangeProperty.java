/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Neil C Smith. All rights reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 *
 */

package net.neilcsmith.praxis.impl;

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PNumber;

/**
 *
 * @author Neil C Smith
 */
public class FloatRangeProperty extends AbstractProperty {

    private double min;
    private double max;
    private Binding binding;

    private FloatRangeProperty(Binding binding,
            double min, double max, ControlInfo info) {
        super(info);
        this.binding = binding;
        this.min = min;
        this.max = max;
    }

    @Override
    protected void setArguments(long time, CallArguments args) throws Exception {
        if (args.getCount() != 2) {
            throw new IllegalArgumentException();
        }
        PNumber low = PNumber.coerce(args.getArg(0));
        PNumber high = PNumber.coerce(args.getArg(1));
        binding.setBoundLowValue(time, low.value());
        binding.setBoundHighValue(time, high.value());
    }


    private Argument[] holder = new Argument[2];
    @Override
    protected CallArguments getArguments() {
        holder[0] = PNumber.valueOf(binding.getBoundLowValue());
        holder[1] = PNumber.valueOf(binding.getBoundHighValue());
        return CallArguments.create(holder);
    }


    public static FloatRangeProperty create( Binding binding,
            double min, double max, double low, double high) {
        if (min > max || low > high || low < min || low > max || high > max) {
            throw new IllegalArgumentException();
        }
        if (binding == null) {
            binding = new DefaultBinding(low, high);
        }
        ArgumentInfo inf = PNumber.info(min, max);
        ArgumentInfo[] arguments = new ArgumentInfo[]{inf, inf};
        Argument[] defaults = new Argument[]{PNumber.valueOf(low), PNumber.valueOf(high)};
        ControlInfo info = ControlInfo.createPropertyInfo(arguments, defaults, null);
        return new FloatRangeProperty(binding, min, max, info);
    }


    public static interface Binding {

        public void setBoundLowValue(long time, double low);

        public void setBoundHighValue(long time, double high);

        public double getBoundLowValue();

        public double getBoundHighValue();

    }

    private static class DefaultBinding implements Binding {

        private double low;
        private double high;

        private DefaultBinding(double low, double high) {
            this.low = low;
            this.high = high;
        }

        public void setBoundLowValue(long time, double low) {
            this.low = low;
        }

        public void setBoundHighValue(long time, double high) {
            this.high = high;
        }

        public double getBoundLowValue() {
            return low;
        }

        public double getBoundHighValue() {
            return high;
        }

    }

}
