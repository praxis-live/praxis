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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import net.neilcsmith.praxis.video.ClientConfiguration;
import net.neilcsmith.praxis.video.Player;
import net.neilcsmith.praxis.video.PlayerConfiguration;
import net.neilcsmith.praxis.video.PlayerFactory;
import net.neilcsmith.praxis.video.QueueContext;
import net.neilcsmith.praxis.video.WindowHints;
import net.neilcsmith.praxis.video.pipes.FrameRateListener;
import net.neilcsmith.praxis.video.pipes.SinkIsFullException;
import net.neilcsmith.praxis.video.pipes.VideoPipe;
import net.neilcsmith.praxis.video.render.Surface;
//import org.lwjgl.opengl.Display;
//import org.lwjgl.opengl.GL11;

/**
 *
 * @author Neil C Smith
 */
public class PGLPlayer implements Player {

    private final static Factory FACTORY = new Factory();
    private final static Logger LOG = Logger.getLogger(PGLPlayer.class.getName());
    private final static double DEG_90 = Math.toRadians(90);
    private final static double DEG_180 = Math.toRadians(180);
    private final static double DEG_270 = Math.toRadians(270);
//    private final Object LOCK = new Object();
    private int width, height; // dimensions of surface
    private int outputWidth, outputHeight, outputRotation, outputDevice;
    private double fps; // frames per second
    private long period; // period per frame in nanosecs
//    private long frameIndex; // index of current frame
    private long time; // time of currently computing frame in relation to System.nanotime
    private volatile boolean running = false; // flag to control animation
    private Frame frame = null;
    private PGLApplet applet = null;
//    private BufferStrategy bs = null;
    private PGLOutputSink sink = null;
    // listener list
    private List<FrameRateListener> listeners = new ArrayList<FrameRateListener>();
    private boolean rendering = false; // used by frame rate listeners
    private String title;
    private boolean fullScreen;
    private IntBuffer imageBuffer;
    private int frames;
    private int skips;
//    private Texture image;
//    private TextureRenderer renderer;
    private QueueContext queue;

    private PGLPlayer(String title,
            int width,
            int height,
            double fps,
            boolean fullScreen,
            int outputWidth,
            int outputHeight,
            int outputRotation,
            int outputDevice,
            QueueContext queue) {
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
        sink = new PGLOutputSink();
        this.outputWidth = outputWidth;
        this.outputHeight = outputHeight;
        this.outputRotation = outputRotation;
        this.outputDevice = outputDevice;
        this.queue = queue;
    }

    @Override
    public void run() {
        LOG.info("Starting experimental PGL renderer.");
        running = true;
        try {
            init();
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Unable to start OpenGL player", ex);
            running = false;
            dispose();
            return;
        }
        period = (long) (1000000000.0 / fps);

        time = System.nanoTime();

        long now = 0L;
        long difference = 0L;
        while (running) {
            frames++;
            time += period;
            now = System.nanoTime();
            difference = now - time;
            if (difference > 0) {
                fireListeners();
//                updateOnly();
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST, "Frame skipped - Difference : {0}", (difference));
                }
                skips++;
            } else {
                fireListeners();
                while (difference < -1000000L) {
                    try {
                        queue.process(1, TimeUnit.MILLISECONDS);
//                        Thread.sleep(1);
                    } catch (Exception ex) {
                        LOG.log(Level.WARNING, "Queue exception", ex);
                    }
                    now = System.nanoTime();
                    difference = now - time;
                }
                applet.requestDraw(time);
            }

        }

        dispose();

    }

    private void init() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Dimension dim;
                if (outputRotation == 90 || outputRotation == 270) {
                    dim = new Dimension(outputHeight, outputWidth);
                } else {
                    dim = new Dimension(outputWidth, outputHeight);
                }
                GraphicsDevice gd = findScreenDevice();

                frame = new JFrame(title, gd.getDefaultConfiguration());
                ((JFrame) frame).getContentPane().setBackground(Color.BLACK);
                ((JFrame) frame).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        terminate();
//                        frame.setVisible(false);
//                        synchronized (LOCK) {
//                            try {
//                                LOCK.wait(10000);
//                                frame.setVisible(false);
//                            } catch (InterruptedException ex) {
//                                Logger.getLogger(PGLPlayer.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//                        }
                    }
                });
                frame.setLayout(new GridBagLayout());
                Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank");
                frame.setCursor(cursor);
                applet = new PGLApplet(sink, width, height);
                applet.setMinimumSize(dim);
                applet.setPreferredSize(dim);
                applet.setBackground(Color.BLACK);
                applet.setIgnoreRepaint(true);
                frame.add(applet);
                if (fullScreen) {
                    frame.setUndecorated(true);
                    frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
                    frame.validate();
                    gd.setFullScreenWindow(frame);

                } else {
                    frame.pack();
                    frame.setVisible(true);
                }

                applet.init();
                applet.requestDraw(0);
                LOG.log(Level.FINE, "Frame : {0}", frame.getBounds());
                LOG.log(Level.FINE, "Canvas : {0}", applet.getBounds());

            }
        });

//        context = GLContext.createContext(canvas);
//        surface = context.createSurface(width, height, false);
//        switch (outputRotation) {
//            case 90:
//            case 270:
//                rotated = new GLSurface(height, width, false);
//                break;
//            case 180:
//                rotated = new GLSurface(width, height, false);
//                break;
//
//        }
    }

    private GraphicsDevice findScreenDevice() {
        GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = null;
        if (outputDevice < 0) {
            gd = gEnv.getDefaultScreenDevice();
        } else {
            GraphicsDevice[] screens = gEnv.getScreenDevices();
            if (outputDevice < screens.length) {
                gd = screens[outputDevice];
            } else {
                gd = screens[0];
            }
        }
        LOG.log(Level.FINE, "Searching for screen index : {0}", outputDevice);
        LOG.log(Level.FINE, "Found Screen Device : {0}", gd);
        return gd;
    }

    private void dispose() {
        applet.dispose();

        sink.disconnect();

        disposeFrame();
    }

//    private void disposeGL() {
//        if (context != null) {
//            context.dispose();
//            context = null;
//        }
//        synchronized (LOCK) {
//            LOCK.notifyAll();
//        }
//    }
    private void disposeFrame() {
        try {
            EventQueue.invokeAndWait(new Runnable() {
                @Override
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
//        surface = null;

    }

//    private void updateOnly() {
//        rendering = false;
////        fireListeners();
//        sink.process(surface, time, rendering);
//    }
//    private void updateAndRender() {
//        rendering = true;
////        fireListeners();
//
//        try {
//            sink.process(surface, time, rendering);
//            renderToScreen(surface);
////            switch (outputRotation) {
////                case 0:
////                    surface.draw(g2d, 0, 0, outputWidth, outputHeight);
////                    break;
////                case 90:
////                    rotated.process(GLTransform.ROTATE_90, surface);
////                    rotated.draw(g2d, 0, 0, outputHeight, outputWidth);
////                    break;
////                case 180:
////                    rotated.process(GLTransform.ROTATE_180, surface);
////                    rotated.draw(g2d, 0, 0, outputWidth, outputHeight);
////                    break;
////                case 270:
////                    rotated.process(GLTransform.ROTATE_270, surface);
////                    rotated.draw(g2d, 0, 0, outputHeight, outputWidth);
////                    break;
////            }
//        } catch (Exception exception) {
//            LOG.log(Level.WARNING, "Exception in render", exception);
//            return;
//        }
//    }
//    private void renderToScreen(GLSurface surface) throws Exception {
//        try {
//            GLRenderer renderer = context.getRenderer();
//            renderer.target(null);
//            renderer.clear(); // snapshot seems to have a bug where the background is released?!
//            renderer.setBlendFunction(GL11.GL_ONE, GL11.GL_ZERO);
//            renderer.setColor(1, 1, 1, 1);
//            renderer.draw(surface, 0, 0, outputWidth, outputHeight);
//            renderer.flush();
////            GLRenderer.safe();
//        } catch (Exception exception) {
//            LOG.log(Level.SEVERE, "Error in rendering surface", exception);
//        }
//
//        Display.update();
//    }
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
        running = false;
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

    static PlayerFactory getFactory() {
        return FACTORY;
    }

    private static class Factory implements PlayerFactory {

        @Override
        public Player createPlayer(PlayerConfiguration config, ClientConfiguration[] clients)
                throws Exception {
            if (clients.length != 1 || clients[0].getSourceCount() != 0 || clients[0].getSinkCount() != 1) {
                throw new IllegalArgumentException("Invalid client configuration");
            }

            int width = config.getWidth();
            int height = config.getHeight();
            int outWidth = width;
            int outHeight = height;
            int rotation = 0;
            int device = -1;
            boolean fullscreen = false;
            String title = "OpenGL";

            WindowHints wHints = clients[0].getLookup().get(WindowHints.class);
            if (wHints != null) {
                fullscreen = wHints.isFullScreen();
                title = wHints.getTitle() + " [GL]";
            }

            ClientConfiguration.Dimension dim
                    = clients[0].getLookup().get(ClientConfiguration.Dimension.class);
            if (dim != null) {
                outWidth = dim.getWidth();
                outHeight = dim.getHeight();
            }

            ClientConfiguration.Rotation rot
                    = clients[0].getLookup().get(ClientConfiguration.Rotation.class);
            if (rot != null) {
                rotation = rot.getAngle();
            }

            if (rotation != 0) {
                LOG.warning("OpenGL pipeline doesn't currently support rotation");
                rotation = 0;
            }

            ClientConfiguration.DeviceIndex dev
                    = clients[0].getLookup().get(ClientConfiguration.DeviceIndex.class);
            if (dev != null) {
                device = dev.getValue();
            }

            QueueContext queue = config.getLookup().get(QueueContext.class);

            return new PGLPlayer(title,
                    config.getWidth(),
                    config.getHeight(),
                    config.getFPS(),
                    fullscreen,
                    outWidth,
                    outHeight,
                    rotation,
                    device,
                    queue);

        }
    }
}
