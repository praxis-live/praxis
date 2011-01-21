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
package net.neilcsmith.praxis.gui.impl;

import net.neilcsmith.praxis.util.interpolation.Interpolator;
import net.neilcsmith.praxis.util.interpolation.LinearInterpolator;
import java.util.logging.Logger;
import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.CallArguments;
import net.neilcsmith.praxis.core.info.ArgumentInfo;
import net.neilcsmith.praxis.core.info.ControlInfo;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PString;
import net.neilcsmith.praxis.gui.ControlBinding;
import net.neilcsmith.praxis.util.interpolation.ExponentialInterpolator;
import net.neilcsmith.praxis.util.PMath;

/**
 *
 * @author Neil C Smith
 */
public class BoundedValueAdaptor extends ControlBinding.Adaptor {

    private final static double DEFAULT_MINIMUM = 0;
    private final static double DEFAULT_MAXIMUM = 1;
    private static final Logger LOG = Logger.getLogger(BoundedValueAdaptor.class.getName());
    private BoundedRangeModel model;
    private ControlInfo info;
    private boolean isUpdating;
    private double minimum;
    private double maximum;
    private PNumber prefMin;
    private PNumber prefMax;
    private PString prefScale;
    private double value;
    private Interpolator interpolator;

    public BoundedValueAdaptor(BoundedRangeModel model) {
        if (model == null) {
            throw new NullPointerException();
        }
        this.model = model;
        model.addChangeListener(new ChangeHandler());
        interpolator = LinearInterpolator.getInstance();
        this.value = DEFAULT_MINIMUM;
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

    public void setPreferredScale(PString scale) {
        prefScale = scale;
        updateScale();
    }

    public PString getInterpolator() {
        return prefScale;
    }

    private void updateAllowedRange() {
        PNumber infMin = null;
        PNumber infMax = null;
        if (info != null) {
            ArgumentInfo[] aIn = info.getInputsInfo();
            if (aIn.length > 0) {
                infMin = coerce(aIn[0].getProperties().get("minimum"));
                infMax = coerce(aIn[0].getProperties().get("maximum"));
            }
        }
        PNumber calcMin = PMath.getMaximum(infMin, prefMin);
        double min = calcMin == null ? DEFAULT_MINIMUM : calcMin.value();
        PNumber calcMax = PMath.getMinimum(infMax, prefMax);
        double max = calcMax == null ? DEFAULT_MAXIMUM : calcMax.value();
        if (max < min || min > max) {
            min = max = 0;
        }
        minimum = min;
        maximum = max;
        updateModel();
    }

    private void updateScale() {
        Argument intHint = prefScale;
        if (intHint == null || intHint.isEmpty()) {
            if (info != null) {
                intHint = info.getProperties().get("scale-hint");
            }
            
        }
        if (intHint == null) {
            interpolator = LinearInterpolator.getInstance();
        } else if (intHint.toString().equalsIgnoreCase("Exponential")) {
            interpolator = ExponentialInterpolator.getInstance();
        } else {
            interpolator = LinearInterpolator.getInstance();
        }
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
            if (args.getSize() > 0) {
                try {
                    double val = PNumber.coerce(args.get(0)).value();
                    if (val != value) {
                        value = val;
                        updateModel();
                    }
                } catch (Exception ex) {
                }
            }
        }
    }

    private void updateModel() {
        value = value < minimum ? minimum : (value > maximum ? maximum : value);
        int val = convertToInt(value);
        isUpdating = true;
        model.setValue(val);
        model.setExtent(0);
        isUpdating = false;
    }

    @Override
    public void updateBindingConfiguration() {
        ControlBinding binding = getBinding();
        if (binding == null) {
            return;
        }
        info = binding.getBindingInfo();
        if (info != null) {
            updateAllowedRange();
            updateScale();
        }

    }

    private class ChangeHandler implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (!isUpdating) {
                value = convertToDouble(model.getValue());
                send(CallArguments.create(PNumber.valueOf(value)));
            }
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
        if (maximum == minimum) {
            return 0;
        }
        double ratio = (value - minimum) / (maximum - minimum);
        ratio = interpolator.reverseInterpolate(ratio);
        int mMin = model.getMinimum();
        int mMax = model.getMaximum();
        int val = (int) Math.round(ratio * (mMax - mMin));
        val += mMin;
        return val;
    }
}
