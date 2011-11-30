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
package net.neilcsmith.praxis.core.info;

import java.util.Arrays;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
//import net.neilcsmith.praxis.core.Control.Type;
import net.neilcsmith.praxis.core.types.PMap;

/**
 *
 * @author Neil C Smith
 */
public class ControlInfo extends Argument {
    
    public final static String KEY_TRANSIENT = "transient";
    public final static String KEY_DEPRECATED = "deprecated";
    public final static String KEY_EXPERT = "expert";
//    public final static String KEY_DUPLICATES = "duplicates";

    public static enum Type {

        Function, Trigger, Property, ReadOnlyProperty
    };
    
    private final static ArgumentInfo[] EMPTY_INFO = new ArgumentInfo[0];
    
    private ArgumentInfo[] inputs;
    private ArgumentInfo[] outputs;
    private Argument[] defaults;
    private PMap properties;
    private Type type;

    private ControlInfo(ArgumentInfo[] inputs, ArgumentInfo[] outputs,
            Argument[] defaults, Type type, PMap properties) {

        this.inputs = inputs;
        this.outputs = outputs;
        this.defaults = defaults;
        this.type = type;
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
//            if (isProperty()) {
//                return o.isProperty() && Arrays.equals(inputs, o.inputs)
//                        && Arrays.equals(defaults, o.defaults)
//                        && properties.equals(o.properties);
//            } else {
//                return !o.isProperty() && Arrays.equals(inputs, o.inputs)
//                        && Arrays.equals(outputs, o.outputs)
//                        && properties.equals(o.properties);
//            }
            return type == o.type
                    && Arrays.equals(inputs, o.inputs)
                    && Arrays.equals(outputs, o.outputs)
                    && Arrays.equals(defaults, o.defaults)
                    && properties.equals(o.properties);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Arrays.deepHashCode(this.inputs);
        hash = 11 * hash + Arrays.deepHashCode(this.outputs);
        hash = 11 * hash + Arrays.deepHashCode(this.defaults);
        hash = 11 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 11 * hash + (this.properties != null ? this.properties.hashCode() : 0);
        return hash;
    }

    @Deprecated
    public boolean isProperty() {
        return type == Type.ReadOnlyProperty || type == Type.Property;
    }

    public Type getType() {
        return type;
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

    public static ControlInfo createFunctionInfo(ArgumentInfo[] inputs,
            ArgumentInfo[] outputs, PMap properties) {
        return create(inputs, outputs, null, Type.Function, properties);
    }
    
    public static ControlInfo createTriggerInfo(PMap properties) {
        return create(EMPTY_INFO, EMPTY_INFO, null, Type.Trigger, properties);
    }

    public static ControlInfo createPropertyInfo(ArgumentInfo[] arguments, Argument[] defaults, PMap properties) {
        return create(arguments, arguments, defaults, Type.Property, properties);
    }

    public static ControlInfo createReadOnlyPropertyInfo(ArgumentInfo[] arguments, PMap properties) {
        return create(new ArgumentInfo[0], arguments, null, Type.ReadOnlyProperty, properties);
    }

    private static ControlInfo create(ArgumentInfo[] inputs,
            ArgumentInfo[] outputs,
            Argument[] defaults,
            Type type,
            PMap properties) {

        ArgumentInfo[] ins = Arrays.copyOf(inputs, inputs.length);
        ArgumentInfo[] outs;
        if (outputs == inputs) {
            // property - make same as inputs
            outs = ins;
        } else {
            outs = Arrays.copyOf(outputs, outputs.length);
        }
        if (defaults != null) {
            defaults = defaults.clone();
        }
        if (properties == null) {
            properties = PMap.EMPTY;
        }

        return new ControlInfo(ins, outs, defaults, type, properties);


    }

    public static ControlInfo coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof ControlInfo) {
            return (ControlInfo) arg;
        }
        throw new ArgumentFormatException();
    }
}
