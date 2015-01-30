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

    private final int w;
    private final int h;
    private final Context context;
    private final PGLOutputSink sink;

    private volatile long renderTime;
    private long lastRenderTime;
    private PGLSurface surface;

    PGLApplet(PGLOutputSink sink, int width, int height) {
        this.sink = sink;
        this.w = width;
        this.h = height;
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
        return w;
    }

    @Override
    public int sketchHeight() {
        return h;
    }

    @Override
    public String sketchRenderer() {
        return PGLGraphics.ID;
    }

    @Override
    public void setup() {
        assert surface == null;
        if (surface == null) {
            surface = context.createSurface(w, h, false);
        }
    }

    @Override
    public synchronized void draw() {
//        assert renderTime != lastRenderTime;
//        System.out.println(((PJOGL)((PGLGraphics)g).pgl).gl.getGL2().getContext());
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
//        background(0,0,0);
//        blendMode(REPLACE);
        clear();
        image(img, 0, 0);
//        rect(100,100,100,100);
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
        protected PGLGraphics createGraphics(int width, int height) {
            return (PGLGraphics) PGLApplet.this.createGraphics(width, height, PGLGraphics.ID);
        }

        @Override
        protected PGLGraphics primary() {
            return (PGLGraphics) g;
        }

    }

}
