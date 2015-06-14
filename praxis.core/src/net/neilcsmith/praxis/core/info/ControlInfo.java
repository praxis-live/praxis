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
 */
package net.neilcsmith.praxis.core.info;

import java.util.Arrays;
import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PString;

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

        Function, Action, Property, ReadOnlyProperty
    };

    private final static ArgumentInfo[] EMPTY_INFO = new ArgumentInfo[0];
    private final static Argument[] EMPTY_DEFAULTS = new Argument[0];

    private final ArgumentInfo[] inputs;
    private final ArgumentInfo[] outputs;
    private final Argument[] defaults;
    private final PMap properties;
    private final Type type;

    private volatile String string;

    private ControlInfo(ArgumentInfo[] inputs,
            ArgumentInfo[] outputs,
            Argument[] defaults,
            Type type,
            PMap properties,
            String string
    ) {

        this.inputs = inputs;
        this.outputs = outputs;
        this.defaults = defaults;
        this.type = type;
        this.properties = properties;
        this.string = string;
    }

    @Override
    public String toString() {
        String str = string;
        if (str == null) {
            str = buildString();
            string = str;
        }
        return str;
    }

    private String buildString() {
        
        switch (type) {
            case Action:
                return PArray.valueOf(
                        PString.valueOf(type),
                        properties)
                        .toString();
            case Function:
                return PArray.valueOf(
                        PString.valueOf(type),
                        PArray.valueOf(inputs),
                        PArray.valueOf(outputs),
                        properties)
                        .toString();
            default:
                return PArray.valueOf(
                        PString.valueOf(type),
                        PArray.valueOf(outputs),
                        PArray.valueOf(defaults),
                        properties)
                        .toString();
                
        }
        
    }

//    @Override
//    public boolean isEquivalent(Argument arg) {
//        return equals(arg);
//    }
 
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
        return defaults.clone();
    }

    public ArgumentInfo[] getInputsInfo() {
        return inputs.clone();
    }

    public ArgumentInfo[] getOutputsInfo() {
        return outputs.clone();
    }

    public static ControlInfo createFunctionInfo(ArgumentInfo[] inputs,
            ArgumentInfo[] outputs, PMap properties) {
        return create(inputs, outputs, null, Type.Function, properties);
    }

    @Deprecated
    public static ControlInfo createTriggerInfo(PMap properties) {
        return create(EMPTY_INFO, EMPTY_INFO, null, Type.Action, properties);
    }

    public static ControlInfo createActionInfo(PMap properties) {
        return create(EMPTY_INFO, EMPTY_INFO, null, Type.Action, properties);
    }

    public static ControlInfo createPropertyInfo(ArgumentInfo[] arguments, Argument[] defaults, PMap properties) {
        return create(arguments, arguments, defaults, Type.Property, properties);
    }

    public static ControlInfo createReadOnlyPropertyInfo(ArgumentInfo[] arguments, PMap properties) {
        return create(EMPTY_INFO, arguments, null, Type.ReadOnlyProperty, properties);
    }

    private static ControlInfo create(ArgumentInfo[] inputs,
            ArgumentInfo[] outputs,
            Argument[] defaults,
            Type type,
            PMap properties) {

        ArgumentInfo[] ins = inputs.length == 0 ? EMPTY_INFO : inputs.clone();
        ArgumentInfo[] outs;
        if (outputs == inputs) {
            // property - make same as inputs
            outs = ins;
        } else {
            outs = outputs.length == 0 ? EMPTY_INFO : outputs.clone();
        }
        if (defaults != null) {
            defaults = defaults.clone();
        } else {
            defaults = EMPTY_DEFAULTS;
        }
        if (properties == null) {
            properties = PMap.EMPTY;
        }

        return new ControlInfo(ins, outs, defaults, type, properties, null);

    }

    public static ControlInfo coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof ControlInfo) {
            return (ControlInfo) arg;
        } else {
            return valueOf(arg.toString());
        }
    }

    private static ControlInfo valueOf(String string) throws ArgumentFormatException {
        try {
            PArray arr = PArray.valueOf(string);
            Type type = Type.valueOf(arr.get(0).toString());
            switch (type) {
                case Function :
                    return parseFunction(string, arr);
                case Action :
                    return parseAction(string, arr);
                default : 
                    return parseProperty(string, type, arr);
            }
        } catch (Exception ex) {
            throw new ArgumentFormatException(ex);
        }
        
    }
    
    private static ControlInfo parseFunction(String string, PArray array) throws Exception {
        // array(1) is inputs
        PArray args = PArray.coerce(array.get(1));
        ArgumentInfo[] inputs = new ArgumentInfo[args.getSize()];
        for (int i=0; i<inputs.length; i++) {
            inputs[i] = ArgumentInfo.coerce(args.get(i));
        }
        // array(2) is outputs
        args = PArray.coerce(array.get(2));
        ArgumentInfo[] outputs = new ArgumentInfo[args.getSize()];
        for (int i=0; i<outputs.length; i++) {
            outputs[i] = ArgumentInfo.coerce(args.get(i));
        }
        // optional array(3) is properties
        PMap properties;
        if (array.getSize() > 3) {
            properties = PMap.coerce(array.get(3));
        } else {
            properties = PMap.EMPTY;
        }
        return new ControlInfo(inputs, outputs, EMPTY_DEFAULTS, Type.Function, properties, string);
    }
    
    private static ControlInfo parseAction(String string, PArray array) throws Exception {
        // optional array(1) is properties
        PMap properties;
        if (array.getSize() > 1) {
            properties = PMap.coerce(array.get(1));
        } else {
            properties = PMap.EMPTY;
        }
        return new ControlInfo(EMPTY_INFO, EMPTY_INFO, EMPTY_DEFAULTS, Type.Action, properties, string);
    }
    
    private static ControlInfo parseProperty(String string, Type type, PArray array) throws Exception {
        // array(1) is outputs
        PArray args = PArray.coerce(array.get(1));
        ArgumentInfo[] outputs = new ArgumentInfo[args.getSize()];
        for (int i=0; i<outputs.length; i++) {
            outputs[i] = ArgumentInfo.coerce(args.get(i));
        }
        ArgumentInfo[] inputs = type == Type.ReadOnlyProperty ?
                EMPTY_INFO : outputs;
        // array(2) is defaults
        args = PArray.coerce(array.get(2));
        Argument[] defs = new Argument[args.getSize()];
        for (int i=0; i<defs.length; i++) {
            defs[i] = PString.coerce(args.get(i));
        }
        // optional array(3) is properties
        PMap properties;
        if (array.getSize() > 3) {
            properties = PMap.coerce(array.get(3));
        } else {
            properties = PMap.EMPTY;
        }
        return new ControlInfo(inputs, outputs, defs, type, properties, string);
    }

}
