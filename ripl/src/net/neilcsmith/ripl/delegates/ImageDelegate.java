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
package net.neilcsmith.ripl.delegates;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.neilcsmith.ripl.core.ResizeMode;
import net.neilcsmith.ripl.core.Surface;
import net.neilcsmith.ripl.core.SurfaceGraphics;
import net.neilcsmith.ripl.utils.ResizeUtils;

/**
 *
 * @author Neil C Smith
 */
public class ImageDelegate extends Delegate {
    
    private static Logger logger = Logger.getLogger(ImageDelegate.class.getName());

    private BufferedImage source;
    private Cache cache;
    private ResizeMode mode;

    private ImageDelegate(BufferedImage source, ResizeMode mode) {
        this.source = source;
        this.mode = mode;
    }

    private ImageDelegate(BufferedImage source, ResizeMode mode, int outputWidth, int outputHeight) {
        this(source, mode);
        createCache(outputWidth, outputHeight);
    }
    
    public void setResizeMode(ResizeMode mode) {
        if (mode == null) {
            throw new NullPointerException();
        }
        if (this.mode.getType() != ResizeMode.Type.Stretch ||
                mode.getType() != ResizeMode.Type.Stretch) {
            // if both types weren't Stretch we need to recache
            cache = null;
        }
        this.mode = mode;
    }
    
    public ResizeMode getResizeMode() {
        return mode;
    }

    @Override
    public void process(Surface input, Surface output) {
            int outputWidth = output.getWidth();
            int outputHeight = output.getHeight();
            SurfaceGraphics sg = output.getGraphics();
            // check cache
            if (cache == null || outputWidth != cache.outputWidth
                    || outputHeight != cache.outputHeight) {
                createCache(outputWidth, outputHeight);
            }
            // check if we need to draw input on output
            if (input != output) {
                if (cache.image.getTransparency() != BufferedImage.OPAQUE ||
                        cache.image.getWidth() != outputWidth ||
                        cache.image.getHeight() != outputHeight) {
                    sg.drawSurface(input, 0, 0);
                }
            }
            // render image
            sg.drawImage(cache.image, cache.x, cache.y);
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
        int width = source.getWidth();
        int height = source.getHeight();
        if (source.getType() == BufferedImage.TYPE_INT_RGB ||
                source.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
            cache = new Cache(source, width, height, 0, 0);
        } else {
            BufferedImage image;
            if (source.getTransparency() == BufferedImage.OPAQUE) {
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            } else {
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
            }
            image.getGraphics().drawImage(source, 0, 0, null);
            cache = new Cache(image, width, height, 0, 0);
        }
    }

    private void createResizedCache(Dimension srcDim, Dimension dstDim) {
        Rectangle srcBnds = new Rectangle();
        Rectangle dstBnds = new Rectangle();
        ResizeUtils.calculateBounds(srcDim, dstDim, mode, srcBnds, dstBnds);
        BufferedImage image;
        if (source.getTransparency() == BufferedImage.OPAQUE) {
            image = new BufferedImage(dstBnds.width, dstBnds.height, BufferedImage.TYPE_INT_RGB);
        } else {
            image = new BufferedImage(dstBnds.width, dstBnds.height, BufferedImage.TYPE_INT_ARGB_PRE);
        }
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        image.getGraphics().drawImage(source, 0, 0, dstBnds.width, dstBnds.height,
                srcBnds.x, srcBnds.y, srcBnds.x + srcBnds.width, srcBnds.y + srcBnds.height, null);
        cache = new Cache(image, dstDim.width, dstDim.height, dstBnds.x, dstBnds.y);
        g2d.dispose();

    }

    private class Cache {

        BufferedImage image;
        int outputWidth;
        int outputHeight;
        int x;
        int y;

        private Cache(BufferedImage image, int outputWidth, int outputHeight, int x, int y) {
            this.image = image;
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

        BufferedImage image = ImageIO.read(uri.toURL());
        if (image == null) {
            throw new IOException();
        }

        if (guide == null) {
            return new ImageDelegate(image, mode);
        } else {
            return new ImageDelegate(image, mode, guide.width, guide.height);
        }
    }
}
