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
 */
package org.praxislive.video.pgl.code;

import java.util.Optional;
import org.praxislive.code.DefaultCodeDelegate;
import org.praxislive.video.pgl.code.userapi.Constants;
import org.praxislive.video.pgl.code.userapi.PFont;
import org.praxislive.video.pgl.code.userapi.PGraphics2D;
import org.praxislive.video.pgl.code.userapi.PImage;
import org.praxislive.video.pgl.code.userapi.PShader;
import org.praxislive.video.pgl.code.userapi.PShape;
import processing.core.PApplet;

public class P2DCodeDelegate extends DefaultCodeDelegate {

    public int width;
    public int height;

    public int mouseX, mouseY, pmouseX, pmouseY, keyCode;
    public char key;
    public boolean keyPressed, mousePressed;
    
    PGraphics2D pg;

    void configure(PApplet parent, PGraphics2D pg, int width, int height) {
        this.pg = pg;
        this.width = width;
        this.height = height;
        this.mouseX = parent.mouseX;
        this.mouseY = parent.mouseY;
        this.pmouseX = parent.pmouseX;
        this.pmouseY = parent.pmouseY;
        this.keyCode = parent.keyCode;
        this.key = parent.key;
        this.keyPressed = parent.keyPressed;
        this.mousePressed = parent.mousePressed;
    }

    public void init(){}
    
    public void update(){}
    
    public void setup(){}
    
    public void draw(){}
    
    @Override
    public <T> Optional<T> find(Class<T> type) {
        if (processing.core.PImage.class.isAssignableFrom(type)) {
            return pg == null ? Optional.empty() : pg.find(type);
        } else {
            return super.find(type);
        }
    }

    // extension delegate methods
    public PShader createShader(String vertShader, String fragShader) {
        return pg.createShader(vertShader, fragShader);
    }

    // delegate methods
    public void beginShape() {
        pg.beginShape();
    }

    public void beginShape(Constants.ShapeMode kind) {
        pg.beginShape(kind);
    }

    public void edge(boolean edge) {
        pg.edge(edge);
    }

    public void textureMode(Constants.TextureMode mode) {
        pg.textureMode(mode);
    }

    public void textureWrap(Constants.TextureWrap wrap) {
        pg.textureWrap(wrap);
    }

    public void texture(PImage image) {
        pg.texture(image);
    }

    public void noTexture() {
        pg.noTexture();
    }

    public void vertex(double x, double y) {
        pg.vertex(x, y);
    }

    public void vertex(double x, double y, double u, double v) {
        pg.vertex(x, y, u, v);
    }

    public void endShape() {
        pg.endShape();
    }

    public void endShape(Constants.ShapeEndMode mode) {
        pg.endShape(mode);
    }

    public PShape createShape() {
        return pg.createShape();
    }
    
    public PShape createShape(Constants.ShapeType type) {
        return pg.createShape(type);
    }

    public PShape createShape(PShape source) {
        return pg.createShape(source);
    }
    
    public void clip(double a, double b, double c, double d) {
        pg.clip(a, b, c, d);
    }

    public void noClip() {
        pg.noClip();
    }

    public void blendMode(Constants.BlendMode mode) {
        pg.blendMode(mode);
    }

    public void bezierVertex(double x2, double y2, double x3, double y3, double x4, double y4) {
        pg.bezierVertex(x2, y2, x3, y3, x4, y4);
    }

    public void quadraticVertex(double cx, double cy, double x3, double y3) {
        pg.quadraticVertex(cx, cy, x3, y3);
    }

    public void curveVertex(double x, double y) {
        pg.curveVertex(x, y);
    }

    public void point(double x, double y) {
        pg.point(x, y);
    }

    public void line(double x1, double y1, double x2, double y2) {
        pg.line(x1, y1, x2, y2);
    }

    public void triangle(double x1, double y1, double x2, double y2, double x3, double y3) {
        pg.triangle(x1, y1, x2, y2, x3, y3);
    }

    public void quad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        pg.quad(x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public void rectMode(Constants.DrawingMode mode) {
        pg.rectMode(mode);
    }

    public void square(double x, double y, double extent) {
        pg.square(x, y, extent);
    }
    
    public void rect(double a, double b, double c, double d) {
        pg.rect(a, b, c, d);
    }

    public void rect(double a, double b, double c, double d, double r) {
        pg.rect(a, b, c, d, r);
    }

    public void rect(double a, double b, double c, double d, double tl, double tr, double br, double bl) {
        pg.rect(a, b, c, d, tl, tr, br, bl);
    }

    public void ellipseMode(Constants.DrawingMode mode) {
        pg.ellipseMode(mode);
    }

    public void circle(double x, double y, double extent) {
        pg.circle(x, y, extent);
    }

    public void ellipse(double a, double b, double c, double d) {
        pg.ellipse(a, b, c, d);
    }

    public void arc(double a, double b, double c, double d, double start, double stop) {
        pg.arc(a, b, c, d, start, stop);
    }

    public void arc(double a, double b, double c, double d, double start, double stop, int mode) {
        pg.arc(a, b, c, d, start, stop, mode);
    }

    public double bezierPoint(double a, double b, double c, double d, double t) {
        return pg.bezierPoint(a, b, c, d, t);
    }

    public double bezierTangent(double a, double b, double c, double d, double t) {
        return pg.bezierTangent(a, b, c, d, t);
    }

    public void bezier(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        pg.bezier(x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public double curvePoint(double a, double b, double c, double d, double t) {
        return pg.curvePoint(a, b, c, d, t);
    }

    public double curveTangent(double a, double b, double c, double d, double t) {
        return pg.curveTangent(a, b, c, d, t);
    }

    public void curveDetail(int detail) {
        pg.curveDetail(detail);
    }

    public void curveTightness(double tightness) {
        pg.curveTightness(tightness);
    }

    public void curve(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        pg.curve(x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public void smooth() {
        pg.smooth();
    }

    public void smooth(int level) {
        pg.smooth(level);
    }

    public void noSmooth() {
        pg.noSmooth();
    }

    public void imageMode(int mode) {
        pg.imageMode(mode);
    }

    public void image(PImage img, double a, double b) {
        pg.image(img, a, b);
    }

    public void image(PImage img, double a, double b, double c, double d) {
        pg.image(img, a, b, c, d);
    }

    public void image(PImage img, double a, double b, double c, double d, int u1, int v1, int u2, int v2) {
        pg.image(img, a, b, c, d, u1, v1, u2, v2);
    }

    public void shape(PShape shape) {
        pg.shape(shape);
    }

    public void shape(PShape shape, double x, double y) {
        pg.shape(shape, x, y);
    }

    public void shape(PShape shape, double a, double b, double c, double d) {
        pg.shape(shape, a, b, c, d);
    }
    
    public double textAscent() {
        return pg.textAscent();
    }

    public double textDescent() {
        return pg.textDescent();
    }

    public void textFont(PFont font) {
        pg.textFont(font);
    }

    public void textFont(PFont font, double size) {
        pg.textFont(font, size);
    }

    public void textLeading(double leading) {
        pg.textLeading(leading);
    }

    public void textSize(double size) {
        pg.textSize(size);
    }

    public double textWidth(char c) {
        return pg.textWidth(c);
    }

    public double textWidth(String str) {
        return pg.textWidth(str);
    }

    public double textWidth(char[] chars, int start, int length) {
        return pg.textWidth(chars, start, length);
    }

    public void text(char c, double x, double y) {
        pg.text(c, x, y);
    }

    public void text(char c, double x, double y, double z) {
        pg.text(c, x, y, z);
    }

    public void text(String str, double x, double y) {
        pg.text(str, x, y);
    }

    public void text(char[] chars, int start, int stop, double x, double y) {
        pg.text(chars, start, stop, x, y);
    }

    public void text(String str, double x, double y, double z) {
        pg.text(str, x, y, z);
    }

    public void text(char[] chars, int start, int stop, double x, double y, double z) {
        pg.text(chars, start, stop, x, y, z);
    }

    public void text(String str, double x1, double y1, double x2, double y2) {
        pg.text(str, x1, y1, x2, y2);
    }

    public void text(int num, double x, double y) {
        pg.text(num, x, y);
    }

    public void text(int num, double x, double y, double z) {
        pg.text(num, x, y, z);
    }

    public void text(double num, double x, double y) {
        pg.text(num, x, y);
    }

    public void text(double num, double x, double y, double z) {
        pg.text(num, x, y, z);
    }

    public void pushMatrix() {
        pg.pushMatrix();
    }

    public void popMatrix() {
        pg.popMatrix();
    }

    public void translate(double x, double y) {
        pg.translate(x, y);
    }

    public void rotate(double angle) {
        pg.rotate(angle);
    }

    public void rotateX(double angle) {
        pg.rotateX(angle);
    }

    public void rotateY(double angle) {
        pg.rotateY(angle);
    }

    public void scale(double s) {
        pg.scale(s);
    }

    public void scale(double x, double y) {
        pg.scale(x, y);
    }

    public void shearX(double angle) {
        pg.shearX(angle);
    }

    public void shearY(double angle) {
        pg.shearY(angle);
    }

    public void resetMatrix() {
        pg.resetMatrix();
    }

    public void applyMatrix(double n00, double n01, double n02, double n10, double n11, double n12) {
        pg.applyMatrix(n00, n01, n02, n10, n11, n12);
    }

    public void applyMatrix(double n00, double n01, double n02, double n03, double n10, double n11, double n12, double n13, double n20, double n21, double n22, double n23, double n30, double n31, double n32, double n33) {
        pg.applyMatrix(n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22, n23, n30, n31, n32, n33);
    }

    public void strokeWeight(double weight) {
        pg.strokeWeight(weight);
    }

    public void strokeJoin(int join) {
        pg.strokeJoin(join);
    }

    public void strokeCap(int cap) {
        pg.strokeCap(cap);
    }

    public void noStroke() {
        pg.noStroke();
    }

    public void stroke(int rgb) {
        pg.stroke(rgb);
    }

    public void stroke(double gray) {
        pg.stroke(gray);
    }

    public void stroke(double gray, double alpha) {
        pg.stroke(gray, alpha);
    }

    public void stroke(double v1, double v2, double v3) {
        pg.stroke(v1, v2, v3);
    }

    public void stroke(double v1, double v2, double v3, double alpha) {
        pg.stroke(v1, v2, v3, alpha);
    }

    public void noTint() {
        pg.noTint();
    }

    public void tint(double gray) {
        pg.tint(gray);
    }

    public void tint(double gray, double alpha) {
        pg.tint(gray, alpha);
    }

    public void tint(double v1, double v2, double v3) {
        pg.tint(v1, v2, v3);
    }

    public void tint(double v1, double v2, double v3, double alpha) {
        pg.tint(v1, v2, v3, alpha);
    }

    public void noFill() {
        pg.noFill();
    }

    public void fill(double gray) {
        pg.fill(gray);
    }

    public void fill(double gray, double alpha) {
        pg.fill(gray, alpha);
    }

    public void fill(double v1, double v2, double v3) {
        pg.fill(v1, v2, v3);
    }

    public void fill(double v1, double v2, double v3, double alpha) {
        pg.fill(v1, v2, v3, alpha);
    }

    public void colorMode(Constants.ColorMode mode) {
        pg.colorMode(mode);
    }

    public void colorMode(Constants.ColorMode mode, double max) {
        pg.colorMode(mode, max);
    }

    public void background(double gray) {
        pg.background(gray);
    }

    public void background(double gray, double alpha) {
        pg.background(gray, alpha);
    }

    public void background(double v1, double v2, double v3) {
        pg.background(v1, v2, v3);
    }

    public void background(double v1, double v2, double v3, double alpha) {
        pg.background(v1, v2, v3, alpha);
    }

    public void clear() {
        pg.clear();
    }

    public void background(PImage image) {
        pg.background(image);
    }

    public void shader(PShader shader) {
        pg.shader(shader);
    }

    public void resetShader() {
        pg.resetShader();
    }

    public void filter(PShader shader) {
        pg.filter(shader);
    }

}
