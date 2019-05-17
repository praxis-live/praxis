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
 *
 *
 * Parts of the API of this package, as well as some of the code, is derived from
 * the Processing project (http://processing.org)
 *
 * Copyright (c) 2004-09 Ben Fry and Casey Reas
 * Copyright (c) 2001-04 Massachusetts Institute of Technology
 *
 */

package org.praxislive.video.pgl.code.userapi;

import java.util.Optional;
import org.praxislive.code.userapi.PVector;
import org.praxislive.video.pgl.PGLContext;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class PShape {
    
    private final processing.core.PShape shape;
    private final PGLContext context;
    
    protected PShape(processing.core.PShape shape) {
        this(shape, null);
    }
    
    PShape(processing.core.PShape shape, PGLContext context) {
        this.shape = shape;
        this.context = context;
    }
    
    public <T> Optional<T> find(Class<T> type) {
        if (processing.core.PShape.class.isAssignableFrom(type)) {
            return Optional.of(type.cast(shape));
        } else {
            return Optional.empty();
        }
    }
    
    processing.core.PShape unwrap(PGLContext context) {
        if (this.context != null && this.context == context) {
            return shape;
        } else {
            return context.asPGLShape(shape);
        }
    }
    
//    public PShape /* void */ setFamily(int family) {
//        shape.setFamily(family);
//    }
//
//    public PShape /* void */ setKind(int kind) {
//        shape.setKind(kind);
//    }
//
//    public PShape /* void */ setName(String name) {
//        shape.setName(name);
//    }
//
//    public String getName() {
//        return shape.getName();
//    }
//
//    public boolean isVisible() {
//        return shape.isVisible();
//    }
//
//    public PShape /* void */ setVisible(boolean visible) {
//        shape.setVisible(visible);
//    }

    public PShape /* void */ disableStyle() {
        shape.disableStyle();
        return this;
    }

    public PShape /* void */ enableStyle() {
        shape.enableStyle();
        return this;
    }

    public double getWidth() {
        return shape.getWidth();
    }

    public double getHeight() {
        return shape.getHeight();
    }

    public double getDepth() {
        return shape.getDepth();
    }

    public boolean is2D() {
        return shape.is2D();
    }

    public boolean is3D() {
        return shape.is3D();
    }

    public void set3D(boolean val) {
        shape.set3D(val);
    }

    public PShape /* void */ textureMode(Constants.TextureMode mode) {
        shape.textureMode(mode.unwrap());
        return this;
    }

    public PShape /* void */ texture(PImage tex) {
        shape.texture(tex.unwrap(context));
        return this;
    }

    public PShape /* void */ noTexture() {
        shape.noTexture();
        return this;
    }

    public PShape /* void */ beginContour() {
        shape.beginContour();
        return this;
    }

    public PShape /* void */ endContour() {
        shape.endContour();
        return this;
    }

    public PShape /* void */ vertex(double x, double y) {
        shape.vertex((float)x, (float)y);
        return this;
    }

    public PShape /* void */ vertex(double x, double y, double u, double v) {
        shape.vertex((float)x, (float)y, (float)u, (float)v);
        return this;
    }

    public PShape /* void */ vertex(double x, double y, double z) {
        shape.vertex((float)x, (float)y, (float)z);
        return this;
    }

    public PShape /* void */ vertex(double x, double y, double z, double u, double v) {
        shape.vertex((float)x, (float)y, (float)z, (float)u, (float)v);
        return this;
    }

    public PShape /* void */ normal(double nx, double ny, double nz) {
        shape.normal((float)nx, (float)ny, (float)nz);
        return this;
    }

    public PShape /* void */ attribPosition(String name, double x, double y, double z) {
        shape.attribPosition(name, (float)x, (float)y, (float)z);
        return this;
    }

    public PShape /* void */ attribNormal(String name, double nx, double ny, double nz) {
        shape.attribNormal(name, (float)nx, (float)ny, (float)nz);
        return this;
    }

//    public PShape /* void */ attribColor(String name, int color) {
//        shape.attribColor(name, color);
//    }

    public PShape /* void */ attrib(String name, double... values) {
        shape.attrib(name, doublesToFloats(values));
        return this;
    }

    public PShape /* void */ attrib(String name, int... values) {
        shape.attrib(name, values);
        return this;
    }

    public PShape /* void */ attrib(String name, boolean... values) {
        shape.attrib(name, values);
        return this;
    }

    public PShape /* void */ beginShape() {
        shape.beginShape();
        return this;
    }

    public PShape /* void */ beginShape(Constants.ShapeMode kind) {
        shape.beginShape(kind.unwrap());
        return this;
    }

    public void endShape() {
        shape.endShape();
    }

    public void endShape(Constants.ShapeEndMode mode) {
        shape.endShape(mode.unwrap());
    }

    public PShape /* void */ strokeWeight(double weight) {
        shape.strokeWeight((float)weight);
        return this;
    }

//    public PShape /* void */ strokeJoin(int join) {
//        shape.strokeJoin(join);
//    }
//
//    public PShape /* void */ strokeCap(int cap) {
//        shape.strokeCap(cap);
//    }

    public PShape /* void */ noFill() {
        shape.noFill();
        return this;
    }

//    public PShape /* void */ fill(int rgb) {
//        shape.fill(rgb);
//    }
//
//    public PShape /* void */ fill(int rgb, double alpha) {
//        shape.fill(rgb, alpha);
//    }

    public PShape /* void */ fill(double gray) {
        shape.fill((float)gray);
        return this;
    }

    public PShape /* void */ fill(double gray, double alpha) {
        shape.fill((float)gray, (float)alpha);
        return this;
    }

    public PShape /* void */ fill(double x, double y, double z) {
        shape.fill((float)x, (float)y, (float)z);
        return this;
    }

    public PShape /* void */ fill(double x, double y, double z, double a) {
        shape.fill((float)x, (float)y, (float)z, (float)a);
        return this;
    }

    public PShape /* void */ noStroke() {
        shape.noStroke();
        return this;
    }

//    public PShape /* void */ stroke(int rgb) {
//        shape.stroke(rgb);
//    }
//
//    public PShape /* void */ stroke(int rgb, double alpha) {
//        shape.stroke(rgb, alpha);
//    }

    public PShape /* void */ stroke(double gray) {
        shape.stroke((float)gray);
        return this;
    }

    public PShape /* void */ stroke(double gray, double alpha) {
        shape.stroke((float)gray, (float)alpha);
        return this;
    }

    public PShape /* void */ stroke(double x, double y, double z) {
        shape.stroke((float)x, (float)y, (float)z);
        return this;
    }

    public PShape /* void */ stroke(double x, double y, double z, double alpha) {
        shape.stroke((float)x, (float)y, (float)z, (float)alpha);
        return this;
    }

    public PShape /* void */ noTint() {
        shape.noTint();
        return this;
    }

//    public PShape /* void */ tint(int rgb) {
//        shape.tint(rgb);
//    }
//
//    public PShape /* void */ tint(int rgb, double alpha) {
//        shape.tint(rgb, (float)alpha);
//    }

    public PShape /* void */ tint(double gray) {
        shape.tint((float)gray);
        return this;
    }

    public PShape /* void */ tint(double gray, double alpha) {
        shape.tint((float)gray, (float)alpha);
        return this;
    }

    public PShape /* void */ tint(double x, double y, double z) {
        shape.tint((float)x, (float)y, (float)z);
        return this;
    }

    public PShape /* void */ tint(double x, double y, double z, double alpha) {
        shape.tint((float)x, (float)y, (float)z, (float)alpha);
        return this;
    }

//    public PShape /* void */ ambient(int rgb) {
//        shape.ambient(rgb);
//    }

    public PShape /* void */ ambient(double gray) {
        shape.ambient((float)gray);
        return this;
    }

    public PShape /* void */ ambient(double x, double y, double z) {
        shape.ambient((float)x, (float)y, (float)z);
        return this;
    }

//    public PShape /* void */ specular(int rgb) {
//        shape.specular(rgb);
//    }

    public PShape /* void */ specular(double gray) {
        shape.specular((float)gray);
        return this;
    }

    public PShape /* void */ specular(double x, double y, double z) {
        shape.specular((float)x, (float)y, (float)z);
        return this;
    }

//    public PShape /* void */ emissive(int rgb) {
//        shape.emissive(rgb);
//    }

    public PShape /* void */ emissive(double gray) {
        shape.emissive((float)gray);
        return this;
    }

    public PShape /* void */ emissive(double x, double y, double z) {
        shape.emissive((float)x, (float)y, (float)z);
        return this;
    }

    public PShape /* void */ shininess(double shine) {
        shape.shininess((float)shine);
        return this;
    }

//    public PShape /* void */ bezierDetail(int detail) {
//        shape.bezierDetail(detail);
//    }

    public PShape /* void */ bezierVertex(double x2, double y2, double x3, double y3, double x4, double y4) {
        shape.bezierVertex((float)x2, (float)y2, (float)x3, (float)y3, (float)x4, (float)y4);
        return this;
    }

    public PShape /* void */ bezierVertex(double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4) {
        shape.bezierVertex((float)x2, (float)y2, (float)z2, (float)x3, (float)y3, (float)z3, (float)x4, (float)y4, (float)z4);
        return this;
    }

    public PShape /* void */ quadraticVertex(double cx, double cy, double x3, double y3) {
        shape.quadraticVertex((float)cx, (float)cy, (float)x3, (float)y3);
        return this;
    }

    public PShape /* void */ quadraticVertex(double cx, double cy, double cz, double x3, double y3, double z3) {
        shape.quadraticVertex((float)cx, (float)cy, (float)cz, (float)x3, (float)y3, (float)z3);
        return this;
    }

    public PShape /* void */ curveDetail(int detail) {
        shape.curveDetail(detail);
        return this;
    }

    public PShape /* void */ curveTightness(double tightness) {
        shape.curveTightness((float)tightness);
        return this;
    }

    public PShape /* void */ curveVertex(double x, double y) {
        shape.curveVertex((float)x, (float)y);
        return this;
    }

    public PShape /* void */ curveVertex(double x, double y, double z) {
        shape.curveVertex((float)x, (float)y, (float)z);
        return this;
    }
//
//    public PShape /* void */ draw(PGraphics g) {
//        shape.draw(g);
//    }

//    public PShape getParent() {
//        return shape.getParent();
//    }

    public int getChildCount() {
        return shape.getChildCount();
    }

//    public processing.core.PShape[] getChildren() {
//        return shape.getChildren();
//    }

    public PShape getChild(int index) {
        return new PShape(shape.getChild(index), context);
    }

    public PShape getChild(String target) {
        return new PShape(shape.getChild(target), context);
    }

//    public processing.core.PShape findChild(String target) {
//        return shape.findChild(target);
//    }

    public PShape /* void */ addChild(PShape who) {
        shape.addChild(who.shape);
        return this;
    }

    public PShape /* void */ addChild(PShape who, int idx) {
        shape.addChild(who.shape, idx);
        return this;
    }

    public PShape /* void */ removeChild(int idx) {
        shape.removeChild(idx);
        return this;
    }

//    public PShape /* void */ addName(String nom, processing.core.PShape shape) {
//        this.shape.addName(nom, shape);
//    }

//    public int getChildIndex(processing.core.PShape who) {
//        return shape.getChildIndex(who);
//    }

    public PShape getTessellation() {
        return new PShape(shape.getTessellation(), context);
    }

//    public int getFamily() {
//        return shape.getFamily();
//    }

//    public int getKind() {
//        return shape.getKind();
//    }

//    public double[] getParams() {
//        return shape.getParams();
//    }
//
//    public double[] getParams(double[] target) {
//        return shape.getParams(target);
//    }

//    public double getParam(int index) {
//        return shape.getParam(index);
//    }

//    public PShape /* void */ setPath(int vcount, double[][] verts) {
//        shape.setPath(vcount, verts);
//    }

    public int getVertexCount() {
        return shape.getVertexCount();
    }

    public PVector getVertex(int index) {
        processing.core.PVector v = shape.getVertex(index);
        return new PVector(v.x, v.y, v.z);
    }

    public PVector getVertex(int index, PVector vec) {
        processing.core.PVector v = shape.getVertex(index);
        vec.x = v.x;
        vec.y = v.y;
        vec.z = v.z;
        return vec;
    }

    public double getVertexX(int index) {
        return shape.getVertexX(index);
    }

    public double getVertexY(int index) {
        return shape.getVertexY(index);
    }

    public double getVertexZ(int index) {
        return shape.getVertexZ(index);
    }

    public void setVertex(int index, double x, double y) {
        shape.setVertex(index, (float) x, (float) y);
    }

    public void setVertex(int index, double x, double y, double z) {
        shape.setVertex(index, (float) x, (float) y, (float) z);
    }

    public void setVertex(int index, PVector vec) {
        shape.setVertex(index, new processing.core.PVector(
                (float)vec.x, (float)vec.y, (float)vec.z));
    }

    public PVector getNormal(int index) {
        processing.core.PVector v = shape.getNormal(index);
        return new PVector(v.x, v.y, v.z);
    }

    public PVector getNormal(int index, PVector vec) {
        processing.core.PVector v = shape.getNormal(index);
        vec.x = v.x;
        vec.y = v.y;
        vec.z = v.z;
        return vec;
    }

    public double getNormalX(int index) {
        return shape.getNormalX(index);
    }

    public double getNormalY(int index) {
        return shape.getNormalY(index);
    }

    public double getNormalZ(int index) {
        return shape.getNormalZ(index);
    }

    public void setNormal(int index, double nx, double ny, double nz) {
        shape.setNormal(index, (float)nx, (float)ny, (float)nz);
    }

    public void setAttrib(String name, int index, double... values) {
        shape.setAttrib(name, index, doublesToFloats(values));
    }

    public void setAttrib(String name, int index, int... values) {
        shape.setAttrib(name, index, values);
    }

    public void setAttrib(String name, int index, boolean... values) {
        shape.setAttrib(name, index, values);
    }

    public double getTextureU(int index) {
        return shape.getTextureU(index);
    }

    public double getTextureV(int index) {
        return shape.getTextureV(index);
    }

    public void setTextureUV(int index, double u, double v) {
        shape.setTextureUV(index, (float)u, (float)v);
    }

    public void setTextureMode(int mode) {
        shape.setTextureMode(mode);
    }

    public void setTexture(PImage tex) {
        shape.setTexture(tex.unwrap(context));
    }

//    public int getFill(int index) {
//        return shape.getFill(index);
//    }
//
//    public PShape /* void */ setFill(boolean fill) {
//        shape.setFill(fill);
//    }
//
//    public PShape /* void */ setFill(int fill) {
//        shape.setFill(fill);
//    }
//
//    public PShape /* void */ setFill(int index, int fill) {
//        shape.setFill(index, fill);
//    }
//
//    public int getTint(int index) {
//        return shape.getTint(index);
//    }
//
//    public PShape /* void */ setTint(boolean tint) {
//        shape.setTint(tint);
//    }
//
//    public PShape /* void */ setTint(int fill) {
//        shape.setTint(fill);
//    }
//
//    public PShape /* void */ setTint(int index, int tint) {
//        shape.setTint(index, tint);
//    }

//    public int getStroke(int index) {
//        return shape.getStroke(index);
//    }
//
//    public void setStroke(boolean stroke) {
//        shape.setStroke(stroke);
//    }
//
//    public void setStroke(int stroke) {
//        shape.setStroke(stroke);
//    }
//
//    public void setStroke(int index, int stroke) {
//        shape.setStroke(index, stroke);
//    }

    public double getStrokeWeight(int index) {
        return shape.getStrokeWeight(index);
    }

    public void setStrokeWeight(double weight) {
        shape.setStrokeWeight((float)weight);
    }

    public void setStrokeWeight(int index, double weight) {
        shape.setStrokeWeight(index, (float)weight);
    }

//    public PShape /* void */ setStrokeJoin(int join) {
//        shape.setStrokeJoin(join);
//    }
//
//    public PShape /* void */ setStrokeCap(int cap) {
//        shape.setStrokeCap(cap);
//    }

//    public int getAmbient(int index) {
//        return shape.getAmbient(index);
//    }
//
//    public PShape /* void */ setAmbient(int ambient) {
//        shape.setAmbient(ambient);
//    }
//
//    public PShape /* void */ setAmbient(int index, int ambient) {
//        shape.setAmbient(index, ambient);
//    }
//
//    public int getSpecular(int index) {
//        return shape.getSpecular(index);
//    }
//
//    public PShape /* void */ setSpecular(int specular) {
//        shape.setSpecular(specular);
//    }
//
//    public PShape /* void */ setSpecular(int index, int specular) {
//        shape.setSpecular(index, specular);
//    }
//
//    public int getEmissive(int index) {
//        return shape.getEmissive(index);
//    }
//
//    public PShape /* void */ setEmissive(int emissive) {
//        shape.setEmissive(emissive);
//    }
//
//    public PShape /* void */ setEmissive(int index, int emissive) {
//        shape.setEmissive(index, emissive);
//    }
//
//    public double getShininess(int index) {
//        return shape.getShininess(index);
//    }
//
//    public PShape /* void */ setShininess(double shine) {
//        shape.setShininess((float)shine);
//    }
//
//    public PShape /* void */ setShininess(int index, double shine) {
//        shape.setShininess(index, (float)shine);
//    }

//    public int[] getVertexCodes() {
//        return shape.getVertexCodes();
//    }
//
//    public int getVertexCodeCount() {
//        return shape.getVertexCodeCount();
//    }
//
//    public int getVertexCode(int index) {
//        return shape.getVertexCode(index);
//    }

    public boolean isClosed() {
        return shape.isClosed();
    }

    public boolean contains(double x, double y) {
        return shape.contains((float)x, (float)y);
    }

    public PShape /* void */ translate(double x, double y) {
        shape.translate((float)x, (float)y);
        return this;
    }

    public PShape /* void */ translate(double x, double y, double z) {
        shape.translate((float)x, (float)y, (float)z);
        return this;
    }

    public PShape /* void */ rotateX(double angle) {
        shape.rotateX((float)angle);
        return this;
    }

    public PShape /* void */ rotateY(double angle) {
        shape.rotateY((float)angle);
        return this;
    }

    public PShape /* void */ rotateZ(double angle) {
        shape.rotateZ((float)angle);
        return this;
    }

    public PShape /* void */ rotate(double angle) {
        shape.rotate((float)angle);
        return this;
    }

    public PShape /* void */ rotate(double angle, double v0, double v1, double v2) {
        shape.rotate((float)angle, (float)v0, (float)v1, (float)v2);
        return this;
    }

    public PShape /* void */ scale(double s) {
        shape.scale((float)s);
        return this;
    }

    public PShape /* void */ scale(double x, double y) {
        shape.scale((float)x, (float)y);
        return this;
    }

    public PShape /* void */ scale(double x, double y, double z) {
        shape.scale((float)x, (float)y, (float)z);
        return this;
    }

    public PShape /* void */ resetMatrix() {
        shape.resetMatrix();
        return this;
    }
//
//    public PShape /* void */ applyMatrix(PMatrix source) {
//        shape.applyMatrix(source);
//    }
//
//    public PShape /* void */ applyMatrix(PMatrix2D source) {
//        shape.applyMatrix(source);
//    }
//
//    public PShape /* void */ applyMatrix(double n00, double n01, double n02, double n10, double n11, double n12) {
//        shape.applyMatrix(n00, n01, n02, n10, n11, n12);
//    }
//
//    public PShape /* void */ applyMatrix(PMatrix3D source) {
//        shape.applyMatrix(source);
//    }
//
//    public PShape /* void */ applyMatrix(double n00, double n01, double n02, double n03, double n10, double n11, double n12, double n13, double n20, double n21, double n22, double n23, double n30, double n31, double n32, double n33) {
//        shape.applyMatrix(n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22, n23, n30, n31, n32, n33);
//    }
//
//    public PShape /* void */ colorMode(int mode) {
//        shape.colorMode(mode);
//    }
//
//    public PShape /* void */ colorMode(int mode, double max) {
//        shape.colorMode(mode, max);
//    }
//
//    public PShape /* void */ colorMode(int mode, double maxX, double maxY, double maxZ) {
//        shape.colorMode(mode, maxX, maxY, maxZ);
//    }
//
//    public PShape /* void */ colorMode(int mode, double maxX, double maxY, double maxZ, double maxA) {
//        shape.colorMode(mode, maxX, maxY, maxZ, maxA);
//    }
    
    private static float[] doublesToFloats(double[] values) {
        float[] v = new float[values.length];
        for (int i=0; i<v.length; i++) {
            v[i] = (float) values[i];
        }
        return v;
    }
    
    
}
