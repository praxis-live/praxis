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

import com.tinkerforge.Device;
import java.lang.reflect.Field;
import org.praxislive.code.CodeConnector;
import org.praxislive.code.CodeFactory;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class TFCodeConnector extends CodeConnector<TFCodeDelegate>{
    
    private Field deviceField;
    private Class<? extends Device> deviceType;

    public TFCodeConnector(CodeFactory.Task<TFCodeDelegate> task,
            TFCodeDelegate delegate) {
        super(task, delegate);
    }
    
    Field extractDeviceField() {
        return deviceField;
    }
    
    Class<? extends Device> extractDeviceType() {
        return deviceType == null ? Device.class : deviceType;
    }

    @Override
    protected void addDefaultControls() {
        super.addDefaultControls();
        addControl(new DeviceProperty.Descriptor("uid", Integer.MIN_VALUE + 100));
        addControl(new ConnectedProperty.Descriptor("connected", Integer.MIN_VALUE + 101));
    }

    @Override
    protected void analyseField(Field field) {
        if (field.isAnnotationPresent(
                org.praxislive.tinkerforge.userapi.TinkerForge.class)) {
            if (deviceField != null) {
                // log this
                return;
            }
            Class<?> type = field.getType();
            if (Device.class.isAssignableFrom(type)) {
                deviceType = (Class<? extends Device>) type;
                field.setAccessible(true);
                deviceField = field;
            }
            return;
        }
        super.analyseField(field);
    }
    
    
    
}
