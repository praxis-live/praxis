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
package org.praxislive.video.render.ops;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import org.praxislive.video.render.PixelData;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.utils.ImageUtils;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class TransformBlit implements SurfaceOp {
    
    private final static Logger LOG = Logger.getLogger(TransformBlit.class.getName());

    private final Rectangle srcRegion = new Rectangle();
    private final Rectangle dstRegion = new Rectangle();
    private final AffineTransform transform = new AffineTransform();
    private boolean hasSrcRegion;
    private boolean hasDstRegion;
    private BlendMode blendMode = BlendMode.Normal;
    private double opacity = 1;

    
    public void setTransform(AffineTransform transform) {
        if (transform == null) {
            this.transform.setToIdentity();
        } else {
            this.transform.setTransform(transform);
        }   
    }
    
    public AffineTransform getTransform(AffineTransform transform) {
        if (transform == null) {
            transform = new AffineTransform(this.transform);
        } else {
            transform.setTransform(this.transform);
        }
        return transform;
    }

    public TransformBlit setSourceRegion(Rectangle rect) {
        if (rect == null) {
            hasSrcRegion = false;
        } else {
            hasSrcRegion = true;
            srcRegion.setBounds(rect);
        }
        return this;
    }

    public TransformBlit setSourceRegion(int x, int y, int width, int height) {
        hasSrcRegion = true;
        srcRegion.setBounds(x, y, width, height);
        return this;
    }

    public Rectangle getSourceRegion(Rectangle rect) {
        if (hasSrcRegion) {
            if (rect == null) {
                rect = new Rectangle(srcRegion);
            } else {
                rect.setBounds(srcRegion);
            }
            return rect;
        } else {
            return null;
        }
    }

    public boolean hasSourceRegion() {
        return hasSrcRegion;
    }

    public TransformBlit setDestinationRegion(Rectangle rect) {
        if (rect == null) {
            hasDstRegion = false;
        } else {
            hasDstRegion = true;
            dstRegion.setBounds(rect);
        }
        return this;
    }

    public TransformBlit setDestinationRegion(int x, int y, int width, int height) {
        hasDstRegion = true;
        dstRegion.setBounds(x, y, width, height);
        return this;
    }

    public Rectangle getDestinationRegion(Rectangle rect) {
        if (hasDstRegion) {
            if (rect == null) {
                rect = new Rectangle(dstRegion);
            } else {
                rect.setBounds(dstRegion);
            }
            return rect;
        } else {
            return null;
        }
    }

    public boolean hasDestinationRegion() {
        return hasDstRegion;
    }

    public TransformBlit setBlendMode(BlendMode blendMode) {
        if (blendMode == null) {
            throw new NullPointerException();
        }
        this.blendMode = blendMode;
        return this;
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    public TransformBlit setOpacity(double opacity) {
        if (opacity < 0 || opacity > 1) {
            throw new IllegalArgumentException();
        }
        this.opacity = opacity;
        return this;
    }

    public double getOpacity() {
        return opacity;
    }

    public void process(PixelData output, PixelData... inputs) {
        if (inputs.length < 1) {
            return;
        }
        AlphaComposite cmp = compositeFromBlend();
        if (cmp != null) {
            processDirect(cmp, output, inputs[0]);
        } else {
            processIndirect(output, inputs[0]);
        }
    }

    private void processDirect(AlphaComposite cmp, PixelData output, PixelData input) {
        LOG.fine("Processing TransformBlit direct");
        BufferedImage out = ImageUtils.toImage(output);
        BufferedImage in = ImageUtils.toImage(input);
        Graphics2D g2d = out.createGraphics();
        g2d.setComposite(cmp);
        g2d.setTransform(transform);
        int sx1 = hasSrcRegion ? srcRegion.x : 0;
        int sy1 = hasSrcRegion ? srcRegion.y : 0;
        int sx2 = hasSrcRegion ? sx1 + srcRegion.width : in.getWidth();
        int sy2 = hasSrcRegion ? sy1 + srcRegion.height : in.getHeight();
        int dx1 = hasDstRegion ? dstRegion.x : 0;
        int dy1 = hasDstRegion ? dstRegion.y : 0;
        int dx2 = hasDstRegion ? dx1 + dstRegion.width : out.getWidth();
        int dy2 = hasDstRegion ? dy1 + dstRegion.height : out.getHeight();
        g2d.drawImage(in, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
    }

    private AlphaComposite compositeFromBlend() {
        if (blendMode == BlendMode.Normal) {
            return AlphaComposite.SrcOver.derive((float) opacity);
        } else {
            return null;
        }

    }

    private void processIndirect(PixelData output, PixelData input) {
        int sx1 = hasSrcRegion ? srcRegion.x : 0;
        int sy1 = hasSrcRegion ? srcRegion.y : 0;
        int sx2 = hasSrcRegion ? sx1 + srcRegion.width : input.getWidth();
        int sy2 = hasSrcRegion ? sy1 + srcRegion.height : input.getHeight();
        
        if (!hasDstRegion) {
            dstRegion.setBounds(0, 0, output.getWidth(), output.getHeight());
        }
        
        Rectangle bounds = transform.createTransformedShape(dstRegion).getBounds();
        bounds = bounds.intersection(new Rectangle(0,0,output.getWidth(), output.getHeight()));
        LOG.fine("Calculated bounds\n" + bounds);
//        int dx = hasDstRegion ? dstRegion.x : 0;
//        int dy = hasDstRegion ? dstRegion.y : 0;
//        int dw = hasDstRegion ? dstRegion.width : output.getWidth();
//        int dh = hasDstRegion ? dstRegion.height : output.getHeight();

//        if (dw <= 0 || dh <= 0) {
//            return;
//        }

        // get temp data
        TempData tmp = TempData.create(bounds.width, bounds.height, true);

        // draw to temp
        BufferedImage tmpIm = ImageUtils.toImage(tmp);
        BufferedImage in = ImageUtils.toImage(input);
        Graphics2D g2d = tmpIm.createGraphics();
        g2d.setComposite(AlphaComposite.Src);
        g2d.translate(-bounds.x, -bounds.y);
        g2d.transform(transform);   
        
        g2d.drawImage(in, 
                dstRegion.x, dstRegion.y, 
                dstRegion.x + dstRegion.width, 
                dstRegion.y + dstRegion.height, 
                sx1, sy1, sx2, sy2, null);

//        Blit.op(blend, dx, dy).process(output, tmp);
        new Blit().setX(bounds.x).setY(bounds.y).setBlendMode(blendMode).setOpacity(opacity).process(output, tmp);

        tmp.release();
    }
}
