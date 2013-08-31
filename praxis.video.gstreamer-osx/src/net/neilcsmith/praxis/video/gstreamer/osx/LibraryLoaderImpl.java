/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 Neil C Smith.
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
 * 
 * 
 * This code is ported from the Processing project - http://processing.org
 *
 * Copyright (c) 2011 Andres Colubri
 * Based on code by Tal Shalif
 * 
 */
package net.neilcsmith.praxis.video.gstreamer.osx;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gstreamer.Gst;
import org.gstreamer.Registry;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
class LibraryLoaderImpl {
    
    private final static Logger LOG = LibraryLoader.LOG;

    private final static LibraryLoaderImpl INSTANCE = new LibraryLoaderImpl();
    private static final Object[][] DEPENDENCIES = {
        // glib libraries
        {"gio-2.0", new String[]{}, true},
        {"glib-2.0", new String[]{}, true},
        {"gmodule-2.0", new String[]{}, true},
        {"gobject-2.0", new String[]{}, true},
        {"gthread-2.0", new String[]{}, true},
        // Core gstreamer libraries  
        {"gstapp-0.10", new String[]{}, true},
        {"gstaudio-0.10", new String[]{}, true},
        {"gstbase-0.10", new String[]{}, true},
        {"gstbasevideo-0.10", new String[]{}, true},
        //{"gstcdda-0.10", new String[]{}, true},
        {"gstcontroller-0.10", new String[]{}, true},
        {"gstdataprotocol-0.10", new String[]{}, true},
        {"gstfft-0.10", new String[]{}, true},
        {"gstinterfaces-0.10", new String[]{}, true},
        //{"gstnet-0.10", new String[]{}, true},
        {"gstnetbuffer-0.10", new String[]{}, true},
        {"gstpbutils-0.10", new String[]{}, true},
        {"gstphotography-0.10", new String[]{}, true},
        {"gstreamer-0.10", new String[]{}, true},
        {"gstriff-0.10", new String[]{}, true},
        {"gstrtp-0.10", new String[]{}, true},
        {"gstrtsp-0.10", new String[]{}, true},
        {"gstsdp-0.10", new String[]{}, true},
        //{"gstsignalprocessor-0.10", new String[]{}, true},
        {"gsttag-0.10", new String[]{}, true},
        {"gstvideo-0.10", new String[]{}, true},
        // External libraries
        {"libiconv-2", new String[]{}, false},
        {"libintl-8", new String[]{}, false},
        {"libjpeg-8", new String[]{}, false},
        {"libogg-0", new String[]{}, false},
        {"liborc-0.4-0", new String[]{}, false},
        {"liborc-test-0.4-0", new String[]{}, false},
        {"libpng14-14", new String[]{}, false},
        {"libtheora-0", new String[]{}, false},
        {"libtheoradec-1", new String[]{}, false},
        {"libtheoraenc-1", new String[]{}, false},
        {"libvorbis-0", new String[]{}, false},
        {"libvorbisenc-2", new String[]{}, false},
        {"libvorbisfile-3", new String[]{}, false},
        {"libxml2-2", new String[]{}, false},
        {"zlib1", new String[]{}, false}};
    private static final Map<String, Object> loadedMap = new HashMap<String, Object>();
    private static final int RECURSIVE_LOAD_MAX_DEPTH = 5;
    private boolean inited = false;
    private String libDir;
    private String pluginDir;

    private LibraryLoaderImpl() {
    }

    void load() throws Exception {
        synchronized (this) {
            if (inited) {
                return;
            } else {
                inited = true;
            }
        }
        File dir;
        if (Platform.is64Bit()) {
            dir = InstalledFileLocator.getDefault().locate("modules/lib/macosx64",
                    "net.neilcsmith.praxis.video.gstreamer.osx", false);
        } else {
            dir = InstalledFileLocator.getDefault().locate("modules/lib/macosx32",
                    "net.neilcsmith.praxis.video.gstreamer.osx", false);
        }
        if (dir == null || !dir.isDirectory()) {
            throw new IllegalStateException("Can't find lib directory");
        }
        libDir = dir.getAbsolutePath();
        dir = new File(dir, "plugins");
        if (!dir.isDirectory()) {
            throw new IllegalStateException("Can't find plugin directory");
        }
        pluginDir = dir.getAbsolutePath();
        loadLibs();
        String[] args = {""};
        Gst.setUseDefaultContext(false);
        Gst.init("Praxis core video", args);
        addPlugins();
    }

    private void loadLibs() {
        for (Object[] a : DEPENDENCIES) {
            load(a[0].toString(), DummyLibrary.class, true, 0, (Boolean) a[2]);
        }
    }

    private void addPlugins() {

        Registry reg = Registry.getDefault();
        boolean res;
        res = reg.scanPath(pluginDir);
        if (!res) {
            throw new IllegalStateException("Cannot load GStreamer plugins from " + pluginDir);
        }

    }

    private String[] findDeps(String name) {

        for (Object[] a : DEPENDENCIES) {
            if (name.equals(a[0])) {

                return (String[]) a[1];
            }
        }

        return new String[]{};
    }

    private Object load(String name, Class<?> clazz, boolean forceReload,
            int depth, boolean reqLib) {

        assert depth < RECURSIVE_LOAD_MAX_DEPTH : String.format(
                "recursive max load depth %s has been exceeded", depth);

        Object library = loadedMap.get(name);

        if (null == library || forceReload) {

            // Logger.getAnonymousLogger().info(String.format("%" + ((depth + 1) * 2)
            // + "sloading %s", "->", name));

            try {
                String[] deps = findDeps(name);

                for (String lib : deps) {
                    load(lib, DummyLibrary.class, false, depth + 1, reqLib);
                }

                library = loadLibrary(name, clazz, reqLib);

                if (library != null) {
                    loadedMap.put(name, library);
                }
            } catch (Exception e) {
                if (reqLib) {
                    throw new RuntimeException(String.format("can not load required library %s",
                            name, e));
                } else {
                    LOG.warning(String.format("can not load library %s", name, e));
                }
            }
        }

        return library;
    }

    private Object loadLibrary(String name, Class<?> clazz, boolean reqLib) {

        // Logger.getAnonymousLogger().info(String.format("loading %s", name));

        String[] nameFormats = new String[]{"lib%s", "lib%s-0", "%s", "%s-0"};

        UnsatisfiedLinkError linkError = null;

        for (String fmt : nameFormats) {
            try {
                String s = String.format(fmt, name);
                LOG.log(Level.FINE, "Trying to load library file {0}", s);
                NativeLibrary.addSearchPath(s, libDir);
                Object obj = Native.loadLibrary(s, clazz);
                LOG.log(Level.FINE, "Loaded library {0} succesfully!", s);
                return obj;
            } catch (UnsatisfiedLinkError ex) {
                linkError = ex;
            }
        }

        if (reqLib) {
            throw new UnsatisfiedLinkError(
                    String.format(
                    "can't load library %s (%1$s|lib%1$s|lib%1$s-0) with -Djna.library.path=%s. Last error:%s",
                    name, System.getProperty("jna.library.path"), linkError));
        } else {
            LOG.warning(String.format(
                    "can't load library %s (%1$s|lib%1$s|lib%1$s-0) with -Djna.library.path=%s. Last error:%s",
                    name, System.getProperty("jna.library.path"), linkError));
            return null;
        }
    }

    static LibraryLoaderImpl getInstance() {
        return INSTANCE;
    }

    public interface DummyLibrary extends Library {
    }
}
