/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2019 Neil C Smith.
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
 *
 *
 * Parts of the API of this package, as well as some of the code, is derived from
 * the Processing project (http://processing.org)
 *
 * Copyright (c) 2004-09 Ben Fry and Casey Reas
 * Copyright (c) 2001-04 Massachusetts Institute of Technology
 *
 */
package org.praxislive.video.code.userapi;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import static org.praxislive.video.code.userapi.VideoConstants.*;
import org.praxislive.video.render.Surface;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.ops.BlendMode;
import org.praxislive.video.render.ops.Blit;
import org.praxislive.video.render.ops.RectFill;
import org.praxislive.video.render.ops.ScaledBlit;
import org.praxislive.video.render.ops.ShapeRender;
import org.praxislive.video.render.ops.TextRender;
import org.praxislive.video.render.ops.TransformBlit;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public abstract class PGraphics extends PImage {

    private final static double alphaOpaque = 0.999;
//    private PImage image;
    private BlendMode blendMode = BlendMode.Normal;
    private double opacity = 1;
    private Color fillColor = Color.WHITE;
    private Color strokeColor = Color.BLACK;
    private BasicStroke stroke = new BasicStroke(1);
    private PShape shape;
    private AffineTransform transform;
    private Font font;
    private final Blit blit;
    private final ScaledBlit scaledBlit;
    private final TransformBlit transformBlit;
    private final RectFill rectFill;
    private final ShapeRender shapeRender;
    private final TextRender textRender;

    protected PGraphics(int width, int height) {
        super(width, height);
        this.blit = new Blit();
        this.scaledBlit = new ScaledBlit();
        this.transformBlit = new TransformBlit();
        this.rectFill = new RectFill();
        this.shapeRender = new ShapeRender();
        this.textRender = new TextRender();
    }

    protected abstract Surface getSurface();

    public void beginDraw() {
        resetMatrix();
    }
    
    public void endDraw() {
        
    }
    
    // BEGINNING OF PUBLIC DRAWING METHODS
    public void background(double grey) {
        background(grey, grey, grey, 255);
    }

    public void background(double grey, double alpha) {
        background(grey, grey, grey, alpha);
    }

    public void background(double r, double g, double b) {
        background(r, g, b, 255);
    }

    public void background(double r, double g, double b, double a) {
        Surface s = getSurface();
        s.clear();
        int ir = round(r);
        int ig = round(g);
        int ib = round(b);
        if (ir == 0 && ig == 0 && ib == 0) {
            return;
        }
        int ia = round(a);
        Color bg = new Color(ir, ig, ib, ia);
//        image.process(RectFill.op(bg, NORMAL, 0, 0,
//                image.getWidth(), image.getHeight()));
        rectFill.setBlendMode(BlendMode.Normal)
                .setOpacity(1)
                .setColor(bg)
                .setBounds(0, 0, s.getWidth(), s.getHeight());
        s.process(rectFill);
    }

    public void beginShape() {
        shape = PShape.beginShape();
    }

    public void bezier(double x1, double y1,
            double x2, double y2,
            double x3, double y3,
            double x4, double y4) {
        beginShape();
        vertex(x1, y1);
        bezierVertex(x2, y2, x3, y3, x4, y4);
        endShape();
    }

    public void bezierVertex(double x1, double y1,
            double x2, double y2, double x3, double y3) {
        shape.bezierVertex(x1, y1, x2, y2, x3, y3);
    }

    public void blendMode(org.praxislive.video.code.userapi.VideoConstants.BlendMode blend) {
        blendMode(blend, 1);
    }

    public void blendMode(org.praxislive.video.code.userapi.VideoConstants.BlendMode blend, double opacity) {
        this.blendMode = extractBlendMode(blend);
        if (opacity < 0) {
            opacity = 0;
        } else if (opacity > 1) {
            opacity = 1;
        }
        this.opacity = opacity;
    }
    
    private BlendMode extractBlendMode(org.praxislive.video.code.userapi.VideoConstants.BlendMode mode) {
        switch (mode) {
            case Add : return BlendMode.Add;
            case Subtract : return BlendMode.Sub;
            case Difference : return BlendMode.Difference;
            case Multiply : return BlendMode.Multiply;
            case Screen : return BlendMode.Screen;
            case BitXor : return BlendMode.BitXor;
            case Mask : return BlendMode.Mask;
            default : return BlendMode.Normal;
        }
    }

    public void breakShape() {
        shape.breakShape();
    }

    public void clear() {
        getSurface().clear();
    }
    
    public void copy(PImage src) {
        getSurface().copy(src.getSurface());
    }

    public void circle(double x, double y, double extent) {
        ellipse(x, y, extent, extent);
    }
    
    public void ellipse(double x, double y, double w, double h) {
        x -= (w / 2);
        y -= (h / 2);
        renderShape(new Ellipse2D.Double(x, y, w, h));
    }

    public void endShape() {
        endShape(OPEN);
    }

    public void endShape(boolean close) {
        shape.endShape(close);
        renderShape(shape.getShape());
    }

    public void fill(double grey) {
        fill(grey, grey, grey, 255);
    }

    public void fill(double grey, double alpha) {
        fill(grey, grey, grey, alpha);
    }

    public void fill(double r, double g, double b) {
        fill(r, g, b, 255);
    }

    public void fill(double r, double g, double b, double a) {
        fillColor = new Color(round(r), round(g), round(b), round(a));
    }

    public void image(PImage src, double x, double y) {
        if (transform != null) {
            image(src, x, y, src.width, src.height, 0, 0, src.width, src.height);
            return;
        }
        blit.setX((int) x)
                .setY((int) y)
                .setBlendMode(blendMode)
                .setOpacity(opacity)
                .setSourceRegion(null);
        getSurface().process(blit, src.getSurface());
    }

    public void image(PImage src, double x, double y, double w, double h,
            double u, double v) {
        if (transform != null) {
            image(src, x, y, w, h, u, v, u + w, v + h);
            return;
        }
        blit.setX((int) x)
                .setY((int) y)
                .setBlendMode(blendMode)
                .setOpacity(opacity)
                .setSourceRegion((int) u, (int) v, (int) w, (int) h);
        getSurface().process(blit, src.getSurface());
    }

    public void image(PImage src, double x, double y, double w, double h) {
        //image(src, x, y, w, h, 0, 0, image.width, image.height);
        // @TODO check this is correct
        image(src, x, y, w, h, 0, 0, src.width, src.height);
    }

    public void image(PImage src, double x, double y, double w, double h,
            double u1, double v1, double u2, double v2) {
        int ix = (int) x;
        int iy = (int) y;
        int iw = (int) w;
        int ih = (int) h;
        int iu1 = (int) u1;
        int iv1 = (int) v1;
        int iu2 = (int) u2;
        int iv2 = (int) v2;
        int srcW = iu2 - iu1;
        int srcH = iv2 - iv1;
        if (transform != null) {
            transformBlit.setBlendMode(blendMode)
                    .setOpacity(opacity)
                    .setSourceRegion(iu1, iv1, srcW, srcH)
                    .setDestinationRegion(ix, iy, iw, ih)
                    .setTransform(transform);
            getSurface().process(transformBlit, src.getSurface());
        } else {
            if (iw == srcW && ih == srcH) {
                blit.setX(ix)
                        .setY(iy)
                        .setBlendMode(blendMode)
                        .setOpacity(opacity)
                        .setSourceRegion(iu1, iv1, srcW, srcH);
                getSurface().process(blit, src.getSurface());
            } else {
                scaledBlit.setBlendMode(blendMode)
                        .setOpacity(opacity)
                        .setSourceRegion(iu1, iv1, srcW, srcH)
                        .setDestinationRegion(ix, iy, iw, ih);
                getSurface().process(scaledBlit, src.getSurface());
            }
        }
    }

    public void line(double x1, double y1, double x2, double y2) {
        if (strokeColor == null) {
            return;
        }
        renderShape(new Line2D.Double(x1, y1, x2, y2));
    }

    public void noFill() {
        fillColor = null;
    }

    public void noSmooth() {
    }

    public void noStroke() {
        strokeColor = null;
    }

    public void op(SurfaceOp op) {
        getSurface().process(op);
    }

    public void op(SurfaceOp op, PImage src) {
        getSurface().process(op, src.getSurface());
    }

    public void point(double x, double y) {
//        line(x, y, x, y);
//        image.process(RectFill.op(strokeColor, blend, round(x), round(y),
//                1, 1));
        if (transform != null) {
            line(x, y, x, y);
            return;
        }
        rectFill.setBlendMode(blendMode)
                .setOpacity(opacity)
                .setColor(strokeColor)
                .setBounds(round(x), round(y), 1, 1);
        getSurface().process(rectFill);
    }

    public void quad(double x1, double y1, double x2, double y2,
            double x3, double y3, double x4, double y4) {
        int[] xPoints = new int[4];
        int[] yPoints = new int[4];
        xPoints[0] = (int) x1;
        xPoints[1] = (int) x2;
        xPoints[2] = (int) x3;
        xPoints[3] = (int) x4;
        yPoints[0] = (int) y1;
        yPoints[1] = (int) y2;
        yPoints[2] = (int) y3;
        yPoints[3] = (int) y4;
        polygon(xPoints, yPoints, 4);
    }
    
    public void square(double x, double y, double extent) {
        rect(x, y, extent, extent);
    }

    public void rect(double x, double y, double w, double h) {
        if (strokeColor == null) {
            if (fillColor == null) {
                return;
            }
            if (transform == null) {
                rectFill.setBlendMode(blendMode)
                        .setOpacity(opacity)
                        .setColor(fillColor)
                        .setBounds((int) x, (int) y, round(w), round(h));
                getSurface().process(rectFill);
                return;
            }
        }

        renderShape(new Rectangle2D.Double(x, y, w, h));

    }
    
    public void release(PImage image) {
        image.getSurface().release();
    }

    public void resetMatrix() {
        transform = null;
    }

    public void rotate(double angle) {
        if (transform == null) {
            transform = new AffineTransform();
        }
        transform.rotate(angle);
    }

    public void scale(double scale) {
        scale(scale, scale);
    }

    public void scale(double x, double y) {
        if (transform == null) {
            transform = new AffineTransform();
        }
        transform.scale(x, y);
    }

    public void smooth() {
    }

    public void stroke(double grey) {
        stroke(grey, grey, grey, 255);
    }

    public void stroke(double grey, double alpha) {
        stroke(grey, grey, grey, alpha);
    }

    public void stroke(double r, double g, double b) {
        stroke(r, g, b, 255);
    }

    public void stroke(double r, double g, double b, double a) {
        strokeColor = new Color(round(r), round(g), round(b), round(a));
    }

    public void strokeWeight(double weight) {
        if (weight < 1) {
            weight = 1;
        }
        stroke = new BasicStroke((float) weight);
    }
    
    public void text(String text, double x, double y) {
        renderText(text, x, y);
    }
    
    public void textFont(PFont font) {
        textFont(font, 12);
    }
    
    public void textFont(PFont font, double size) {
        this.font = font.getFont().deriveFont((float) size);
    }

    public void translate(double x, double y) {
        if (transform == null) {
            transform = new AffineTransform();
        }
        transform.translate(x, y);
    }

    public void triangle(double x1, double y1, double x2, double y2,
            double x3, double y3) {
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        xPoints[0] = (int) x1;
        xPoints[1] = (int) x2;
        xPoints[2] = (int) x3;
        yPoints[0] = (int) y1;
        yPoints[1] = (int) y2;
        yPoints[2] = (int) y3;
        polygon(xPoints, yPoints, 3);
    }

    public void vertex(double x, double y) {
        shape.vertex(x, y);
    }

    // END OF PUBLIC DRAWING METHODS
    private void renderShape(Shape shape) {
        if (strokeColor == null && fillColor == null) {
            return;
        }

        shapeRender.setBlendMode(blendMode)
                .setOpacity(opacity)
                .setFillColor(fillColor)
                .setStrokeColor(strokeColor)
                .setStroke(stroke)
                .setTransform(transform)
                .setShape(shape);
        getSurface().process(shapeRender);

    }
    
    private void renderText(String text, double x, double y) {
        if (fillColor == null || font == null) {
            return;
        }
        
        textRender.setFont(font)
                .setColor(fillColor)
                .setTransform(transform)
                .setX(x)
                .setY(y)
                .setText(text);
        getSurface().process(textRender);
        
    }
    

    private void polygon(final int[] xPoints, final int[] yPoints, final int nPoints) {
        renderShape(new Polygon(xPoints, yPoints, nPoints));
    }

    private int round(double x) {
        return (int) (x + 0.5);
    }
}
