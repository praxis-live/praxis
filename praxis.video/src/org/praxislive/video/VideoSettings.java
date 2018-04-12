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
package org.praxislive.video;

import org.praxislive.settings.Settings;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class VideoSettings {

    public enum FullScreenMode {

        Default, Exclusive, Fake
    }

    private final static String KEY_FULLSCREENMODE = "video.fullscreenmode";

    private final static boolean DEFAULT_FSEM;

    static {
        String os = System.getProperty("os.name", "");
        if (os.contains("Windows")) {
            DEFAULT_FSEM = false;
        } else if (os.contains("Mac") || os.contains("Darwin")) {
            DEFAULT_FSEM = true;
        } else {
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

}
