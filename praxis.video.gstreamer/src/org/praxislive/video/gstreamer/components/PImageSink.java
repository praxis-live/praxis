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
 */
package org.praxislive.video.gstreamer.components;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.FlowReturn;
import org.freedesktop.gstreamer.Sample;
import org.freedesktop.gstreamer.Structure;
import org.freedesktop.gstreamer.elements.AppSink;
import org.praxislive.video.code.userapi.PImage;
import org.praxislive.video.render.NativePixelData;
import org.praxislive.video.render.PixelData;
import org.praxislive.video.render.Surface;
import org.praxislive.video.render.SurfaceOp;

/**
 *
 * @author Neil C Smith - http://www.neilcsmith.net
 */
class PImageSink {

    private final static String DEFAULT_CAPS;

    static {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            DEFAULT_CAPS = "video/x-raw, format=BGRx";
        } else {
            DEFAULT_CAPS = "video/x-raw, format=xRGB";
        }
    }

    private final AppSink sink;
    private final Lock imageLock;
    private final NewSampleListener newSampleListener;
    private final NewPrerollListener newPrerollListener;

    private GStreamerSurface surface;

    private int requestWidth;
    private int requestHeight;
    private int requestRate;

    PImageSink() {
        this(new AppSink("PImageSink"));
    }
    
    PImageSink(AppSink sink) {
        this.sink = sink;
        sink.set("emit-signals", true);
        newSampleListener = new NewSampleListener();
        newPrerollListener = new NewPrerollListener();
        sink.connect(newSampleListener);
        sink.connect(newPrerollListener);
        sink.setCaps(Caps.fromString(DEFAULT_CAPS));
        imageLock = new ReentrantLock();
    }

    private String buildCapsString() {
        if (requestWidth < 1 && requestHeight < 1 && requestRate < 1) {
            return DEFAULT_CAPS;
        }
        StringBuilder sb = new StringBuilder(DEFAULT_CAPS);
        if (requestWidth > 0) {
            sb.append(",width=");
            sb.append(requestWidth);
        }
        if (requestHeight > 0) {
            sb.append(",height=");
            sb.append(requestHeight);
        }
        if (requestRate > 0) {
            sb.append(",framerate=");
            sb.append(requestRate);
            sb.append("/1");
        }
        return sb.toString();
    }

    AppSink getElement() {
        return sink;
    }

    void requestFrameSize(int width, int height) {
        this.requestWidth = width;
        this.requestHeight = height;
        sink.setCaps(Caps.fromString(buildCapsString()));
    }
    
    void requestFrameRate(double rate) {
        requestRate = (int) Math.round(rate);
        sink.setCaps(Caps.fromString(buildCapsString()));
    }
    
    boolean render(Consumer<PImage> renderer) {
        imageLock.lock();
        try {
            if (surface != null && surface.sample != null) {
                Buffer b = surface.sample.getBuffer();
                surface.nativeData = b.map(false);
                try {
                    renderer.accept(surface.image);
                } catch(Exception ex) {
                    // ??
                }
                surface.nativeData = null;
                b.unmap();
                return true;
            } else {
                return false;
            }
        } finally {
            imageLock.unlock();
        }
    } 
    
    void dispose() {
        imageLock.lock();
        try {
            if (surface != null && surface.sample != null) {
                surface.sample.dispose();
            }
            surface = null;
        } finally {
            imageLock.unlock();
        }
    }

    private class NewSampleListener implements AppSink.NEW_SAMPLE {

        @Override
        public FlowReturn newSample(AppSink sink) {
            imageLock.lock();
            try {
                Sample sample = sink.pullSample();
                Structure capsStruct = sample.getCaps().getStructure(0);
                int width = capsStruct.getInteger("width");
                int height = capsStruct.getInteger("height");
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
                imageLock.unlock();
            }
            return FlowReturn.OK;
        }

    }

    private class NewPrerollListener implements AppSink.NEW_PREROLL {

        @Override
        public FlowReturn newPreroll(AppSink sink) {
            imageLock.lock();
            try {
                Sample sample = sink.pullPreroll();
                Structure capsStruct = sample.getCaps().getStructure(0);
                int width = capsStruct.getInteger("width");
                int height = capsStruct.getInteger("height");
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
                imageLock.unlock();
            }
            return FlowReturn.OK;
        }

    }

    private static class GStreamerSurface extends Surface implements NativePixelData {

        private static PixelData[] EMPTY = new PixelData[0];

        private final PImage image;

        private Sample sample;
        private ByteBuffer nativeData;
        private int[] data;
        private int modCount;

        private GStreamerSurface(int width, int height) {
            super(width, height, false);
            image = new PImage(width, height) {
                @Override
                protected Surface getSurface() {
                    return GStreamerSurface.this;
                }
            };
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

        public NativePixelData.Format getFormat() {
            return NativePixelData.Format.INT_RGB;
        }

    }

}
