/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008/09 - Neil C Smith. All rights reserved.
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
package net.neilcsmith.ripl.gstreamer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.delegates.VideoDelegate;
import net.neilcsmith.ripl.ops.Blit;
import net.neilcsmith.ripl.ops.GraphicsOp;
import net.neilcsmith.ripl.utils.ResizeMode;
import net.neilcsmith.ripl.utils.ResizeUtils;
import org.gstreamer.Bus;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.Pipeline;
import org.gstreamer.elements.RGBDataSink;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractGstDelegate extends VideoDelegate {

    private static Logger logger = Logger.getLogger(AbstractGstDelegate.class.getName());
    private final AtomicReference<State> state;
    private BufferedImage image;
    private final Lock imageLock;
    private Pipeline pipe;
    private Rectangle srcRegion;
    private int srcWidth;
    private int srcHeight;
    private Rectangle destRegion;
    private int destWidth;
    private int destHeight;
    private volatile boolean waitOnFrame;
    private volatile boolean newFrameAvailable = true;
    private volatile boolean looping;

    protected AbstractGstDelegate() {
        Gst.init();
        state = new AtomicReference<VideoDelegate.State>(State.New);
        imageLock = new ReentrantLock();
    }

    @Override
    public State initialize() throws StateException {
        // this is where we call down to build pipeline
        if (state.compareAndSet(State.New, State.Ready)) {
            try {
                pipe = buildPipeline(new RGBListener());
                makeBusConnections(pipe.getBus());
                pipe.setState(org.gstreamer.State.READY); // in gst thread?
                return State.Ready;
            } catch (Exception ex) {
                error("Error building pipeline", ex);
                return State.Error;
            }
        } else {
            throw new StateException();
        }
    }

    @Override
    public final void play() throws StateException {
        State s;
        do {
            s = state.get();
            if (s == State.Playing) {
                return;
            }
            if (s != State.Paused && s != State.Ready) {
                throw new StateException("Illegal call to play when state is " + s);
            }
        } while (!state.compareAndSet(s, State.Playing));
        try {
            doPlay();
        } catch (Exception ex) {
            error("Error while attempting Play", ex);
        }
    }

    protected void doPlay() throws Exception {
        Gst.getExecutor().execute(new Runnable() {

            public void run() {
                pipe.play();
            }
        });
    }

    @Override
    public final void pause() throws StateException {
        State s;
        do {
            s = state.get();
            if (s == State.Paused) {
                return;
            }
            if (s != State.Playing && s != State.Ready) {
                throw new StateException("Illegal call to pause when state is " + s);
            }
        } while (!state.compareAndSet(s, State.Paused));
        try {
            doPause();
        } catch (Exception ex) {
            error("Error while attempting Pause", ex);
        }
    }

    protected void doPause() throws Exception {
        Gst.getExecutor().execute(new Runnable() {

            public void run() {
                pipe.pause();

            }
        });
    }

    @Override
    public final void stop() throws StateException {
        State s;
        do {
            s = state.get();
            if (s == State.Ready) {
                return;
            }
            if (s != State.Playing && s != State.Paused) {
                throw new StateException("Illegal call to stop when state is " + s);
            }
        } while (!state.compareAndSet(s, State.Ready));
        try {
            doStop();
        } catch (Exception ex) {
            error("Error while attempting Stop", ex);
        }
    }

    protected void doStop() throws Exception {
        Gst.getExecutor().execute(new Runnable() {

            public void run() {
                pipe.stop();
            }
        });
    }

    @Override
    public final void dispose() {
        State s;
        do {
            s = state.get();
            if (s == State.Disposed) {
                return;
            }
        } while (!state.compareAndSet(s, State.Disposed));
        doDispose();
    }

    protected void doDispose() {
        pipe.setState(org.gstreamer.State.NULL);
        pipe.dispose();
//        Gst.deinit();

    }

    protected void error(String message, Exception ex) {
        State s;
        do {
            s = state.get();
            if (s == State.Disposed) {
                break;
            }
        } while (!state.compareAndSet(s, State.Error));
        logger.log(Level.WARNING, message);
        if (ex != null && logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Detailed error log :", ex);
        }
    }

    @Override
    public State getState() {
        return state.get();
    }

    @Override
    public boolean canWaitOnFrame() {
        return true;
    }

    @Override
    public void setWaitOnFrame(boolean wait) {
        waitOnFrame = wait;
    }

    @Override
    public boolean getWaitOnFrame() {
        return waitOnFrame;
    }

    @Override
    public void setLooping(boolean loop) {
        if (loop) {
            if (isLoopable()) {
                looping = true;
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    @Override
    public boolean isLooping() {
        return looping;
    }

    @Override
    public long getDuration() {
        return pipe.queryDuration(TimeUnit.NANOSECONDS);
    }

    @Override
    public long getPosition() {
        return pipe.queryPosition(TimeUnit.NANOSECONDS);
    }

    @Override
    public void process(Surface input, Surface output) {
        if (input != output) {
            output.process(Blit.op(), input);
        }
        State s = state.get();
        if (s == State.Playing || s == State.Paused) {
            drawVideo(output);
        }

    }

    private void drawVideo(Surface output) {
        if (waitOnFrame) {
            while (state.get() == State.Playing && !newFrameAvailable) {
                Thread.yield();
            }
        }

        imageLock.lock();

        try {
            if (image != null) {
                checkRegions(output);
                output.process(new GraphicsOp(new GraphicsOp.Callback() {
                    public void draw(Graphics2D g2d, Image[] images) {
                        
                        int dx1 = destRegion.x;
                        int dy1 = destRegion.y;
                        int dx2 = dx1 + destRegion.width;
                        int dy2 = dy1 + destRegion.height;
                        int sx1 = srcRegion.x;
                        int sy1 = srcRegion.y;
                        int sx2 = sx1 + srcRegion.width;
                        int sy2 = sy1 + srcRegion.height;
                        g2d.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
                    }
                }));
                newFrameAvailable = false;
            }

        } finally {
            imageLock.unlock();
        }
    }

    private void checkRegions(Surface output) {
        if (srcRegion == null || destRegion == null
                || srcWidth != image.getWidth() || srcHeight != image.getHeight()
                || destWidth != output.getWidth() || destHeight != output.getHeight()) {
            Rectangle src = new Rectangle();
            Rectangle dest = new Rectangle();
            Dimension srcDim = new Dimension(image.getWidth(), image.getHeight());
            Dimension destDim = new Dimension(output.getWidth(), output.getHeight());
            ResizeUtils.calculateBounds(srcDim, destDim, getResizeMode(), src, dest);
            srcRegion = src;
            destRegion = dest;
        }

    }

    @Override
    public void setResizeMode(ResizeMode mode) {
        super.setResizeMode(mode);
        srcRegion = null;
        destRegion = null;
    }

    private BufferedImage getImage(int width, int height) {
        if (image != null && image.getWidth() == width && image.getHeight() == height) {
            return image;
        }
        if (image != null) {
            image.flush();
        }
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//        image.setAccelerationPriority(0.0f);
        return image;
    }

    private void makeBusConnections(Bus bus) {
        bus.connect(new Bus.ERROR() {

            public void errorMessage(GstObject arg0, int arg1, String arg2) {
                error(arg0 + " : " + arg2, null);
            }
        });
        bus.connect(new Bus.EOS() {

            public void endOfStream(GstObject arg0) {
                try {
                    if (isLooping()) {
                        pipe.seek(0, TimeUnit.NANOSECONDS);
                    } else {
                        stop();
                    }
                } catch (Exception ex) {
                    error("", ex);
                }
            }
        });
    }

    private class RGBListener implements RGBDataSink.Listener {

        public void rgbFrame(int width, int height, IntBuffer rgb) {

            if (!imageLock.tryLock()) {
                return;
            }

            try {
                image = getImage(width, height);
                int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                rgb.get(pixels, 0, width * height);
                newFrameAvailable = true;
            } finally {
                imageLock.unlock();
            }


        }
    }

    protected abstract Pipeline buildPipeline(RGBDataSink.Listener listener) throws Exception;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }
}
