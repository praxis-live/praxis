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

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class TFUtils {
    
    public static Class<? extends Device> getDeviceClass(int deviceID) {
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
    
    
    // Maps a normal UTF-16 encoded string to the LCD charset
    // Converted from public domain code at
    // http://www.tinkerforge.com/en/doc/Software/Bricklets/LCD20x4_Bricklet_Java.html#lcd-20x4-bricklet-java-examples
    static String lcdString(String utf16, int requiredLength) {
        StringBuilder ks0066u = new StringBuilder(requiredLength);
        char c;

        for (int i = 0; i < utf16.length(); i++) {
            int codePoint = utf16.codePointAt(i);

            if (Character.isHighSurrogate(utf16.charAt(i))) {
                // Skip low surrogate
                i++;
            }

            // ASCII subset from JIS X 0201
            if (codePoint >= 0x0020 && codePoint <= 0x007e) {
                // The LCD charset doesn't include '\' and '~', use similar characters instead
                switch (codePoint) {
                    case 0x005c:
                        c = (char) 0xa4;
                        break; // REVERSE SOLIDUS maps to IDEOGRAPHIC COMMA
                    case 0x007e:
                        c = (char) 0x2d;
                        break; // TILDE maps to HYPHEN-MINUS
                    default:
                        c = (char) codePoint;
                        break;
                }
            } // Katakana subset from JIS X 0201
            else if (codePoint >= 0xff61 && codePoint <= 0xff9f) {
                c = (char) (codePoint - 0xfec0);
            } // Special characters
            else {
                switch (codePoint) {
                    case 0x00a5:
                        c = (char) 0x5c;
                        break; // YEN SIGN
                    case 0x2192:
                        c = (char) 0x7e;
                        break; // RIGHTWARDS ARROW
                    case 0x2190:
                        c = (char) 0x7f;
                        break; // LEFTWARDS ARROW
                    case 0x00b0:
                        c = (char) 0xdf;
                        break; // DEGREE SIGN maps to KATAKANA SEMI-VOICED SOUND MARK
                    case 0x03b1:
                        c = (char) 0xe0;
                        break; // GREEK SMALL LETTER ALPHA
                    case 0x00c4:
                        c = (char) 0xe1;
                        break; // LATIN CAPITAL LETTER A WITH DIAERESIS
                    case 0x00e4:
                        c = (char) 0xe1;
                        break; // LATIN SMALL LETTER A WITH DIAERESIS
                    case 0x00df:
                        c = (char) 0xe2;
                        break; // LATIN SMALL LETTER SHARP S
                    case 0x03b5:
                        c = (char) 0xe3;
                        break; // GREEK SMALL LETTER EPSILON
                    case 0x00b5:
                        c = (char) 0xe4;
                        break; // MICRO SIGN
                    case 0x03bc:
                        c = (char) 0xe4;
                        break; // GREEK SMALL LETTER MU
                    case 0x03c2:
                        c = (char) 0xe5;
                        break; // GREEK SMALL LETTER FINAL SIGMA
                    case 0x03c1:
                        c = (char) 0xe6;
                        break; // GREEK SMALL LETTER RHO
                    case 0x221a:
                        c = (char) 0xe8;
                        break; // SQUARE ROOT
                    case 0x00b9:
                        c = (char) 0xe9;
                        break; // SUPERSCRIPT ONE maps to SUPERSCRIPT (minus) ONE
                    case 0x00a4:
                        c = (char) 0xeb;
                        break; // CURRENCY SIGN
                    case 0x00a2:
                        c = (char) 0xec;
                        break; // CENT SIGN
                    case 0x2c60:
                        c = (char) 0xed;
                        break; // LATIN CAPITAL LETTER L WITH DOUBLE BAR
                    case 0x00f1:
                        c = (char) 0xee;
                        break; // LATIN SMALL LETTER N WITH TILDE
                    case 0x00d6:
                        c = (char) 0xef;
                        break; // LATIN CAPITAL LETTER O WITH DIAERESIS
                    case 0x00f6:
                        c = (char) 0xef;
                        break; // LATIN SMALL LETTER O WITH DIAERESIS
                    case 0x03f4:
                        c = (char) 0xf2;
                        break; // GREEK CAPITAL THETA SYMBOL
                    case 0x221e:
                        c = (char) 0xf3;
                        break; // INFINITY
                    case 0x03a9:
                        c = (char) 0xf4;
                        break; // GREEK CAPITAL LETTER OMEGA
                    case 0x00dc:
                        c = (char) 0xf5;
                        break; // LATIN CAPITAL LETTER U WITH DIAERESIS
                    case 0x00fc:
                        c = (char) 0xf5;
                        break; // LATIN SMALL LETTER U WITH DIAERESIS
                    case 0x03a3:
                        c = (char) 0xf6;
                        break; // GREEK CAPITAL LETTER SIGMA
                    case 0x03c0:
                        c = (char) 0xf7;
                        break; // GREEK SMALL LETTER PI
                    case 0x0304:
                        c = (char) 0xf8;
                        break; // COMBINING MACRON
                    case 0x00f7:
                        c = (char) 0xfd;
                        break; // DIVISION SIGN

                    default:
                    case 0x25a0:
                        c = (char) 0xff;
                        break; // BLACK SQUARE
                }
            }

            // Special handling for 'x' followed by COMBINING MACRON
            if (c == (char) 0xf8) {
                if (ks0066u.charAt(ks0066u.length() - 1) != 'x') {
                    c = (char) 0xff; // BLACK SQUARE
                }

                if (ks0066u.length() > 0) {
                    ks0066u.deleteCharAt(ks0066u.length() - 1);
                }
            }

            ks0066u.append(c);

            if (ks0066u.length() == requiredLength) {
                break;
            }
        }

        while (ks0066u.length() < requiredLength) {
            ks0066u.append(' ');
        }
        
        return ks0066u.toString();
    }

    
}
