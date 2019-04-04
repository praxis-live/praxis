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

package org.praxislive.video.code;

import java.util.function.UnaryOperator;
import org.praxislive.code.DefaultCodeDelegate;
import org.praxislive.video.code.userapi.PFont;
import org.praxislive.video.code.userapi.PGraphics;
import org.praxislive.video.code.userapi.PImage;
import org.praxislive.video.code.userapi.VideoConstants;
import org.praxislive.video.render.SurfaceOp;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class VideoCodeDelegate extends DefaultCodeDelegate {
    
    public int width;
    public int height;
    
    VideoCodeContext<?> context;
    PGraphics pg;

    void setupGraphics(PGraphics pg, int width, int height) {
        this.pg = pg;
        this.width = width;
        this.height = height;
    }

    public void init(){}
    
    public void update(){}
    
    public void setup(){}
    
    public void draw(){}
    
    public final void attachAlphaQuery(String source, UnaryOperator<Boolean> query) {
        context.attachAlphaQuery(source, query);
    }
    
    public final void attachRenderQuery(UnaryOperator<Boolean> query) {
        context.attachRenderQuery(query);
    }
    
    public final void attachRenderQuery(String source, UnaryOperator<Boolean> query) {
        context.attachRenderQuery(source, query);
    }
    
    // Start generated PGraphics 
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

    public void bezier(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        pg.bezier(x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public void bezierVertex(double x1, double y1, double x2, double y2, double x3, double y3) {
        pg.bezierVertex(x1, y1, x2, y2, x3, y3);
    }

    public void blendMode(VideoConstants.BlendMode blend) {
        pg.blendMode(blend);
    }

    public void blendMode(VideoConstants.BlendMode blend, double opacity) {
        pg.blendMode(blend, opacity);
    }

    public void breakShape() {
        pg.breakShape();
    }

    public void clear() {
        pg.clear();
    }

    public void copy(PImage src) {
        pg.copy(src);
    }

    public void circle(double x, double y, double extent) {
        pg.circle(x, y, extent);
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

    public void image(PImage src, double x, double y) {
        pg.image(src, x, y);
    }

    public void image(PImage src, double x, double y, double w, double h, double u, double v) {
        pg.image(src, x, y, w, h, u, v);
    }

    public void image(PImage src, double x, double y, double w, double h) {
        pg.image(src, x, y, w, h);
    }

    public void image(PImage src, double x, double y, double w, double h, double u1, double v1, double u2, double v2) {
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

    public void op(SurfaceOp op, PImage src) {
        pg.op(op, src);
    }

    public void point(double x, double y) {
        pg.point(x, y);
    }

    public void quad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        pg.quad(x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public void square(double x, double y, double extent) {
        pg.square(x, y, extent);
    }

    public void rect(double x, double y, double w, double h) {
        pg.rect(x, y, w, h);
    }

    public void release(PImage image) {
        pg.release(image);
    }
    
    public void resetMatrix() {
        pg.resetMatrix();
    }

    public void rotate(double angle) {
        pg.rotate(angle);
    }

    public void scale(double scale) {
        pg.scale(scale);
    }

    public void scale(double x, double y) {
        pg.scale(x, y);
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

    public void text(String text, double x, double y) {
        pg.text(text, x, y);
    }

    public void textFont(PFont font) {
        pg.textFont(font);
    }

    public void textFont(PFont font, double size) {
        pg.textFont(font, size);
    }

    public void translate(double x, double y) {
        pg.translate(x, y);
    }

    public void triangle(double x1, double y1, double x2, double y2, double x3, double y3) {
        pg.triangle(x1, y1, x2, y2, x3, y3);
    }

    public void vertex(double x, double y) {
        pg.vertex(x, y);
    }
    // End generated PGraphics
    
    
}
