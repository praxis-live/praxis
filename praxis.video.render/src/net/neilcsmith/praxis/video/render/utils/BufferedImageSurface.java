/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 Neil C Smith.
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
package net.neilcsmith.praxis.video.render.utils;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.neilcsmith.praxis.video.render.PixelData;
import net.neilcsmith.praxis.video.render.Surface;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.ops.GraphicsOp;

/**
 *
 * @author Neil C Smith
 */
public class BufferedImageSurface extends Surface {

    private final static Logger LOG = Logger.getLogger(BufferedImageSurface.class.getName());

    private final static PixelData[] EMPTY_INPUTS = new PixelData[0];
    private final static Image[] EMPTY_IMAGES = new Image[0];
    private BufferedImage image;
    private PixelWrapper pixelData;
    private boolean clear = true;
    private int modCount;

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

    public Surface createSurface(int width, int height, boolean alpha) {
        return new BufferedImageSurface(width, height, alpha);
    }

    @Override
    public void release() {
        // no op
    }

    protected BufferedImage getImage() {
        return image;
    }

    private PixelData getPixelData() {
        if (pixelData == null) {
            int[] data = ((DataBufferInt) (image.getRaster().getDataBuffer())).getData();
            pixelData = new PixelWrapper(data, getWidth(), getHeight(), hasAlpha());
        }
        return pixelData;
    }

    private PixelData getPixelData(Surface surface) {
        if (surface instanceof BufferedImageSurface) {
            return ((BufferedImageSurface) surface).getPixelData();
        } else {
            throw new RuntimeException();
        }
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
        modCount++;
        Graphics2D g = image.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.dispose();
        clear = true;
    }

    @Override
    public boolean isClear() {
        return clear;
    }

    @Override
    public void copy(Surface source) {
        modCount++;
        clear = false;
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
        modCount++;
        if (op instanceof GraphicsOp) {
            Graphics2D g = image.createGraphics();
            ((GraphicsOp) op).getCallback().draw(g, createImageArray(inputs));
            return;
        }
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
        clear = false;
    }

    private Image[] createImageArray(Surface[] inputs) {
        if (inputs.length == 0) {
            return EMPTY_IMAGES;
        } else {
            Image[] ret = new Image[inputs.length];
            for (int i=0; i < ret.length; i++) {
                Surface s = inputs[i];
                if (s instanceof BufferedImageSurface) {
                    ret[i] = ((BufferedImageSurface) s).image;
                } else {
                    ret[i] = ImageUtils.toImage(getPixelData(s));
                }
            }
            return ret;
        }
    }

    public void save(String type, File file) throws IOException {
        boolean success = ImageIO.write(image, type, file);
        if (!success) {
            throw new IOException("Can't find writer for supplied type : " + type);
        }
    }

    @Override
    public int getModCount() {
        return modCount;
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
