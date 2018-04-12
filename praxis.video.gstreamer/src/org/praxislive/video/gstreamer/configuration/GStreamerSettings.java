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
package org.praxislive.video.gstreamer.configuration;

import com.sun.jna.Platform;
import org.praxislive.settings.Settings;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class GStreamerSettings {

    private final static String KEY_CAPTURE_PREFIX = "video.gstreamer.capture";
    private final static String KEY_LIBRARY_PATH = "video.gstreamer.path";

    private final static String DEFAULT_CAPTURE_PREFIX;
    private final static String DEFAULT_LIBRARY_PATH;

    static {
        if (Platform.isWindows()) {
            DEFAULT_CAPTURE_PREFIX = "ksvideosrc device-index=";
            if (Platform.is64Bit()) {
                DEFAULT_LIBRARY_PATH = "C:\\gstreamer\\1.0\\x86_64\\bin\\";
            } else {
                DEFAULT_LIBRARY_PATH = "C:\\gstreamer\\1.0\\x86\\bin\\";
            }
        } else if (Platform.isMac()) {
            DEFAULT_CAPTURE_PREFIX = "avfvideosrc device-index=";
            DEFAULT_LIBRARY_PATH = "/Library/Frameworks/GStreamer.framework/Libraries/";
        } else {
            DEFAULT_CAPTURE_PREFIX = "v4l2src device=/dev/video";
            DEFAULT_LIBRARY_PATH = "";
        }
    }

    public static String getDefaultCaptureDevice(int idx) {
        if (idx < 1) {
            throw new IllegalArgumentException();
        }
        return DEFAULT_CAPTURE_PREFIX + (idx - 1);
    }

    public static void resetCaptureDevice(int idx) {
        setCaptureDevice(idx, null);
    }

    public static String getCaptureDevice(int idx) {
        if (idx < 1) {
            throw new IllegalArgumentException();
        }
        return Settings.get(KEY_CAPTURE_PREFIX + idx, getDefaultCaptureDevice(idx));
    }

    public static void setCaptureDevice(int idx, String device) {
        if (idx < 1) {
            throw new IllegalArgumentException();
        }
        Settings.put(KEY_CAPTURE_PREFIX + idx, device);
    }
    
    public static String getDefaultLibraryPath() {
        return DEFAULT_LIBRARY_PATH;
    }

    public static void resetLibraryPath() {
        setLibraryPath(null);
    }

    public static String getLibraryPath() {
        return Settings.get(KEY_LIBRARY_PATH, getDefaultLibraryPath());
    }

    public static void setLibraryPath(String libPath) {
        Settings.put(KEY_LIBRARY_PATH, libPath);
    }

}
