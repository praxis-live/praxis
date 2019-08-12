/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2019 Neil C Smith.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */
package org.praxislive.core;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.praxislive.core.types.PArray;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PString;

/**
 *
 * @author Neil C Smith
 */
public class ControlInfo extends Value {

    public final static String KEY_TRANSIENT = "transient";
    public final static String KEY_DEPRECATED = "deprecated";
    public final static String KEY_EXPERT = "expert";
//    public final static String KEY_DUPLICATES = "duplicates";

    public static enum Type {

        Function, Action, Property, ReadOnlyProperty
    };

    private final static ArgumentInfo[] EMPTY_INFO = new ArgumentInfo[0];
    private final static Value[] EMPTY_DEFAULTS = new Value[0];

    private final ArgumentInfo[] inputs;
    private final ArgumentInfo[] outputs;
    private final Value[] defaults;
    private final PMap properties;
    private final Type type;

    private volatile String string;

    ControlInfo(ArgumentInfo[] inputs,
            ArgumentInfo[] outputs,
            Value[] defaults,
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
                return PArray.of(
                        PString.of(type),
                        properties)
                        .toString();
            case Function:
                return PArray.of(PString.of(type),
                        PArray.of(inputs),
                        PArray.of(outputs),
                        properties)
                        .toString();
            default:
                return PArray.of(PString.of(type),
                        PArray.of(outputs),
                        PArray.of(defaults),
                        properties)
                        .toString();
                
        }
        
    }

//    @Override
//    public boolean isEquivalent(Value arg) {
//        return equals(arg);
//    }
 
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ControlInfo) {
            ControlInfo o = (ControlInfo) obj;
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

    public Type controlType() {
        return type;
    }
    
    @Deprecated
    public Type getType() {
        return type;
    }

    public PMap properties() {
        return properties;
    }
    
    @Deprecated
    public PMap getProperties() {
        return properties;
    }
    
    public List<Value> defaults() {
        return Arrays.asList(defaults.clone());
    }

    @Deprecated
    public Value[] getDefaults() {
        return defaults.clone();
    }
    
    public List<ArgumentInfo> inputs() {
        return Arrays.asList(inputs.clone());
    }

    @Deprecated
    public ArgumentInfo[] getInputsInfo() {
        return inputs.clone();
    }

    public List<ArgumentInfo> outputs() {
        return Arrays.asList(outputs.clone());
    }
    
    @Deprecated
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

    public static ControlInfo createPropertyInfo(ArgumentInfo[] arguments, Value[] defaults, PMap properties) {
        return create(arguments, arguments, defaults, Type.Property, properties);
    }

    public static ControlInfo createReadOnlyPropertyInfo(ArgumentInfo[] arguments, PMap properties) {
        return create(EMPTY_INFO, arguments, null, Type.ReadOnlyProperty, properties);
    }

    private static ControlInfo create(ArgumentInfo[] inputs,
            ArgumentInfo[] outputs,
            Value[] defaults,
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

    @Deprecated
    public static ControlInfo coerce(Value arg) throws ValueFormatException {
        if (arg instanceof ControlInfo) {
            return (ControlInfo) arg;
        } else {
            return parse(arg.toString());
        }
    }

    public static Optional<ControlInfo> from(Value arg) {
        try {
            return Optional.of(coerce(arg));
        } catch (ValueFormatException ex) {
            return Optional.empty();
        }
    }
    
    public static ControlInfo parse(String string) throws ValueFormatException {
        try {
            PArray arr = PArray.parse(string);
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
            throw new ValueFormatException(ex);
        }
        
    }
    
    private static ControlInfo parseFunction(String string, PArray array) throws Exception {
        // array(1) is inputs
        PArray args = PArray.coerce(array.get(1));
        ArgumentInfo[] inputs = new ArgumentInfo[args.size()];
        for (int i=0; i<inputs.length; i++) {
            inputs[i] = ArgumentInfo.coerce(args.get(i));
        }
        // array(2) is outputs
        args = PArray.coerce(array.get(2));
        ArgumentInfo[] outputs = new ArgumentInfo[args.size()];
        for (int i=0; i<outputs.length; i++) {
            outputs[i] = ArgumentInfo.coerce(args.get(i));
        }
        // optional array(3) is properties
        PMap properties;
        if (array.size() > 3) {
            properties = PMap.coerce(array.get(3));
        } else {
            properties = PMap.EMPTY;
        }
        return new ControlInfo(inputs, outputs, EMPTY_DEFAULTS, Type.Function, properties, string);
    }
    
    private static ControlInfo parseAction(String string, PArray array) throws Exception {
        // optional array(1) is properties
        PMap properties;
        if (array.size() > 1) {
            properties = PMap.coerce(array.get(1));
        } else {
            properties = PMap.EMPTY;
        }
        return new ControlInfo(EMPTY_INFO, EMPTY_INFO, EMPTY_DEFAULTS, Type.Action, properties, string);
    }
    
    private static ControlInfo parseProperty(String string, Type type, PArray array) throws Exception {
        // array(1) is outputs
        PArray args = PArray.coerce(array.get(1));
        ArgumentInfo[] outputs = new ArgumentInfo[args.size()];
        for (int i=0; i<outputs.length; i++) {
            outputs[i] = ArgumentInfo.coerce(args.get(i));
        }
        ArgumentInfo[] inputs = type == Type.ReadOnlyProperty ?
                EMPTY_INFO : outputs;
        // array(2) is defaults
        args = PArray.coerce(array.get(2));
        Value[] defs = new Value[args.size()];
        for (int i=0; i<defs.length; i++) {
            defs[i] = PString.coerce(args.get(i));
        }
        // optional array(3) is properties
        PMap properties;
        if (array.size() > 3) {
            properties = PMap.coerce(array.get(3));
        } else {
            properties = PMap.EMPTY;
        }
        return new ControlInfo(inputs, outputs, defs, type, properties, string);
    }

}
