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
package net.neilcsmith.praxis.video.gst1.components;

import java.net.URI;
import java.util.logging.Logger;

/**
 *
 * @author Neil C Smith
 */
class VideoDelegateFactory {

    private final static Logger LOG = Logger.getLogger(VideoDelegateFactory.class.getName());

    private static VideoDelegateFactory instance = new VideoDelegateFactory();

    private VideoDelegateFactory() {
        preloadLibs();
    }

    private void preloadLibs() {
//        try {
//            for (GStreamerLibraryLoader loader : Lookup.getDefault().lookupAll(GStreamerLibraryLoader.class)) {
//
//                try {
//                    loader.load();
//                } catch (Exception ex) {
//                    LOG.log(Level.WARNING, "Exception thrown while trying to load GStreamer libraries with loader " + loader, ex);
//                }
//            }
//        } catch (Exception ex) {
//            LOG.log(Level.WARNING, "Exception thrown while trying to load GStreamer library loaders", ex);
//        }
    }

    static VideoDelegateFactory getInstance() {
        return instance;
    }
    
    VideoDelegate createCaptureDelegate(String binDescription) {
        return BinDelegate.create(binDescription);
    }

    VideoDelegate createPlayBinDelegate(URI resource) {
        return PlayBinDelegate.create(resource);
    }


}
