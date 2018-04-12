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
 */
package org.praxislive.video.pgl.code;

import java.lang.reflect.Field;
import org.praxislive.logging.LogLevel;
import org.praxislive.video.pgl.PGLGraphics;
import org.praxislive.video.pgl.PGLSurface;
import org.praxislive.video.pgl.code.userapi.OffScreen;
import org.praxislive.video.pgl.code.userapi.PGraphics2D;
import org.praxislive.video.render.Surface;
import processing.core.PStyle;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class P2DOffScreenGraphicsInfo {

    private final int width;
    private final int height;
    private final double scaleWidth;
    private final double scaleHeight;
    private final boolean persistent;
    private final Field field;

    private P2DCodeContext context;
    private PGraphics graphics;
    private PGLSurface surface;

    private P2DOffScreenGraphicsInfo(Field field,
            int width,
            int height,
            double scaleWidth,
            double scaleHeight,
            boolean persistent) {
        this.field = field;
        this.width = width;
        this.height = height;
        this.scaleWidth = scaleWidth;
        this.scaleHeight = scaleHeight;
        this.persistent = persistent;
    }

    void attach(P2DCodeContext context, P2DOffScreenGraphicsInfo previous) {
        this.context = context;
        if (previous != null) {
            if (previous.surface != null) {
                surface = previous.surface;
                previous.surface = null;
            }
        }
    }

    void validate(PGLSurface output) {
        if (!isValid(surface, output)) {
            if (graphics != null) {
                graphics.release();
                graphics = null;
            }
            if (surface != null) {
                surface.release();
            }
            surface = output.createSurface(calculateWidth(output), calculateHeight(output), true);
        }
        
        PGLGraphics p2d = surface.getGraphics();
        if (graphics == null || graphics.width != p2d.width || graphics.height != p2d.height) {
            graphics = new PGraphics(p2d.width, p2d.height);
            graphics.init(surface.getGraphics(), true);
            try {
                field.set(context.getDelegate(), graphics);
            } catch (Exception ex) {
                context.getLog().log(LogLevel.ERROR, ex);
            }
        } else if (!persistent) {
            graphics.init(surface.getGraphics(), false);
        }
    }

    void endFrame() {
        if (!persistent) {
            if (graphics != null) {
                graphics.release();
            }
            if (surface != null) {
                surface.release();
            }
        }
    }

    void release() {
        if (surface != null) {
            surface.release();
        }
        surface = null;
        graphics = null;
    }

    private boolean isValid(PGLSurface surface, PGLSurface output) {
        if (surface == null) {
            return false;
        }
        if (surface.getContext() != output.getContext()) {
            return false;
        }
        return surface.getWidth() == calculateWidth(output)
                && surface.getHeight() == calculateHeight(output);

    }

    private int calculateWidth(Surface output) {
        int w = width < 1 ? output.getWidth() : width;
        w *= scaleWidth;
        return Math.max(w, 1);
    }

    private int calculateHeight(Surface output) {
        int h = height < 1 ? output.getHeight() : height;
        h *= scaleHeight;
        return Math.max(h, 1);
    }

    static P2DOffScreenGraphicsInfo create(Field field) {
        OffScreen ann = field.getAnnotation(OffScreen.class);
        if (ann == null
                || !PGraphics2D.class.isAssignableFrom(field.getType())) {
            return null;
        }
        field.setAccessible(true);
        int width = ann.width();
        int height = ann.height();
        double scaleWidth = ann.scaleWidth();
        double scaleHeight = ann.scaleHeight();
        boolean persistent = ann.persistent();
        return new P2DOffScreenGraphicsInfo(field,
                width,
                height,
                scaleWidth,
                scaleHeight,
                persistent);
    }

    private class PGraphics extends PGraphics2D {

        private int matrixStackDepth;
        private PStyle styles;
        private PGLGraphics pgl;
        
        private PGraphics(int width, int height) {
            super(width, height);
        }

        private void init(PGLGraphics pgl, boolean setupRequired) {
            this.pgl = pgl;
            pgl.beginDraw();
            pgl.resetMatrix();
            pgl.pushStyle();
            pgl.style(setupRequired ? null : styles);
            initGraphics(pgl);
        }

        private void release() {
            releaseGraphics();
            if (matrixStackDepth != 0) {
                context.getLog().log(LogLevel.ERROR, "Mismatched matrix push / pop");
                while (matrixStackDepth > 0) {
                    pgl.popMatrix();
                    matrixStackDepth--;
                }
            }
            styles = pgl.getStyle(styles);
            pgl.popStyle();
            pgl.resetMatrix();
            pgl.resetShader();
            pgl = null;
        }

        @Override
        public void beginDraw() {
            context.beginOffscreen();
            super.beginDraw();
        }

        @Override
        public void endDraw() {
            if (matrixStackDepth != 0) {
                context.getLog().log(LogLevel.ERROR, "Mismatched matrix push / pop");
                while (matrixStackDepth > 0) {
                    pgl.popMatrix();
                    matrixStackDepth--;
                }
            }
            context.endOffscreen();
        }

        @Override
        public void pushMatrix() {
            if (matrixStackDepth == 32) {
                context.getLog().log(LogLevel.ERROR, "Matrix stack full in popMatrix()");
                return;
            }
            matrixStackDepth++;
            super.pushMatrix();
        }

        @Override
        public void popMatrix() {
            if (matrixStackDepth == 0) {
                context.getLog().log(LogLevel.ERROR, "Matrix stack empty in popMatrix()");
                return;
            }
            matrixStackDepth--;
            super.popMatrix();
        }

    }

}
