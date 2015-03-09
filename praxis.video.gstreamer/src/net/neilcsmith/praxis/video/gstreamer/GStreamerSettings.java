/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2015 Neil C Smith.
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
package net.neilcsmith.praxis.video.gstreamer;

import net.neilcsmith.praxis.settings.Settings;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class GStreamerSettings {

    private final static String KEY_CAPTURE_PREFIX = "video.capture";
    private final static String DEFAULT_CAPTURE_PREFIX;

    static {
        String os = System.getProperty("os.name", "");
        if (os.contains("Windows")) {
            DEFAULT_CAPTURE_PREFIX = "ksvideosrc device=index=";
        } else if (os.contains("Mac") || os.contains("Darwin")) {
            DEFAULT_CAPTURE_PREFIX = "qtkitvideosrc device-index=";
        } else {
            DEFAULT_CAPTURE_PREFIX = "v4l2src device=/dev/video";
        }

    }

    private GStreamerSettings() {
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

}
