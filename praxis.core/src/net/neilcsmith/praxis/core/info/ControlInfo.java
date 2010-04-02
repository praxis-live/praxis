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

import net.neilcsmith.praxis.core.Argument;
import net.neilcsmith.praxis.core.ArgumentFormatException;
import net.neilcsmith.praxis.core.types.PArray;
import net.neilcsmith.praxis.core.types.PMap;
import net.neilcsmith.praxis.core.types.PNumber;
import net.neilcsmith.praxis.core.types.PBoolean;
import net.neilcsmith.praxis.core.types.PString;

/**
 *
 * @author Neil C Smith
 * @TODO handle possible null pointers in returns
 */
public class ControlInfo extends Argument {

    public static final PString IS_PROPERTY_KEY = PString.valueOf("is_property");
    public static final PString INPUTS_REQUIRED_KEY = PString.valueOf("inputs_required");
    public static final PString INPUTS_VARIABLE_KEY = PString.valueOf("inputs_variable");
    public static final PString OUTPUTS_ASSURED_KEY = PString.valueOf("outputs_assured");
    public static final PString OUTPUTS_VARIABLE_KEY = PString.valueOf("outputs_variable");
    public static final PString INPUTS_INFO_KEY = PString.valueOf("inputs_info");
    public static final PString OUTPUTS_INFO_KEY = PString.valueOf("outputs_info");
    public static final PString DEFAULTS_KEY = PString.valueOf("defaults");
    public final static PString PROPERTIES_KEY = PString.valueOf("properties");

    private PMap data;

    private ControlInfo(PMap data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data.toString();
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ControlInfo) {
            ControlInfo o = (ControlInfo) obj;
            return data.equals(o.data);
        }
        return false;
    }

    public boolean isProperty() {
        try {
            return PBoolean.coerce(data.get(IS_PROPERTY_KEY)).value();
        } catch (Exception ex) {
            return false;
        }
    }

    public int getRequiredInputsCount() {
        try {
            return PNumber.coerce(data.get(INPUTS_REQUIRED_KEY)).toIntValue();
        } catch (Exception ex) {
            return 0;
        }
    }

    public int getVariableInputsCount() {
        try {
            return PNumber.coerce(data.get(INPUTS_VARIABLE_KEY)).toIntValue();
        } catch (Exception ex) {
            return 0;
        }
    }

    public int getAssuredOutputsCount() {
        try {
            return PNumber.coerce(data.get(OUTPUTS_ASSURED_KEY)).toIntValue();
        } catch (Exception ex) {
            return 0;
        }
    }

    public int getVariableOutputsCount() {
        try {
            return PNumber.coerce(data.get(OUTPUTS_VARIABLE_KEY)).toIntValue();
        } catch (Exception ex) {
            return 0;
        }
    }

    public PMap getProperties() {
        Argument arg = data.get(PROPERTIES_KEY);
        if (arg == null || arg.isEmpty()) {
            return PMap.EMPTY;
        } else {
            try {
                return PMap.coerce(arg);
            } catch (Exception ex) {
                return PMap.EMPTY;
            }
        }

    }

    public Argument[] getDefaults() {
        Argument arg = data.get(DEFAULTS_KEY);
        if (arg == null || arg.isEmpty()) {
            return new Argument[0];
        } else {
            try {
                PArray a = PArray.coerce(arg);
                Argument[] def = new Argument[a.getSize()];
                for (int i = 0; i < def.length; i++) {
                    def[i] = a.get(i);
                }
                return def;
            } catch (Exception ex) {
                return new Argument[0];
            }
        }
    }

    public ArgumentInfo[] getInputsInfo() {
        Argument arg = data.get(INPUTS_INFO_KEY);
        if (arg == null || arg.isEmpty()) {
            return new ArgumentInfo[0];
        } else {
            try {
                PArray a = PArray.coerce(arg);
                ArgumentInfo[] infos = new ArgumentInfo[a.getSize()];
                for (int i = 0; i < a.getSize(); i++) {
                    infos[i] = ArgumentInfo.coerce(a.get(i));
                }
                return infos;
            } catch (Exception ex) {
                return new ArgumentInfo[0];
            }
        }
    }

    public ArgumentInfo[] getOutputsInfo() {
        Argument arg = data.get(OUTPUTS_INFO_KEY);
        if (arg == null || arg.isEmpty()) {
            return new ArgumentInfo[0];
        } else {
            try {
                PArray a = PArray.coerce(arg);
                ArgumentInfo[] infos = new ArgumentInfo[a.getSize()];
                for (int i = 0; i < a.getSize(); i++) {
                    infos[i] = ArgumentInfo.coerce(a.get(i));
                }
                return infos;
            } catch (Exception ex) {
                return new ArgumentInfo[0];
            }
        }
    }

    public static ControlInfo create(ArgumentInfo[] inputs,
            ArgumentInfo[] outputs, PMap properties) {
        int inputCount = inputs.length;
        int outputCount = outputs.length;
        return create(inputs, outputs, null, false,
                inputCount, 0, outputCount, 0, properties);
    }

    public static ControlInfo create(ArgumentInfo[] inputs,
            ArgumentInfo[] outputs,
            int inputsRequired, int inputsVariable,
            int outputsAssured, int outputsVariable,
            PMap properties) {
        int inputCount = inputs.length;
        int outputCount = outputs.length;
        if (inputsRequired < 0 || inputsRequired > inputCount ||
                outputsAssured < 0 || outputsAssured > outputCount) {
            throw new IllegalArgumentException("Required / Assured count must be between 0 and number of arguments");
        }
        if (inputsVariable < 0 || inputsVariable > inputCount ||
                outputsVariable < 0 || outputsVariable > outputCount) {
            throw new IllegalArgumentException("Variable count must be between 0 and number of arguments");
        }

        return create(inputs, outputs, null, false,
                inputsRequired, inputsVariable,
                outputsAssured, outputsVariable,
                properties);

    }

    public static ControlInfo createPropertyInfo(ArgumentInfo[] arguments, Argument[] defaults,
            int assured, int variable, PMap properties) {
        int argCount = arguments.length;
        if (argCount < 1) {
            throw new IllegalArgumentException("Property controls must have at least one argument");
        }
        if (defaults == null) {
            throw new NullPointerException("Property controls must have defaults");
        }
        if (assured < 1 || assured > argCount) {
            throw new IllegalArgumentException("Assured count must be between 1 and number of arguments");
        }
        if (variable < 0 || variable > argCount) {
            throw new IllegalArgumentException("Variable count must be between 0 and number of arguments");
        }

        return create(arguments, arguments, defaults, true, 0, variable, assured, variable, properties);
    }

    public static ControlInfo createPropertyInfo(ArgumentInfo[] arguments, Argument[] defaults, PMap properties) {
        return createPropertyInfo(arguments, defaults, arguments.length, 0, properties);
    }

    private static ControlInfo create(ArgumentInfo[] inputs,
            ArgumentInfo[] outputs,
            Argument[] defaults,
            boolean is_property,
            int inputsRequired, int inputsVariable,
            int outputsAssured, int outputsVariable,
            PMap properties) {

        PArray ins = PArray.valueOf(inputs);
        PArray outs;
        if (outputs == inputs) {
            // property - make same as inputs
            outs = ins;
        } else {
            outs = PArray.valueOf(outputs);
        }
        PNumber in_req = PNumber.valueOf(inputsRequired);
        PNumber in_var = PNumber.valueOf(inputsVariable);
        PNumber out_ass = PNumber.valueOf(outputsAssured);
        PNumber out_var = PNumber.valueOf(outputsVariable);
        PBoolean is_prop = PBoolean.valueOf(is_property);
        PArray defs = null;
        if (defaults == null) {
            defs = PArray.EMPTY;
        } else {
            defs = PArray.valueOf(defaults);
        }
        if (properties == null) {
            properties = PMap.EMPTY;
        }

        PMap data = PMap.valueOf(INPUTS_INFO_KEY, ins,
                INPUTS_REQUIRED_KEY, in_req,
                INPUTS_VARIABLE_KEY, in_var,
                OUTPUTS_INFO_KEY, outs,
                OUTPUTS_ASSURED_KEY, out_ass,
                OUTPUTS_VARIABLE_KEY, out_var,
                IS_PROPERTY_KEY, is_prop,
                DEFAULTS_KEY, defs,
                PROPERTIES_KEY, properties);


        return new ControlInfo(data);
    }

    public static ControlInfo coerce(Argument arg) throws ArgumentFormatException {
        if (arg instanceof ControlInfo) {
            return (ControlInfo) arg;
        }
        throw new ArgumentFormatException();
    }
}
