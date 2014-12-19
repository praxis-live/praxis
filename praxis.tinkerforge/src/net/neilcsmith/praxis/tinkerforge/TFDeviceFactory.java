/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2014 Neil C Smith.
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
package net.neilcsmith.praxis.tinkerforge;

import com.tinkerforge.BrickDC;
import com.tinkerforge.BrickIMU;
import com.tinkerforge.BrickMaster;
import com.tinkerforge.BrickServo;
import com.tinkerforge.BrickStepper;
import com.tinkerforge.BrickletAmbientLight;
import com.tinkerforge.BrickletAnalogIn;
import com.tinkerforge.BrickletAnalogOut;
import com.tinkerforge.BrickletBarometer;
import com.tinkerforge.BrickletColor;
import com.tinkerforge.BrickletCurrent12;
import com.tinkerforge.BrickletCurrent25;
import com.tinkerforge.BrickletDistanceIR;
import com.tinkerforge.BrickletDistanceUS;
import com.tinkerforge.BrickletDualButton;
import com.tinkerforge.BrickletDualRelay;
import com.tinkerforge.BrickletGPS;
import com.tinkerforge.BrickletHallEffect;
import com.tinkerforge.BrickletHumidity;
import com.tinkerforge.BrickletIO16;
import com.tinkerforge.BrickletIO4;
import com.tinkerforge.BrickletIndustrialDigitalIn4;
import com.tinkerforge.BrickletIndustrialDigitalOut4;
import com.tinkerforge.BrickletIndustrialDual020mA;
import com.tinkerforge.BrickletIndustrialQuadRelay;
import com.tinkerforge.BrickletJoystick;
import com.tinkerforge.BrickletLCD16x2;
import com.tinkerforge.BrickletLCD20x4;
import com.tinkerforge.BrickletLEDStrip;
import com.tinkerforge.BrickletLine;
import com.tinkerforge.BrickletLinearPoti;
import com.tinkerforge.BrickletMoisture;
import com.tinkerforge.BrickletMotionDetector;
import com.tinkerforge.BrickletMultiTouch;
import com.tinkerforge.BrickletNFCRFID;
import com.tinkerforge.BrickletPTC;
import com.tinkerforge.BrickletPiezoBuzzer;
import com.tinkerforge.BrickletPiezoSpeaker;
import com.tinkerforge.BrickletRemoteSwitch;
import com.tinkerforge.BrickletRotaryEncoder;
import com.tinkerforge.BrickletRotaryPoti;
import com.tinkerforge.BrickletSegmentDisplay4x7;
import com.tinkerforge.BrickletSolidStateRelay;
import com.tinkerforge.BrickletSoundIntensity;
import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.BrickletTemperatureIR;
import com.tinkerforge.BrickletTilt;
import com.tinkerforge.BrickletVoltage;
import com.tinkerforge.BrickletVoltageCurrent;
import com.tinkerforge.Device;
import com.tinkerforge.IPConnection;
import java.lang.reflect.Constructor;

/**
 *
 * @author Neil C Smith
 */
class TFDeviceFactory {

    private final static TFDeviceFactory INSTANCE = new TFDeviceFactory();

    private TFDeviceFactory() {
    }

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
            case BrickletAnalogIn.DEVICE_IDENTIFIER:
                return BrickletAnalogIn.class;
            case BrickletAnalogOut.DEVICE_IDENTIFIER:
                return BrickletAnalogOut.class;
            case BrickletBarometer.DEVICE_IDENTIFIER:
                return BrickletBarometer.class;
            case BrickletColor.DEVICE_IDENTIFIER:
                return BrickletColor.class;
            case BrickletCurrent12.DEVICE_IDENTIFIER:
                return BrickletCurrent12.class;
            case BrickletCurrent25.DEVICE_IDENTIFIER:
                return BrickletCurrent25.class;
            case BrickletDistanceIR.DEVICE_IDENTIFIER:
                return BrickletDistanceIR.class;
            case BrickletDistanceUS.DEVICE_IDENTIFIER:
                return BrickletDistanceUS.class;
            case BrickletDualButton.DEVICE_IDENTIFIER:
                return BrickletDualButton.class;
            case BrickletDualRelay.DEVICE_IDENTIFIER:
                return BrickletDualRelay.class;
            case BrickletGPS.DEVICE_IDENTIFIER:
                return BrickletGPS.class;
            case BrickletHallEffect.DEVICE_IDENTIFIER:
                return BrickletHallEffect.class;
            case BrickletHumidity.DEVICE_IDENTIFIER:
                return BrickletHumidity.class;
            case BrickletIndustrialDigitalIn4.DEVICE_IDENTIFIER:
                return BrickletIndustrialDigitalIn4.class;
            case BrickletIndustrialDigitalOut4.DEVICE_IDENTIFIER:
                return BrickletIndustrialDigitalOut4.class;
            case BrickletIndustrialDual020mA.DEVICE_IDENTIFIER:
                return BrickletIndustrialDual020mA.class;
            case BrickletIndustrialQuadRelay.DEVICE_IDENTIFIER:
                return BrickletIndustrialQuadRelay.class;
            case BrickletIO16.DEVICE_IDENTIFIER:
                return BrickletIO16.class;
            case BrickletIO4.DEVICE_IDENTIFIER:
                return BrickletIO4.class;
            case BrickletJoystick.DEVICE_IDENTIFIER:
                return BrickletJoystick.class;
            case BrickletLCD16x2.DEVICE_IDENTIFIER:
                return BrickletLCD16x2.class;
            case BrickletLCD20x4.DEVICE_IDENTIFIER:
                return BrickletLCD20x4.class;
            case BrickletLEDStrip.DEVICE_IDENTIFIER:
                return BrickletLEDStrip.class;
            case BrickletLine.DEVICE_IDENTIFIER:
                return BrickletLine.class;
            case BrickletLinearPoti.DEVICE_IDENTIFIER:
                return BrickletLinearPoti.class;
            case BrickletMoisture.DEVICE_IDENTIFIER:
                return BrickletMoisture.class;
            case BrickletMotionDetector.DEVICE_IDENTIFIER:
                return BrickletMotionDetector.class;
            case BrickletMultiTouch.DEVICE_IDENTIFIER:
                return BrickletMultiTouch.class;
            case BrickletNFCRFID.DEVICE_IDENTIFIER:
                return BrickletNFCRFID.class;
            case BrickletPiezoBuzzer.DEVICE_IDENTIFIER:
                return BrickletPiezoBuzzer.class;
            case BrickletPiezoSpeaker.DEVICE_IDENTIFIER:
                return BrickletPiezoSpeaker.class;
            case BrickletPTC.DEVICE_IDENTIFIER:
                return BrickletPTC.class;
            case BrickletRemoteSwitch.DEVICE_IDENTIFIER:
                return BrickletRemoteSwitch.class;
            case BrickletRotaryEncoder.DEVICE_IDENTIFIER:
                return BrickletRotaryEncoder.class;
            case BrickletRotaryPoti.DEVICE_IDENTIFIER:
                return BrickletRotaryPoti.class;
            case BrickletSegmentDisplay4x7.DEVICE_IDENTIFIER:
                return BrickletSegmentDisplay4x7.class;
            case BrickletSolidStateRelay.DEVICE_IDENTIFIER:
                return BrickletSolidStateRelay.class;
            case BrickletSoundIntensity.DEVICE_IDENTIFIER:
                return BrickletSoundIntensity.class;
            case BrickletTemperature.DEVICE_IDENTIFIER:
                return BrickletTemperature.class;
            case BrickletTemperatureIR.DEVICE_IDENTIFIER:
                return BrickletTemperatureIR.class;
            case BrickletTilt.DEVICE_IDENTIFIER:
                return BrickletTilt.class;
            case BrickletVoltage.DEVICE_IDENTIFIER:
                return BrickletVoltage.class;
            case BrickletVoltageCurrent.DEVICE_IDENTIFIER:
                return BrickletVoltageCurrent.class;

            // Bricks
            case BrickDC.DEVICE_IDENTIFIER:
                return BrickDC.class;
            case BrickIMU.DEVICE_IDENTIFIER:
                return BrickIMU.class;
            case BrickMaster.DEVICE_IDENTIFIER:
                return BrickMaster.class;
            case BrickServo.DEVICE_IDENTIFIER:
                return BrickServo.class;
            case BrickStepper.DEVICE_IDENTIFIER:
                return BrickStepper.class;

        }
        return null;
    }

    static TFDeviceFactory getDefault() {
        return INSTANCE;
    }
}
