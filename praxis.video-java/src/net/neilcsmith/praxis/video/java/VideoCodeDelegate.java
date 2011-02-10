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

import java.util.logging.Level;
import java.util.logging.Logger;
import net.neilcsmith.praxis.java.CodeDelegate;
import net.neilcsmith.ripl.Surface;
import net.neilcsmith.ripl.SurfaceOp;
import net.neilcsmith.ripl.delegates.CompositeDelegate;
import net.neilcsmith.ripl.delegates.Delegate;
import net.neilcsmith.ripl.ops.Blend;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class VideoCodeDelegate extends CodeDelegate implements Delegate, CompositeDelegate {

    private final static Logger LOG = Logger.getLogger(VideoCodeDelegate.class.getName());
    private final static Surface[] EMPTY_SOURCES = new Surface[0];
    private PImage dst;
    private PGraphics pg;
    private boolean inited;
    public PImage src;
    public Surface[] sources;
    public int width;
    public int height;

    public final void update(long time) {
        // ignore - tick() provides same function earlier in update cycle.
    }

    public final void process(Surface surface) {
        validateGraphics(surface);
        sources = EMPTY_SOURCES;
        processImpl();
    }

    public final void process(Surface surface, Surface... sources) {
        validateGraphics(surface);
        width = surface.getWidth();
        height = surface.getHeight();
        if (sources.length > 0) {
            Surface s = sources[0];
            if (src == null || src.getSurface() != s) {
                src = new PImage(s);
            }
        }
        this.sources = sources;
        processImpl();
    }

    private void processImpl() {
        if (!inited) {
            try {
                setup();
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "", ex);
            }
            inited = true;
        }
        try {
            draw();
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "", ex);
        }
    }

    private void validateGraphics(Surface surface) {
        if (dst == null) {
            dst = new PImage(surface);
            pg = new PGraphics(dst);
        } else if (dst.getSurface() != surface) {
            dst.setSurface(surface);
        }
    }

    public boolean forceRender() {
        return false;
    }

    public boolean usesInput() {
        return true;
    }

    public void setup() {
    }

    public void draw() {
        process();
    } //old API used process()

    public void process() {
    }

    // PGraphics impl

    public void background(double grey) {
        pg.background(grey);
    }

    public void background(double grey, double alpha) {
        pg.background(grey, alpha);
    }

    public void background(double r, double g, double b) {
        pg.background(r, g, b);
    }

    public void background(double r, double g, double b, double a) {
        pg.background(r, g, b, a);
    }

    public void beginShape() {
        pg.beginShape();
    }

    public void bezierVertex(double x1, double y1,
            double x2, double y2, double x3, double y3) {
        pg.bezierVertex(x1, y1, x2, y2, x3, y3);
    }

    public void blendMode(Blend blend) {
        pg.blendMode(blend);
    }

    public void breakShape() {
        pg.breakShape();
    }

    public void clear() {
        pg.clear();
    }

    public void ellipse(double x, double y, double w, double h) {
        pg.ellipse(x, y, w, h);
    }

    public void endShape() {
        pg.endShape();
    }

    public void endShape(boolean close) {
        pg.endShape(close);
    }

    public void fill(double grey) {
        pg.fill(grey);
    }

    public void fill(double grey, double alpha) {
        pg.fill(grey, alpha);
    }

    public void fill(double r, double g, double b) {
        pg.fill(r, g, b);
    }

    public void fill(double r, double g, double b, double a) {
        pg.fill(r, g, b, a);
    }

    public void image(PImage image, double x, double y) {
        pg.image(image, x, y);
    }

    public void image(PImage src, double x, double y, double w, double h,
            double u, double v) {
        pg.image(src, x, y, w, h, u, v);
    }

    public void image(PImage src, double x, double y, double w, double h) {
        pg.image(src, x, y, w, h);
    }

    public void image(PImage src, double x, double y, double w, double h,
            double u1, double v1, double u2, double v2) {
        pg.image(src, x, y, w, h, u1, v1, u2, v2);
    }

    public void line(double x1, double y1, double x2, double y2) {
        pg.line(x1, y1, x2, y2);
    }

    public void noFill() {
        pg.noFill();
    }

    public void noSmooth() {
        pg.noSmooth();
    }

    public void noStroke() {
        pg.noStroke();
    }

    public void op(SurfaceOp op) {
        pg.op(op);
    }

    public void op(SurfaceOp op, Surface src) {
        pg.op(op, src);
    }

    public void op(SurfaceOp op, PImage src) {
        pg.op(op, src);
    }

    public void point(double x, double y) {
        pg.point(x, y);
    }

    public void quad(double x1, double y1, double x2, double y2,
            double x3, double y3, double x4, double y4) {
        pg.quad(x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public void rect(double x, double y, double w, double h) {
        pg.rect(x, y, w, h);
    }

    public void smooth() {
        pg.smooth();
    }

    public void stroke(double grey) {
        pg.stroke(grey);
    }

    public void stroke(double grey, double alpha) {
        pg.stroke(grey, alpha);
    }

    public void stroke(double r, double g, double b) {
        pg.stroke(r, g, b);
    }

    public void stroke(double r, double g, double b, double a) {
        pg.stroke(r, g, b, a);
    }

    public void strokeWeight(double weight) {
        pg.strokeWeight(weight);
    }

    public void triangle(double x1, double y1, double x2, double y2,
            double x3, double y3) {
        pg.triangle(x1, y1, x2, y2, x3, y3);
    }

    public void vertex(double x, double y) {
        pg.vertex(x, y);
    }
}
