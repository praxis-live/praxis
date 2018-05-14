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
 *
 */
package org.praxislive.video.gstreamer.components;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.praxislive.video.gstreamer.configuration.GStreamerSettings;
import org.freedesktop.gstreamer.Gst;

/**
 *
 * @author Neil C Smith
 */
class GStreamerLibrary {

    private static final GStreamerLibrary INSTANCE = new GStreamerLibrary();

    private GStreamerLibrary() {
        initLibraryPaths();
    }

    private void initLibraryPaths() {
        String libPath = GStreamerSettings.getLibraryPath().trim();
        if (libPath.isEmpty()) {
            return;
        }
        if (Platform.isWindows()) {
            try {
                Kernel32 k32 = Kernel32.INSTANCE;
                String path = System.getenv("path");
                if (path == null || path.trim().isEmpty()) {
                    k32.SetEnvironmentVariable("path", libPath);
                } else {
                    k32.SetEnvironmentVariable("path", libPath + File.pathSeparator + path);
                }
                return;
            } catch (Throwable e) {
                Logger.getLogger(GStreamerLibrary.class.getName())
                        .log(Level.SEVERE, "Unable to set Windows library path", e);
                // fall through
            }
        }
        String jnaPath = System.getProperty("jna.library.path", "").trim();
        if (jnaPath.isEmpty()) {
            System.setProperty("jna.library.path", libPath);
        } else {
            System.setProperty("jna.library.path", jnaPath + File.pathSeparator + libPath);
        }

    }

    void init() {
        Gst.init();
    }
    
    static GStreamerLibrary getInstance() {
        return INSTANCE;
    }
   
    

}
