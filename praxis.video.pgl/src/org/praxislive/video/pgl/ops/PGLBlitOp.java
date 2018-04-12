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
package org.praxislive.video.pgl.ops;

import java.awt.Rectangle;
import org.praxislive.video.pgl.PGLContext;
import org.praxislive.video.pgl.PGLGraphics;
import org.praxislive.video.pgl.PGLShader;
import org.praxislive.video.pgl.PGLSurface;

import org.praxislive.video.render.Surface;
import org.praxislive.video.render.SurfaceOp;
import org.praxislive.video.render.ops.BlendMode;
import org.praxislive.video.render.ops.Blit;
import processing.core.PImage;

/**
 *
 * @author Neil C Smith <http://neilcsmith.net>
 */
public class PGLBlitOp extends AbstractBlitOp {

    private final PGLContext context;

    private Rectangle rect;

    PGLBlitOp(PGLContext context) {
        super(Blit.class);
        this.context = context;
        rect = new Rectangle();
    }

    @Override
    public void process(SurfaceOp op, PGLSurface output, Bypass bypass, Surface... inputs) {
        if (inputs.length > 0) {
            if (process((Blit) op, output, inputs[0])) {
                return;
            }
        }
        bypass.process(op, inputs);
    }

    private boolean process(Blit blit, PGLSurface dst, Surface src) {
        BlendMode mode = blit.getBlendMode();
        if (canProcessDirect(mode)) {
            PGLGraphics g = dst.getGraphics();
            PImage img = dst.getContext().asImage(src);
            assert g != img;
            g.beginDraw();
            setupBlending(g, mode, (float) blit.getOpacity());
            Rectangle bounds = blit.getSourceRegion(rect);
            if (bounds == null) {
                g.image(img, blit.getX(), blit.getY());
            } else {
                int x = blit.getX();
                int y = blit.getY();
                int w = bounds.width;
                int h = bounds.height;
                g.image(img, x, y, w, h, bounds.x, bounds.y, bounds.x + w, bounds.y + h);
            }
            return true;
        } else {
            return processIndirect(blit, dst, src);
        }
    }

    private boolean processIndirect(Blit blit, PGLSurface dst, Surface src) {
        PGLShader shader = getShader(blit.getBlendMode());
        if (shader == null) {
            return false;
        }
        if (blit.getX() == 0
                && blit.getY() == 0
                && !blit.hasSourceRegion()
                && dst.getWidth() == src.getWidth()
                && dst.getHeight() == src.getHeight()) {
            // identity blit
            PImage dstImage = context.asImage(dst);
            PImage srcImage = context.asImage(src);
            PGLSurface tmp = dst.createSurface();
            int w = tmp.getWidth();
            int h = tmp.getHeight();
            PGLGraphics g = tmp.getGraphics();
            g.beginDraw();
            g.blendMode(PGLGraphics.REPLACE);
            g.shader(shader);
            shader.set("src", srcImage);
            shader.set("dst", dstImage);
            shader.set("alpha", (float) blit.getOpacity());
            g.noStroke();
            g.beginShape(PGLGraphics.QUADS);
            g.vertex(0, 0, 0, 0);
            g.vertex(w, 0, 1, 0);
            g.vertex(w, h, 1, 1);
            g.vertex(0, h, 0, 1);
            g.endShape();
            g.resetShader();
            dst.copy(tmp);
            tmp.release();
            return true;
        } else {
            return false;
        }
    }

    // shaders
    private PGLShader differenceShader;
    private PGLShader screenShader;
    private PGLShader bitxorShader;
    private PGLShader maskShader;

    private PGLShader getShader(BlendMode mode) {
        switch (mode) {
            case Difference:
                if (differenceShader == null) {
                    differenceShader = new PGLShader(context, VERTEX_SHADER, DIFFERENCE_FRAGMENT);
                }
                return differenceShader;
            case Screen:
                if (screenShader == null) {
                    screenShader = new PGLShader(context, VERTEX_SHADER, SCREEN_FRAGMENT);
                }
                return screenShader;
            case BitXor:
                if (bitxorShader == null) {
                    bitxorShader = new PGLShader(context, VERTEX_SHADER, BITXOR_FRAGMENT);
                }
                return bitxorShader;
            case Mask:
                if (maskShader == null) {
                    maskShader = new PGLShader(context, VERTEX_SHADER, MASK_FRAGMENT);
                }
                return maskShader;
            default:
                return null;
        }
    }

    private final static String VERTEX_SHADER
            = "uniform mat4 transformMatrix;\n"
            + "uniform mat4 srcMatrix;\n"
            + "uniform mat4 dstMatrix;\n"
            + "attribute vec4 position;\n"
            + "attribute vec2 texCoord;\n"
            + "varying vec4 srcTexCoord;\n"
            + "varying vec4 dstTexCoord;\n"
            + "\n"
            + "void main()\n"
            + "{\n"
            + "  srcTexCoord = srcMatrix * vec4(texCoord, 1.0, 1.0);\n"
            + "  dstTexCoord = dstMatrix * vec4(texCoord, 1.0, 1.0);\n"
            + "  gl_Position = transformMatrix * position;\n"
            + "}";

    private final static String FRAGMENT_HEADER
            = "uniform sampler2D src;\n"
            + "uniform sampler2D dst;\n"
            + "uniform float alpha;\n"
            + "varying vec4 srcTexCoord;\n"
            + "varying vec4 dstTexCoord;\n"
            + "\n"
            + "void main() {\n"
            + "  vec4 srcColor = texture2D(src, srcTexCoord.st);\n"
            + "  vec4 dstColor = texture2D(dst, dstTexCoord.st);\n"
            + "  srcColor *= alpha;\n";

    private final static String FRAGMENT_FOOTER
            = "  gl_FragColor =  dstColor;\n"
            + "}";

    private static final String DIFFERENCE_FRAGMENT
            = FRAGMENT_HEADER
            + "  dstColor.rgb = abs(dstColor.rgb - srcColor.rgb);\n"
            + "  dstColor.a = srcColor.a + ((1.0 - srcColor.a) * dstColor.a);\n"
            + FRAGMENT_FOOTER;

    private static final String SCREEN_FRAGMENT
            = FRAGMENT_HEADER
            + "  vec3 one = vec3(1.0);"
            + "  dstColor.rgb = one - ((one - srcColor.rgb) * (one - dstColor.rgb));\n"
            + "  dstColor.a = srcColor.a + ((1.0 - srcColor.a) * dstColor.a);\n"
            + FRAGMENT_FOOTER;

    private static final String BITXOR_FRAGMENT
            = FRAGMENT_HEADER
            + "  ivec3 iSrc = ivec3(int(srcColor.r * 255.0), int(srcColor.g * 255.0), int(srcColor.b * 255.0));\n"
            + "  ivec3 iDst = ivec3(int(dstColor.r * 255.0), int(dstColor.g * 255.0), int(dstColor.b * 255.0));\n"
            + "  iDst = iSrc ^ iDst;\n"
            + "  dstColor.rgb = vec3(float(iDst.r) / 255.0, float(iDst.g) / 255.0, float(iDst.b) / 255.0);\n"
            + "  dstColor.a = srcColor.a + ((1.0 - srcColor.a) * dstColor.a);\n"
            + FRAGMENT_FOOTER;

    private static final String MASK_FRAGMENT
            = FRAGMENT_HEADER
            + "  dstColor = (srcColor * dstColor) + (dstColor * (1.0 - alpha));\n"
            + FRAGMENT_FOOTER;

}
