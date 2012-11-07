/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.neilcsmith.praxis.video.opengl.internal;

import java.awt.Canvas;
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
    
    
    
    private int width;
    private int height;

    private GLContext() {
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    } 
    
    public GLSurface createSurface(int width, int height, boolean alpha) {
        return new GLSurface(width, height, alpha);
    }
    
    public void dispose() {
        try {
            Display.setParent(null);
        } catch (LWJGLException ex) {
        }
        Display.destroy();
        current = null;
    }

    public static GLContext createContext(Canvas canvas) throws GLException {
        if (current != null) {
            throw new GLException(); //@ TODO - make thread safe!
        }
        try {
            Display.setParent(canvas);
            createDisplayPixelFormat();
            Display.setVSyncEnabled(true);
            GLContext ctxt = new GLContext();
            ctxt.width = canvas.getWidth();
            ctxt.height = canvas.getHeight();
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
