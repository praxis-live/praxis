/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
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
 */
package net.neilcsmith.praxis.core.info;

import java.util.Arrays;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.types.PMap;

/**
 *
 * @author Neil C Smith
 * @TODO handle possible null pointers in returns
 */
public class ControlInfo extends Argument {

    private ArgumentInfo[] inputs;
    private ArgumentInfo[] outputs;
    private Argument[] defaults;
    private boolean isProperty;
    private PMap properties;

    private ControlInfo(ArgumentInfo[] inputs, ArgumentInfo[] outputs,
            Argument[] defaults, boolean isProperty, PMap properties) {

        this.inputs = inputs;
        this.outputs = outputs;
        this.defaults = defaults;
        this.isProperty = isProperty;
        this.properties = properties;

    }

    @Override
    public String toString() {
        return "ControlInfo toString() unsupported";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ControlInfo) {
            ControlInfo o = (ControlInfo) obj;
            if (isProperty) {
                return o.isProperty && Arrays.equals(inputs, o.inputs)
                        && Arrays.equals(defaults, o.defaults)
                        && properties.equals(o.properties);
            } else {
                return !o.isProperty && Arrays.equals(inputs, inputs)
                        && Arrays.equals(outputs, outputs)
                        && properties.equals(o.properties);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Arrays.deepHashCode(this.inputs);
        hash = 11 * hash + Arrays.deepHashCode(this.outputs);
        hash = 11 * hash + Arrays.deepHashCode(this.defaults);
        hash = 11 * hash + (this.isProperty ? 1 : 0);
        hash = 11 * hash + (this.properties != null ? this.properties.hashCode() : 0);
        return hash;
    }

    public boolean isProperty() {
        return isProperty;
    }

    public PMap getProperties() {
        return properties;
    }

    public Argument[] getDefaults() {
        if (defaults == null) {
            return null;
        } else {
            return Arrays.copyOf(defaults, defaults.length);
        }
    }

    public ArgumentInfo[] getInputsInfo() {
        return Arrays.copyOf(inputs, inputs.length);
    }

    public ArgumentInfo[] getOutputsInfo() {
        return Arrays.copyOf(outputs, outputs.length);
    }

    public static ControlInfo create(ArgumentInfo[] inputs,
            ArgumentInfo[] outputs, PMap properties) {
        return create(inputs, outputs, null, false, properties);
    }

    public static ControlInfo createPropertyInfo(ArgumentInfo[] arguments, Argument[] defaults, PMap properties) {
        return create(arguments, arguments, defaults, true, properties);
    }

    private static ControlInfo create(ArgumentInfo[] inputs,
            ArgumentInfo[] outputs,
            Argument[] defaults,
            boolean isProperty,
            PMap properties) {

        ArgumentInfo[] ins = Arrays.copyOf(inputs, inputs.length);
        ArgumentInfo[] outs;
        if (outputs == inputs) {
            // property - make same as inputs
            outs = ins;
        } else {
            outs = Arrays.copyOf(outputs, outputs.length);
        }
        if (properties == null) {
            properties = PMap.EMPTY;
        }

        return new ControlInfo(inputs, outputs, defaults, isProperty, properties);


    }

    public static ControlInfo coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof ControlInfo) {
            return (ControlInfo) arg;
        }
        throw new ArgumentFormatException();
    }
}
