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
package org.praxislive.video.pgl.code;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.praxislive.code.ResourceProperty;
import org.praxislive.video.pgl.code.userapi.PShape;
import processing.data.XML;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class ShapeLoader extends ResourceProperty.Loader<PShape> {

    private final static ShapeLoader INSTANCE = new ShapeLoader();

    private ShapeLoader() {
        super(PShape.class);
    }

    @Override
    public PShape load(URI uri) throws IOException {
        String path = uri.getPath();
        if (path.endsWith(".svg") || path.endsWith(".svgz")) {
            return loadSVG(uri);
        } else if (path.endsWith(".obj")) {
            return loadOBJ(uri);
        } else {
            throw new IOException("Unknown file type");
        }
    }

    private PShape loadSVG(URI uri) throws IOException {
        try {
            InputStream is = uri.toURL().openStream();
            XML xml = new XML(is);
            PShapeSVG svg = new PShapeSVG(xml);
            return new PShapeImpl(svg);
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            } else {
                throw new IOException(ex);
            }
        }
    }
    
    private PShape loadOBJ(URI uri) {
        return new PShapeImpl(new PShapeOBJ(uri));
    }

    static ShapeLoader getDefault() {
        return INSTANCE;
    }

    private static class PShapeImpl extends PShape {

        private PShapeImpl(processing.core.PShape shape) {
            super(shape);
        }

    }

}
