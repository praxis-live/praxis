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
 */
package org.praxislive.gui.impl;

import java.util.logging.Level;
import org.praxislive.util.interpolation.Interpolator;
import org.praxislive.util.interpolation.LinearInterpolator;
import java.util.logging.Logger;
import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.praxislive.core.Argument;
import org.praxislive.core.ArgumentFormatException;
import org.praxislive.core.CallArguments;
import org.praxislive.core.info.ArgumentInfo;
import org.praxislive.core.info.ControlInfo;
import org.praxislive.core.types.PNumber;
import org.praxislive.gui.ControlBinding;
import org.praxislive.util.PMath;

/**
 *
 * @author Neil C Smith
 */
@Deprecated
public class BoundedRangeAdaptor extends ControlBinding.Adaptor implements ChangeListener {

    private final static double DEFAULT_MINIMUM = 0;
    private final static double DEFAULT_MAXIMUM = 1;
    private static Logger logger = Logger.getLogger(BoundedRangeAdaptor.class.getName());
    private BoundedRangeModel model;
    private ControlInfo info;
    private boolean isUpdating;
    private boolean isRange;
    private double minimum;
    private double maximum;
    private PNumber prefMin;
    private PNumber prefMax;
    private double low;
    private double high;
    private Interpolator interpolator;

    public BoundedRangeAdaptor(BoundedRangeModel model) {
        if (model == null) {
            throw new NullPointerException();
        }
        this.model = model;
        model.addChangeListener(this);
        interpolator = LinearInterpolator.getInstance();
        isRange = true;
        this.minimum = DEFAULT_MINIMUM;
        this.maximum = DEFAULT_MAXIMUM;
        updateModel();
        // @TODO temporary sync fix
        setSyncRate(ControlBinding.SyncRate.Medium);
    }

    public void setPreferredMinimum(PNumber min) {
        prefMin = min;
        updateAllowedRange();
    }

    public PNumber getPreferredMinimum() {
        return prefMin;
    }

    public void setPreferredMaximum(PNumber max) {
        prefMax = max;
        updateAllowedRange();
    }

    public PNumber getPreferredMaximum() {
        return prefMax;
    }

    private void updateAllowedRange() {
        PNumber infMinL = null;
        PNumber infMaxL = null;
        PNumber infMinH = null;
        PNumber infMaxH = null;
        if (info != null) {
            ArgumentInfo[] aIn = info.getInputsInfo();
            if (aIn.length > 0) {
                infMinL = coerce(aIn[0].getProperties().get("minimum"));
                infMaxL = coerce(aIn[0].getProperties().get("maximum"));
            }
            if (aIn.length > 1) {
                infMinH = coerce(aIn[1].getProperties().get("minimum"));
                infMaxH = coerce(aIn[1].getProperties().get("maximum"));
            }
        }

        PNumber calcMin = PMath.getMaximum(infMinL, infMinH, prefMin);
        double min = calcMin == null ? DEFAULT_MINIMUM : calcMin.value();
        PNumber calcMax = PMath.getMinimum(infMaxL, infMaxH, prefMax);
        double max = calcMax == null ? DEFAULT_MAXIMUM : calcMax.value();
        if (max < min || min > max) {
            min = max = 0;
        }
        minimum = min;
        maximum = max;
        updateModel();
    }

    private PNumber coerce(Argument arg) {
        if (arg == null) {
            return null;
        }
        try {
            return PNumber.coerce(arg);
        } catch (ArgumentFormatException ex) {
            return null;
        }
    }

    @Override
    public void update() {
        ControlBinding binding = getBinding();
        if (binding == null) {
            return;
        }
        CallArguments args = binding.getArguments();
        if (!model.getValueIsAdjusting()) {
            if (args.getSize() >= 2) {
                try {
                    double lo = PNumber.coerce(args.get(0)).value();
                    double hi = PNumber.coerce(args.get(1)).value();
                    if (lo != low || hi != high) {
                        low = lo;
                        high = hi;
                        updateModel();
                    }
                } catch (Exception ex) {
                }
            }
        }
    }

    private void updateModel() {
        low = low < minimum ? minimum : (low > maximum ? maximum : low);
        high = high < low ? low : (high > maximum ? maximum : high);
        int val = convertToInt(low);
        int ext = convertToInt(high) - val;
        isUpdating = true;
        // must set values together otherwise extent might go out of range
        model.setRangeProperties(val, ext,
                model.getMinimum(), model.getMaximum(), model.getValueIsAdjusting());
        isUpdating = false;
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Model Updated : Low - " + low + " High - " + high +
                    " Value - " + model.getValue() + " Extent - " + model.getExtent());
        }
    }

    @Override
    public void updateBindingConfiguration() {
        ControlBinding binding = getBinding();
        if (binding == null) {
            return;
        }
        info = binding.getBindingInfo();

    }

    public void stateChanged(ChangeEvent e) {
        if (!isUpdating) {
            int val = model.getValue();
            int ext = model.getExtent();
            low = convertToDouble(val);
            high = convertToDouble(val + ext);
            send(CallArguments.create(new Argument[]{
                        PNumber.valueOf(low), PNumber.valueOf(high)
                    }));
        }
    }

    @Override
    public boolean getValueIsAdjusting() {
        return model.getValueIsAdjusting();
    }

    

    private double convertToDouble(int value) {
        int mMin = model.getMinimum();
        int mMax = model.getMaximum();
        double ratio = ((double) (value - mMin)) / (mMax - mMin);
        ratio = interpolator.interpolate(ratio);
        return (ratio * (maximum - minimum)) + minimum;
    }

    private int convertToInt(double value) {
        double ratio = (value - minimum) / (maximum - minimum);
        ratio = interpolator.reverseInterpolate(ratio);
        int mMin = model.getMinimum();
        int mMax = model.getMaximum();
        int val = (int) Math.round(ratio * (mMax - mMin));
        val += mMin;
        return val;
    }
}
