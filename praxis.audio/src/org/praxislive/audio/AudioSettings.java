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
 */
package org.praxislive.audio;

import org.praxislive.settings.Settings;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class AudioSettings {
    
    public final static String KEY_LIBRARY = "audio.library";
    public final static String KEY_DEVICE = "audio.device";
    public final static String KEY_BUFFERSIZE = "audio.buffersize";
    public final static String KEY_INPUT_DEVICE = "audio.input-device";
    
    private static final int DEFAULT_BUFFERSIZE = 2048;
    private static final String DEFAULT_LIBRARY = "JavaSound";
    
    private AudioSettings() {}
    
    public static String getLibrary() {
        return Settings.get(KEY_LIBRARY, DEFAULT_LIBRARY);
    }
    
    public static void setLibrary(String library) {
        Settings.put(KEY_LIBRARY, library);
    }
    
    public static int getBuffersize() {
        return Settings.getInt(KEY_BUFFERSIZE, DEFAULT_BUFFERSIZE);
    }
    
    public static void setBuffersize(int buffersize) {
        Settings.putInt(KEY_BUFFERSIZE, buffersize);
    }
    
    public static String getDeviceName() {
        return Settings.get(KEY_DEVICE, "");
    }
    
    public static void setDeviceName(String device) {
        if (device == null || device.trim().isEmpty()) {
            Settings.put(KEY_DEVICE, null);
        } else {
            Settings.put(KEY_DEVICE, device);
        }
    }
    
    public static String getInputDeviceName() {
        return Settings.get(KEY_INPUT_DEVICE, "");
    }
    
    public static void setInputDeviceName(String inputDevice) {
        if (inputDevice == null || inputDevice.trim().isEmpty()) {
            Settings.put(KEY_INPUT_DEVICE, null);
        } else {
            Settings.put(KEY_INPUT_DEVICE, inputDevice);
        }
    }
    
}
