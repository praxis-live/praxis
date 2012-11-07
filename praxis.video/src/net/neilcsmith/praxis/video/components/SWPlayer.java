/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Neil C Smith.
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
package net.neilcsmith.praxis.video.components;

import java.awt.event.KeyEvent;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.video.Player;
import net.neilcsmith.praxis.video.pipes.FrameRateListener;
import net.neilcsmith.praxis.video.pipes.VideoPipe;
import net.neilcsmith.praxis.video.pipes.SinkIsFullException;
import net.neilcsmith.praxis.video.render.Surface;

/**
 *
 * @author Neil C Smith
 */
class SWPlayer implements Player {

    private final static Logger LOG = Logger.getLogger(SWPlayer.class.getName());

    private final static double DEG_90 = Math.toRadians(90);
    private final static double DEG_180 = Math.toRadians(180);
    private final static double DEG_270 = Math.toRadians(270);

    private int noSleepsPerYield = 0; // maximum number of frames without sleep before yielding
    private int maxSkip = 2; // maximum number of frames that can be skipped before rendering
    private int width,  height; // dimensions of surface
    private int outputWidth, outputHeight, outputRotation;
    private double fps; // frames per second
    private long period; // period per frame in nanosecs
//    private long frameIndex; // index of current frame
    private long time; // time of currently computing frame in relation to System.nanotime
    private volatile boolean running = false; // flag to control animation
    private SWSurface surface = null; // surface to be passed up tree
    private SWSurface rotated = null; // rotated surface if required
    private Frame frame = null;
    private Canvas canvas = null;
    private BufferStrategy bs = null;
    private OutputSink sink = null;
    // listener list
    private List<FrameRateListener> listeners = new ArrayList<FrameRateListener>();
    private boolean rendering = false; // used by frame rate listeners
    private String title;
    private boolean fullScreen;

    private SWPlayer(String title,
            int width,
            int height,
            double fps,
            boolean fullScreen,
            int outputWidth,
            int outputHeight,
            int outputRotation) {
        if (width <= 0 || height <= 0 || fps <= 0) {
            throw new IllegalArgumentException();
        }
        if (title == null) {
            throw new NullPointerException();
        }
        this.width = width;
        this.height = height;
        this.fps = fps;
        this.title = title;
        this.fullScreen = fullScreen;
        sink = new OutputSink();
        this.outputWidth = outputWidth;
        this.outputHeight = outputHeight;
        this.outputRotation = outputRotation;
    }

    public void run() {
        LOG.info("Starting software renderer.");
        running = true;
        try {
            init();
        } catch (Exception ex) {
            running = false;
            dispose();
            return;
        }
        period = (long) (1000000000.0 / fps);


        //render first frame
        time = System.nanoTime();
        updateAndRender();

        long afterTime = 0L; // time after render
        long sleepTime = 0L; // time to sleep
        long overSleepTime = 0L; // time over slept
        long excess = 0L; // excess time taken to render frame
        int noSleeps = 0;

        time = System.nanoTime();

        // animation loop
//        while (running) {
//            updateAndRender();
//            afterTime = System.nanoTime();
//            sleepTime = (period - (afterTime - time)) - overSleepTime;
//            if (sleepTime > 0) {
//                try {
//                    Thread.sleep(sleepTime / 1000000L);
//                } catch (InterruptedException ex) {
//                }
//                overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
//            } else {
//                excess -= sleepTime;
//                overSleepTime = 0L;
//                noSleeps++;
//                if (noSleeps > noSleepsPerYield) {
//                    Thread.yield();
//                    noSleeps = 0;
//                }
//            }
//            time += period;
//
//            int skips = 0;
//            while ((excess > period) && (skips < maxSkip)) {
//                excess -= period;
//                updateOnly();
//                time += period;
//                skips++;
//            }
//
//        }

//        long minSleepTime = 1000000L;
        long now = 0L;
        long difference = 0L;
        while (running) {
            time += period;
            now = System.nanoTime();
            difference = now - time;
            if (difference > 0) {
                updateOnly();
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST, "Frame skipped - Difference : " + (difference));
                }
            } else {
                while (difference < -1000000L) {
                    try {
                        Thread.sleep(1);
                    } catch (Exception ex) {}
                    now = System.nanoTime();
                    difference = now - time;
                }
                updateAndRender();
            }


        }

        dispose();

    }

    private void init() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {

            public void run() {
                Dimension dim;
                if (outputRotation == 90 || outputRotation == 270) {
                    dim = new Dimension(outputHeight, outputWidth);
                } else {
                    dim = new Dimension(outputWidth, outputHeight);
                }
                frame = new Frame(title);
//                frame.setIgnoreRepaint(true);
                frame.setBackground(Color.BLACK);
                frame.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
                        terminate();
                    }
                    });
                frame.setLayout(new GridBagLayout());
                Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank");
                frame.setCursor(cursor);
                canvas = new Canvas();
                canvas.setMinimumSize(dim);
                canvas.setPreferredSize(dim);
                canvas.setBackground(Color.BLACK);
                canvas.setIgnoreRepaint(true);
                frame.add(canvas);
                if (fullScreen) {
                    frame.setUndecorated(true);
                    frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
                    frame.validate();
                    GraphicsDevice gd =
                            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                    gd.setFullScreenWindow(frame);
                    
                } else {
                    frame.pack();
                    frame.setVisible(true);
                }

                LOG.info("Frame : " + frame.getBounds());
                LOG.info("Canvas : " + canvas.getBounds());

                if (Boolean.getBoolean("ripl.exp.screensaver")) {
                    ScreenSaverListener l = new ScreenSaverListener();
                    frame.addKeyListener(l);
                    canvas.addKeyListener(l);
                }

            }
            });

        canvas.createBufferStrategy(2);
        bs = canvas.getBufferStrategy();

        surface = new SWSurface(width, height, false);

        switch(outputRotation) {
            case 90:
            case 270:
                rotated = new SWSurface(height, width, false);
                break;
            case 180:
                rotated = new SWSurface(width, height, false);
                break;

        }

    }

    private void dispose() {
        try {
            EventQueue.invokeAndWait(new Runnable() {

                public void run() {
                    if (frame != null) {
                        frame.setVisible(false);
                        frame.dispose();
                        frame = null;
                    }
                }
            });
        } catch (Exception ex) {
        }
        bs = null;
        surface = null;
//        Pipe[] sources = sink.getSources();
//        for (Pipe src : sources) {
//            sink.removeSource(src);
//        }
        sink.removeSource(sink.source);

    }

    private void updateOnly() {
        rendering = false;
        fireListeners();
        sink.process(surface, time, rendering);
    }

    private void updateAndRender() {
        rendering = true;
        fireListeners();
        Graphics2D g2d = (Graphics2D) bs.getDrawGraphics();
        try {
            sink.process(surface, time, rendering);
            switch (outputRotation) {
                case 0:
                    surface.draw(g2d, 0, 0, outputWidth, outputHeight);
                    break;
                case 90:
                    rotated.process(SWTransform.ROTATE_90, surface);
                    rotated.draw(g2d, 0, 0, outputHeight, outputWidth);
                    break;
                case 180:
                    rotated.process(SWTransform.ROTATE_180, surface);
                    rotated.draw(g2d, 0, 0, outputWidth, outputHeight);
                    break;
                case 270:
                    rotated.process(SWTransform.ROTATE_270, surface);
                    rotated.draw(g2d, 0, 0, outputHeight, outputWidth);
                    break;
            }
        } catch (Exception exception) {
            LOG.log(Level.WARNING, "Exception in render", exception);
            g2d.dispose();
            return;
        }
        if (!bs.contentsLost()) {
            bs.show();
            Toolkit.getDefaultToolkit().sync();
        }
        g2d.dispose();
    }



    private void fireListeners() {
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
            listeners.get(i).nextFrame(this);
        }
    }

    public VideoPipe getSource(int index) {
        throw new IndexOutOfBoundsException();
    }

    public int getSourceCount() {
        return 0;
    }

    public VideoPipe getSink(int index) {
        if (index == 0) {
            return getOutputSink();
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public int getSinkCount() {
        return 1;
    }

    public VideoPipe getOutputSink() {
        return sink;
    }

    public void terminate() {
        running = false;
    }

    public void addFrameRateListener(FrameRateListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
    }

    public void removeFrameRateListener(FrameRateListener listener) {
        listeners.remove(listener);
    }

    public long getTime() {
        return time;
    }

    public boolean isRendering() {
        return rendering;
    }

    private class OutputSink extends VideoPipe {

        private VideoPipe source; // only allow one connection
        private long time;
        private boolean render;

        public void registerSource(VideoPipe source) {
            if (this.source == null) {
                this.source = source;
            } else {
                throw new SinkIsFullException();
            }
        }

        public void unregisterSource(VideoPipe source) {
            if (this.source == source) {
                this.source = null;
            }
        }

        public boolean isRenderRequired(VideoPipe source, long time) {
            if (source == this.source && time == this.time) {
                return render;
            } else {
                return false;
            }
        }

        private void process(Surface surface, long time, boolean render) {
            this.render = render;
            this.time = time;
            if (this.source != null) {
                callSource(source, surface, time);
            } else {
                surface.clear();
            }
        }

        @Override
        public int getSourceCount() {
            return source == null ? 0 : 1;
        }

        @Override
        public int getSourceCapacity() {
            return 1;
        }

        @Override
        public VideoPipe getSource(int idx) {
            if (idx == 0 && source != null) {
                return source;
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        @Override
        public int getSinkCount() {
            return 0;
        }

        @Override
        public int getSinkCapacity() {
            return 0;
        }

        @Override
        public VideoPipe getSink(int idx) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        protected void process(VideoPipe sink, Surface buffer, long time) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void registerSink(VideoPipe sink) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void unregisterSink(VideoPipe sink) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }


    private class ScreenSaverListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            System.exit(0);
        }
        
    }

    public static SWPlayer create(
            String title,
            int width,
            int height,
            double fps,
            boolean fullscreen) {
        return new SWPlayer(title, width, height, fps, fullscreen, width, height, 0);
    }

    public static SWPlayer create(
            String title,
            int width,
            int height,
            double fps,
            boolean fullscreen,
            int outputWidth,
            int outputHeight,
            int rotation) {
        if (rotation != 0 && rotation != 90 && rotation != 180 && rotation != 270) {
            rotation = 0;
            LOG.warning("Rotation should be 0, 90, 180 or 270. Switching to 0.");
        }
        return new SWPlayer(title, width, height, fps, fullscreen, outputWidth, outputHeight, rotation);
    }

}
