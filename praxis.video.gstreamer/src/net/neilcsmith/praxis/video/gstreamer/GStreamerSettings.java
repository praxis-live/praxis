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
import net.neilcsmith.praxis.video.VideoSettings;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
@Deprecated
public class GStreamerSettings {

//    private final static String KEY_CAPTURE_PREFIX = "video.capture";
//    private final static String DEFAULT_CAPTURE_PREFIX;

//    static {
//        String os = System.getProperty("os.name", "");
//        if (os.contains("Windows")) {
//            DEFAULT_CAPTURE_PREFIX = "ksvideosrc device-index=";
//        } else if (os.contains("Mac") || os.contains("Darwin")) {
//            DEFAULT_CAPTURE_PREFIX = "qtkitvideosrc device-index=";
//        } else {
//            DEFAULT_CAPTURE_PREFIX = "v4l2src device=/dev/video";
//        }
//
//    }

    private GStreamerSettings() {
    }
    
    public static String getDefaultCaptureDevice(int idx) {
        return VideoSettings.getDefaultCaptureDevice(idx);
    }

    public static void resetCaptureDevice(int idx) {
        VideoSettings.resetCaptureDevice(idx);
    }
     
    public static String getCaptureDevice(int idx) {
        return VideoSettings.getCaptureDevice(idx);
    }

    public static void setCaptureDevice(int idx, String device) {
        VideoSettings.setCaptureDevice(idx, device);
    }

}
