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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import net.neilcsmith.ripl.core.Bounds;
import net.neilcsmith.ripl.core.ImageData;
import net.neilcsmith.ripl.core.PixelData;
import net.neilcsmith.ripl.core.Surface;
import net.neilcsmith.ripl.core.SurfaceCapabilities;
import net.neilcsmith.ripl.core.SurfaceConfiguration;
import net.neilcsmith.ripl.core.SurfaceGraphics;
import net.neilcsmith.ripl.core.SurfaceOp;

/**
 *
 * @author Neil C Smith
 */
public class ImageSurface extends Surface {

    private final static PixelData[] EMPTY_INPUTS = new PixelData[0];
    private final static SurfaceCapabilities caps = new SurfaceCapabilities(true);
    private BufferedImage image;
//    private ImageWrapper imageData;
    private PixelWrapper pixelData;
//    private Graphics2D g2d;
    private GraphicsWrapper graphics;

    public ImageSurface(int width, int height, boolean alpha) {
        super(width, height, alpha);
        if (alpha) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }
    }

    public Surface createSurface(int width, int height, boolean alpha, SurfaceCapabilities caps) {
        return new ImageSurface(width, height, alpha);
    }

    public SurfaceCapabilities getCapabilities() {
        return caps;
    }

    @Override
    public void release() {
    // @TODO implement caching
    }

    public Image getImage() {
        invalidateGraphics();
        return image;
    }

//    @Override
//    public ImageData getImageData() {
//        invalidateGraphics();
//        if (imageData == null) {
//            imageData = new ImageWrapper(image, new Bounds(0, 0, getWidth(), getHeight()));
//        }
//        return imageData;
//    }
    
    

    public PixelData getPixelData() {
        invalidateGraphics();
        if (pixelData == null) {
            int[] data = ((DataBufferInt) (image.getRaster().getDataBuffer())).getData();
//            Bounds bounds = new Bounds(0, 0, getWidth(), getHeight());
//            pixelData = new PixelWrapper(data, getWidth(), bounds);
            pixelData = new PixelWrapper(data, getWidth(), getHeight(), getWidth(), hasAlpha());
        }
        return pixelData;
    }

    @Override
    public SurfaceGraphics getGraphics() {
        if (graphics == null) {
            graphics = new GraphicsWrapper(image.createGraphics());
        } else {
            graphics.reset();
        }
        return graphics;
    }

    private void invalidateGraphics() {
        if (graphics != null) {
            graphics.g2d = NullG2D.getInstance();
            graphics = null;
        }
    }

    @Override
    public boolean checkCompatible(Surface surface, boolean checkDimensions, boolean checkAlpha) {
//        if (checkDimensions) {
//            return surface instanceof ImageSurface && 
//                    surface.getWidth() == getWidth() &&
//                    surface.getHeight() == getHeight();
//        } else {
//            return surface instanceof ImageSurface;
//        }
        if (!(surface instanceof ImageSurface)) {
            return false;
        }
        if (checkDimensions && (surface.getWidth() != getWidth() ||
                surface.getHeight() != getHeight())) {
            return false;
        }
        if (checkAlpha && (surface.hasAlpha() != hasAlpha())) {
            return false;
        }
        return true;
    }

    @Override
    public void clear() {
        SurfaceGraphics g = getGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private class GraphicsWrapper extends SurfaceGraphics {

        private Graphics2D g2d;
        private Graphics2D originalGraphics;
        private boolean resetRequired;

        private GraphicsWrapper(Graphics2D g2d) {
            this.originalGraphics = g2d;
            this.g2d = (Graphics2D) g2d.create();
        }

        @Override
        public void draw(Shape s) {
            g2d.draw(s);
        }

        @Override
        public void fill(Shape s) {
            g2d.fill(s);
        }

        @Override
        public void drawLine(int x1, int y1, int x2, int y2) {
            g2d.drawLine(x1, y1, x2, y2);
        }

        @Override
        public void drawRect(int x, int y, int width, int height) {
            g2d.drawRect(x, y, width, height);
        }

        @Override
        public void fillRect(int x, int y, int width, int height) {
            g2d.fillRect(x, y, width, height);
        }

        @Override
        public void drawPolygon(Polygon p) {
            g2d.drawPolygon(p);
        }

        @Override
        public void fillPolygon(Polygon p) {
            g2d.fillPolygon(p);
        }

        @Override
        public void drawOval(int x, int y, int width, int height) {
            g2d.drawOval(x, y, width, height);
        }

        @Override
        public void fillOval(int x, int y, int width, int height) {
            g2d.fillOval(x, y, width, height);
        }

        @Override
        public void drawString(String str, int x, int y) {
            g2d.drawString(str, x, y);
        }

        @Override
        public void drawImage(Image img, int x, int y) {
            g2d.drawImage(img, x, y, null);
        }

        @Override
        public void drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2) {
            g2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
        }

        @Override
        public void copyArea(int x, int y, int width, int height, int dx, int dy) {
            g2d.copyArea(x, y, width, height, dx, dy);
        }

        @Override
        public void setComposite(Composite comp) {
            g2d.setComposite(comp);
        }

        @Override
        public void setPaint(Paint paint) {
            g2d.setPaint(paint);
        }

        @Override
        public void setStroke(Stroke s) {
            resetRequired = true;
            g2d.setStroke(s);
        }

        @Override
        public void setFont(Font font) {
            resetRequired = true;
            g2d.setFont(font);
        }

        @Override
        public void translate(int x, int y) {
            resetRequired = true;
            g2d.translate(x, y);
        }

        @Override
        public void rotate(double theta, double x, double y) {
            resetRequired = true;
            g2d.rotate(theta, x, y);
        }

        @Override
        public void scale(double sx, double sy) {
            resetRequired = true;
            g2d.scale(sx, sy);
        }

        @Override
        public void shear(double shx, double shy) {
            resetRequired = true;
            g2d.shear(shx, shy);
        }

        @Override
        public void transform(AffineTransform tx) {
            resetRequired = true;
            g2d.transform(tx);
        }

        @Override
        public void clip(Shape s) {
            resetRequired = true;
            g2d.clip(s);
        }

        @Override
        public void clipRect(int x, int y, int width, int height) {
            resetRequired = true;
            g2d.clipRect(x, y, width, height);
        }

//        @Override
//        public GraphicsConfiguration getDeviceConfiguration() {
//            return g2d.getDeviceConfiguration();
//        }

        @Override
        public FontRenderContext getFontRenderContext() {
            return g2d.getFontRenderContext();
        }

        @Override
        public void drawSurface(Surface surface, int x, int y) {
            // @TODO - use image bounds
//            
//            g2d.drawImage(img, x, y, null);
//            g2d.drawImage(surface.getImage(), x, y, null);
            if (surface instanceof ImageSurface) {
                g2d.drawImage(((ImageSurface) surface).getImage(), x, y, null);
            }
        }

        @Override
        public void drawSurface(Surface surface, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2) {
//            Image img = surface.getImageData().getImage();
//            Image img = surface.getImage();
//            g2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
            if (surface instanceof ImageSurface) {
                Image img = ((ImageSurface) surface).getImage();
                g2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
            }
        }

        @Override
        public void reset() {
            if (!resetRequired) {
                g2d.setColor(Color.BLACK);
                g2d.setComposite(AlphaComposite.SrcOver);
            } else {
                g2d = (Graphics2D) originalGraphics.create();
            }

        }

        
        

        @Override
        public void setRenderingHint(Key key, Object hintValue) {
            resetRequired = true;
            g2d.setRenderingHint(key, hintValue);
        }

        @Override
        public Object getRenderingHint(Key key) {
            return g2d.getRenderingHint(key);
        }
    }

//    private class ImageWrapper implements ImageData {
//
//        private Image img;
//        private Bounds bounds;
//
//        private ImageWrapper(Image img, Bounds bounds) {
//            this.img = img;
//            this.bounds = bounds;
//        }
//
//        public Image getImage() {
//            return img;
//        }
//
//        public Bounds getBounds() {
//            return bounds;
//        }
//    }

    private class PixelWrapper implements PixelData {

        private int[] data;
        private int scanline;
//        private Bounds bounds;
        private int width;
        private int height;
        private boolean alpha;

//        private PixelWrapper(int[] data, int scanline, Bounds bounds) {
//            this.data = data;
//            this.scanline = scanline;
//            this.bounds = bounds;
//        }

        private PixelWrapper(int[] data, int width, int height, int scanline, boolean alpha) {
            this.data = data;
            this.width = width;
            this.height = height;
            this.scanline = scanline;
            this.alpha = alpha;
        }
        
        public int[] getData() {
            return data;
        }

        public int getScanline() {
            return scanline;
        }

//        public Bounds getBounds() {
//            return bounds;
//        }

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

    @Override
    public SurfaceConfiguration getConfiguration() {
        return new SurfaceConfiguration();
    }

    @Override
    public void process(SurfaceOp op, Surface ... inputs) {
        PixelData[] inputData;
        int inLen = inputs.length;
        if (inLen > 0) {
            inputData = new PixelData[inLen];
            for (int i=0; i < inLen; i++) {
                inputData[i] = ((ImageSurface) inputs[i]).getPixelData();
            }
        } else {
            inputData = EMPTY_INPUTS;
        }
        op.process(getPixelData(), inputData);
    }


}
