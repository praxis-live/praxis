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

import com.jogamp.newt.opengl.GLWindow;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.video.Player;
import net.neilcsmith.praxis.video.QueueContext;
import net.neilcsmith.praxis.video.WindowHints;
import net.neilcsmith.praxis.video.pipes.FrameRateListener;
import net.neilcsmith.praxis.video.pipes.VideoPipe;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PSurface;
import processing.opengl.PJOGL;

/**
 *
 * @author Neil C Smith
 */
public class PGLPlayer implements Player {

    private final static Logger LOG = Logger.getLogger(PGLPlayer.class.getName());
    private final int surfaceWidth, surfaceHeight; // dimensions of surface
    private final int outputWidth, outputHeight, outputRotation, outputDevice;
    private final double fps; // frames per second
    private final long frameNanos;
    private final WindowHints wHints;
    private final QueueContext queue;

    private volatile boolean running = false; // flag to control animation
    private volatile long time;
    private List<FrameRateListener> listeners = new ArrayList<>();
    private Applet applet = null;
    private PGLOutputSink sink = null;

    PGLPlayer(int width,
            int height,
            double fps,
            int outputWidth,
            int outputHeight,
            int outputRotation,
            int outputDevice,
            WindowHints wHints,
            QueueContext queue) {
        if (width <= 0 || height <= 0 || fps <= 0) {
            throw new IllegalArgumentException();
        }
        this.surfaceWidth = width;
        this.surfaceHeight = height;
        this.fps = fps;
        frameNanos = (long) (1000000000.0 / fps);
        this.outputWidth = outputWidth;
        this.outputHeight = outputHeight;
        this.outputRotation = outputRotation;
        this.outputDevice = outputDevice;
        this.wHints = wHints;
        this.queue = queue;
        sink = new PGLOutputSink();
    }

    @Override
    public void run() {
        LOG.info("Starting experimental PGL renderer.");
        running = true;
        time = System.nanoTime();
        try {
            init();
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Unable to start OpenGL player", ex);
        }
        while (running) {
            try {
                if (System.nanoTime() < (time + frameNanos - 2_000_000)) {
                    synchronized (applet) {
                        queue.process(0, TimeUnit.MILLISECONDS);
                    }
                }// else {
                    Thread.sleep(1);
//                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Exception during run", ex);
            }
        }
    }

    private void init() throws Exception {
        applet = new Applet();
        if (outputDevice > -1) {
            PApplet.runSketch(new String[]{
                "--display=" + (outputDevice + 1),
                "PraxisLIVE"
            }, applet);
        } else {
            PApplet.runSketch(new String[]{"PraxisLIVE"}, applet);
        }
    }

    private void fireListeners() {
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
            listeners.get(i).nextFrame(this);
        }
    }

    @Override
    public VideoPipe getSource(int index) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getSourceCount() {
        return 0;
    }

    @Override
    public VideoPipe getSink(int index) {
        if (index == 0) {
            return getOutputSink();
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int getSinkCount() {
        return 1;
    }

    public VideoPipe getOutputSink() {
        return sink;
    }

    @Override
    public void terminate() {
        applet.exit();
    }

    @Override
    public void addFrameRateListener(FrameRateListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
    }

    @Override
    public void removeFrameRateListener(FrameRateListener listener) {
        listeners.remove(listener);
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public boolean isRendering() {
        return applet.rendering;
    }

    private class Applet extends PApplet {

        private final PGLContext context;
        private boolean rendering;
        private PGLSurface pglSurface;

        private Applet() {
            context = new PGLContext(this, surfaceWidth, surfaceHeight);
        }

        @Override
        protected PGraphics makeGraphics(int w, int h, String renderer, String path, boolean primary) {
            if (PGLGraphics.ID.equals(renderer)) {
                PGLGraphics pgl = new PGLGraphics(context, primary, w, h);
//                pgl.setParent(this);
                return pgl;
            } else if (PGLGraphics3D.ID.equals(renderer)) {
                PGLGraphics3D pgl3d = new PGLGraphics3D(context, primary, w, h);
//                pgl3d.setParent(this);
                return pgl3d;
            } else {
                throw new Error();
//            return super.makeGraphics(w, h, renderer, path, primary);
            }
        }

        @Override
        public void settings() {
            PJOGL.profile = 3;
            if (wHints.isFullScreen()) {
                if (outputDevice > -1) {
                    fullScreen(PGLGraphics.ID, outputDevice + 1);
                } else {
                    fullScreen(PGLGraphics.ID);
                }
            } else {
                size(outputWidth, outputHeight, PGLGraphics.ID);
            }
        }

        @Override
        protected PSurface initSurface() {
            PSurface s = super.initSurface();
            s.setTitle(wHints.getTitle());
            GLWindow window = (GLWindow) surface.getNative();
            window.setAlwaysOnTop(wHints.isAlwaysOnTop());
            window.setUndecorated(wHints.isUndecorated());
            return s;
        }

        @Override
        public void setup() {
            assert pglSurface == null;
            if (pglSurface == null) {
                pglSurface = context.createSurface(surfaceWidth, surfaceHeight, false);
            }
            noCursor();
            frameRate((float) fps);
        }

        @Override
        public synchronized void draw() {
            time = System.nanoTime() + frameNanos;
            fireListeners();
            sink.process(pglSurface, time);
            PImage img = context.asImage(pglSurface);
            context.primary().endOffscreen();
            clear();
            translate(width / 2, height / 2);
            rotate(radians(outputRotation));
            if (outputRotation == 0 || outputRotation == 180) {
                image(img, -outputWidth / 2, -outputHeight / 2,
                        outputWidth, outputHeight);
            } else {
                image(img, -outputHeight / 2, -outputWidth / 2,
                        outputHeight, outputWidth);
            }
        }

        @Override
        public synchronized void dispose() {
            context.dispose();
            sink.disconnect();
            super.dispose();
        }

        @Override
        public void exitActual() {
            running = false;
        }

    }

}
