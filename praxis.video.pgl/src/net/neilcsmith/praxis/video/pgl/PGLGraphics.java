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

import com.jogamp.nativewindow.ScalableSurface;
import com.jogamp.newt.opengl.GLWindow;
import java.awt.Rectangle;
import java.nio.IntBuffer;
import java.util.Collections;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PGL;
import processing.opengl.PGraphics2D;
import processing.opengl.PSurfaceJOGL;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class PGLGraphics extends PGraphics2D {

    final static String ID = PGLGraphics.class.getName();

    private final PGLContext context;
    private PGLTexture pixelTexture;
    private PImage pixelImage;

    PGLGraphics(PGLContext context, boolean primary, int w, int h) {
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
    public processing.core.PSurface createSurface() {
        return new PSurface(this);
    }

    private static class PSurface extends PSurfaceJOGL {

        public PSurface(PGraphics graphics) {
            super(graphics);
        }

        protected void initWindow() {
            window = GLWindow.create(screen, pgl.getCaps());
            if (displayDevice == null) {
                displayDevice = window.getMainMonitor();
            }

            int displayNum = sketch.sketchDisplay();
            if (displayNum > 0) {  // if -1, use the default device
                if (displayNum <= monitors.size()) {
                    displayDevice = monitors.get(displayNum - 1);
                } else {
                    System.err.format("Display %d does not exist, "
                            + "using the default display instead.%n", displayNum);
                    for (int i = 0; i < monitors.size(); i++) {
                        System.err.format("Display %d is %s%n", i + 1, monitors.get(i));
                    }
                }
            }

            boolean spanDisplays = sketch.sketchDisplay() == PConstants.SPAN;
            screenRect = spanDisplays
                    ? new Rectangle(0, 0, screen.getWidth(), screen.getHeight())
                    : new Rectangle(0, 0,
                            displayDevice.getViewportInWindowUnits().getWidth(),
                            displayDevice.getViewportInWindowUnits().getHeight());

            // Set the displayWidth/Height variables inside PApplet, so that they're
            // usable and can even be returned by the sketchWidth()/Height() methods.
            sketch.displayWidth = screenRect.width;
            sketch.displayHeight = screenRect.height;

            sketchWidth = sketch.sketchWidth();
            sketchHeight = sketch.sketchHeight();

            boolean fullScreen = sketch.sketchFullScreen();
            // Removing the section below because sometimes people want to do the
            // full screen size in a window, and it also breaks insideSettings().
            // With 3.x, fullScreen() is so easy, that it's just better that way.
            // https://github.com/processing/processing/issues/3545
    /*
             // Sketch has already requested to be the same as the screen's
             // width and height, so let's roll with full screen mode.
             if (screenRect.width == sketchWidth &&
             screenRect.height == sketchHeight) {
             fullScreen = true;
             sketch.fullScreen();
             }
             */

            if (fullScreen || spanDisplays) {
                sketchWidth = screenRect.width;
                sketchHeight = screenRect.height;
            }

            float[] reqSurfacePixelScale;
            if (graphics.is2X()) {
                // Retina
                reqSurfacePixelScale = new float[]{ScalableSurface.AUTOMAX_PIXELSCALE,
                    ScalableSurface.AUTOMAX_PIXELSCALE};
            } else {
                // Non-retina
                reqSurfacePixelScale = new float[]{ScalableSurface.IDENTITY_PIXELSCALE,
                    ScalableSurface.IDENTITY_PIXELSCALE};
            }
            window.setSurfaceScale(reqSurfacePixelScale);
            window.setSize(sketchWidth, sketchHeight);
//    window.setResizable(false);
            setSize(sketchWidth, sketchHeight);
            sketchX = displayDevice.getViewportInWindowUnits().getX();
            sketchY = displayDevice.getViewportInWindowUnits().getY();
            if (fullScreen) {
                PApplet.hideMenuBar();
//                window.setTopLevelPosition(sketchX, sketchY);
//      placedWindow = true;
                if (spanDisplays) {
                    window.setFullscreen(monitors);
                } else {
                    window.setFullscreen(Collections.singletonList(displayDevice));
                }
            }
        }

    }

}
