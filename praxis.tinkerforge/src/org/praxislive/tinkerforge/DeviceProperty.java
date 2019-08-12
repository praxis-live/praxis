/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
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
 *
 */
package org.praxislive.tinkerforge;

import org.praxislive.code.AbstractBasicProperty;
import org.praxislive.code.CodeContext;
import org.praxislive.code.ControlDescriptor;
import org.praxislive.core.Value;
import org.praxislive.core.Control;
import org.praxislive.core.ArgumentInfo;
import org.praxislive.core.ControlInfo;
import org.praxislive.core.types.PMap;
import org.praxislive.core.types.PString;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class DeviceProperty extends AbstractBasicProperty {

    private final static ControlInfo INFO = ControlInfo.createPropertyInfo(
            new ArgumentInfo[]{ArgumentInfo.of(PString.class,
                        PMap.of(ArgumentInfo.KEY_SUGGESTED_VALUES,
                                TFCodeContext.AUTO))},
            new Value[]{PString.EMPTY},
            PMap.EMPTY
    );

    private TFCodeContext context;
    
    @Override
    protected void set(long time, Value arg) throws Exception {
        context.setUID(arg.toString());
    }

    @Override
    protected Value get() {
        return PString.of(context.getUID());
    }

//    @Override
    public ControlInfo getInfo() {
        return INFO;
    }
    

    static class Descriptor extends ControlDescriptor {

        private final DeviceProperty control;

        Descriptor(String id, int index) {
            super(id, Category.Property, index);
            control = new DeviceProperty();
        }

        @Override
        public ControlInfo getInfo() {
            return control.getInfo();
        }

        @Override
        public void attach(CodeContext<?> context, Control previous) {
            control.context = (TFCodeContext) context;
        }

        @Override
        public Control getControl() {
            return control;
        }

    }

}
