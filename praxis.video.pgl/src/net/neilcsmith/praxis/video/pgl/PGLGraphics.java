/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2014 Neil C Smith.
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
package net.neilcsmith.praxis.video.pgl;

import java.nio.IntBuffer;
import processing.core.PImage;
import processing.opengl.PGL;
import processing.opengl.PGraphics2D;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class PGLGraphics extends PGraphics2D {

    public final static String ID = PGLGraphics.class.getName();

    private final PGLContext context;
    private PGLTexture pixelTexture;
    private PImage pixelImage;

    PGLGraphics(PGLContext context, boolean primary, int w, int h) {
        this.context = context;
        setPrimary(primary);
        setSize(w, h);
    }

    void writePixelsARGB(int[] pixels, boolean hasAlpha) {
        if (drawing) {
            flush();
        }
        if (pixelTexture == null || pixelTexture.contextIsOutdated()) {
            pixelTexture = new PGLTexture(this, texture.width, texture.height,
                    texture.getParameters());
            pixelTexture.invertedY(true);
            pixelImage = wrapTexture(pixelTexture);
        }
        int len = width * height;
        IntBuffer buf = context.getScratchBuffer(len);
        buf.put(pixels, 0, len);
        buf.rewind();
        context.writePixelsARGB(buf, pixelTexture, hasAlpha);
        int curBlend = blendMode;
        blendMode(REPLACE);
        copy(pixelImage, 0, 0, width, height, 0, height, width, -height);
        blendMode(curBlend);
    }

    
    

    protected void readPixelsARGB(int[] pixels) {
        this.pixels = pixels;
        this.pixelBuffer = context.getScratchBuffer(pixels.length);
        if (drawing) {
            flush();
        }
        readPixels();
        this.pixels = null;
        this.pixelBuffer = null;
        
    }

    @Override
    protected void blendModeImpl() {
        if (blendMode != lastBlendMode) {
            flush();
        }

        pgl.enable(PGL.BLEND);

        if (blendMode == REPLACE) {
            if (blendEqSupported) {
                pgl.blendEquation(PGL.FUNC_ADD);
            }
            pgl.blendFunc(PGL.ONE, PGL.ZERO);

        } else if (blendMode == BLEND) {
            if (blendEqSupported) {
                pgl.blendEquation(PGL.FUNC_ADD);
            }
            pgl.blendFunc(PGL.ONE, PGL.ONE_MINUS_SRC_ALPHA);

        } else if (blendMode == ADD) {
            if (blendEqSupported) {
                pgl.blendEquation(PGL.FUNC_ADD);
            }
            pgl.blendFunc(PGL.ONE, PGL.ONE);

        } else if (blendMode == MULTIPLY) {
            if (blendEqSupported) {
                pgl.blendEquation(PGL.FUNC_ADD);
            }
            pgl.blendFunc(PGL.DST_COLOR, PGL.ONE_MINUS_SRC_ALPHA);

        } else {
            throw new IllegalArgumentException();
        }

////<editor-fold defaultstate="collapsed" desc="comment">
//            if (blendMode == SUBTRACT) {
//            if (blendEqSupported) {
//                pgl.blendEquation(PGL.FUNC_REVERSE_SUBTRACT);
//                pgl.blendFunc(PGL.ONE, PGL.SRC_ALPHA);
//            } else {
//                throw new IllegalArgumentException();
//            }
//
//        } else if (blendMode == LIGHTEST) {
//            if (blendEqSupported) {
//                pgl.blendEquation(PGL.FUNC_MAX);
//                pgl.blendFunc(PGL.SRC_ALPHA, PGL.DST_ALPHA);
//            } else {
//                PGraphics.showWarning(BLEND_DRIVER_ERROR, "LIGHTEST");
//            }
//
//        } else if (blendMode == DARKEST) {
//            if (blendEqSupported) {
//                pgl.blendEquation(PGL.FUNC_MIN);
//                pgl.blendFunc(PGL.SRC_ALPHA, PGL.DST_ALPHA);
//            } else {
//                PGraphics.showWarning(BLEND_DRIVER_ERROR, "DARKEST");
//            }
//
//        } else if (blendMode == EXCLUSION) {
//            if (blendEqSupported) {
//                pgl.blendEquation(PGL.FUNC_ADD);
//            }
//            pgl.blendFunc(PGL.ONE_MINUS_DST_COLOR, PGL.ONE_MINUS_SRC_COLOR);
//
//        } else if (blendMode == MULTIPLY) {
//            if (blendEqSupported) {
//                pgl.blendEquation(PGL.FUNC_ADD);
//            }
//            pgl.blendFunc(PGL.DST_COLOR, PGL.SRC_COLOR);
//
//        } else if (blendMode == SCREEN) {
//            if (blendEqSupported) {
//                pgl.blendEquation(PGL.FUNC_ADD);
//            }
//            pgl.blendFunc(PGL.ONE_MINUS_DST_COLOR, PGL.ONE);
//
//        } else if (blendMode == DIFFERENCE) {
//            PGraphics.showWarning(BLEND_RENDERER_ERROR, "DIFFERENCE");
//
//        } else if (blendMode == OVERLAY) {
//            PGraphics.showWarning(BLEND_RENDERER_ERROR, "OVERLAY");
//
//        } else if (blendMode == HARD_LIGHT) {
//            PGraphics.showWarning(BLEND_RENDERER_ERROR, "HARD_LIGHT");
//
//        } else if (blendMode == SOFT_LIGHT) {
//            PGraphics.showWarning(BLEND_RENDERER_ERROR, "SOFT_LIGHT");
//
//        } else if (blendMode == DODGE) {
//            PGraphics.showWarning(BLEND_RENDERER_ERROR, "DODGE");
//
//        } else if (blendMode == BURN) {
//            PGraphics.showWarning(BLEND_RENDERER_ERROR, "BURN");
//        }
//</editor-fold>
        lastBlendMode = blendMode;
    }

    @Override
    public void beginDraw() {
        if (drawing) {
            assert context.current == this;
            return;
        }
        if (context.current != null && context.current != getPrimaryPG()) {
            context.current.endDraw();
        }
        super.beginDraw();
        context.current = this;
    }

    @Override
    public void endDraw() {
        if (!drawing) {
            assert context.current != this;
            return;
        }
        super.endDraw();
        context.current = getPrimaryPG();
    }
    
    

    void endOffscreen() {
//        PGraphics current = getCurrentPG();
//        if (current == null) {
//            assert current != null || primarySurface;
//            return;
//        }
//        PGraphics primary = getPrimaryPG();
//        if (current != primary) {
//            current.endDraw();
//        }
        if (context.current != getPrimaryPG()) {
            context.current.endDraw();
            context.current = getPrimaryPG();
        }
    }
    
//    protected PGraphics getCurrent() {
//        return ((PGLGraphics)getPrimaryPG()).getCurrentPG();
//    }

      

}
