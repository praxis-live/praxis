/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2016 Neil C Smith.
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
package net.neilcsmith.praxis.video.gst1.components;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.video.render.NativePixelData;
import net.neilcsmith.praxis.video.render.PixelData;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.ops.ScaledBlit;
import net.neilcsmith.praxis.video.utils.ResizeMode;
import net.neilcsmith.praxis.video.utils.ResizeUtils;
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Bus;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.FlowReturn;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.GstObject;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.Sample;
import org.freedesktop.gstreamer.Structure;
import org.freedesktop.gstreamer.elements.AppSink;

/**
 *
 * @author Neil C Smith
 */
public abstract class AbstractGstDelegate extends VideoDelegate {

    private static Logger logger = Logger.getLogger(AbstractGstDelegate.class.getName());
    private final AtomicReference<State> state;
    private GStreamerSurface surface;
    private final Lock surfaceLock;
    private Pipeline pipe;
    private AppSink sink;
    private NewSampleListener newSampleListener;
    private NewPrerollListener newPrerollListener;
    private Rectangle srcRegion;
    private int srcWidth;
    private int srcHeight;
    private Rectangle destRegion;
    private int destWidth;
    private int destHeight;
//    private volatile boolean newFrameAvailable = true;
    private volatile boolean looping;

    protected AbstractGstDelegate() {
        GStreamerLibrary.getInstance().init();
        state = new AtomicReference<>(State.New);
        surfaceLock = new ReentrantLock();
    }

    @Override
    public State initialize() throws StateException {
        // this is where we call down to build pipeline
        if (state.compareAndSet(State.New, State.Ready)) {
            try {
                sink = new AppSink("sink");
                sink.set("emit-signals", true);
                newSampleListener = new NewSampleListener();
                newPrerollListener = new NewPrerollListener();
                sink.connect(newSampleListener);
                sink.connect(newPrerollListener);
                if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                    sink.setCaps(Caps.fromString("video/x-raw, format=BGRx"));
                } else {
                    sink.setCaps(Caps.fromString("video/x-raw, format=xRGB"));
                }
                pipe = buildPipeline(sink);
                makeBusConnections(pipe.getBus());
                pipe.setState(org.freedesktop.gstreamer.State.READY);
                pipe.getState(); // in gst thread?
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
        Gst.getExecutor().execute(new Runnable() {

            @Override
            public void run() {
                doPlay();

            }
        });
    }

    protected void doPlay() {
        pipe.play();
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
        Gst.getExecutor().execute(new Runnable() {

            @Override
            public void run() {
                doPause();

            }
        });
    }

    protected void doPause() {
        pipe.pause();
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
        Gst.getExecutor().execute(new Runnable() {

            @Override
            public void run() {
                doStop();

            }
        });
    }

    protected void doStop() {
        pipe.stop();
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
        Gst.getExecutor().execute(new Runnable() {

            @Override
            public void run() {
                doDispose();

            }
        });
    }

    protected void doDispose() {
        sink.disconnect(newSampleListener);
        sink.disconnect(newPrerollListener);
        sink.dispose();
        pipe.setState(org.freedesktop.gstreamer.State.NULL);
        pipe.getState();
        pipe.getBus().dispose();
        pipe.dispose();
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
    public void setLooping(boolean loop) {
        if (loop) {
            if (isLoopable()) {
                looping = true;
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            looping = false;
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

    public void process(Surface output) {
        State s = state.get();
        if (s == State.Playing || s == State.Paused) {
            drawVideo(output);
        }

    }

    private void drawVideo(Surface output) {

        surfaceLock.lock();

        try {
            if (surface != null && surface.sample != null) {
                Buffer b = surface.sample.getBuffer();
                surface.nativeData = b.map(false);
                checkRegions(output);
                output.process(new ScaledBlit().setSourceRegion(srcRegion).setDestinationRegion(destRegion),
                        surface);
                surface.nativeData = null;
                b.unmap();
//                newFrameAvailable = false;
            }

        } finally {
            surfaceLock.unlock();
        }
    }

    private void checkRegions(Surface output) {
        if (srcRegion == null || destRegion == null
                || srcWidth != surface.getWidth() || srcHeight != surface.getHeight()
                || destWidth != output.getWidth() || destHeight != output.getHeight()) {
            Rectangle src = new Rectangle();
            Rectangle dest = new Rectangle();
            Dimension srcDim = new Dimension(surface.getWidth(), surface.getHeight());
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

    private void makeBusConnections(Bus bus) {
        bus.connect(new Bus.ERROR() {

            public void errorMessage(GstObject arg0, int arg1, String arg2) {
                error(arg0 + " : " + arg2, null);
            }
        });
        bus.connect(new Bus.EOS() {

            public void endOfStream(GstObject arg0) {
                doEOS();
            }
        });
    }

    protected void doEOS() {
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

    protected abstract Pipeline buildPipeline(Element sink) throws Exception;

    private class NewSampleListener implements AppSink.NEW_SAMPLE {

        @Override
        public FlowReturn newSample(AppSink sink) {
            surfaceLock.lock();
            Sample sample = sink.pullSample();
            Structure capsStruct = sample.getCaps().getStructure(0);
            int width = capsStruct.getInteger("width");
            int height = capsStruct.getInteger("height");
            try {
                if (surface == null || surface.getWidth() != width || surface.getHeight() != height) {
                    if (surface != null && surface.sample != null) {
                        surface.sample.dispose();
                    }
                    surface = new GStreamerSurface(width, height);
                } else {
                    if (surface.sample != null) {
                        surface.sample.dispose();
                    }
                }
                surface.sample = sample;
                surface.modCount++;
            } finally {
                surfaceLock.unlock();
            }
            return FlowReturn.OK;
        }

    }

    private class NewPrerollListener implements AppSink.NEW_PREROLL {

        @Override
        public FlowReturn newPreroll(AppSink sink) {
            surfaceLock.lock();
            Sample sample = sink.pullPreroll();
            Structure capsStruct = sample.getCaps().getStructure(0);
            int width = capsStruct.getInteger("width");
            int height = capsStruct.getInteger("height");
            try {
                if (surface == null || surface.getWidth() != width || surface.getHeight() != height) {
                    if (surface != null && surface.sample != null) {
                        surface.sample.dispose();
                    }
                    surface = new GStreamerSurface(width, height);
                } else {
                    if (surface.sample != null) {
                        surface.sample.dispose();
                    }
                }
                surface.sample = sample;
                surface.modCount++;
            } finally {
                surfaceLock.unlock();
            }
            return FlowReturn.OK;
        }

    }

    private static class GStreamerSurface extends Surface implements NativePixelData {

        private static PixelData[] EMPTY = new PixelData[0];

        private Sample sample;
        private ByteBuffer nativeData;
        private int[] data;
        private int modCount;

        private GStreamerSurface(int width, int height) {
            super(width, height, false);
        }

        @Override
        public int getModCount() {
            return modCount;
        }

        @Override
        public void process(SurfaceOp op, Surface... inputs) {
            if (inputs.length > 0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            modCount++;
            op.process(this, EMPTY);
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isClear() {
            return false;
        }

        @Override
        public void release() {
            // no op
        }

        @Override
        public void copy(Surface source) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean checkCompatible(Surface surface, boolean checkDimensions, boolean checkAlpha) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Surface createSurface(int width, int height, boolean alpha) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int[] getData() {
            if (data == null) {
                data = new int[width * height];
            }
            IntBuffer ib = getNativeData().asIntBuffer();
            ib.get(data);
            return data;
        }

        public int getOffset() {
            return 0;
        }

        public int getScanline() {
            return width;
        }

        public ByteBuffer getNativeData() {
            return nativeData;
        }

        public Format getFormat() {
            return Format.INT_RGB;
        }

    }

}
