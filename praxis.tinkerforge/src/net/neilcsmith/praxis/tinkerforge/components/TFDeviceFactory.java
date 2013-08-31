/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2013 Neil C Smith.
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
package net.neilcsmith.praxis.tinkerforge.components;

import com.tinkerforge.BrickMaster;
import com.tinkerforge.BrickServo;
import com.tinkerforge.BrickletAmbientLight;
import com.tinkerforge.BrickletDistanceIR;
import com.tinkerforge.BrickletGPS;
import com.tinkerforge.BrickletIO16;
import com.tinkerforge.BrickletJoystick;
import com.tinkerforge.BrickletLCD20x4;
import com.tinkerforge.BrickletLinearPoti;
import com.tinkerforge.BrickletRotaryPoti;
import com.tinkerforge.Device;
import com.tinkerforge.IPConnection;
import java.lang.reflect.Constructor;

/**
 *
 * @author Neil C Smith
 */
class TFDeviceFactory {

    private final static TFDeviceFactory INSTANCE = new TFDeviceFactory();
    
    private TFDeviceFactory(){}

    Device createDevice(int deviceID, String uid, IPConnection ipcon) throws Exception {
        Class<? extends Device> cls = getDeviceClass(deviceID);
        if (cls == null) {
            throw new IllegalArgumentException("Unknown Device");
        }
        Constructor<? extends Device> con = cls.getConstructor(String.class, IPConnection.class);
        return con.newInstance(uid, ipcon);
    }

    Class<? extends Device> getDeviceClass(int deviceID) {
        switch (deviceID) {
            // Bricklets
            case BrickletAmbientLight.DEVICE_IDENTIFIER:
                return BrickletAmbientLight.class;
            case BrickletDistanceIR.DEVICE_IDENTIFIER:
                return BrickletDistanceIR.class;
            case BrickletIO16.DEVICE_IDENTIFIER:
                return BrickletIO16.class;
            case BrickletJoystick.DEVICE_IDENTIFIER:
                return BrickletJoystick.class;
            case BrickletLCD20x4.DEVICE_IDENTIFIER:
                return BrickletLCD20x4.class;
            case BrickletGPS.DEVICE_IDENTIFIER:
                return BrickletGPS.class;
            case BrickletLinearPoti.DEVICE_IDENTIFIER:
                return BrickletLinearPoti.class;
            case BrickletRotaryPoti.DEVICE_IDENTIFIER:
                return BrickletRotaryPoti.class;
                
            // Bricks
            case BrickServo.DEVICE_IDENTIFIER:
                return BrickServo.class;
            case BrickMaster.DEVICE_IDENTIFIER:
                return BrickMaster.class;
        
        }
        return null;
    }

    static TFDeviceFactory getDefault() {
        return INSTANCE;
    }
}
