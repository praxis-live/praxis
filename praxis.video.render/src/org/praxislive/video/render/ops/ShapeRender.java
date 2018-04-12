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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import org.praxislive.video.render.PixelData;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.utils.ImageUtils;


/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ShapeRender implements SurfaceOp {

    private final static double EPSILON = 0.997;

    private Shape shape;
    private AffineTransform transform;
    private BasicStroke stroke;
    private BlendMode blendMode;
    private double opacity;
    private Color fillColor;
    private Color strokeColor;

    public ShapeRender() {
        this.blendMode = BlendMode.Normal;
        this.opacity = 1;
    }
    
    public ShapeRender setShape(Shape shape) {
        this.shape = shape;
        return this;
    }
    
    public Shape getShape() {
        return shape;
    }
    
    public ShapeRender setStroke(BasicStroke stroke) {
        this.stroke = stroke;
        return this;
    }
    
    public BasicStroke getStroke() {
        return stroke;
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    public ShapeRender setBlendMode(BlendMode blendMode) {
        if (blendMode == null) {
            throw new NullPointerException();
        }
        this.blendMode = blendMode;
        return this;
    }

    public double getOpacity() {
        return opacity;
    }

    public ShapeRender setOpacity(double opacity) {
        if (opacity < 0) {
            opacity = 0;
        } else if (opacity > 1) {
            opacity = 1;
        }
        this.opacity = opacity;
        return this;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public ShapeRender setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public ShapeRender setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }
    
    public ShapeRender setTransform(AffineTransform transform) {
        this.transform = transform;
        return this;
    }
    
    public AffineTransform getTransform() {
        return transform;
    }

    @Override
    public void process(PixelData output, PixelData... inputs) {
        if (shape == null) {
            return;
        }
        if (blendMode == BlendMode.Normal) {
            processDirect(output);
        } else {
            processIndirect(output);
        }
    }

    private void processDirect(PixelData output) {
        BufferedImage im = ImageUtils.toImage(output);
        Graphics2D g2d = im.createGraphics();
//        double opacity = ((Blend) blend).getExtraAlpha();
        if (opacity < EPSILON) {
            g2d.setComposite(AlphaComposite.SrcOver.derive((float) opacity));
        }
        drawShape(g2d, transform == null ? shape : transform.createTransformedShape(shape));
    }

    private void processIndirect(PixelData output) {
        Shape sh = transform == null ? shape : transform.createTransformedShape(shape);
        Rectangle sRct = sh.getBounds();
        if (stroke != null) {
            int growth = Math.round(stroke.getLineWidth());// / 2);
            sRct.grow(growth, growth);
        }
        int tx = sRct.x > 0 ? -sRct.x : 0;
        int ty = sRct.y > 0 ? -sRct.y : 0;
        Rectangle dRct = new Rectangle(0, 0, output.getWidth(), output.getHeight());
        Rectangle intersection = dRct.intersection(sRct);
        if (intersection.isEmpty()) {
            return;
        }
        TempData tmp = TempData.create(intersection.width, intersection.height, true);
        BufferedImage bi = ImageUtils.toImage(tmp);
        Graphics2D g2d = bi.createGraphics();
        g2d.translate(tx, ty);
        drawShape(g2d, sh);
        SubPixels dst = SubPixels.create(output, intersection);
//        blend.process(tmp, dst);
        BlendUtil.process(tmp, dst, blendMode, opacity);
        tmp.release();
    }

    private void drawShape(Graphics2D g2d, Shape sh) {
        if (fillColor != null) {
            g2d.setColor(fillColor);
            g2d.fill(sh);
        }
        if (stroke != null && strokeColor != null) {
            g2d.setStroke(stroke);
            g2d.setColor(strokeColor);
            g2d.draw(sh);
        }
    }

//    private static Shape copyShape(Shape shape) {
//        if (shape instanceof RectangularShape) {
//            return (Shape) ((RectangularShape)shape).clone();
//        } else {
//            return new Path2D.Double(shape);
//        }
//
//    }
}
