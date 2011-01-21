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
package net.neilcsmith.praxis.video.java;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.SurfaceOp;
import net.neilcsmith.ripl.ops.Blend;
import net.neilcsmith.ripl.ops.Blit;
import net.neilcsmith.ripl.ops.Bounds;
import net.neilcsmith.ripl.ops.GraphicsOp;
import net.neilcsmith.ripl.ops.ScaledBlit;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class PGraphics {

    private final static Logger LOG = Logger.getLogger(PGraphics.class.getName());
    private final static Set<String> warned = new HashSet<String>();
    private final static double alphaOpaque = 0.999;
    private PImage image;
    private Blend blend = Blend.NORMAL;
    private Color fillColor = Color.WHITE;
    private Color strokeColor = Color.BLACK;
    private BasicStroke stroke = new BasicStroke(1);

    public PGraphics(PImage image) {
        if (image == null) {
            throw new NullPointerException();
        }
        this.image = image;
    }

    public PImage getImage() {
        return image;
    }

    private static void warn(String msg) {
        if (!warned.contains(msg)) {
            LOG.warning(msg);
            warned.add(msg);
        }
    }

    public void blendMode(Blend blend) {
        if (blend == null) {
            throw new NullPointerException();
        }
        this.blend = blend;
    }

    public void clear() {
        image.getSurface().clear();
    }

    public void ellipse(double x, double y, double w, double h) {
        x -= (w / 2);
        y -= (h / 2);
        final int ix, iy, iw, ih;
        ix = (int) x;
        iy = (int) y;
        iw = (int) w;
        ih = (int) h;
        if (blend.getType() != Blend.Type.Normal
                || blend.getExtraAlpha() < alphaOpaque) {
            warn("ellipse() doesn't support custom blend modes yet.");
        }
        image.process(new GraphicsOp(new GraphicsOp.Callback() {

            public void draw(Graphics2D g2d, Image[] images) {
                if (fillColor != null) {
                    g2d.setColor(fillColor);
                    g2d.fillOval(ix, iy, iw, ih);
                }
                if (strokeColor != null) {
                    g2d.setColor(strokeColor);
                    g2d.setStroke(stroke);
                    g2d.drawOval(ix, iy, iw, ih);
                }
            }
        }));
    }

    public void fill(double r, double g, double b) {
        fill(r, g, b, 1);
    }

    public void fill(double r, double g, double b, double a) {
        r /= 255;
        g /= 255;
        b /= 255;
        a /= 255;
        fillColor = new Color((float) r, (float) g, (float) b, (float) a);
    }

    public void image(PImage src, double x, double y) {
        image.process(Blit.op(blend, (int) x, (int) y), src);
    }

    public void image(PImage src, double x, double y, double w, double h,
            double u, double v) {
        image.process(Blit.op(blend,
                new Bounds((int) u, (int) v, (int) w, (int) h),
                (int) x, (int) y), src);
    }

    public void image(PImage src, double x, double y, double w, double h) {
        image(src, x, y, w, h, 0, 0, image.width, image.height);
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
        if (iw == srcW && ih == srcH) {
            image.process(Blit.op(blend, new Bounds(iu1, iv1, srcW, srcH), ix, iy), src);
        } else {
            image.process(ScaledBlit.op(blend, new Bounds(iu1, iv1, srcW, srcH),
                    new Bounds(ix, iy, iw, ih)), src);
        }
    }

    public void line(double x1, double y1, double x2, double y2) {
        if (strokeColor == null) {
            return;
        }
        if (blend.getType() != Blend.Type.Normal
                || blend.getExtraAlpha() < alphaOpaque) {
            warn("line() / point() doesn't support custom blend modes yet.");
        }
        final int ix1, iy1, ix2, iy2;
        ix1 = (int) x1;
        iy1 = (int) y1;
        ix2 = (int) x2;
        iy2 = (int) y2;
        image.process(new GraphicsOp(new GraphicsOp.Callback() {

            public void draw(Graphics2D g2d, Image[] images) {
                if (strokeColor != null) {
                    g2d.setColor(strokeColor);
                    g2d.setStroke(stroke);
                    g2d.drawLine(ix1, iy1, ix2, iy2);

                }
            }
        }));
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
        image.process(op);
    }

    public void op(SurfaceOp op, Surface src) {
        image.getSurface().process(op, src);
    }

    public void op(SurfaceOp op, PImage src) {
        image.process(op, src);
    }

    public void point(double x, double y) {
        line(x,y,x,y);
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

    public void rect(double x, double y, double w, double h) {
        final int ix, iy, iw, ih;
        ix = (int) x;
        iy = (int) y;
        iw = (int) w;
        ih = (int) h;
        if (blend.getType() != Blend.Type.Normal
                || blend.getExtraAlpha() < alphaOpaque) {
            warn("rect() doesn't support custom blend modes yet.");
        }
        image.process(new GraphicsOp(new GraphicsOp.Callback() {

            public void draw(Graphics2D g2d, Image[] images) {
                if (fillColor != null) {
                    g2d.setColor(fillColor);
                    g2d.fillRect(ix, iy, iw, ih);
                }
                if (strokeColor != null) {
                    g2d.setColor(strokeColor);
                    g2d.setStroke(stroke);
                    g2d.drawRect(ix, iy, iw, ih);
                }
            }
        }));
    }

    public void smooth() {
    }

    public void stroke(double r, double g, double b) {
        stroke(r, g, b, 1);
    }

    public void stroke(double r, double g, double b, double a) {
        r /= 255;
        g /= 255;
        b /= 255;
        a /= 255;
        strokeColor = new Color((float) r, (float) g, (float) b, (float) a);
    }

    public void strokeWeight(double weight) {
        if (weight < 1) {
            weight = 1;
        }
        stroke = new BasicStroke((float) weight);
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

    private void polygon(final int[] xPoints, final int[] yPoints, final int nPoints) {
        if (blend.getType() != Blend.Type.Normal
                || blend.getExtraAlpha() < alphaOpaque) {
            warn("triangle()/quad() doesn't support custom blend modes yet.");
        }
        image.process(new GraphicsOp(new GraphicsOp.Callback() {

            public void draw(Graphics2D g2d, Image[] images) {
                if (fillColor != null) {
                    g2d.setColor(fillColor);
                    g2d.fillPolygon(xPoints, yPoints, nPoints);
                }
                if (strokeColor != null) {
                    g2d.setColor(strokeColor);
                    g2d.setStroke(stroke);
                    g2d.drawPolygon(xPoints, yPoints, nPoints);
                }
            }
        }));
    }
}
