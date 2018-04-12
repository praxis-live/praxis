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
package org.praxislive.video.render.utils;

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
import org.praxislive.video.render.PixelData;
import org.praxislive.video.render.Surface;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.ops.Blit;
import org.praxislive.video.render.ops.GraphicsOp;
import org.praxislive.video.render.ops.Reverse;

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

//    private PixelData getPixelData(Surface surface) {
//        if (surface instanceof BufferedImageSurface) {
//            return ((BufferedImageSurface) surface).getPixelData();
//        } else {
//            throw new RuntimeException();
//        }
//    }
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
        if (source.hasAlpha() || source.getWidth() < getWidth()
                || source.getHeight() < getHeight()) {
            clear();
        }
        processImpl(new Blit(), source);
    }

    @Override
    public void process(SurfaceOp op, Surface... inputs) {
        modCount++;
        switch (inputs.length) {
            case 0:
                processImpl(op);
                break;
            case 1:
                processImpl(op, inputs[0]);
                break;
            default:
                processImpl(op, inputs);
        }
        clear = false;
    }

    private void processImpl(SurfaceOp op) {
        if (op instanceof GraphicsOp) {
            processGraphicsOp((GraphicsOp) op, EMPTY_IMAGES);
        } else {
            op.process(getPixelData(), EMPTY_INPUTS);
        }
    }

    private void processImpl(SurfaceOp op, Surface input) {
        if (input instanceof BufferedImageSurface) {
            if (op instanceof GraphicsOp) {
                processGraphicsOp((GraphicsOp) op,
                        new Image[]{((BufferedImageSurface) input).getImage()});
            } else {
                op.process(getPixelData(),
                        new PixelData[]{((BufferedImageSurface) input).getPixelData()});
            }
        } else {
            SurfaceOp rev = Reverse.op(op, getPixelData());
            input.process(rev);
        }
    }

    private void processImpl(SurfaceOp op, Surface[] inputs) {
        if (op instanceof GraphicsOp) {
            processGraphicsOp((GraphicsOp) op, createImageArray(inputs));
        } else {
            PixelData[] pixelInputs = new PixelData[inputs.length];
            for (int i = 0; i < inputs.length; i++) {
                if (inputs[i] instanceof BufferedImageSurface) {
                    pixelInputs[i] = ((BufferedImageSurface) inputs[i]).getPixelData();
                } else {
                    throw new UnsupportedOperationException("not yet implemented");
                }
            }
            op.process(pixelData, pixelInputs);
        }
    }

    private Image[] createImageArray(Surface[] inputs) {
        if (inputs.length == 0) {
            return EMPTY_IMAGES;
        } else {
            Image[] ret = new Image[inputs.length];
            for (int i = 0; i < ret.length; i++) {
                Surface s = inputs[i];
                if (s instanceof BufferedImageSurface) {
                    ret[i] = ((BufferedImageSurface) s).getImage();
                } else {
                    throw new UnsupportedOperationException("not yet implemented");
                }
            }
            return ret;
        }
    }

    private void processGraphicsOp(GraphicsOp op, Image[] images) {
        Graphics2D g = getImage().createGraphics();
        op.getCallback().draw(g, images);
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
