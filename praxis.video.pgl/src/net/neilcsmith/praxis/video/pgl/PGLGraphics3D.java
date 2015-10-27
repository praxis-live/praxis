/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2015 Neil C Smith.
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
import processing.opengl.PGraphics3D;
import processing.opengl.PGraphicsOpenGL;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class PGLGraphics3D extends PGraphics3D {

    final static String ID = PGLGraphics3D.class.getName();

    private final PGLContext context;
    private PGLTexture pixelTexture;
    private PImage pixelImage;

    PGLGraphics3D(PGLContext context, boolean primary, int w, int h) {
        this.context = context;
        setParent(context.parent());
        setPrimary(primary);
        setSize(w, h);
    }

    public PGLContext getContext() {
        return context;
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
        context.writePixelsARGB(buf, pixelTexture);
        int curBlend = blendMode;
        if (hasAlpha) {
            blendMode(REPLACE);
        } else {
            background(0.f);
            blendMode(ADD);
        }
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
        if (context.current != getPrimaryPG()) {
            context.current.endDraw();
            context.current = getPrimaryPG();
        }
    }

    @Override
    protected void colorCalc(float gray, float alpha) {
        if (gray > colorModeX) {
            gray = colorModeX;
        }
        if (alpha > colorModeA) {
            alpha = colorModeA;
        }

        if (gray < 0) {
            gray = 0;
        }
        if (alpha < 0) {
            alpha = 0;
        }

        calcA = (alpha == colorModeA) ? 1 : alpha / colorModeA;

        calcR = gray / colorModeX;
        calcR = (alpha == colorModeA) ? calcR : calcR * calcA;
        calcG = calcR;
        calcB = calcR;

        calcRi = (int) (calcR * 255);
        calcGi = (int) (calcG * 255);
        calcBi = (int) (calcB * 255);
        calcAi = (int) (calcA * 255);
        calcColor = (calcAi << 24) | (calcRi << 16) | (calcGi << 8) | calcBi;
        calcAlpha = (calcAi != 255);
    }

    @Override
    protected void colorCalc(float x, float y, float z, float a) {
        if (x > colorModeX) {
            x = colorModeX;
        }
        if (y > colorModeY) {
            y = colorModeY;
        }
        if (z > colorModeZ) {
            z = colorModeZ;
        }
        if (a > colorModeA) {
            a = colorModeA;
        }

        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (z < 0) {
            z = 0;
        }
        if (a < 0) {
            a = 0;
        }

        calcA = (a == colorModeA) ? 1 : a / colorModeA;

        switch (colorMode) {
            case RGB:
                calcR = x / colorModeX;
                calcG = y / colorModeY;
                calcB = z / colorModeZ;
                break;

            case HSB:
                x /= colorModeX; // h
                y /= colorModeY; // s
                z /= colorModeZ; // b

                if (y == 0) {  // saturation == 0
                    calcR = calcG = calcB = z;

                } else {
                    float which = (x - (int) x) * 6.0f;
                    float f = which - (int) which;
                    float p = z * (1.0f - y);
                    float q = z * (1.0f - y * f);
                    float t = z * (1.0f - (y * (1.0f - f)));

                    switch ((int) which) {
                        case 0:
                            calcR = z;
                            calcG = t;
                            calcB = p;
                            break;
                        case 1:
                            calcR = q;
                            calcG = z;
                            calcB = p;
                            break;
                        case 2:
                            calcR = p;
                            calcG = z;
                            calcB = t;
                            break;
                        case 3:
                            calcR = p;
                            calcG = q;
                            calcB = z;
                            break;
                        case 4:
                            calcR = t;
                            calcG = p;
                            calcB = z;
                            break;
                        case 5:
                            calcR = z;
                            calcG = p;
                            calcB = q;
                            break;
                    }
                }
                break;
        }

        if (a != colorModeA) {
            calcR *= calcA;
            calcG *= calcA;
            calcB *= calcA;
        }

        calcRi = (int) (255 * calcR);
        calcGi = (int) (255 * calcG);
        calcBi = (int) (255 * calcB);
        calcAi = (int) (255 * calcA);
        calcColor = (calcAi << 24) | (calcRi << 16) | (calcGi << 8) | calcBi;
        calcAlpha = (calcAi != 255);
    }

    @Override
    protected PGL createPGL(PGraphicsOpenGL pg) {
        return new PGLJOGL(pg);
    }

    @Override
    public void dispose() {
        
        if (pixelTexture != null) {
            pixelTexture.dispose();
        }
        
        super.dispose();

    }

    @Override
    public processing.core.PSurface createSurface() {
        return new PGLGraphicsPSurface(this);
    }

}
