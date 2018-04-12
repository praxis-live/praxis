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
 *
 * Parts of the API of this package, as well as some of the code, is derived from
 * the Processing project (http://processing.org)
 *
 * Copyright (c) 2004-09 Ben Fry and Casey Reas
 * Copyright (c) 2001-04 Massachusetts Institute of Technology
 *
 */
package org.praxislive.video.code;

import java.io.IOException;
import java.net.URI;
import org.praxislive.code.ResourceProperty;
import org.praxislive.video.code.userapi.PImage;
import org.praxislive.video.render.Surface;
import org.praxislive.video.render.utils.BufferedImageSurface;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class ImageLoader extends ResourceProperty.Loader<PImage> {
    
    private final static ImageLoader INSTANCE = new ImageLoader();

    private ImageLoader() {
        super(PImage.class);
    }

    @Override
    public PImage load(URI uri) throws IOException {
        Surface s = BufferedImageSurface.load(uri);
        return new PImageImpl(s);
    }

    static ImageLoader getDefault() {
        return INSTANCE;
    }
    
    private static class PImageImpl extends PImage {

        private final Surface surface;

        private PImageImpl(Surface surface) {
            super(surface.getWidth(), surface.getHeight());
            this.surface = surface;
        }

        @Override
        protected Surface getSurface() {
            return surface;
        }

    }

}
