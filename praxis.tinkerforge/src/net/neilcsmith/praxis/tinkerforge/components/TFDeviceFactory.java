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

import com.tinkerforge.BrickletAmbientLight;
import com.tinkerforge.BrickletDistanceIR;
import com.tinkerforge.BrickletLCD20x4;
import com.tinkerforge.BrickletRotaryPoti;
import com.tinkerforge.Device;
import com.tinkerforge.IPConnection;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Neil C Smith
 */
class TFDeviceFactory {

    private final static TFDeviceFactory INSTANCE = new TFDeviceFactory();

    Device createDevice(int deviceID, String uid, IPConnection ipcon) {
        switch (deviceID) {
            case BrickletAmbientLight.DEVICE_IDENTIFIER:
                return new BrickletAmbientLight(uid, ipcon);
            case BrickletDistanceIR.DEVICE_IDENTIFIER:
                return new BrickletDistanceIR(uid, ipcon);
            case BrickletLCD20x4.DEVICE_IDENTIFIER:
                return new BrickletLCD20x4(uid, ipcon);
            case BrickletRotaryPoti.DEVICE_IDENTIFIER:
                return new BrickletRotaryPoti(uid, ipcon);
        }
        throw new IllegalArgumentException("Unknown device");

    }

    String getDeviceName(int deviceID) {
        switch (deviceID) {
            case BrickletAmbientLight.DEVICE_IDENTIFIER:
                return "Ambient Light Bricklet";
            case BrickletDistanceIR.DEVICE_IDENTIFIER:
                return "Distance IR Bricklet";
            case BrickletLCD20x4.DEVICE_IDENTIFIER:
                return "LCD 20x4 Bricklet";
            case BrickletRotaryPoti.DEVICE_IDENTIFIER:
                return "Rotary Poti Bricklet";
        }
        return "Unknown Device";
    }

    static TFDeviceFactory getDefault() {
        return INSTANCE;
    }
}
