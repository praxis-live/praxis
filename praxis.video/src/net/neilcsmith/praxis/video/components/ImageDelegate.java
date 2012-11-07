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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.URI;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.ops.Blend;
import net.neilcsmith.praxis.video.render.ops.Blit;
import net.neilcsmith.praxis.video.render.ops.Bounds;
import net.neilcsmith.praxis.video.render.ops.ScaledBlit;
import net.neilcsmith.praxis.video.render.utils.BufferedImageSurface;
import net.neilcsmith.praxis.video.utils.ResizeMode;
import net.neilcsmith.praxis.video.utils.ResizeUtils;

/**
 *
 * @author Neil C Smith
 */
class ImageDelegate {

//    private static Logger logger = Logger.getLogger(ImageDelegate.class.getName());
    private BufferedImageSurface source;
    private Cache cache;
    private ResizeMode mode;

    private ImageDelegate(BufferedImageSurface source, ResizeMode mode) {
        this.source = source;
        this.mode = mode;
    }

    private ImageDelegate(BufferedImageSurface source, ResizeMode mode, int outputWidth, int outputHeight) {
        this(source, mode);
        createCache(outputWidth, outputHeight);
    }

    public void setResizeMode(ResizeMode mode) {
        if (mode == null) {
            throw new NullPointerException();
        }
        if (this.mode.getType() != ResizeMode.Type.Stretch
                || mode.getType() != ResizeMode.Type.Stretch) {
            // if both types weren't Stretch we need to recache
            cache = null;
        }
        this.mode = mode;
    }

    public ResizeMode getResizeMode() {
        return mode;
    }

    public void process(Surface surface) {
        int outputWidth = surface.getWidth();
        int outputHeight = surface.getHeight();
//            SurfaceGraphics sg = output.getGraphics();
//            // check cache
//            if (cache == null || outputWidth != cache.outputWidth
//                    || outputHeight != cache.outputHeight) {
//                createCache(outputWidth, outputHeight);
//            }
//            // check if we need to draw input on output
//            if (input != output) {
//                if (cache.image.getTransparency() != BufferedImage.OPAQUE ||
//                        cache.image.getWidth() != outputWidth ||
//                        cache.image.getHeight() != outputHeight) {
//                    sg.drawSurface(input, 0, 0);
//                }
//            }
//            // render image
//            sg.drawImage(cache.image, cache.x, cache.y);


        if (cache == null || outputWidth != cache.outputWidth
                || outputHeight != cache.outputHeight) {
            createCache(outputWidth, outputHeight);
        }
        // check if we need to draw input on output
//        if (input != surface) {
//            if (cache.surface.hasAlpha()
//                    || cache.surface.getWidth() != outputWidth
//                    || cache.surface.getHeight() != outputHeight) {
//                surface.process(Blit.op(), input);
//            }
//        }
        // render cache
//        surface.process(Blit.op(cache.x, cache.y), cache.surface);
        if (cache.srcBnds != null) {
            surface.process(Blit.op(Blend.NORMAL, cache.srcBnds, cache.x, cache.y), cache.surface);
        } else {
            surface.process(Blit.op(cache.x, cache.y), cache.surface);
        }

    }

    private void createCache(int outputWidth, int outputHeight) {
        int srcWidth = source.getWidth();
        int srcHeight = source.getHeight();
        if (srcWidth == outputWidth && srcHeight == outputHeight) {
            createIdentityCache();
        } else {
            createResizedCache(new Dimension(srcWidth, srcHeight),
                    new Dimension(outputWidth, outputHeight));
        }
    }

    private void createIdentityCache() {
//        int width = source.getWidth();
//        int height = source.getHeight();
//        if (source.getType() == BufferedImage.TYPE_INT_RGB ||
//                source.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
//            cache = new Cache(source, width, height, 0, 0);
//        } else {
//            BufferedImage image;
//            if (source.getTransparency() == BufferedImage.OPAQUE) {
//                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//            } else {
//                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
//            }
//            image.getGraphics().drawImage(source, 0, 0, null);
//            cache = new Cache(image, width, height, 0, 0);
//        }
        cache = new Cache(source, null, source.getWidth(), source.getHeight(), 0, 0);
    }

    private void createResizedCache(Dimension srcDim, Dimension dstDim) {
        Rectangle srcBnds = new Rectangle();
        Rectangle dstBnds = new Rectangle();
        ResizeUtils.calculateBounds(srcDim, dstDim, mode, srcBnds, dstBnds);
        if (srcBnds.width == dstBnds.width && srcBnds.height == dstBnds.height) {
            cache = new Cache(source, new Bounds(srcBnds), dstDim.width, dstDim.height, dstBnds.x, dstBnds.y);
        } else {
            BufferedImageSurface s = new BufferedImageSurface(dstBnds.width, dstBnds.height, source.hasAlpha());
            SurfaceOp blit = ScaledBlit.op(Blend.NORMAL, new Bounds(srcBnds),
                    new Bounds(0, 0, dstBnds.width, dstBnds.height));
            s.process(blit, source);
            cache = new Cache(s, null, dstDim.width, dstDim.height, dstBnds.x, dstBnds.y);
        }

    }

    private class Cache {

        BufferedImageSurface surface;
        int outputWidth;
        int outputHeight;
        int x;
        int y;
        Bounds srcBnds;

        private Cache(BufferedImageSurface surface, Bounds srcBnds, int outputWidth, int outputHeight, int x, int y) {
            this.surface = surface;
            this.srcBnds = srcBnds;
            this.outputWidth = outputWidth;
            this.outputHeight = outputHeight;
            this.x = x;
            this.y = y;
        }
    }

    public static ImageDelegate create(URI uri, ResizeMode mode, Dimension guide) throws IOException {

        if (uri == null || mode == null) {
            throw new NullPointerException();
        }

        BufferedImageSurface image = BufferedImageSurface.load(uri);

        if (guide == null) {
            return new ImageDelegate(image, mode);
        } else {
            return new ImageDelegate(image, mode, guide.width, guide.height);
        }
    }
}
