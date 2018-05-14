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
package org.praxislive.video.pgl.code.userapi;

import org.praxislive.video.pgl.PGLGraphics3D;
import processing.core.PConstants;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public abstract class PGraphics3D extends PGraphics {

    protected PGraphics3D(int width, int height) {
        super(width, height);
    }

    protected void initGraphics(PGLGraphics3D graphics) {
        super.init(graphics, graphics.getContext());
    }

    protected PGLGraphics3D releaseGraphics() {
        return (PGLGraphics3D) release();
    }

    // PROCESSING API BELOW
    public void beginCamera() {
        g.beginCamera();
    }

    public void bezier(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4) {
        g.bezier((float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2,
                (float) x3, (float) y3, (float) z3, (float) x4, (float) y4, (float) z4);
    }

    public void bezierVertex(double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4) {
        g.bezierVertex((float) x2, (float) y2, (float) z2,
                (float) x3, (float) y3, (float) z3,
                (float) x4, (float) y4, (float) z4);
    }

    public void box(double size) {
        g.box((float) size);
    }

    public void box(double w, double h, double d) {
        g.box((float) w, (float) h, (float) d);
    }

    public void camera() {
        g.camera();
    }

    public void camera(double eyeX, double eyeY, double eyeZ, double centerX, double centerY, double centerZ, double upX, double upY, double upZ) {
        g.camera((float) eyeX, (float) eyeY, (float) eyeZ,
                (float) centerX, (float) centerY, (float) centerZ,
                (float) upX, (float) upY, (float) upZ);
    }

    public void curve(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4) {
        g.curve((float) x1, (float) y1, (float) z1,
                (float) x2, (float) y2, (float) z2,
                (float) x3, (float) y3, (float) z3,
                (float) x4, (float) y4, (float) z4);
    }

    public void curveVertex(double x, double y, double z) {
        g.curveVertex((float) x, (float) y, (float) z);
    }

    public void endCamera() {
        g.endCamera();
    }

    public void frustum(double left, double right, double bottom, double top, double near, double far) {
        g.frustum((float) left, (float) right, (float) bottom,
                (float) top, (float) near, (float) far);
    }

    public void hint(Constants.Hint hint) {
        g.hint(hint.unwrap());
    }
    
    public double modelX(double x, double y, double z) {
        return g.modelX((float) x, (float) y, (float) z);
    }

    public double modelY(double x, double y, double z) {
        return g.modelY((float) x, (float) y, (float) z);
    }

    public double modelZ(double x, double y, double z) {
        return g.modelZ((float) x, (float) y, (float) z);
    }

    public void normal(double nx, double ny, double nz) {
        g.normal((float) nx, (float) ny, (float) nz);
    }

    public void ortho() {
        g.ortho();
    }

    public void ortho(double left, double right, double bottom, double top) {
        g.ortho((float) left, (float) right, (float) bottom, (float) top);
    }

    public void ortho(double left, double right, double bottom, double top, double near, double far) {
        g.ortho((float) left, (float) right, (float) bottom, (float) top, (float) near, (float) far);
    }

    public void perspective() {
        g.perspective();
    }

    public void perspective(double fovy, double aspect, double zNear, double zFar) {
        g.perspective((float) fovy, (float) aspect, (float) zNear, (float) zFar);
    }

    public void point(double x, double y, double z) {
        g.point((float) x, (float) y, (float) z);
    }

    public void quadraticVertex(double cx, double cy, double cz, double x3, double y3, double z3) {
        g.quadraticVertex((float) cx, (float) cy, (float) cz,
                (float) x3, (float) y3, (float) z3);
    }

    public void rotate(double angle, double x, double y, double z) {
        g.rotate((float) angle, (float) x, (float) y, (float) z);
    }

    public void rotateZ(double angle) {
        g.rotateZ((float) angle);
    }

    public void scale(double x, double y, double z) {
        g.scale((float) x, (float) y, (float) z);
    }

    public double screenX(double x, double y) {
        return g.screenX((float) x, (float) y);
    }

    public double screenX(double x, double y, double z) {
        return g.screenX((float) x, (float) y, (float) z);
    }

    public double screenY(double x, double y) {
        return g.screenY((float) x, (float) y);
    }

    public double screenY(double x, double y, double z) {
        return g.screenY((float) x, (float) y, (float) z);
    }

    public double screenZ(double x, double y, double z) {
        return g.screenZ((float) x, (float) y, (float) z);
    }

    public void sphere(double r) {
        g.sphere((float) r);
    }

    public void sphereDetail(int res) {
        g.sphereDetail(res);
    }

    public void sphereDetail(int ures, int vres) {
        g.sphereDetail(ures, vres);
    }

    public void translate(double x, double y, double z) {
        g.translate((float) x, (float) y, (float) z);
    }

    public void vertex(double x, double y, double z) {
        g.vertex((float) x, (float) y, (float) z);
    }

    public void vertex(double x, double y, double z, double u, double v) {
        g.vertex((float) x, (float) y, (float) z, (float) u, (float) v);
    }

    public void ambient(double gray) {
        g.ambient((float) gray);
    }

    public void ambient(double v1, double v2, double v3) {
        g.ambient((float) v1, (float) v2, (float) v3);
    }

    public void specular(double gray) {
        g.specular((float) gray);
    }

    public void specular(double v1, double v2, double v3) {
        g.specular((float) v1, (float) v2, (float) v3);
    }

    public void shininess(double shine) {
        g.shininess((float) shine);
    }

    public void emissive(double gray) {
        g.emissive((float) gray);
    }

    public void emissive(double v1, double v2, double v3) {
        g.emissive((float) v1, (float) v2, (float) v3);
    }

    public void lights() {
        g.lights();
    }

    public void noLights() {
        g.noLights();
    }

    public void ambientLight(double v1, double v2, double v3) {
        g.ambientLight((float) v1, (float) v2, (float) v3);
    }

    public void ambientLight(double v1, double v2, double v3, double x, double y, double z) {
        g.ambientLight((float) v1, (float) v2, (float) v3, (float) x, (float) y, (float) z);
    }

    public void directionalLight(double v1, double v2, double v3, double nx, double ny, double nz) {
        g.directionalLight((float) v1, (float) v2, (float) v3, (float) nx, (float) ny, (float) nz);
    }

    public void pointLight(double v1, double v2, double v3, double x, double y, double z) {
        g.pointLight((float) v1, (float) v2, (float) v3, (float) x, (float) y, (float) z);
    }

    public void spotLight(double v1, double v2, double v3, double x, double y, double z, double nx, double ny, double nz, double angle, double concentration) {
        g.spotLight((float) v1, (float) v2, (float) v3,
                (float) x, (float) y, (float) z,
                (float) nx, (float) ny, (float) nz,
                (float) angle, (float) concentration);
    }

    public void lightFalloff(double constant, double linear, double quadratic) {
        g.lightFalloff((float) constant, (float) linear, (float) quadratic);
    }

    public void lightSpecular(double v1, double v2, double v3) {
        g.lightSpecular((float) v1, (float) v2, (float) v3);
    }

}
