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

package net.neilcsmith.praxis.video.render.ops;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import net.neilcsmith.praxis.video.render.PixelData;
import net.neilcsmith.praxis.video.render.SurfaceOp;
import net.neilcsmith.praxis.video.render.utils.ImageUtils;


/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class ShapeRender implements SurfaceOp {

    private final static Logger LOG = Logger.getLogger(ShapeRender.class.getName());
    private final static double EPSILON = 0.997;

    private Shape shape;
    private BasicStroke stroke;
    private BlendFunction blend;
    private Color fillColor;
    private Color strokeColor;
    private boolean direct;

    private ShapeRender(Shape shape, Color fillColor, BasicStroke stroke,
            Color strokeColor, BlendFunction blend) {
        this.shape = shape;
        this.fillColor = fillColor;
        this.stroke = stroke;
        this.strokeColor = strokeColor;
        this.blend = blend;
        if (blend instanceof Blend) {
            Blend b = (Blend) blend;
            if (b.getType() == Blend.Type.Normal && 
                    (stroke == null || b.getExtraAlpha() > EPSILON) ) {
                direct = true;
            }
        }
    }

    public void process(PixelData output, PixelData... inputs) {
        if (direct) {
            processDirect(output);
        } else {
            processIndirect(output);
        }
    }

    private void processDirect(PixelData output) {
        BufferedImage im = ImageUtils.toImage(output);
        Graphics2D g2d = im.createGraphics();
        double opacity = ((Blend) blend).getExtraAlpha();
        if (opacity < EPSILON) {
            g2d.setComposite(AlphaComposite.SrcOver.derive((float) opacity));
        }
        drawShape(g2d);
    }

    private void processIndirect(PixelData output) {
        Rectangle sRct = shape.getBounds();
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
        TempData tmp = TempData.create(intersection.width, intersection.height, output.hasAlpha());
        BufferedImage bi = ImageUtils.toImage(tmp);
        Graphics2D g2d = bi.createGraphics();
        g2d.translate(tx, ty);
        drawShape(g2d);
        SubPixels dst = SubPixels.create(output, intersection);
        blend.process(tmp, dst);
        tmp.release();
    }

    private void drawShape(Graphics2D g2d) {
        if (fillColor != null) {
            g2d.setColor(fillColor);
            g2d.fill(shape);
        }
        if (stroke != null) {
            g2d.setStroke(stroke);
            g2d.setColor(strokeColor);
            g2d.draw(shape);
        }
    }


    public static SurfaceOp op(Shape shape, Color fillColor) {
        return op(shape, fillColor, Blend.NORMAL);
    }

    public static SurfaceOp op(Shape shape, Color fillColor, BlendFunction blend) {
        return op(shape, fillColor, null, null, blend);
    }

    public static SurfaceOp op(Shape shape, Color fillColor, BasicStroke stroke,
            Color strokeColor) {
        return op(shape, fillColor, stroke, strokeColor, Blend.NORMAL);
    }

    public static SurfaceOp op(Shape shape, Color fillColor, BasicStroke stroke,
            Color strokeColor, BlendFunction blend) {
        if (shape == null || blend == null) {
            throw new NullPointerException();
        }
        if (stroke == null) {
            if (fillColor == null) {
                throw new NullPointerException();
            }
        } else if (strokeColor == null) {
            throw new NullPointerException();
        }

        return new ShapeRender(copyShape(shape), fillColor, stroke, strokeColor, blend);
    }

    private static Shape copyShape(Shape shape) {
        if (shape instanceof RectangularShape) {
            return (Shape) ((RectangularShape)shape).clone();
        } else {
            return new Path2D.Double(shape);
        }

    }
}
