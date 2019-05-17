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
 */
package org.praxislive.video.pgl.code.userapi;

import java.util.Optional;
import org.praxislive.video.pgl.PGLContext;
import org.praxislive.video.pgl.PGLGraphics;
import org.praxislive.video.pgl.PGLShader;
import processing.core.PConstants;
import processing.opengl.PShapeOpenGL;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
abstract class PGraphics extends PImage {
    
    processing.core.PGraphics g;
    PGLContext context;

    PGraphics(int width, int height) {
        super(width, height);
    }
    
    void init(processing.core.PGraphics g, PGLContext context) {
        this.g = g;
        this.context = context;
//        this.width = g.width;
//        this.height = g.height;
    }
    
    processing.core.PGraphics release() {
        processing.core.PGraphics ret = g;
        g = null;
        context = null;
//        this.width = 0;
//        this.height = 0;
        return ret;
    }
    
    @Override
    protected processing.core.PImage unwrap(PGLContext context) {
        return context == this.context ? g : null;
    }

    /**
     * Search for an instance of the given type.
     * @param <T>
     * @param type class to search for
     * @return Optional wrapping the result if found, or empty if not
     */
    @Override
    public <T> Optional<T> find(Class<T> type) {
        if (processing.core.PImage.class.isAssignableFrom(type)) {
            return Optional.ofNullable(type.cast(g));
        } else {
            return super.find(type);
        }
    }
    
    // EXTENSION METHODS
    
    public PShader createShader(String vertShader, String fragShader) {
        return new PShader(context, new PGLShader(context, vertShader, fragShader));
    }
    
    // PROCESSING API BELOW
    
    public void beginDraw() {
        g.beginDraw();
    }
    
    public void endDraw() {
        g.endDraw();
    }
    
    public void beginShape() {
        g.beginShape();
    }

    public void beginShape(Constants.ShapeMode kind) {
        g.beginShape(kind.unwrap());
    }

    public void edge(boolean edge) {
        g.edge(edge);
    }

    

    public void textureMode(Constants.TextureMode mode) {
        g.textureMode(mode.unwrap());
    }

    public void textureWrap(Constants.TextureWrap wrap) {
        g.textureWrap(wrap.unwrap());
    }

    public void texture(PImage image) {
        g.texture(image.unwrap(context));
    }

    public void noTexture() {
        g.noTexture();
    }

    public void vertex(double x, double y) {
        g.vertex((float)x, (float)y);
    }

    public void vertex(double x, double y, double u, double v) {
        g.vertex((float)x, (float)y, (float)u, (float)v);
    }

    public void endShape() {
        g.endShape();
    }

    public void endShape(Constants.ShapeEndMode mode) {
        g.endShape(mode.unwrap());
    }

    public PShape createShape() {
        return new PShape(g.createShape(), context);
    }
    
    public PShape createShape(Constants.ShapeType type) {
        return new PShape(g.createShape(type.unwrap()), context);
    }

    public PShape createShape(PShape source) {
        PGLGraphics pg = context.primary();
        int prevTextureMode = pg.textureMode;
        pg.textureMode = PConstants.NORMAL;
//        PShapeOpenGL glShape = PShapeOpenGL.createShape(pg, source.unwrap(context));
        PShapeOpenGL glShape = PShapeOpenGL.createShape(pg, source.find(processing.core.PShape.class).get());
        pg.textureMode = prevTextureMode;
        return new PShape(glShape, context);
    }
//
//    public PShape createShape(int type) {
//        return g.createShape(type);
//    }
//
//    public PShape createShape(int kind, double... p) {
//        return g.createShape(kind, p);
//    }

//    public PShader loadShader(String fragFilename) {
//        return g.loadShader(fragFilename);
//    }
//
//    public PShader loadShader(String fragFilename, String vertFilename) {
//        return g.loadShader(fragFilename, vertFilename);
//    }

    public void shader(PShader shader) {
        g.shader(shader.unwrap(context));
    }
//
//    public void shader(PShader shader, int kind) {
//        g.shader(shader.unwrap(), kind);
//    }
//
    public void resetShader() {
        g.resetShader();
        g.resetShader(PConstants.POINTS);
        g.resetShader(PConstants.LINES);
    }
//
//    public void resetShader(int kind) {
//        g.resetShader(kind);
//    }
//
    public void filter(PShader shader) {
        g.filter(shader.unwrap(context));
    }

    public void clip(double a, double b, double c, double d) {
        g.clip((float)a, (float)b, (float)c, (float)d);
    }

    public void noClip() {
        g.noClip();
    }

    public void blendMode(Constants.BlendMode mode) {
        g.blendMode(mode.unwrap());
    }

    public void bezierVertex(double x2, double y2, double x3, double y3, double x4, double y4) {
        g.bezierVertex((float)x2, (float)y2, (float)x3, (float)y3, (float)x4, (float)y4);
    }


    public void quadraticVertex(double cx, double cy, double x3, double y3) {
        g.quadraticVertex((float)cx, (float)cy, (float)x3, (float)y3);
    }


    public void curveVertex(double x, double y) {
        g.curveVertex((float)x, (float)y);
    }


    public void point(double x, double y) {
        g.point((float)x, (float)y);
    }


    public void line(double x1, double y1, double x2, double y2) {
        g.line((float)x1, (float)y1, (float)x2, (float)y2);
    }


    public void triangle(double x1, double y1, double x2, double y2, double x3, double y3) {
        g.triangle((float)x1, (float)y1, (float)x2, (float)y2, (float)x3, (float)y3);
    }

    public void quad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        g.quad((float)x1, (float)y1, (float)x2, (float)y2,
                (float)x3, (float)y3, (float)x4, (float)y4);
    }

    public void rectMode(Constants.DrawingMode mode) {
        g.rectMode(mode.unwrap());
    }

    public void square(double x, double y, double extent) {
        g.square((float) x, (float) y, (float) extent);
    }
    
    public void rect(double a, double b, double c, double d) {
        g.rect((float)a, (float)b, (float)c, (float)d);
    }

    public void rect(double a, double b, double c, double d, double r) {
        g.rect((float)a, (float)b, (float)c, (float)d, (float)r);
    }

    public void rect(double a, double b, double c, double d, double tl, double tr, double br, double bl) {
        g.rect((float)a, (float)b, (float)c, (float)d,
                (float)tl, (float)tr, (float)br, (float)bl);
    }

    public void ellipseMode(Constants.DrawingMode mode) {
        g.ellipseMode(mode.unwrap());
    }

    public void circle(double x, double y, double extent) {
        g.circle((float) x, (float) y, (float) extent);
    }
    
    public void ellipse(double a, double b, double c, double d) {
        g.ellipse((float)a, (float)b, (float)c, (float)d);
    }

    public void arc(double a, double b, double c, double d, double start, double stop) {
        g.arc((float)a, (float)b, (float)c, (float)d, (float)start, (float)stop);
    }

    public void arc(double a, double b, double c, double d, double start, double stop, int mode) {
        g.arc((float)a, (float)b, (float)c, (float)d,
                (float)start, (float)stop, mode);
    }


    public double bezierPoint(double a, double b, double c, double d, double t) {
        return g.bezierPoint((float)a, (float)b, (float)c, (float)d, (float)t);
    }

    public double bezierTangent(double a, double b, double c, double d, double t) {
        return g.bezierTangent((float)a, (float)b, (float)c, (float)d, (float)t);
    }

//    public void bezierDetail(int detail) {
//        g.bezierDetail(detail);
//    }

    public void bezier(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        g.bezier((float)x1, (float)y1, (float)x2, (float)y2,
                (float)x3, (float)y3, (float)x4, (float)y4);
    }

    

    public double curvePoint(double a, double b, double c, double d, double t) {
        return g.curvePoint((float)a, (float)b, (float)c, (float)d, (float)t);
    }

    public double curveTangent(double a, double b, double c, double d, double t) {
        return g.curveTangent((float)a, (float)b, (float)c, (float)d, (float)t);
    }

    public void curveDetail(int detail) {
        g.curveDetail(detail);
    }

    public void curveTightness(double tightness) {
        g.curveTightness((float)tightness);
    }

    public void curve(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        g.curve((float)x1, (float)y1, (float)x2, (float)y2,
                (float)x3, (float)y3, (float)x4, (float)y4);
    }

    public void smooth() {
        g.smooth();
    }

    public void smooth(int level) {
        g.smooth(level);
    }

    public void noSmooth() {
        g.noSmooth();
    }

    public void imageMode(int mode) {
        g.imageMode(mode);
    }

    public void image(PImage img, double a, double b) {
        g.image(img.unwrap(context), (float)a, (float)b);
    }

    public void image(PImage img, double a, double b, double c, double d) {
        g.image(img.unwrap(context), (float)a, (float)b, (float)c, (float)d);
    }

    public void image(PImage img, double a, double b, double c, double d, int u1, int v1, int u2, int v2) {
        g.image(img.unwrap(context), (float)a, (float)b, (float)c, (float)d,
                u1, v1, u2, v2);
    }

//    public void shapeMode(int mode) {
//        g.shapeMode(mode);
//    }
//
    public void shape(PShape shape) {
        g.shape(shape.unwrap(context));
    }

    public void shape(PShape shape, double x, double y) {
        g.shape(shape.unwrap(context), (float)x, (float)y);
    }

    public void shape(PShape shape, double a, double b, double c, double d) {
        g.shape(shape.unwrap(context), (float)a, (float)b, (float)c, (float)d);
    }

//    public void textAlign(int alignX) {
//        g.textAlign(alignX);
//    }
//
//    public void textAlign(int alignX, int alignY) {
//        g.textAlign(alignX, alignY);
//    }

    public double textAscent() {
        return g.textAscent();
    }

    public double textDescent() {
        return g.textDescent();
    }

    public void textFont(PFont font) {
        g.textFont(font.unwrap(context));
    }

    public void textFont(PFont font, double size) {
        g.textFont(font.unwrap(context, (float) size));
    }

    public void textLeading(double leading) {
        g.textLeading((float)leading);
    }

//    public void textMode(int mode) {
//        g.textMode(mode);
//    }

    public void textSize(double size) {
        g.textSize((float)size);
    }

    public double textWidth(char c) {
        return g.textWidth(c);
    }

    public double textWidth(String str) {
        return g.textWidth(str);
    }

    public double textWidth(char[] chars, int start, int length) {
        return g.textWidth(chars, start, length);
    }

    public void text(char c, double x, double y) {
        g.text(c, (float)x, (float)y);
    }

    public void text(char c, double x, double y, double z) {
        g.text(c, (float)x, (float)y, (float)z);
    }

    public void text(String str, double x, double y) {
        g.text(str, (float)x, (float)y);
    }

    public void text(char[] chars, int start, int stop, double x, double y) {
        g.text(chars, start, stop, (float)x, (float)y);
    }

    public void text(String str, double x, double y, double z) {
        g.text(str, (float)x, (float)y, (float)z);
    }

    public void text(char[] chars, int start, int stop, double x, double y, double z) {
        g.text(chars, start, stop, (float)x, (float)y, (float)z);
    }

    public void text(String str, double x1, double y1, double x2, double y2) {
        g.text(str, (float)x1, (float)y1, (float)x2, (float)y2);
    }

    public void text(int num, double x, double y) {
        g.text(num, (float)x, (float)y);
    }

    public void text(int num, double x, double y, double z) {
        g.text(num, (float)x, (float)y, (float)z);
    }

    public void text(double num, double x, double y) {
        g.text((float)num, (float)x, (float)y);
    }

    public void text(double num, double x, double y, double z) {
        g.text((float)num, (float)x, (float)y, (float)z);
    }

    public void pushMatrix() {
        g.pushMatrix();
    }

    public void popMatrix() {
        g.popMatrix();
    }

    public void translate(double x, double y) {
        g.translate((float)x, (float)y);
    }

    public void rotate(double angle) {
        g.rotate((float)angle);
    }

    public void rotateX(double angle) {
        g.rotateX((float)angle);
    }

    public void rotateY(double angle) {
        g.rotateY((float)angle);
    }

    public void scale(double s) {
        g.scale((float)s);
    }

    public void scale(double x, double y) {
        g.scale((float)x, (float)y);
    }

    public void shearX(double angle) {
        g.shearX((float)angle);
    }

    public void shearY(double angle) {
        g.shearY((float)angle);
    }

    public void resetMatrix() {
        g.resetMatrix();
    }

//    public void applyMatrix(PMatrix source) {
//        g.applyMatrix(source);
//    }


    public void applyMatrix(double n00, double n01, double n02, double n10, double n11, double n12) {
        g.applyMatrix((float)n00, (float)n01, (float)n02,
                (float)n10, (float)n11, (float)n12);
    }


    public void applyMatrix(double n00, double n01, double n02, double n03,
            double n10, double n11, double n12, double n13,
            double n20, double n21, double n22, double n23,
            double n30, double n31, double n32, double n33) {
        g.applyMatrix((float)n00, (float)n01, (float)n02,(float)n03,
                (float)n10, (float)n11, (float)n12, (float)n13, 
                (float)n20, (float)n21, (float)n22, (float)n23,
                (float)n30, (float)n31, (float)n32, (float)n33);
    }

//    public PMatrix getMatrix() {
//        return g.getMatrix();
//    }
//
//    public void setMatrix(PMatrix source) {
//        g.setMatrix(source);
//    }

//    public void pushStyle() {
//        g.pushStyle();
//    }
//
//    public void popStyle() {
//        g.popStyle();
//    }

//    public void style(PStyle s) {
//        g.style(s);
//    }
//
//    public PStyle getStyle() {
//        return g.getStyle();
//    }
//
//    public PStyle getStyle(PStyle s) {
//        return g.getStyle(s);
//    }

    public void strokeWeight(double weight) {
        g.strokeWeight((float)weight);
    }

    public void strokeJoin(int join) {
        g.strokeJoin(join);
    }

    public void strokeCap(int cap) {
        g.strokeCap(cap);
    }

    public void noStroke() {
        g.noStroke();
    }

//    public void stroke(int rgb) {
//        g.stroke(rgb);
//    }

    public void stroke(double gray) {
        g.stroke((float) gray);
    }

    public void stroke(double gray, double alpha) {
        g.stroke((float) gray, (float) alpha);
    }

    public void stroke(double v1, double v2, double v3) {
        g.stroke((float)v1, (float)v2, (float)v3);
    }

    public void stroke(double v1, double v2, double v3, double alpha) {
        g.stroke((float)v1, (float)v2, (float)v3, (float)alpha);
    }

    public void noTint() {
        g.noTint();
    }

    public void tint(double gray) {
        g.tint((float)gray);
    }

    public void tint(double gray, double alpha) {
        g.tint((float)gray, (float)alpha);
    }

    public void tint(double v1, double v2, double v3) {
        g.tint((float)v1, (float)v2, (float)v3);
    }

    public void tint(double v1, double v2, double v3, double alpha) {
        g.tint((float)v1, (float)v2, (float)v3, (float)alpha);
    }

    public void noFill() {
        g.noFill();
    }

    public void fill(double gray) {
        g.fill((float)gray);
    }

    public void fill(double gray, double alpha) {
        g.fill((float)gray, (float)alpha);
    }

    public void fill(double v1, double v2, double v3) {
        g.fill((float)v1, (float)v2, (float)v3);
    }

    public void fill(double v1, double v2, double v3, double alpha) {
        g.fill((float)v1, (float)v2, (float)v3, (float)alpha);
    }

    public void colorMode(Constants.ColorMode mode) {
        g.colorMode(mode.unwrap());
    }

    public void colorMode(Constants.ColorMode mode, double max) {
        g.colorMode(mode.unwrap(), (float) max);
    }

    public void background(double gray) {
        g.background((float)gray);
    }

    public void background(double gray, double alpha) {
        g.background((float)gray, (float)alpha);
    }

    public void background(double v1, double v2, double v3) {
        g.background((float)v1, (float)v2, (float)v3);
    }

    public void background(double v1, double v2, double v3, double alpha) {
        g.background((float)v1, (float)v2, (float)v3, (float)alpha);
    }

    public void clear() {
        g.clear();
    }

    public void background(PImage image) {
        g.background(image.unwrap(context));
    }
    
    
}
