/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 - Neil C Smith. All rights reserved.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.ripl.render;

import net.neilcsmith.ripl.FrameRateListener;
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
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import net.neilcsmith.ripl.Player;
import net.neilcsmith.ripl.Sink;
import net.neilcsmith.ripl.SinkIsFullException;
import net.neilcsmith.ripl.Source;
import net.neilcsmith.ripl.SourceIsFullException;
import net.neilcsmith.ripl.Surface;

/**
 *
 * @author Neil C Smith
 * @TODO refactor into Player interface in core.
 */
public class RiplPlayer implements Player {

    private int noSleepsPerYield = 0; // maximum number of frames without sleep before yielding
    private int maxSkip = 2; // maximum number of frames that can be skipped before rendering
    private int width,  height; // dimensions of image
    private double fps; // frames per second
    private long period; // period per frame in nanosecs
//    private long frameIndex; // index of current frame
    private long time; // time of currently computing frame in relation to System.nanotime
    private volatile boolean running = false; // flag to control animation
    private ImageSurface surface = null; // surface to be passed up tree
    private Frame frame = null;
    private Canvas canvas = null;
    private BufferStrategy bs = null;
    private OutputSink sink = null;
    // listener list
    private List<FrameRateListener> listeners = new ArrayList<FrameRateListener>();
    private boolean rendering = false; // used by frame rate listeners
    private String title;
    private boolean fullScreen;

    public RiplPlayer(int width, int height, double fps) {
        this("RIPL", width, height, fps, false);

    }

    public RiplPlayer(String title, int width, int height, double fps, boolean fullScreen) {
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
    }

    public void run() {
        running = true;
//        time = System.nanoTime();
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
        while (running) {
            updateAndRender();
            afterTime = System.nanoTime();
            sleepTime = (period - (afterTime - time)) - overSleepTime;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1000000L);
                } catch (InterruptedException ex) {
                }
                overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
            } else {
                excess -= sleepTime;
                overSleepTime = 0L;
                noSleeps++;
                if (noSleeps > noSleepsPerYield) {
                    Thread.yield();
                    noSleeps = 0;
                }
            }
            time += period;

            int skips = 0;
            while ((excess > period) && (skips < maxSkip)) {
                excess -= period;
                updateOnly();
                time += period;
                skips++;
            }

        }

        dispose();

    }

    private void init() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {

            public void run() {
                Dimension dim = new Dimension(width, height);
                frame = new Frame(title);
//                frame.setSize(dim);
                frame.setIgnoreRepaint(true);
                frame.setBackground(Color.BLACK);
                frame.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
//                        System.exit(0);
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
//                    frame.setAlwaysOnTop(true);
//                    frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
                    GraphicsDevice gd =
                            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                    gd.setFullScreenWindow(frame);
                } else {
                    frame.pack();
                    frame.setVisible(true);
                }


//                    canvas.createBufferStrategy(2);
//                    bs = canvas.getBufferStrategy();

            }
            });

//            surface = new ImageSurface(width, height, false);
//        surface = new DirectSurface(width, height);

        canvas.createBufferStrategy(2);
        bs = canvas.getBufferStrategy();

//        surface = new VolatileImageSurface(width, height, canvas.getGraphicsConfiguration());
        surface = new ImageSurface(width, height, false);

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
        Source[] sources = sink.getSources();
        for (Source src : sources) {
            sink.removeSource(src);
        }

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
//        Graphics g2d = canvas.getGraphics();
//        if (surface instanceof DirectSurface) {
//            try {
//                ((DirectSurface) surface).setGraphics(g2d);
//                sink.process(surface, time, rendering);
//            } catch (Exception exception) {
//                System.out.println("Exception caught");
//                g2d.dispose();
//                return;
//            }
//        } else {
        try {
            sink.process(surface, time, rendering);
//                g2d.drawImage(surface.getImageData().getImage(), 0, 0, null);
            g2d.drawImage(surface.getImage(), 0, 0, null);
        } catch (Exception exception) {
            g2d.dispose();
            return;
        }
//        }
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

    public Source getSource(int index) {
        throw new IndexOutOfBoundsException();
    }

    public int getSourceCount() {
        return 0;
    }

    public Sink getSink(int index) {
        if (index == 0) {
            return getOutputSink();
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public int getSinkCount() {
        return 1;
    }

    public Sink getOutputSink() {
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

    private class OutputSink implements Sink {

        private Source source; // only allow one connection
        private long time;
        private boolean render;

        public void addSource(Source source) throws SinkIsFullException, SourceIsFullException {
            if (this.source == null) {
                source.registerSink(this);
                this.source = source;
            } else {
                throw new SinkIsFullException();
            }
        }

        public void removeSource(Source source) {
            if (this.source == source) {
                source.unregisterSink(this);
                this.source = null;
            }
        }

        public boolean isRenderRequired(Source source, long time) {
            if (source == this.source && time == this.time) {
                return render;
            } else {
                return false;
            }
        }

        public Source[] getSources() {
            if (source == null) {
                return new Source[0];
            } else {
                return new Source[]{source};
            }
        }

        private void process(Surface surface, long time, boolean render) {
            this.render = render;
            this.time = time;
            if (this.source != null) {
                source.process(surface, this, time);
            } else {
                surface.clear();
            }
        }
    }
//    private class DirectSurface extends Surface {
//
//        private Graphics2D g2d;
//
//        public DirectSurface(int width, int height) {
//            super(width, height, false);
//        }
//
//        public Image getImage() {
//            throw new UnsupportedOperationException();
//        }
//
//        public int[] getPixels() {
//            throw new UnsupportedOperationException();
//        }
//
//        private void setGraphics(Graphics2D g2d) {
//            this.g2d = g2d;
//        }
//
//        public Graphics2D getGraphics() {
//            return g2d;
//        }
//
//        public boolean isCompatible(Surface surface) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Surface createSurface(int width, int height, boolean alpha, SurfaceCapabilities caps) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public SurfaceCapabilities getCapabilities() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void clear() {
//            g2d.setColor(Color.BLACK);
//            g2d.fillRect(0, 0, width, height);
//        }
//
//        @Override
//        public void release() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//    }

//    private class VolatileImageSurface extends Surface {
//
//        VolatileImage image;
//        GraphicsConfiguration gc;
//
//        public VolatileImageSurface(int width, int height, GraphicsConfiguration gc) {
//            super(width, height, false);
//            this.gc = gc;
//            this.image = gc.createCompatibleVolatileImage(width, height);
//            System.out.println("Image is Accelerated : " + image.getCapabilities().isAccelerated());
//            System.out.println("Image is true volatile : " + image.getCapabilities().isTrueVolatile());
//        }
//
//        public Image getImage() {
//            return image;
//        }
//
//        public int[] getPixels() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public Graphics2D getGraphics() {
//            return image.createGraphics();
//        }
//
//        public boolean isCompatible(Surface surface) {
//            return surface instanceof ImageSurface;
//        }
//
//        public Surface createSurface(int width, int height, boolean alpha, SurfaceCapabilities caps) {
//            return new ImageSurface(width, height, alpha);
//        }
//
//        public SurfaceCapabilities getCapabilities() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void clear() {
//            Graphics2D g2d = image.createGraphics();
//            g2d.setColor(Color.BLACK);
//            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
//        }
//
//        @Override
//        public void release() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//    }
}
