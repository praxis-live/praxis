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
import net.neilcsmith.praxis.video.VideoSettings;
import net.neilcsmith.praxis.video.WindowHints;
import net.neilcsmith.praxis.video.pipes.FrameRateListener;
import net.neilcsmith.praxis.video.pipes.SinkIsFullException;
import net.neilcsmith.praxis.video.pipes.VideoPipe;
import net.neilcsmith.praxis.video.render.Surface;

/**
 *
 * @author Neil C Smith
 */
public class PGLPlayer implements Player {

    private final static Factory FACTORY = new Factory();
    private final static Logger LOG = Logger.getLogger(PGLPlayer.class.getName());
    private int width, height; // dimensions of surface
    private int outputWidth, outputHeight, outputRotation, outputDevice;
    private double fps; // frames per second
    private long period; // period per frame in nanosecs
    private long time; // time of currently computing frame in relation to System.nanotime
    private volatile boolean running = false; // flag to control animation
    private Frame frame = null;
    private PGLApplet applet = null;
    private PGLOutputSink sink = null;
    private List<FrameRateListener> listeners = new ArrayList<>();
    private boolean rendering = false; // used by frame rate listeners
    private final WindowHints wHints;
    private int frames;
    private int skips;
    private QueueContext queue;

    private PGLPlayer(int width,
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
        this.width = width;
        this.height = height;
        this.fps = fps;
        sink = new PGLOutputSink();
        this.outputWidth = outputWidth;
        this.outputHeight = outputHeight;
        this.outputRotation = outputRotation;
        this.outputDevice = outputDevice;
        this.wHints = wHints;
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
                String title = wHints.getTitle();
                if (title.isEmpty()) {
                    title = "Praxis LIVE [GL]";
                } else {
                    title += " [GL]";
                }
                frame = new JFrame(title, gd.getDefaultConfiguration());
                ((JFrame) frame).getContentPane().setBackground(Color.BLACK);
                ((JFrame) frame).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        terminate();
                    }
                });
                frame.setLayout(new GridBagLayout());
                Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank");
                frame.setCursor(cursor);
                applet = new PGLApplet(sink,
                        width,
                        height,
                        dim.width,
                        dim.height,
                        outputRotation);
                applet.setMinimumSize(dim);
                applet.setPreferredSize(dim);
                applet.setBackground(Color.BLACK);
                applet.setIgnoreRepaint(true);
                frame.add(applet);
                if (wHints.isFullScreen()) {
                    boolean fsem = VideoSettings.isFullScreenExclusive();
                    frame.setUndecorated(true);
                    frame.validate();
                    if (fsem) {
                        gd.setFullScreenWindow(frame);
                    } else {
                        frame.pack();
                        frame.setVisible(true);
                        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
                    }

                } else {
                    if (wHints.isUndecorated()) {
                        frame.setUndecorated(true);
                    }
                    if (wHints.isAlwaysOnTop()) {
                        frame.setAlwaysOnTop(true);
                    }
                    frame.pack();
                    frame.setVisible(true);
                }

                applet.init();
                applet.requestDraw(0);
                LOG.log(Level.FINE, "Frame : {0}", frame.getBounds());
                LOG.log(Level.FINE, "Canvas : {0}", applet.getBounds());

            }
        });

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
            switch (rotation) {
                case 0:
                case 90:
                case 180:
                case 270:
                    break;
                default:
                    LOG.warning("OpenGL pipeline doesn't currently support that rotation");
                    rotation = 0;
            }

            ClientConfiguration.DeviceIndex dev
                    = clients[0].getLookup().get(ClientConfiguration.DeviceIndex.class);
            if (dev != null) {
                device = dev.getValue();
            }

            WindowHints wHints = clients[0].getLookup().get(WindowHints.class);
            if (wHints == null) {
                wHints = new WindowHints();
            }
            
            QueueContext queue = config.getLookup().get(QueueContext.class);

            return new PGLPlayer(
                    config.getWidth(),
                    config.getHeight(),
                    config.getFPS(),
                    outWidth,
                    outHeight,
                    rotation,
                    device,
                    wHints,
                    queue);

        }
    }
}
