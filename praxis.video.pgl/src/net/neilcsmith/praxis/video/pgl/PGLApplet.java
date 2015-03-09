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

import java.util.logging.Logger;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
class PGLApplet extends PApplet {

    private final static Logger LOG = Logger.getLogger(PGLApplet.class.getName());

    private final int frameWidth, frameHeight, sketchWidth, sketchHeight, rotation;
    private final Context context;
    private final PGLOutputSink sink;

    private volatile long renderTime;
    private long lastRenderTime;
    private PGLSurface surface;

    PGLApplet(PGLOutputSink sink,
            int frameWidth,
            int frameHeight,
            int sketchWidth,
            int sketchHeight,
            int rotation) {
        this.sink = sink;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.sketchWidth = sketchWidth;
        this.sketchHeight = sketchHeight;
        this.rotation = rotation;
        this.context = new Context();
    }

    @Override
    public void run() {
        LOG.fine("Allowing applet run to end");
    }

    @Override
    protected PGraphics makeGraphics(int w, int h, String renderer, String path, boolean primary) {
        if (PGLGraphics.ID.equals(renderer)) {
            PGLGraphics pgl = new PGLGraphics(context, primary, w, h);
            pgl.setParent(this);
            return pgl;
        } else if (PGLGraphics3D.ID.equals(renderer)) {
            PGLGraphics3D pgl3d = new PGLGraphics3D(context, primary, w, h);
            pgl3d.setParent(this);
            return pgl3d;
        } else {
            throw new Error();
//            return super.makeGraphics(w, h, renderer, path, primary);
        }
    }

    @Override
    public int sketchWidth() {
        return sketchWidth;
    }

    @Override
    public int sketchHeight() {
        return sketchHeight;
    }

    @Override
    public String sketchRenderer() {
        return PGLGraphics.ID;
    }

    @Override
    public void setup() {
        assert surface == null;
        if (surface == null) {
            surface = context.createSurface(frameWidth, frameHeight, false);
        }
    }

    @Override
    public synchronized void draw() {
        if (renderTime == lastRenderTime) {
            return;
        }
        long time = renderTime;
        if (time != lastRenderTime) {
            sink.process(surface, time);
            lastRenderTime = time;
        }
        PImage img = context.asImage(surface);
        context.primary().endOffscreen();
        clear();
        if (rotation == 0) {
            image(img, 0, 0, sketchWidth, sketchHeight);
        } else {
            translate(sketchWidth / 2, sketchHeight / 2);
            rotate(radians(rotation));
            if (rotation == 180) {
                image(img, -sketchWidth / 2, -sketchHeight / 2,
                        sketchWidth, sketchHeight);
            } else {
                image(img, -sketchHeight / 2, -sketchWidth / 2,
                        sketchHeight, sketchWidth);
            }
        }
    }

    @Override
    public void dispose() {
        context.dispose();
        super.dispose();
    }

    void requestDraw(long time) {
        renderTime = time;
        g.requestDraw();
    }

    class Context extends PGLContext {

        @Override
        public PGLGraphics createGraphics(int width, int height) {
            return (PGLGraphics) PGLApplet.this.createGraphics(width, height, PGLGraphics.ID);
        }

        @Override
        public PGLGraphics primary() {
            return (PGLGraphics) g;
        }

        @Override
        public PGLGraphics3D create3DGraphics(int width, int height) {
            return (PGLGraphics3D) PGLApplet.this.createGraphics(width, height, PGLGraphics3D.ID);
        }

    }

}
