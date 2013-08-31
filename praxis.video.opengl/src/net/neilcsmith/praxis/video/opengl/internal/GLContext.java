/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Neil C Smith.
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
package net.neilcsmith.praxis.video.opengl.internal;

import java.awt.Canvas;
import java.util.WeakHashMap;
import net.neilcsmith.praxis.video.opengl.GLException;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;

/**
 *
 * @author nsigma
 */
public class GLContext {

    private final static PixelFormat DEFAULT_PIXEL_FORMAT = new PixelFormat(24, 8, 16, 0, 0);
    private static GLContext current;
    
    private final GLRenderer renderer;
    private final TextureManager textureManager;
    private final int width;
    private final int height;

    
    private WeakHashMap<GLSurface, Boolean> surfaces;

    private GLContext(int width, int height) {
        this.width = width;
        this.height = height;
        this.renderer = new GLRenderer(this);
        this.textureManager = new TextureManager(this);
        surfaces = new WeakHashMap<GLSurface, Boolean>();    
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    } 
    
    public GLRenderer getRenderer() {
        return renderer;
    }
    
    TextureManager getTextureManager() {
        return textureManager;
    }
    
    public GLSurface createSurface(int width, int height, boolean alpha) {
        GLSurface s =  new GLSurface(this, width, height, alpha);
        surfaces.put(s, Boolean.TRUE);
        return s;
    }
    
    public void dispose() {
        for (GLSurface s : surfaces.keySet()) {
            s.clear();
        }
        textureManager.clear();
        try {
            Display.setParent(null);
        } catch (LWJGLException ex) {
        }
        Display.destroy();
        current = null;
    }

    public synchronized static GLContext createContext(Canvas canvas) throws GLException {
        if (current != null) {
            throw new GLException();
        }
        try {
            Display.setParent(canvas);
            createDisplayPixelFormat();
//            Display.setVSyncEnabled(true);
            GLContext ctxt = new GLContext(canvas.getWidth(), canvas.getHeight());
            current = ctxt;
            return ctxt;
        } catch (Exception ex) {
            throw new GLException(ex);
        }

    }
    
    public static GLContext getCurrent() {
        return current;
    }

    

    private static void createDisplayPixelFormat() {
        try {
            Display.create(DEFAULT_PIXEL_FORMAT);
        } catch (Exception ex) {
            Display.destroy();
            try {
                Display.create(new PixelFormat(0, 16, 8));
            } catch (Exception ex2) {
                Display.destroy();
                try {
                    Display.create(new PixelFormat());
                } catch (Exception ex3) {
                    if (ex3.getMessage().contains("Pixel format not accelerated")) {
                        throw new RuntimeException("OpenGL is not supported by the video driver.", ex3);
                    }
                }
            }
        }
    }
}
