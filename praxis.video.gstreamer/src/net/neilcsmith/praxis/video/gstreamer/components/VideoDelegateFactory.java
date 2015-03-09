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
 */
package net.neilcsmith.praxis.video.gstreamer.components;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.video.InvalidVideoResourceException;
import net.neilcsmith.praxis.video.VideoSettings;
import net.neilcsmith.praxis.video.gstreamer.GStreamerLibraryLoader;
import net.neilcsmith.praxis.video.gstreamer.delegates.BinDelegate;
import net.neilcsmith.praxis.video.gstreamer.delegates.KSDelegate;
import net.neilcsmith.praxis.video.gstreamer.delegates.PlayBinDelegate;
import net.neilcsmith.praxis.video.gstreamer.delegates.QTKitDelegate;
import net.neilcsmith.praxis.video.gstreamer.delegates.V4LDelegate;
import org.openide.util.Lookup;

/**
 *
 * @author Neil C Smith
 */
class VideoDelegateFactory {

    private static String DEFAULT_CAPTURE_SCHEME = "v4l2";

    static {
        String os = System.getProperty("os.name");
        if (os != null) {
            if (os.contains("Windows")) {
                DEFAULT_CAPTURE_SCHEME = "ks";
            } else if (os.contains("Mac") || os.contains("Darwin")) {
                DEFAULT_CAPTURE_SCHEME = "qtkit";
            }
        }
    }

    private final static Logger LOG = Logger.getLogger(VideoDelegateFactory.class.getName());

    private static VideoDelegateFactory instance = new VideoDelegateFactory();

    private VideoDelegateFactory() {
        preloadLibs();
    }

    private void preloadLibs() {
        try {
            for (GStreamerLibraryLoader loader : Lookup.getDefault().lookupAll(GStreamerLibraryLoader.class)) {

                try {
                    loader.load();
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Exception thrown while trying to load GStreamer libraries with loader " + loader, ex);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Exception thrown while trying to load GStreamer library loaders", ex);
        }
    }

    static VideoDelegateFactory getInstance() {
        return instance;
    }

    VideoDelegate createCaptureDelegate(URI resource) throws InvalidVideoResourceException, InstantiationException {
        String scheme = resource.getScheme();
        if (scheme == null) {
            throw new InvalidVideoResourceException();
        }
        if ("capture".equals(scheme)) {
            resource = translateCapture(resource);
            scheme = resource.getScheme();
        }

        switch (scheme) {
            case "v4l2":
                return createV4LDelegate(resource);
            case "ks":
                return createKSDelegate(resource);
            case "qtkit":
                return createQTKitDelegate(resource);
            default:
                return createPlayBinDelegate(resource);
        }
    }
    
    VideoDelegate createCaptureDelegate(String binDescription) {
        return BinDelegate.create(binDescription);
    }

    VideoDelegate createPlayBinDelegate(URI resource) {
        return PlayBinDelegate.create(resource);
    }

    private VideoDelegate createV4LDelegate(URI resource) {
        return V4LDelegate.create(resource);
    }

    private VideoDelegate createKSDelegate(URI resource) {
        return KSDelegate.create(resource);
    }

    private VideoDelegate createQTKitDelegate(URI resource) {
        return QTKitDelegate.create(resource);
    }

    private URI translateCapture(URI uri) {
        int idx = 0;
        try {
            String auth = uri.getAuthority();
            if (auth != null) {
                idx = Integer.parseInt(uri.getAuthority());
            }
        } catch (Exception ex) {
        }
        String dev = DEFAULT_CAPTURE_SCHEME + "://" + idx;
        try {
            URI out = new URI(dev);
            if (out.getQuery() == null) {
                String query = uri.getQuery();
                if (query != null) {
                    out = new URI(out.getScheme(), out.getAuthority(), null, query, null);
                }
            }
            return out;
        } catch (URISyntaxException ex) {
            return uri;
        }
    }

}
