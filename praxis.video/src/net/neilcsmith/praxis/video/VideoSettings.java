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
package net.neilcsmith.praxis.video;

import net.neilcsmith.praxis.settings.Settings;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class VideoSettings {

    public enum FullScreenMode {

        Default, Exclusive, Fake
    }

    private final static String KEY_FULLSCREENMODE = "video.fullscreenmode";
    private final static String KEY_CAPTURE_PREFIX = "video.capture";
    private final static String KEY_MEDIA_LIB = "video.medialib";
    
    private final static String DEFAULT_MEDIA_LIB = "gstreamer-0.10";
    private final static boolean DEFAULT_FSEM;
    private final static String DEFAULT_CAPTURE_PREFIX;
    
    @Deprecated
    public final static String KEY_RENDERER = "video.renderer";
    
    @Deprecated
    private final static String DEFAULT_RENDERER = "Software";
    @Deprecated
    private static final String DEFAULT_CAPTURE_SCHEME;

    static {
        String os = System.getProperty("os.name", "");
        if (os.contains("Windows")) {
            DEFAULT_CAPTURE_PREFIX = "ksvideosrc device-index=";
            DEFAULT_CAPTURE_SCHEME = "ks";
            DEFAULT_FSEM = false;
        } else if (os.contains("Mac") || os.contains("Darwin")) {
            DEFAULT_CAPTURE_PREFIX = "qtkitvideosrc device-index=";
            DEFAULT_CAPTURE_SCHEME = "qtkit";
            DEFAULT_FSEM = true;
        } else {
            DEFAULT_CAPTURE_PREFIX = "v4l2src device=/dev/video";
            DEFAULT_CAPTURE_SCHEME = "v4l2";
            DEFAULT_FSEM = true;
        }

    }

    private VideoSettings() {
    }

    public static void setFullScreenMode(FullScreenMode mode) {
        if (mode == FullScreenMode.Default) {
            Settings.put(KEY_FULLSCREENMODE, null);
        } else {
            Settings.put(KEY_FULLSCREENMODE, mode.name());
        }
    }

    public static FullScreenMode getFullScreenMode() {
        String m = Settings.get(KEY_FULLSCREENMODE);
        if (m != null) {
            try {
                return FullScreenMode.valueOf(m);
            } catch (Exception e) {
                // fall through
            }
        }
        return FullScreenMode.Default;
    }

    public static boolean isFullScreenExclusive() {
        switch (getFullScreenMode()) {
            case Exclusive:
                return true;
            case Fake:
                return false;
            default:
                return DEFAULT_FSEM;
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
    
    public static String getMediaLib() {
        return Settings.get(KEY_MEDIA_LIB, DEFAULT_MEDIA_LIB);
    }
    
    public static void setMediaLib(String lib) {
        Settings.put(KEY_MEDIA_LIB, lib);
    }
    
    public static void resetMediaLib() {
        setMediaLib(null);
    }

    @Deprecated
    public static String getRenderer() {
        return Settings.get(KEY_RENDERER, DEFAULT_RENDERER);
    }

    @Deprecated
    public static void setRenderer(String renderer) {
        Settings.put(KEY_RENDERER, renderer);
    }

}
