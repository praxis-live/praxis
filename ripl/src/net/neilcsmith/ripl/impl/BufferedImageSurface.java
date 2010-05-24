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
package net.neilcsmith.ripl.impl;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.net.URI;
import javax.imageio.ImageIO;
import net.neilcsmith.ripl.PixelData;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.SurfaceCapabilities;
import net.neilcsmith.ripl.SurfaceOp;
import net.neilcsmith.ripl.ops.GraphicsOp;

/**
 *
 * @author Neil C Smith
 */
public class BufferedImageSurface extends Surface {

    private final static PixelData[] EMPTY_INPUTS = new PixelData[0];
    private final static SurfaceCapabilities caps = new SurfaceCapabilities(true);
    private BufferedImage image;
    private PixelWrapper pixelData;
    private int mod;

    public BufferedImageSurface(int width, int height, boolean alpha) {
        super(width, height, alpha);
        if (alpha) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }
    }

    private BufferedImageSurface(BufferedImage image, boolean alpha) {
        super(image.getWidth(), image.getHeight(), alpha);
        this.image = image;
    }

    public Surface createSurface(int width, int height, boolean alpha, SurfaceCapabilities caps) {
        return new BufferedImageSurface(width, height, alpha);
    }

    public SurfaceCapabilities getCapabilities() {
        return caps;
    }

    @Override
    public void release() {
        // no op
    }

    protected BufferedImage getImage() {
        return image;
    }

    protected PixelData getPixelData() {
        if (pixelData == null) {
            int[] data = ((DataBufferInt) (image.getRaster().getDataBuffer())).getData();
            pixelData = new PixelWrapper(data, getWidth(), getHeight(), hasAlpha());
        }
        return pixelData;
    }

    @Override
    public boolean checkCompatible(Surface surface, boolean checkDimensions, boolean checkAlpha) {
        if (!(surface instanceof BufferedImageSurface)) {
            return false;
        }
        if (checkDimensions && (surface.getWidth() != getWidth()
                || surface.getHeight() != getHeight())) {
            return false;
        }
        if (checkAlpha && (surface.hasAlpha() != hasAlpha())) {
            return false;
        }
        return true;
    }

    @Override
    public void clear() {
        Graphics2D g = image.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.dispose();
    }

    @Override
    public void copy(Surface source) {
        if (source.hasAlpha() || source.getWidth() < getWidth() ||
                source.getHeight() < getHeight()) {
            clear();
        }
        if (source instanceof BufferedImageSurface) {
            Graphics2D g = image.createGraphics();
            g.drawImage(((BufferedImageSurface) source).getImage(), 0, 0, null);
            g.dispose();
        } else {
            throw new RuntimeException();
        }
    }



    @Override
    public void process(SurfaceOp op, Surface... inputs) {
//        if (op instanceof GraphicsOp) {
//            Graphics2D g = image.createGraphics();
//            ((GraphicsOp) op).getCallback().draw(g, new Image[0]);
//            return;
//        }
        PixelData[] inputData;
        int inLen = inputs.length;
        if (inLen > 0) {
            inputData = new PixelData[inLen];
            for (int i = 0; i < inLen; i++) {
                inputData[i] = getPixelData(inputs[i]);
            }
        } else {
            inputData = EMPTY_INPUTS;
        }
        op.process(getPixelData(), inputData);
    }

    private class PixelWrapper implements PixelData {

        private int[] data;
        private int width;
        private int height;
        private boolean alpha;

        private PixelWrapper(int[] data, int width, int height, boolean alpha) {
            this.data = data;
            this.width = width;
            this.height = height;
            this.alpha = alpha;
        }

        public int[] getData() {
            return data;
        }

        public int getOffset() {
            return 0;
        }

        public int getScanline() {
            return width;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean hasAlpha() {
            return alpha;
        }
    }

    public static BufferedImageSurface load(URI location) throws IOException {

        BufferedImage im = ImageIO.read(location.toURL());
        if (im == null) {
            throw new IOException();
        }
        if (im.getType() == BufferedImage.TYPE_INT_RGB) {
            return new BufferedImageSurface(im, false);
        } else if (im.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
            return new BufferedImageSurface(im, true);
        }
        // convert format
        if (im.getTransparency() == BufferedImage.OPAQUE) {
            BufferedImage im2 = new BufferedImage(im.getWidth(),
                    im.getHeight(), BufferedImage.TYPE_INT_RGB);
            im2.createGraphics().drawImage(im, 0, 0, null);
            return new BufferedImageSurface(im2, false);
        } else {
            BufferedImage im2 = new BufferedImage(im.getWidth(),
                    im.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
            im2.createGraphics().drawImage(im, 0, 0, null);
            return new BufferedImageSurface(im2, true);
        }


    }
}
