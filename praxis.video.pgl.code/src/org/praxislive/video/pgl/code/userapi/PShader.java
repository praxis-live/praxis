/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2019 Neil C Smith.
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
package org.praxislive.video.pgl.code.userapi;

import java.util.Optional;
import org.praxislive.video.pgl.PGLContext;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class PShader {

    private final PGLContext context;
    private final processing.opengl.PShader shader;

    PShader(PGLContext context, processing.opengl.PShader shader) {
        this.context = context;
        this.shader = shader;
    }

    public <T> Optional<T> find(Class<T> type) {
        if (processing.opengl.PShader.class.isAssignableFrom(type)) {
            return Optional.of(type.cast(shader));
        } else {
            return Optional.empty();
        }
    }

    processing.opengl.PShader unwrap(PGLContext context) {
        if (this.context == context) {
            return shader;
        }
        throw new IllegalStateException("Shader context is invalid");
    }

    // PROCESSING API
    public void set(String name, int x) {
        shader.set(name, x);
    }

    public void set(String name, int x, int y) {
        shader.set(name, x, y);
    }

    public void set(String name, int x, int y, int z) {
        shader.set(name, x, y, z);
    }

    public void set(String name, int x, int y, int z, int w) {
        shader.set(name, x, y, z, w);
    }

    public void set(String name, double x) {
        shader.set(name, (float) x);
    }

    public void set(String name, double x, double y) {
        shader.set(name, (float) x, (float) y);
    }

    public void set(String name, double x, double y, double z) {
        shader.set(name, (float) x, (float) y, (float) z);
    }

    public void set(String name, double x, double y, double z, double w) {
        shader.set(name, (float) x, (float) y, (float) z, (float) w);
    }

    public void set(String name, boolean x) {
        shader.set(name, x);
    }

    public void set(String name, boolean x, boolean y) {
        shader.set(name, x, y);
    }

    public void set(String name, boolean x, boolean y, boolean z) {
        shader.set(name, x, y, z);
    }

    public void set(String name, boolean x, boolean y, boolean z, boolean w) {
        shader.set(name, x, y, z, w);
    }

    public void set(String name, PImage tex) {
        shader.set(name, tex.unwrap(context));
    }

}
